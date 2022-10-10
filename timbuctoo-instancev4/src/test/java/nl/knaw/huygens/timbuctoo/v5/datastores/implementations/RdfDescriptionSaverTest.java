package nl.knaw.huygens.timbuctoo.v5.datastores.implementations;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.EntityMetadataProp;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.Metadata;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.MetadataProp;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.SimpleMetadataProp;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.UriMetadataProp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultComparisonFormatter;

import javax.xml.transform.Source;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.MARKDOWN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class RdfDescriptionSaverTest {
  private static final String BASE_URI = "http://example.org/datasets/DUMMY/clusius2";
  private RdfDescriptionSaver testRdfDescriptionSaver;
  private String descriptionFileName;
  private File descriptionFile;
  private LogList logList;
  private ImportStatus importStatus;
  private Metadata metadata;

  @Before
  public void setUp() throws Exception {
    descriptionFileName = "description.xml";
    descriptionFile = new File(descriptionFileName);
    logList = new LogList();
    importStatus = new ImportStatus(logList);

    Map<String, MetadataProp> owner = new TreeMap<>();
    owner.put("name", SimpleMetadataProp.create("http://schema.org/name"));
    owner.put("email", SimpleMetadataProp.create("http://schema.org/email"));

    Map<String, MetadataProp> contact = new TreeMap<>();
    contact.put("name", SimpleMetadataProp.create("http://schema.org/name"));
    contact.put("email", SimpleMetadataProp.create("http://schema.org/email"));

    Map<String, MetadataProp> provenanceInfo = new TreeMap<>();
    provenanceInfo.put("title", SimpleMetadataProp.create("http://purl.org/dc/terms/title"));
    provenanceInfo.put("body", SimpleMetadataProp.create("http://purl.org/dc/terms/description", MARKDOWN));

    Map<String, MetadataProp> props = new TreeMap<>();
    props.put("title", SimpleMetadataProp.create("http://purl.org/dc/terms/title"));
    props.put("description", SimpleMetadataProp.create("http://purl.org/dc/terms/description", MARKDOWN));
    props.put("imageUrl", SimpleMetadataProp.create("http://xmlns.com/foaf/0.1/depiction"));
    props.put("license", UriMetadataProp.create("http://purl.org/dc/terms/license"));
    props.put("owner", EntityMetadataProp.create("http://purl.org/dc/terms/rightsHolder", true, "rightsHolder", owner));
    props.put("contact", EntityMetadataProp.create("http://schema.org/ContactPoint", true, "ContactPoint", contact));
    props.put("provenanceInfo", EntityMetadataProp.create("http://purl.org/dc/terms/provenance", true, "Provenance", provenanceInfo));

    metadata = Metadata.create(Optional.empty(), props);

    testRdfDescriptionSaver = new RdfDescriptionSaver(metadata, descriptionFile, BASE_URI, importStatus);
    testRdfDescriptionSaver.start(0);
  }

  @After
  public void tearDown() throws Exception {
    descriptionFile.delete();
  }

  @Test
  public void addsValue() throws Exception {
    testRdfDescriptionSaver.addValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <title xmlns=\"http://purl.org/dc/terms/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "DWC Data</title> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void addsMultipleValues() throws Exception {
    testRdfDescriptionSaver.addValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.addValue(BASE_URI,
        "http://purl.org/dc/terms/description", "Biographical data of the Digital Web Centre for " +
            "the History of Science (DWC)", null, BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <description xmlns=\"http://purl.org/dc/terms/\">" +
                "Biographical data of the Digital Web Centre for the History of Science (DWC)</description> \n" +
                "    <title xmlns=\"http://purl.org/dc/terms/\">DWC Data</title> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void addsValueToExistingDescriptionFile() throws Exception {
    testRdfDescriptionSaver.addValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.commit();
    testRdfDescriptionSaver.addValue(BASE_URI,
        "http://purl.org/dc/terms/description", "Biographical data of the Digital Web Centre for " +
            "the History of Science (DWC)", null, BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                ">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <description xmlns=\"http://purl.org/dc/terms/\">" +
                "Biographical data of the Digital Web Centre for the History of Science (DWC)</description>\n" +
                "    <title xmlns=\"http://purl.org/dc/terms/\">DWC Data</title>\n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }


  @Test
  public void addsMultipleValuesToSamePredicate() throws Exception {
    testRdfDescriptionSaver.addValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.addValue(BASE_URI,
        "http://purl.org/dc/terms/title", "DWC Data 2",
        null, BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                ">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <title xmlns=\"http://purl.org/dc/terms/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "DWC Data</title> \n" +
                "    <title xmlns=\"http://purl.org/dc/terms/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "DWC Data 2</title> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void doesNotAddDuplicateValuesToSamePredicate() throws Exception {
    testRdfDescriptionSaver.addValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.addValue(BASE_URI,
        "http://purl.org/dc/terms/title", "DWC Data",
        null, BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                ">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <title xmlns=\"http://purl.org/dc/terms/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "DWC Data</title> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void addsRelation() throws Exception {
    testRdfDescriptionSaver.addRelation(BASE_URI,
        "http://purl.org/dc/terms/rightsHolder", "http://example.com/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <rightsHolder xmlns=\"http://purl.org/dc/terms/\" " +
                "       rdf:resource=\"http://example.com/rightsHolder\" /> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();


    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void addsMultipleRelation() throws Exception {
    testRdfDescriptionSaver.addRelation(BASE_URI,
        "http://purl.org/dc/terms/rightsHolder", "http://example.com/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.addRelation(BASE_URI,
        "http://purl.org/dc/terms/description", "http://example.com/summaryProperties", BASE_URI);
    testRdfDescriptionSaver.addRelation(BASE_URI,
        "http://purl.org/dc/terms/provenance", "http://example.com/provenance", BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <description xmlns=\"http://purl.org/dc/terms/\" " +
                "       rdf:resource=\"http://example.com/summaryProperties\" /> \n" +
                "    <provenance xmlns=\"http://purl.org/dc/terms/\" rdf:resource=\"http://example.com/provenance\" /> \n" +
                "    <rightsHolder xmlns=\"http://purl.org/dc/terms/\" " +
                "       rdf:resource=\"http://example.com/rightsHolder\" /> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void doesNotAddDuplicateRelationToSamePredicate() throws Exception {
    testRdfDescriptionSaver.addRelation(BASE_URI,
        "http://purl.org/dc/terms/rightsHolder", "http://example.com/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.addRelation(BASE_URI,
        "http://purl.org/dc/terms/rightsHolder", "http://example.com/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <rightsHolder xmlns=\"http://purl.org/dc/terms/\" " +
                "       rdf:resource=\"http://example.com/rightsHolder\" /> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();


    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }


  @Test
  public void doesNotAddValueWhenSubjectUriDoesNotMatchBaseUri() throws Exception {
    testRdfDescriptionSaver.addValue("http://example.org/datasets/DUMMY/test",
        "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.commit();

    File file = new File(descriptionFileName);
    String descriptionFile = Files.toString(file, Charsets.UTF_8);

    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<rdf:RDF\n" +
        "\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
        "\n" +
        "</rdf:RDF>", descriptionFile);

  }

  @Test
  public void doesNotAddValueWhenSubjectNull() throws Exception {
    testRdfDescriptionSaver.addValue(null, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.commit();

    File file = new File(descriptionFileName);
    String descriptionFile = Files.toString(file, Charsets.UTF_8);

    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<rdf:RDF\n" +
        "\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
        "\n" +
        "</rdf:RDF>", descriptionFile);
  }

  @Test
  public void doesNotAddValueForInvalidPredicate() throws Exception {
    testRdfDescriptionSaver.addValue(BASE_URI, "http://invalid.org/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.commit();

    File file = new File(descriptionFileName);
    String descriptionFile = Files.toString(file, Charsets.UTF_8);

    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<rdf:RDF\n" +
        "\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
        "\n" +
        "</rdf:RDF>", descriptionFile);
  }

  @Test
  public void doesNotAddRelationWhenSubjectUriDoesNotMatchBaseUri() throws Exception {
    testRdfDescriptionSaver.addRelation("http://example.org/datasets/DUMMY/test",
        "http://purl.org/dc/terms/title", "http://purl.org/dc/terms/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.commit();

    File file = new File(descriptionFileName);
    String descriptionFile = Files.toString(file, Charsets.UTF_8);

    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<rdf:RDF\n" +
        "\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
        "\n" +
        "</rdf:RDF>", descriptionFile);
  }

  @Test
  public void doesNotAddRelationWhenSubjectNull() throws Exception {
    testRdfDescriptionSaver.addRelation(null,
        "http://purl.org/dc/terms/title", "http://purl.org/dc/terms/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.commit();

    File file = new File(descriptionFileName);
    String descriptionFile = Files.toString(file, Charsets.UTF_8);

    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<rdf:RDF\n" +
        "\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
        "\n" +
        "</rdf:RDF>", descriptionFile);
  }

  @Test
  public void deletesRelation() throws Exception {
    testRdfDescriptionSaver.addRelation(BASE_URI,
        "http://purl.org/dc/terms/rightsHolder", "http://example.com/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.commit();
    testRdfDescriptionSaver.delRelation(BASE_URI,
        "http://purl.org/dc/terms/rightsHolder", "http://example.com/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        ("<rdf:RDF xmlns=\"http://purl.org/dc/terms/\"\n xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
            "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();

    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );

  }

  @Test
  public void doesNotDeleteRelationWhenSubjectUriDoesNotMatchBaseUri() throws Exception {
    testRdfDescriptionSaver.addRelation(BASE_URI,
        "http://purl.org/dc/terms/rightsHolder", "http://example.com/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.delRelation("http://example.org/datasets/DUMMY/test",
        "http://purl.org/dc/terms/title", "http://purl.org/dc/terms/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <rightsHolder xmlns=\"http://purl.org/dc/terms/\" " +
                "       rdf:resource=\"http://example.com/rightsHolder\" /> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );

  }

  @Test
  public void doesNotDeleteRelationWhenSubjectNull() throws Exception {
    testRdfDescriptionSaver.addRelation(BASE_URI,
        "http://purl.org/dc/terms/rightsHolder", "http://example.com/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.delRelation(null,
        "http://purl.org/dc/terms/title", "http://purl.org/dc/terms/rightsHolder", BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <rightsHolder xmlns=\"http://purl.org/dc/terms/\" " +
                "       rdf:resource=\"http://example.com/rightsHolder\" /> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );

  }

  @Test
  public void deletesValue() throws Exception {
    testRdfDescriptionSaver.addValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.commit();
    testRdfDescriptionSaver.delValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        ("<rdf:RDF xmlns=\"http://purl.org/dc/terms/\"\n xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
            "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();

    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );

  }

  @Test
  public void doesNotDeleteValueWhenSubjectNull() throws Exception {
    testRdfDescriptionSaver.addValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.delValue(null, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                ">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <title xmlns=\"http://purl.org/dc/terms/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "DWC Data</title> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void doesNotDeleteValueWhenWhenSubjectUriDoesNotMatchBaseUri() throws Exception {
    testRdfDescriptionSaver.addValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.delValue("http://example.org/datasets/DUMMY/clusius10",
        "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                ">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <title xmlns=\"http://purl.org/dc/terms/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "DWC Data</title> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

  @Test
  public void replacesValue() throws Exception {
    testRdfDescriptionSaver.addValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.delValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data", null, BASE_URI);
    testRdfDescriptionSaver.addValue(BASE_URI, "http://purl.org/dc/terms/title", "DWC Data New", null, BASE_URI);
    testRdfDescriptionSaver.commit();

    Source expected = Input.fromByteArray(
        (
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                ">\n" +
                "  <rdf:Description rdf:about=\"http://example.org/datasets/DUMMY/clusius2\">\n" +
                "    <title xmlns=\"http://purl.org/dc/terms/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "DWC Data New</title> \n" +
                "  </rdf:Description>\n" +
                "</rdf:RDF>\n"
        ).getBytes(StandardCharsets.UTF_8)
    ).build();

    Source actual = Input.fromFile(descriptionFile).build();
    assertThat(actual,
        isSimilarTo(expected).ignoreWhitespace().withComparisonFormatter(new DefaultComparisonFormatter())
    );
  }

}
