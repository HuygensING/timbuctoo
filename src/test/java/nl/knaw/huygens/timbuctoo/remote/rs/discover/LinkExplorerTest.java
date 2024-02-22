package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.Urlset;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class LinkExplorerTest extends AbstractRemoteTest {
  @Test
  @Disabled
  public void findLinksInDocumentAndFindChildren() throws Exception {
    String path = "/foo/bar/page1.html";
    String capabilityListPath = "/foo/bar/dataset1/capabilitylist.xml";

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

    getMockServer()
      .when(HttpRequest.request()
          .withMethod("GET")
          .withPath(capabilityListPath),
        Times.exactly(1))

      .respond(HttpResponse.response()
        .withStatusCode(200)
        .withHeader("Content-Type", "text/xml; utf-8")
        .withBody(createCapabilityList())
      );

    LinkExplorer explorer = new LinkExplorer(getHttpclient(), getRsContext(), LinkExplorer.linkReader);
    URI uri = composeUri(path);
    ResultIndex index = new ResultIndex();
    Result<LinkList> result = explorer.explore(uri, index, null);

    result.getErrors().forEach(Throwable::printStackTrace);

    assertThat(result.getErrors().isEmpty(), is(true));
    assertThat(result.getContent().isPresent(), is(true));
    LinkList linkList = result.getContent().orElse(new LinkList());
    Set<URI> validUris = linkList.getValidUris();
    assertThat(validUris, containsInAnyOrder(
      composeUri(capabilityListPath),
      URI.create("http://www.example.com/dataset2/capabilitylist.xml")));
    assertThat(linkList.getInvalidUris().size(), equalTo(0));

    assertThat(index.contains(uri), is(true));
    assertThat(result.getChildren().keySet(), containsInAnyOrder(
      composeUri("/foo/bar/dataset1/capabilitylist.xml"),
      URI.create("http://www.example.com/dataset2/capabilitylist.xml")));

    Result<?> child1 = result.getChildren().get(composeUri(capabilityListPath));
    assertThat(child1.getParents().containsKey(result.getUri()), is(true));

    Result<?> child2 = result.getChildren().get(URI.create("http://www.example.com/dataset2/capabilitylist.xml"));
    assertThat(child2.getParents().containsKey(result.getUri()), is(true));

    assertThat(child1.getErrors().isEmpty(), is(true));
    assertThat(child2.getErrors().isEmpty(), is(false));
    //child2.listErrors().forEach(Throwable::printStackTrace); // RemoteException: 404 Not Found

    assertThat(child1.getContent().isPresent(), is(true));
    assertThat(child1.getContent().orElse(null), instanceOf(Urlset.class));
    assertThat(child1.getChildren().size(), equalTo(4));
    // LinkList --> capabilitylist --> resourcelist
    // assertThat result-with-resourcelist (404 btw) has parent result-with-capabilitylist.
    assertThat(child1.getChildren().get(
      URI.create("http://example.com/dataset1/resourcelist.xml"))
      .getParents().containsKey(child1.getUri()), is(true));
  }

  @Test
  @Disabled
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

    LinkExplorer explorer = new LinkExplorer(getHttpclient(), getRsContext(), LinkExplorer.linkReader);
    ResultIndex index = new ResultIndex();
    Result<LinkList> result = explorer.explore(composeUri(path), index, null);

    result.getErrors().forEach(Throwable::printStackTrace);

    assertThat(result.getErrors().isEmpty(), is(true));
    assertThat(result.getContent().isPresent(), is(true));
    LinkList linkList = result.getContent().orElse(new LinkList());
    Set<URI> validUris = linkList.getValidUris();
    assertThat(validUris, containsInAnyOrder(
      composeUri("dataset2/capabilitylist.xml"),
      URI.create("http://www.example.com/dataset1/capabilitylist.xml")));
    assertThat(linkList.getInvalidUris().size(), equalTo(0));

    assertThat(index.contains(composeUri(path)), is(true));
  }

  @Test
  @Disabled
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

    LinkExplorer explorer = new LinkExplorer(getHttpclient(), getRsContext(), LinkExplorer.robotsReader);
    ResultIndex index = new ResultIndex();
    Result<LinkList> result = explorer.explore(composeUri(path), index, null);

    result.getErrors().forEach(Throwable::printStackTrace);

    assertThat(result.getErrors().isEmpty(), is(true));
    assertThat(result.getContent().isPresent(), is(true));
    LinkList linkList = result.getContent().orElse(new LinkList());
    Set<URI> validUris = linkList.getValidUris();
    assertThat(validUris, containsInAnyOrder(
      composeUri("dataset1/resourcelist1.xml"),
      composeUri("/dataset2/resourcelist.xml"),
      composeUri("/some/other/just_a_sitemap.xml")));
    assertThat(linkList.getInvalidUris().size(), equalTo(0));

    assertThat(index.contains(composeUri(path)), is(true));
  }

  private String createValidHtmlWithLinks() {
    return """
        <html>
          <head>
            <link rel="resourcesync"
                  href="dataset1/capabilitylist.xml"/>
            <link rel="resourcesync"
                  href="http://www.example.com/dataset2/capabilitylist.xml"/>
            <link rel='stylesheet' id='style-css'             href='https://www.example.com/mkyong/style.css?ver=1.4.7' type='text/css' media='all' />  </head>
          <body>...</body>
        </html>""";
  }

  private String createValidHtml() {
    return "<html><head/><body/></html>";
  }

  private String createRobotsTxt() {
    return """
        User-agent: *
        Disallow: /cgi-bin/
        Disallow: /tmp/
        Sitemap: dataset1/resourcelist1.xml
        Sitemap: /dataset2/resourcelist.xml
        Sitemap: /some/other/just_a_sitemap.xml""";
  }

  private String createCapabilityList() {
    return
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
                    xmlns:rs="http://www.openarchives.org/rs/terms/">
              <rs:ln rel="describedby"
                     href="http://example.com/info_about_set1_of_resources.xml"/>
              <rs:ln rel="up"
                     href="http://example.com/resourcesync_description.xml"/>
              <rs:md capability="capabilitylist"/>
              <url>
                  <loc>http://example.com/dataset1/resourcelist.xml</loc>
                  <rs:md capability="resourcelist"/>
              </url>
              <url>
                  <loc>http://example.com/dataset1/resourcedump.xml</loc>
                  <rs:md capability="resourcedump"/>
              </url>
              <url>
                  <loc>http://example.com/dataset1/changelist.xml</loc>
                  <rs:md capability="changelist"/>
              </url>
              <url>
                  <loc>http://example.com/dataset1/changedump.xml</loc>
                  <rs:md capability="changedump"/>
              </url>
            </urlset>
            """;
  }
}
