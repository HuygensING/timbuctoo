package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import org.junit.Test;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class LinkListUriExplorerTest extends AbstractRemoteTest {

  @Test
  public void findLinksInDocument() throws Exception {
    String path = "/foo/bar/page1.html";

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(path),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withHeader("Content-Type", "text/html; utf-8")
        .withBody(createValidHtmlWithLinks())
      );

    LinkListUriExplorer explorer = new LinkListUriExplorer(getHttpclient(), getRsContext(), LinkListUriExplorer.linkReader);
    ResultIndex index = new ResultIndex();
    Result<LinkList> result = explorer.explore(composeUri(path), index);

    result.getError().ifPresent(Throwable::printStackTrace);

    assertThat(result.getError().isPresent(), is(false));
    assertThat(result.getContent().isPresent(), is(true));
    LinkList linkList = result.getContent().orElse(new LinkList());
    Set<URI> validUris = linkList.getValidUris();
    assertThat(validUris, containsInAnyOrder(
      composeUri("/foo/bar/dataset1/capabilitylist.xml"),
      URI.create("http://www.example.com/dataset2/capabilitylist.xml")));

  }

  @Test
  public void findLinksInHeader() throws Exception {
    String path = "/foo/bar/page2.html";

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(path),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withHeader("Link", "<http://www.example.com/dataset1/capabilitylist.xml>; rel=\"resourcesync\"")
        .withHeader("Link", "</dataset2/capabilitylist.xml>; rel=\"resourcesync\"")
        .withHeader("Content-Type", "text/html; charset=utf-8")
        .withBody(createValidHtml())
      );

    LinkListUriExplorer explorer = new LinkListUriExplorer(getHttpclient(), getRsContext(), LinkListUriExplorer.linkReader);
    ResultIndex index = new ResultIndex();
    Result<LinkList> result = explorer.explore(composeUri(path), index);

    result.getError().ifPresent(Throwable::printStackTrace);

    assertThat(result.getError().isPresent(), is(false));
    assertThat(result.getContent().isPresent(), is(true));
    LinkList linkList = result.getContent().orElse(new LinkList());
    Set<URI> validUris = linkList.getValidUris();
    assertThat(validUris, containsInAnyOrder(
      composeUri("dataset2/capabilitylist.xml"),
      URI.create("http://www.example.com/dataset1/capabilitylist.xml")));
  }

  @Test
  public void findLinksInRobotsTxt() {
    String path = "/robots.txt";

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(path),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withHeader("Content-Type", "text/plain; utf-8")
        .withBody(createRobotsTxt())
      );

    LinkListUriExplorer explorer = new LinkListUriExplorer(getHttpclient(), getRsContext(), LinkListUriExplorer.robotsReader);
    ResultIndex index = new ResultIndex();
    Result<LinkList> result = explorer.explore(composeUri(path), index);

    result.getError().ifPresent(Throwable::printStackTrace);

    assertThat(result.getError().isPresent(), is(false));
    assertThat(result.getContent().isPresent(), is(true));
    LinkList linkList = result.getContent().orElse(new LinkList());
    Set<URI> validUris = linkList.getValidUris();
    assertThat(validUris, containsInAnyOrder(
      composeUri("dataset1/resourcelist1.xml"),
      composeUri("/dataset2/resourcelist.xml"),
      composeUri("/some/other/just_a_sitemap.xml")));
  }

  private String createValidHtmlWithLinks() {
    return "<html>\n" +
      "  <head>\n" +
      "    <link rel=\"resourcesync\"\n" +
      "          href=\"dataset1/capabilitylist.xml\"/>\n" +
      "    <link rel=\"resourcesync\"\n" +
      "          href=\"http://www.example.com/dataset2/capabilitylist.xml\"/>\n" +
      "    <link rel='stylesheet' id='style-css'  " +
      "           href='https://www.example.com/mkyong/style.css?ver=1.4.7' type='text/css' media='all' />" +
      "  </head>\n" +
      "  <body>...</body>\n" +
      "</html>";
  }

  private String createValidHtml() {
    return "<html><head/><body/></html>";
  }

  private String createRobotsTxt() {
    return "User-agent: *\n" +
      "Disallow: /cgi-bin/\n" +
      "Disallow: /tmp/\n" +
      "Sitemap: dataset1/resourcelist1.xml\n" +
      "Sitemap: /dataset2/resourcelist.xml\n" +
      "Sitemap: /some/other/just_a_sitemap.xml";
  }
}
