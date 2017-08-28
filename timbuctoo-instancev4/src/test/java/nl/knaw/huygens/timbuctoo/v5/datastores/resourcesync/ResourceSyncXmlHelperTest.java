package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultComparisonFormatter;

import javax.xml.transform.Source;
import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class ResourceSyncXmlHelperTest {

  private File file;
  private ResourceSyncXmlHelper instance;

  @Before
  public void setUp() throws Exception {
    file = File.createTempFile("file", "xml");
    instance = new ResourceSyncXmlHelper(file, (node, document) -> {
    });
  }

  @After
  public void tearDown() {
    file.delete();
  }

  @Test
  public void addUrlElementWithCapabilityDoesNotAnUrlElementForAKnownUrl() throws Exception {
    instance.addUrlElementWithCapability("http://example.com", "test");
    instance.addUrlElementWithCapability("http://example.com", "test");
    instance.save();

    Source expected = Input.fromByteArray(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <url>\n" +
      "      <loc>http://example.com</loc>\n" +
      "      <rs:md capability=\"test\"/>\n" +
      "  </url>" +
      "</urlset>").getBytes(StandardCharsets.UTF_8)).build();
    Source actual = Input.fromFile(file).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void addUrlElementWithTypeDoesNotAnUrlElementForAKnownUrl() throws Exception {
    instance.addUrlElementWithType("http://example.com", "application/test");
    instance.addUrlElementWithType("http://example.com", "application/test");
    instance.save();

    Source expected = Input.fromByteArray(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <url>\n" +
      "      <loc>http://example.com</loc>\n" +
      "      <rs:md type=\"application/test\"/>\n" +
      "  </url>" +
      "</urlset>").getBytes(StandardCharsets.UTF_8)).build();
    Source actual = Input.fromFile(file).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void addUrlElementDoesNotAnUrlElementForAKnownUrl() throws Exception {
    instance.addUrlElement("http://example.com");
    instance.addUrlElement("http://example.com");
    instance.save();

    Source expected = Input.fromByteArray(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <url>\n" +
      "      <loc>http://example.com</loc>\n" +
      "  </url>" +
      "</urlset>").getBytes(StandardCharsets.UTF_8)).build();
    Source actual = Input.fromFile(file).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

}
