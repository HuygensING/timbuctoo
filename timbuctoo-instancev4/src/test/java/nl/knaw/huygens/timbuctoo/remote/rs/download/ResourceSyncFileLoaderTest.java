package nl.knaw.huygens.timbuctoo.remote.rs.download;

import nl.knaw.huygens.timbuctoo.v5.IntegrationTest;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static com.google.common.io.Resources.getResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ResourceSyncFileLoaderTest {

  private String baseUrl = "http://127.0.0.1:8080/v5/resourcesync/u33707283d426f900d4d33707283d426f900d4d0d/clusius/";

  @Test
  public void getRemoteFilesListListsAllChangelistAndResourcelistFilesInAnObject() throws Exception {
    String capabilityList = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
      "<urlset xmlns:rs=\"http://www.openarchives.org/rs/terms/\" " +
      "xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
      "  <rs:md capability=\"capabilitylist\"/>\n" +
      "  <rs:ln rel=\"up\" href=\"http://127.0.0.1:8080/.well-known/resourcesync\"/>\n" +
      "  <rs:ln rel=\"describedby\" href=\"" + baseUrl + "description.xml\" type=\"application/rdf+xml\"/>\n" +
      "  <url>\n" +
      "    <loc>" + baseUrl + "resourcelist.xml</loc>\n" +
      "    <rs:md capability=\"resourcelist\"/>\n" +
      "  </url>\n" +
      "  <url>\n" +
      "      <loc>" + baseUrl + "changelist.xml</loc>\n" +
      "      <rs:md capability=\"changelist\"/>\n" +
      "  </url>" +
      "</urlset>\n";

    String resourceList = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
      "<urlset xmlns:rs=\"http://www.openarchives.org/rs/terms/\" " +
      "xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
      "  <rs:md capability=\"resourcelist\" at=\"2018-03-21T10:55:07.907Z\" " +
      "completed=\"2018-03-21T10:56:39.551Z\"/>\n" +
      "  <rs:ln rel=\"up\" href=\"" + baseUrl + "capabilitylist.xml\"/>\n" +
      "  <url>\n" +
      "    <loc>" + baseUrl + "files/dataset.nq</loc>\n" +
      "    <rs:md type=\"application/n-quads\"/>\n" +
      "  </url>\n" +
      "</urlset>\n";

    String changeList = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:md capability=\"changelist\"\n" +
      "         from=\"2013-01-02T00:00:00Z\"\n" +
      "         until=\"2013-01-03T00:00:00Z\"/>\n" +
      "  <url>\n" +
      "      <loc>" + baseUrl + "files/changes1.nqud</loc>\n" +
      "      <lastmod>2013-01-02T13:00:00Z</lastmod>\n" +
      "      <rs:md change=\"updated\" datetime=\"2013-01-02T13:00:00Z\"/>\n" +
      "  </url>\n" +
      "  <url>\n" +
      "      <loc>" + baseUrl + "files/changes2.nqud</loc>\n" +
      "      <lastmod>2013-01-02T13:00:00Z</lastmod>\n" +
      "      <rs:md change=\"updated\" datetime=\"2013-01-02T13:00:00Z\"/>\n" +
      "  </url>\n" +
      "</urlset>";


    InputStream capabilityListStream = new ByteArrayInputStream(capabilityList.getBytes());

    InputStream resourceListStream = new ByteArrayInputStream(resourceList.getBytes());

    InputStream changeListStream = new ByteArrayInputStream(changeList.getBytes());

    InputStream clusiusStream = new FileInputStream(
      new File(getResource(IntegrationTest.class, "bia_clusius.nqud").toURI())
    );

    ResourceSyncFileLoader.RemoteFileRetriever remoteFileRetriever = mock(
      ResourceSyncFileLoader.RemoteFileRetriever.class
    );

    given(remoteFileRetriever.getFile(baseUrl + "capabilitylist.xml")).willReturn(capabilityListStream);

    given(remoteFileRetriever.getFile(baseUrl + "resourcelist.xml")).willReturn(resourceListStream);

    given(remoteFileRetriever.getFile(baseUrl + "files/dataset.nq")
    ).willReturn(clusiusStream);

    given(remoteFileRetriever.getFile(baseUrl + "changelist.xml")
    ).willReturn(changeListStream);


    ResourceSyncFileLoader resourceSyncFileLoader = new ResourceSyncFileLoader(remoteFileRetriever);

    ResourceSyncFileLoader.RemoteFilesList remoteFilesList = resourceSyncFileLoader.getRemoteFilesList(
      baseUrl + "capabilitylist.xml");

    assertThat(remoteFilesList.getChangeList(), containsInAnyOrder(
      hasProperty("url", is(baseUrl + "files/changes1.nqud")),
      hasProperty("url", is(baseUrl + "files/changes2.nqud"))
    ));

    assertThat(remoteFilesList.getResourceList(), contains(
      allOf(hasProperty("url", is(baseUrl + "files/dataset.nq")),
        hasProperty("mimeType", is("application/n-quads")))
    ));

  }


}
