package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import org.junit.Test;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import javax.xml.bind.JAXBException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class WellKnownUriExplorerTest extends AbstractRemoteTest {

  @Test
  public void findDescription() throws Exception {
    String path = "/.well-known/resourcesync";

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(path),
        Times.exactly(1))

      .respond(HttpResponse.response()
          .withStatusCode(200)
          .withBody(createValidDescription())
      );

    WellKnownUriExplorer explorer = new WellKnownUriExplorer(getHttpclient(), getRsContext());
    ResultIndex index = new ResultIndex();
    Result<RsRoot> result = explorer.explore(composeUri(path), index);

    result.getError().ifPresent(Throwable::printStackTrace);

    assertThat(result.getUri(), equalTo(composeUri(path)));
    assertThat(result.getStatusCode(), equalTo(200));
    assertThat(result.getError().isPresent(), is(false));
    assertThat(result.getContent().isPresent(), is(true));
    assertThat(result.getContent().get().getMetadata().getCapability().get(), equalTo("description"));
  }

  @Test
  public void findWrongSitemapDocument() throws Exception {
    String path = "/.well-known/resourcesync";

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(path),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(createValidResourceDump())
      );

    WellKnownUriExplorer explorer = new WellKnownUriExplorer(getHttpclient(), getRsContext());
    ResultIndex index = new ResultIndex();
    Result<RsRoot> result = explorer.explore(composeUri(path), index);

    assertThat(result.getUri(), equalTo(composeUri(path)));
    assertThat(result.getStatusCode(), equalTo(200));
    assertThat(result.getError().isPresent(), is(true));
    assertThat(result.getError().get().getMessage(), containsString("unexpected capability:"));
    assertThat(result.getContent().isPresent(), is(true));
    assertThat(result.getContent().get().getMetadata().getCapability().get(), equalTo("resourcedump"));
    //result.getError().get().printStackTrace();
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

    WellKnownUriExplorer explorer = new WellKnownUriExplorer(getHttpclient(), getRsContext());
    ResultIndex index = new ResultIndex();
    Result<RsRoot> result = explorer.explore(composeUri(path), index);

    assertThat(result.getUri(), equalTo(composeUri(path)));
    assertThat(result.getStatusCode(), equalTo(200));
    assertThat(result.getError().isPresent(), is(true));
    assertThat(result.getError().get(), instanceOf(JAXBException.class));
    assertThat(result.getContent().isPresent(), is(false));
    //result.getError().get().printStackTrace();
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

    WellKnownUriExplorer explorer = new WellKnownUriExplorer(getHttpclient(), getRsContext());
    ResultIndex index = new ResultIndex();
    Result<RsRoot> result = explorer.explore(composeUri(path), index);

    assertThat(result.getUri(), equalTo(composeUri(path)));
    assertThat(result.getStatusCode(), equalTo(404));
    assertThat(result.getError().isPresent(), is(true));
    assertThat(result.getError().get(), instanceOf(RemoteException.class));
    assertThat(result.getContent().isPresent(), is(false));
    //result.getError().get().printStackTrace();
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

  private String createValidResourceDump() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:md capability=\"resourcedump\"\n" +
      "         at=\"2013-01-03T09:00:00Z\"/>\n" +
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
