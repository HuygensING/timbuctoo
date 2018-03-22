package nl.knaw.huygens.timbuctoo.remote.rs.download;

import nl.knaw.huygens.timbuctoo.v5.IntegrationTest;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.stream.Stream;

import static com.google.common.io.Resources.getResource;
import static nl.knaw.huygens.hamcrest.OptionalPresentMatcher.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ResourceSyncFileLoaderTest {

  private String baseUrl = "http://127.0.0.1:8080/v5/resourcesync/u33707283d426f900d4d33707283d426f900d4d0d/clusius/";

  @Test
  public void loadFilesFailsIfBothChangeListAndDatasetRdfUnavailable() throws Exception {

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
      "</urlset>\n";

    String resourceList = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
      "<urlset xmlns:rs=\"http://www.openarchives.org/rs/terms/\" " +
      "xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
      "  <rs:md capability=\"resourcelist\" at=\"2018-03-21T10:55:07.907Z\" " +
      "completed=\"2018-03-21T10:56:39.551Z\"/>\n" +
      "  <rs:ln rel=\"up\" href=\"" + baseUrl + "capabilitylist.xml\"/>\n" +
      "  <url>\n" +
      "    <loc>" + baseUrl + "files/332231fe-6530-4e46-ae17-13499bd609c0-bia_clusius_nqud</loc>\n" +
      "    <rs:md type=\"application/vnd.timbuctoo-rdf.nquads_unified_diff\"/>\n" +
      "  </url>\n" +
      "</urlset>\n";

    InputStream capabilityListStream = new ByteArrayInputStream(capabilityList.getBytes());

    InputStream resourceListStream = new ByteArrayInputStream(resourceList.getBytes());

    InputStream clusiusStream = new FileInputStream(
      new File(getResource(IntegrationTest.class, "bia_clusius.nqud").toURI())
    );


    ResourceSyncFileLoader.RemoteFileRetriever remoteFileRetriever = mock(
      ResourceSyncFileLoader.RemoteFileRetriever.class
    );

    given(remoteFileRetriever.getFile(baseUrl + "capabilitylist.xml")).willReturn(capabilityListStream);

    given(remoteFileRetriever.getFile(baseUrl + "resourcelist.xml")).willReturn(resourceListStream);

    given(remoteFileRetriever.getFile(baseUrl + "files/332231fe-6530-4e46-ae17-13499bd609c0-bia_clusius_nqud")
    ).willReturn(clusiusStream);

    ResourceSyncFileLoader resourceSyncFileLoader = new ResourceSyncFileLoader(remoteFileRetriever);

    Stream<RemoteFile> remoteFileStream = resourceSyncFileLoader.loadFiles(baseUrl + "capabilitylist.xml");

    assertThat(remoteFileStream.findFirst(), is(not(present())));
  }

  @Test
  public void loadFilesLoadsDatasetRdfIfChangeListUnavailable() throws Exception {

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
      "</urlset>\n";

    String resourceList = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
      "<urlset xmlns:rs=\"http://www.openarchives.org/rs/terms/\" " +
      "xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
      "  <rs:md capability=\"resourcelist\" at=\"2018-03-21T10:55:07.907Z\" " +
      "completed=\"2018-03-21T10:56:39.551Z\"/>\n" +
      "  <rs:ln rel=\"up\" href=\"" + baseUrl + "capabilitylist.xml\"/>\n" +
      "  <url>\n" +
      "    <loc>" + baseUrl + "files/dataset.nq</loc>\n" +
      "    <rs:md type=\"application/vnd.timbuctoo-rdf.nquads_unified_diff\"/>\n" +
      "  </url>\n" +
      "</urlset>\n";


    InputStream capabilityListStream = new ByteArrayInputStream(capabilityList.getBytes());

    InputStream resourceListStream = new ByteArrayInputStream(resourceList.getBytes());

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

    ResourceSyncFileLoader resourceSyncFileLoader = new ResourceSyncFileLoader(remoteFileRetriever);

    Stream<RemoteFile> remoteFileStream = resourceSyncFileLoader.loadFiles(baseUrl + "capabilitylist.xml");

    assertThat(remoteFileStream.findFirst().get().getUrl(), is(baseUrl + "files/dataset.nq"));
  }

  @Test
  public void loadFilesLoadsFromChangePatchesIfChangeListIsPresent() throws Exception {

  }

}
