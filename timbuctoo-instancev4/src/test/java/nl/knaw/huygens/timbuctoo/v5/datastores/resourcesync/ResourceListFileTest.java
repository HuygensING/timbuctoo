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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class ResourceListFileTest {


  private File resourcelist;
  private ResourceListFile instance;
  private Clock clock;
  private ResourceSyncUriHelper uriHelper;

  @Before
  public void setUp() throws Exception {
    resourcelist = new File("resourcelist.xml");
    clock = mock(Clock.class);
    given(clock.withZone(any(ZoneId.class))).willReturn(clock);
    uriHelper = mock(ResourceSyncUriHelper.class);
    instance = new ResourceListFile(resourcelist, clock, uriHelper);
  }

  @After
  public void tearDown() {
    resourcelist.delete();
  }

  @Test
  public void addFileAddsFilesToTheResourceListFile() throws Exception {
    given(clock.instant()).willReturn(Instant.parse("2013-01-03T09:00:00Z"));
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
        "      <rs:md type=\"application/octet-stream\"/>" +
        "  </url>" +
        "  <url>" +
        "      <loc>http://example.org/2</loc>" +
        "      <rs:md type=\"application/octet-stream\"/>" +
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
    given(clock.instant()).willReturn(Instant.parse("2013-01-03T09:00:00Z"), Instant.parse("2014-02-03T09:00:00Z"));
    File file = new File("fileName");
    given(uriHelper.uriForFile(file)).willReturn("http://example.org/1");
    File file2 = new File("fileName2");
    given(uriHelper.uriForFile(file2)).willReturn("http://example.org/2");

    instance.addFile(cachedFile(file));
    ResourceListFile otherInstance = new ResourceListFile(resourcelist, clock, uriHelper);
    otherInstance.addFile(cachedFile(file2));

    Source expected = Input.fromByteArray(
      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"" +
        "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">" +
        "  <rs:md capability=\"resourcelist\"" +
        "         at=\"2014-02-03T09:00:00Z\"/>" +
        "  <url>" +
        "      <loc>http://example.org/1</loc>" +
        "      <rs:md type=\"application/octet-stream\"/>" +
        "  </url>" +
        "  <url>" +
        "      <loc>http://example.org/2</loc>" +
        "      <rs:md type=\"application/octet-stream\"/>" +
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
  public void addFileAddsAMimeTypeIfTheCachedFileContainsOne() throws Exception {
    given(clock.instant()).willReturn(Instant.parse("2013-01-03T09:00:00Z"), Instant.parse("2014-02-03T09:00:00Z"));
    File file = new File("fileName");
    given(uriHelper.uriForFile(file)).willReturn("http://example.org/1");
    File file2 = new File("fileName2");
    given(uriHelper.uriForFile(file2)).willReturn("http://example.org/2");

    instance.addFile(cachedFile(file, MediaType.APPLICATION_XML_TYPE));
    instance.addFile(cachedFile(file2));

    Source expected = Input.fromByteArray(
      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"" +
        "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">" +
        "  <rs:md capability=\"resourcelist\"" +
        "         at=\"2014-02-03T09:00:00Z\"/>" +
        "  <url>" +
        "      <loc>http://example.org/1</loc>" +
        "      <rs:md type=\"application/xml\"/>" +
        "  </url>" +
        "  <url>" +
        "      <loc>http://example.org/2</loc>" +
        "      <rs:md type=\"application/octet-stream\"/>" +
        "  </url>" +
        "</urlset>"
      ).getBytes(StandardCharsets.UTF_8)
    ).build();
    Source actual = Input.fromFile(resourcelist).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  private CachedFile cachedFile(final File file, final MediaType type) {
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
      public MediaType getMimeType() {
        return type;
      }

      @Override
      public void close() throws Exception {
      }
    };
  }

  private CachedFile cachedFile(final File file) {
    return cachedFile(file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
  }
}
