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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class CapabilityListTest {

  private File capabilityListFile;
  private File sourceDescription;
  private CapabilityList instance;
  private ResourceSyncUriHelper uriHelper;

  @Before
  public void setUp() throws Exception {
    capabilityListFile = File.createTempFile("capabilityList", "xml");
    sourceDescription = File.createTempFile("sourceDescription", "xml");
    uriHelper = mock(ResourceSyncUriHelper.class);
    given(uriHelper.uriForFile(sourceDescription)).willReturn("http://example.org/sourcedesc");
    instance = new CapabilityList(capabilityListFile, sourceDescription, uriHelper);
  }

  @After
  public void tearDown() {
    capabilityListFile.delete();
  }

  @Test
  public void theConstructorCreatesTheFile() throws Exception {
    Source expected = Input.fromByteArray(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:ln rel=\"up\"\n" +
      "         href=\"http://example.org/sourcedesc\"/>\n" +
      "  <rs:md capability=\"capabilitylist\"/>\n" +
      "</urlset>").getBytes(StandardCharsets.UTF_8)).build();

    Source actual = Input.fromFile(capabilityListFile).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void addResourceListAddsALinkToAResourceList() throws Exception {
    File resourceList = new File("resourceList.xml");
    given(uriHelper.uriForFile(resourceList)).willReturn("http://example.org/resourcelist");

    instance.addResourceList(resourceList);

    Source expected = Input.fromByteArray(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:ln rel=\"up\"\n" +
      "         href=\"http://example.org/sourcedesc\"/>\n" +
      "  <rs:md capability=\"capabilitylist\"/>\n" +
      "  <url>\n" +
      "      <loc>http://example.org/resourcelist</loc>\n" +
      "      <rs:md capability=\"resourcelist\"/>\n" +
      "  </url>" +
      "</urlset>").getBytes(StandardCharsets.UTF_8)).build();
    Source actual = Input.fromFile(capabilityListFile).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }
}
