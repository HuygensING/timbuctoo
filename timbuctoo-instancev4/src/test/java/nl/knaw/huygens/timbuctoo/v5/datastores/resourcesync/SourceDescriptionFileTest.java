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

public class SourceDescriptionFileTest {
  private File sourceDescription;
  private SourceDescriptionFile instance;
  private ResourceSyncUriHelper uriHelper;

  @Before
  public void setUp() throws Exception {
    uriHelper = mock(ResourceSyncUriHelper.class);
    sourceDescription = File.createTempFile("sourceDescription", "xml");
    instance = new SourceDescriptionFile(sourceDescription, uriHelper);
  }

  @After
  public void tearDown() {
    sourceDescription.delete();
  }

  @Test
  public void addCapabilityListAddsALinkToACapabilityList() throws Exception {
    File capabilityList = new File("capabilityList.xml");
    given(uriHelper.uriForFile(capabilityList)).willReturn("http://example.org/capabilitylist");

    File descriptionFile = new File("description.xml");
    given(uriHelper.uriForFile(descriptionFile)).willReturn("http://example.org/description.xml");


    instance.addCapabilityList(capabilityList, descriptionFile);

    Source expected = Input.fromByteArray(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:md capability=\"description\"/>\n" +
      "  <url>\n" +
      "      <loc>http://example.org/capabilitylist</loc>\n" +
      "      <rs:md capability=\"capabilitylist\"/>\n" +
      "      <rs:ln rel=\"describedBy\" href=\"http://example.org/description.xml\" " +
        "type=\"application/rdf+xml\"/>" +
      "  </url>" +
      "</urlset>").getBytes(StandardCharsets.UTF_8)).build();
    Source actual = Input.fromFile(sourceDescription).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void removeCapabilityListRemovesTheTheLink() throws Exception {
    File capabilityList = new File("capabilityList.xml");
    given(uriHelper.uriForFile(capabilityList)).willReturn("http://example.org/capabilitylist");

    File descriptionFile = new File("description.xml");
    given(uriHelper.uriForFile(descriptionFile)).willReturn("http://example.org/description.xml");

    instance.addCapabilityList(capabilityList, descriptionFile);

    Source expected = Input.fromByteArray(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:md capability=\"description\"/>\n" +
      "  <url>\n" +
      "      <loc>http://example.org/capabilitylist</loc>\n" +
      "      <rs:md capability=\"capabilitylist\"/>\n" +
      "      <rs:ln rel=\"describedBy\" href=\"http://example.org/description.xml\" " +
      "type=\"application/rdf+xml\"/>" +
      "  </url>" +
      "</urlset>").getBytes(StandardCharsets.UTF_8)).build();
    Source actual = Input.fromFile(sourceDescription).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );

    instance.removeCapabilityList(capabilityList);

    expected = Input.fromByteArray(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
      "        xmlns:rs=\"http://www.openarchives.org/rs/terms/\">\n" +
      "  <rs:md capability=\"description\"/>\n" +
      "</urlset>").getBytes(StandardCharsets.UTF_8)).build();
    actual = Input.fromFile(sourceDescription).build();
    assertThat(actual,
      isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }
}
