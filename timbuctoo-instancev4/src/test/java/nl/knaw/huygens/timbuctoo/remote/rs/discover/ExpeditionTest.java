package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import org.junit.Test;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ExpeditionTest extends AbstractRemoteTest {

  @Test
  public void createWellknownUri() throws Exception {
    String[][] expectations = {
      { "http://foo.com/.well-known/resourcesync", "http://foo.com/.well-known/resourcesync" },
      { "http://foo.com/", "http://foo.com/.well-known/resourcesync" },
      { "http://foo.com", "http://foo.com/.well-known/resourcesync" },

      { "http://foo.com/bar/.well-known/resourcesync", "http://foo.com/bar/.well-known/resourcesync" },
      { "http://foo.com/bar/", "http://foo.com/.well-known/resourcesync" },
      { "http://foo.com/bar", "http://foo.com/.well-known/resourcesync" },
      { "http://foo.com/bar?this=bla&that=so", "http://foo.com/.well-known/resourcesync" }
    };

    for (String[] expect : expectations) {
      URI uri = URI.create(expect[0]);
      URI wk = Expedition.createWellKnownUri(uri);
      //System.out.println(expect[0] + " -> " + expect[1] + " -> " + wk.toString());
      assertThat(wk, equalTo(URI.create(expect[1])));
    }
  }

  @Test
  public void exploreAndFindResults() throws Exception {
    String path = "/foo";
    getMockServer().reset();
    //description
    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath("/.well-known/resourcesync"),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withBody(createDescription(path))
      );

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath("/robots.txt"),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(404)
        .withBody("No robots here.")
      );

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(path),
        Times.exactly(2))

      .respond(HttpResponse.response()
        .withStatusCode(404)
        .withBody("No foo.")
      );

    String url = composePath(path);

    Expedition expedition = new Expedition(getHttpclient(), getRsContext());
    List<ResultIndex> indexes = expedition.explore(url);

    //indexes.forEach(resultIndex -> resultIndex.getResultMap()
    //  .forEach((uri, result) -> System.out.println(uri + " " + result.getContent().isPresent())));

    assertThat(indexes.size(), equalTo(4));

    long finds = indexes.stream()
      .filter(resultIndex -> !resultIndex.getResultsWithContent().isEmpty()).count();
    assertThat(finds, equalTo(1L));

  }

  private String createDescription(String basePath) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:ln rel=\"describedby\"\n" +
      "         href=\"http://example.com/info_about_source.xml\"/>\n" +
      "  <rs:md capability=\"description\"/>\n" +
      "  <url>\n" +
      "      <loc>" + composePath(basePath + "/capabilitylist1.xml") + "</loc>\n" +
      "      <rs:md capability=\"capabilitylist\"/>\n" +
      "      <rs:ln rel=\"describedby\"\n" +
      "             href=\"http://example.com/info_about_set1_of_resources.xml\"/>\n" +
      "  </url>\n" +
      "  <url>\n" +
      "      <loc>" + composePath(basePath + "/capabilitylist2.xml") + "</loc>\n" +
      "      <rs:md capability=\"capabilitylist\"/>\n" +
      "      <rs:ln rel=\"describedby\"\n" +
      "             href=\"http://example.com/info_about_set2_of_resources.xml\"/>\n" +
      "  </url>\n" +
      "  <url>\n" +
      "      <loc>" + composePath(basePath + "/capabilitylist3.xml") + "</loc>\n" +
      "      <rs:md capability=\"capabilitylist\"/>\n" +
      "      <rs:ln rel=\"describedby\"\n" +
      "             href=\"http://example.com/info_about_set3_of_resources.xml\"/>\n" +
      "  </url>\n" +
      "</urlset>";
  }
}
