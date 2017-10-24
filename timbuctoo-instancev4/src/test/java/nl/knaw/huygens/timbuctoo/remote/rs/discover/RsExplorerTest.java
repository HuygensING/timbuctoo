package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsMd;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import org.junit.Test;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import javax.xml.bind.JAXBException;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class RsExplorerTest extends AbstractRemoteTest {

  @Test
  public void findDescription() throws Exception {
    String path = "/.well-known/resourcesync";
    verifyFindDescription(path);
  }

  @Test
  public void findDescriptionWithoutExplicitUrl() throws Exception {
    String path = "";
    verifyFindDescription(path);
  }

  @Test
  public void findDescriptionWithoutExplicitUrlVariation() throws Exception {
    String path = "/";
    verifyFindDescription(path);
  }

  private void verifyFindDescription(String path) {
    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(path),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(createValidDescription())
      );

    URI uri = composeUri(path);

    RsExplorer explorer = new RsExplorer(getHttpclient(), getRsContext());
    ResultIndex index = new ResultIndex();
    Result<RsRoot> result = explorer.explore(uri, index);

    result.getErrors().forEach(Throwable::printStackTrace);

    assertThat(result.getUri(), equalTo(uri));
    assertThat(result.getStatusCode(), equalTo(200));
    assertThat(result.getErrors().isEmpty(), is(true));
    assertThat(result.getContent().isPresent(), is(true));
    assertThat(result.getContent().map(RsRoot::getMetadata).flatMap(RsMd::getCapability).orElse("invalid"),
      equalTo("description"));

    // index should contain the describedBy uri of the source description:
    assertThat(index.contains("http://example.com/info_about_source.xml"), is(true));
    // result should contain the describedByResult:
    Result<Description> descriptionResult = result.getDescriptionResult().get();
    assertThat(descriptionResult.getUri().toString(), equalTo("http://example.com/info_about_source.xml"));
    assertThat(descriptionResult.getContent().isPresent(), is(false));
    assertThat(descriptionResult.getStatusCode(), equalTo(404));
  }

  @Test
  public void findWrongParentDocument() throws Exception {
    String path1 = "/foo/resourcedump.xml";
    String path2 = "/bla/resourcedump.xml";

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(path1),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(createValidResourceDump(path2))
      );

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(path2),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(createValidResourceDump("/bar/whatever.xml"))
      );

    URI uri = composeUri(path1);

    RsExplorer explorer = new RsExplorer(getHttpclient(), getRsContext());
    ResultIndex index = new ResultIndex();
    Result<RsRoot> result = explorer.explore(uri, index);

    //result.listErrors().forEach(Throwable::printStackTrace);

    assertThat(result.getUri(), equalTo(uri));
    assertThat(result.getStatusCode(), equalTo(200));
    assertThat(result.getErrors().isEmpty(), is(false));
    assertThat(result.getErrors().get(0).getMessage(), containsString("invalid up relation:"));
    assertThat(result.getContent().isPresent(), is(true));
    assertThat(result.getContent().map(RsRoot::getMetadata).flatMap(RsMd::getCapability).orElse("invalid"),
      equalTo("resourcedump"));

    URI uri2 = composeUri(path2);
    Result<RsRoot> parentResult = (Result<RsRoot>) result.getParents().get(uri2);

    parentResult.getErrors().forEach(Throwable::printStackTrace);

    assertThat(parentResult.getUri(), equalTo(uri2));
    assertThat(parentResult.getStatusCode(), equalTo(200));
    assertThat(parentResult.getErrors().isEmpty(), is(true));
    assertThat(parentResult.getContent().isPresent(), is(true));
    assertThat(parentResult.getContent().map(RsRoot::getMetadata).flatMap(RsMd::getCapability).orElse("invalid"),
      equalTo("resourcedump"));
  }

  @Test
  public void findInvalidSitemapDocument() throws Exception {
    String path = "/.well-known/resourcesync";

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(path),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(createValidXml())
      );

    RsExplorer explorer = new RsExplorer(getHttpclient(), getRsContext());
    ResultIndex index = new ResultIndex();
    Result<RsRoot> result = explorer.explore(composeUri(path), index);

    assertThat(result.getUri(), equalTo(composeUri(path)));
    assertThat(result.getStatusCode(), equalTo(200));
    assertThat(result.getErrors().isEmpty(), is(false));
    assertThat(result.getErrors().get(0), instanceOf(JAXBException.class));
    assertThat(result.getContent().isPresent(), is(false));
    //result.listErrors().forEach(Throwable::printStackTrace);
  }

  @Test
  public void receiveStatusError() throws Exception {
    String path = "/.well-known/resourcesync";

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(path),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(404)
        .withBody("Document not found")
      );

    RsExplorer explorer = new RsExplorer(getHttpclient(), getRsContext());
    ResultIndex index = new ResultIndex();
    Result<RsRoot> result = explorer.explore(composeUri(path), index);

    assertThat(result.getUri(), equalTo(composeUri(path)));
    assertThat(result.getStatusCode(), equalTo(404));
    assertThat(result.getErrors().isEmpty(), is(false));
    assertThat(result.getErrors().get(0), instanceOf(RemoteException.class));
    assertThat(result.getContent().isPresent(), is(false));
    //result.listErrors().forEach(Throwable::printStackTrace);
  }

  private String createValidDescription() {
    return
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
        "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
        "  <rs:ln rel=\"describedby\"\n" +
        "         href=\"http://example.com/info_about_source.xml\"/>\n" +
        "  <rs:md capability=\"description\"/>\n" +
        "  <url>\n" +
        "      <loc>http://example.com/capabilitylist1.xml</loc>\n" +
        "      <rs:md capability=\"capabilitylist\"/>\n" +
        "      <rs:ln rel=\"describedby\"\n" +
        "             href=\"http://example.com/info_about_set1_of_resources.xml\"/>\n" +
        "  </url>\n" +
        "  <url>\n" +
        "      <loc>http://example.com/capabilitylist2.xml</loc>\n" +
        "      <rs:md capability=\"capabilitylist\"/>\n" +
        "      <rs:ln rel=\"describedby\"\n" +
        "             href=\"http://example.com/info_about_set2_of_resources.xml\"/>\n" +
        "  </url>\n" +
        "  <url>\n" +
        "      <loc>http://example.com/capabilitylist3.xml</loc>\n" +
        "      <rs:md capability=\"capabilitylist\"/>\n" +
        "      <rs:ln rel=\"describedby\"\n" +
        "             href=\"http://example.com/info_about_set3_of_resources.xml\"/>\n" +
        "  </url>\n" +
        "</urlset>\n" +
        "\n";
  }

  private String createValidResourceDump(String relUpPath) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:md capability=\"resourcedump\"\n" +
      "         at=\"2013-01-03T09:00:00Z\"/>\n" +
      "  <rs:ln rel=\"up\"" +
      "         href=\"" + composePath(relUpPath) + "\"/>" +
      "  <url>\n" +
      "      <loc>http://example.com/resourcedump.zip</loc>\n" +
      "      <lastmod>2013-01-03T09:00:00Z</lastmod>\n" +
      "  </url>\n" +
      "</urlset>";
  }

  private String createValidXml() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<funny xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:md capability=\"description\"\n" +
      "         at=\"2013-01-03T09:00:00Z\"/>\n" +
      "</funny>";
  }

}
