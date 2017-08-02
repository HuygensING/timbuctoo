package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultComparisonFormatter;

import javax.ws.rs.core.MediaType;
import javax.xml.transform.Source;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class FileSystemResourceListTest {


  private File resourcelist;
  private FileSystemResourceList instance;
  private ResourceSyncDateFormatter resourceSyncDateFormatter;

  @Before
  public void setUp() throws Exception {
    resourcelist = new File("resourcelist.xml");
    resourceSyncDateFormatter = mock(ResourceSyncDateFormatter.class);
    instance = new FileSystemResourceList(resourcelist, resourceSyncDateFormatter);
  }

  @After
  public void tearDown() {
    resourcelist.delete();
  }

  @Test
  public void addFileAddsFilesToTheResourceListFile() throws Exception {
    given(resourceSyncDateFormatter.now()).willReturn("2013-01-03T09:00:00Z");
    File file = new File("fileName");
    File file2 = new File("fileName2");

    instance.addFile(cachedFile(file));
    instance.addFile(cachedFile(file2));

    Source expected = Input.fromByteArray(
      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"" +
        "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">" +
        "  <rs:md capability=\"resourcelist\"" +
        "         at=\"2013-01-03T09:00:00Z\"/>" +
        "  <url>" +
        "      <loc>" + file.getPath() + "</loc>" +
        "  </url>" +
        "  <url>" +
        "      <loc>" + file2.getPath() + "</loc>" +
        "  </url>" +
        "</urlset>"
      ).getBytes(StandardCharsets.UTF_8)
    ).build();
    Source actual = Input.fromFile(resourcelist).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void addFileUpdatesAnExistingResourceListFile() throws Exception {
    given(resourceSyncDateFormatter.now()).willReturn("2013-01-03T09:00:00Z", "2014-02-03T09:00:00Z");
    File file = new File("fileName");
    File file2 = new File("fileName2");

    instance.addFile(cachedFile(file));

    FileSystemResourceList otherInstance = new FileSystemResourceList(resourcelist, resourceSyncDateFormatter);
    otherInstance.addFile(cachedFile(file2));

    Source expected = Input.fromByteArray(
      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"" +
        "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">" +
        "  <rs:md capability=\"resourcelist\"" +
        "         at=\"2014-02-03T09:00:00Z\"/>" +
        "  <url>" +
        "      <loc>" + file.getPath() + "</loc>" +
        "  </url>" +
        "  <url>" +
        "      <loc>" + file2.getPath() + "</loc>" +
        "  </url>" +
        "</urlset>"
      ).getBytes(StandardCharsets.UTF_8)
    ).build();
    Source actual = Input.fromFile(resourcelist).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  private CachedFile cachedFile(final File file) {
    return new CachedFile() {
      @Override
      public String getName() {
        return "fileName";
      }

      @Override
      public File getFile() {
        return file;
      }

      @Override
      public Optional<MediaType> getMimeType() {
        return Optional.of(MediaType.APPLICATION_XML_TYPE);
      }

      @Override
      public void close() throws Exception {
      }
    };
  }
}
