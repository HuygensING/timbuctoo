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

public class SourceDescriptionTest {
  private File sourceDescription;
  private SourceDescription instance;

  @Before
  public void setUp() throws Exception {
    sourceDescription = File.createTempFile("sourceDescription", "xml");
    instance = new SourceDescription(sourceDescription);
  }

  @After
  public void tearDown() {
    sourceDescription.delete();
  }

  @Test
  public void theConstructorCreatesTheFile() throws Exception {
    Source expected = Input.fromByteArray(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:md capability=\"description\"/>\n" +
      "</urlset>").getBytes(StandardCharsets.UTF_8)).build();

    Source actual = Input.fromFile(sourceDescription).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void addCapabilityListAddsALinkToACapabilityList() throws Exception {
    File capabilityList = new File("capabilityList.xml");

    instance.addCapabilityList(capabilityList);

    Source expected = Input.fromByteArray(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:md capability=\"description\"/>\n" +
      "  <url>\n" +
      "      <loc>" + capabilityList.getPath() + "</loc>\n" +
      "      <rs:md capability=\"capabilitylist\"/>\n" +
      "  </url>" +
      "</urlset>").getBytes(StandardCharsets.UTF_8)).build();
    Source actual = Input.fromFile(sourceDescription).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }
}
