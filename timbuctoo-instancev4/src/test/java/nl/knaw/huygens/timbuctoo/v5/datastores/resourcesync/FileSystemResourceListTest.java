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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class FileSystemResourceListTest {


  private File resourcelist;
  private FileSystemResourceList instance;
  private ResourceSyncDateFormatter dateFormatter;
  private ResourceSyncUriHelper uriHelper;

  @Before
  public void setUp() throws Exception {
    resourcelist = new File("resourcelist.xml");
    dateFormatter = mock(ResourceSyncDateFormatter.class);
    uriHelper = mock(ResourceSyncUriHelper.class);
    instance = new FileSystemResourceList(resourcelist, dateFormatter, uriHelper);
  }

  @After
  public void tearDown() {
    resourcelist.delete();
  }

  @Test
  public void addFileAddsFilesToTheResourceListFile() throws Exception {
    given(dateFormatter.now()).willReturn("2013-01-03T09:00:00Z");
    File file = new File("fileName");
    given(uriHelper.uriForFile(file)).willReturn("http://example.org/1");
    File file2 = new File("fileName2");
    given(uriHelper.uriForFile(file2)).willReturn("http://example.org/2");

    instance.addFile(cachedFile(file));
    instance.addFile(cachedFile(file2));

    Source expected = Input.fromByteArray(
      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"" +
        "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">" +
        "  <rs:md capability=\"resourcelist\"" +
        "         at=\"2013-01-03T09:00:00Z\"/>" +
        "  <url>" +
        "      <loc>http://example.org/1</loc>" +
        "  </url>" +
        "  <url>" +
        "      <loc>http://example.org/2</loc>" +
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
    given(dateFormatter.now()).willReturn("2013-01-03T09:00:00Z", "2014-02-03T09:00:00Z");
    File file = new File("fileName");
    given(uriHelper.uriForFile(file)).willReturn("http://example.org/1");
    File file2 = new File("fileName2");
    given(uriHelper.uriForFile(file2)).willReturn("http://example.org/2");

    instance.addFile(cachedFile(file));

    FileSystemResourceList otherInstance = new FileSystemResourceList(resourcelist, dateFormatter, uriHelper);
    otherInstance.addFile(cachedFile(file2));

    Source expected = Input.fromByteArray(
      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"" +
        "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">" +
        "  <rs:md capability=\"resourcelist\"" +
        "         at=\"2014-02-03T09:00:00Z\"/>" +
        "  <url>" +
        "      <loc>http://example.org/1</loc>" +
        "  </url>" +
        "  <url>" +
        "      <loc>http://example.org/2</loc>" +
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
