package nl.knaw.huygens.timbuctoo.implementations;

import nl.knaw.huygens.timbuctoo.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.rdfio.implementations.BasicRdfPatchSerializer;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BasicRdfPatchSerializerTest {
  private String result;
  final BasicRdfPatchSerializer basicRdfPatchSerializer = new TestBasicRdfPatchSerializer(this::stringWriter);

  public void stringWriter(String string) {
    result = string;
  }

  @Test
  public void testAddValue() throws LogStorageFailedException {
    basicRdfPatchSerializer.onValue("http://example.org/show/218", "http://www.w3.org/2000/01/rdf-schema#label",
      "That Seventies Show",
      "http://www.w3.org/2001/XMLSchema#string", null);

    assertThat(result, is("+<http://example.org/show/218> <http://www.w3.org/2000/01/rdf-schema#label>" +
      " \"That Seventies Show\"^^<http://www.w3.org/2001/XMLSchema#string> .\n"));

    basicRdfPatchSerializer.onValue("http://en.wikipedia.org/wiki/Helium","http://example.org/elements/specificGravity",
      "1.663E-4","http://www.w3.org/2001/XMLSchema#double","http://example.org/testgraph");

    assertThat(result, is("+<http://en.wikipedia.org/wiki/Helium> <http://example.org/elements/specificGravity>" +
      " \"1.663E-4\"^^<http://www.w3.org/2001/XMLSchema#double> <http://example.org/testgraph> .\n"));

    basicRdfPatchSerializer.onValue("http://example.org/#spiderman","http://example.org/text",
      "This is a multi-line\nliteral with many quotes (\"\"\"\"\")\nand two apostrophes ('').", "http://www.w3.org/2001/XMLSchema#string",null);

    assertThat(result, is("+<http://example.org/#spiderman> <http://example.org/text> \"This is " +
      "a multi-line\\nliteral with many quotes (\\\"\\\"\\\"\\\"\\\")\\nand two apostrophes ('').\"^^<http://www.w3.org/2001/XMLSchema#string> .\n"));
  }

  @Test
  public void testAddLanguageTaggedValue() throws LogStorageFailedException {
    basicRdfPatchSerializer.onLanguageTaggedString("http://example.org/show/218",
      "http://example.org/show/localName", "That Seventies Show", "en", null);

    assertThat(result, is("+<http://example.org/show/218> <http://example.org/show/localName> " +
      "\"That Seventies Show\"@en .\n"));
  }

  @Test
  public void testDelValue() throws LogStorageFailedException {
    basicRdfPatchSerializer.delValue("http://example.org/show/218", "http://www.w3.org/2000/01/rdf-schema#label",
      "That Seventies Show",
      "http://www.w3.org/2001/XMLSchema#string", null);

    assertThat(result, is("-<http://example.org/show/218> <http://www.w3.org/2000/01/rdf-schema#label>" +
      " \"That Seventies Show\"^^<http://www.w3.org/2001/XMLSchema#string> .\n"));

    basicRdfPatchSerializer.delValue("http://en.wikipedia.org/wiki/Helium","http://example.org/elements/specificGravity",
      "1.663E-4","http://www.w3.org/2001/XMLSchema#double",null);

    assertThat(result, is("-<http://en.wikipedia.org/wiki/Helium> <http://example.org/elements/specificGravity>" +
      " \"1.663E-4\"^^<http://www.w3.org/2001/XMLSchema#double> .\n"));

    basicRdfPatchSerializer.delValue("http://example.org/#spiderman","http://example.org/text",
      "This is a multi-line\nliteral with many quotes (\"\"\"\"\")\nand two apostrophes ('').", "http://www.w3.org/2001/XMLSchema#string",null);

    assertThat(result, is("-<http://example.org/#spiderman> <http://example.org/text> \"This is " +
      "a multi-line\\nliteral with many quotes (\\\"\\\"\\\"\\\"\\\")\\nand two apostrophes ('').\"^^<http://www.w3.org/2001/XMLSchema#string> .\n"));
  }

  @Test
  public void delLanguageTaggedValue() throws LogStorageFailedException {
    basicRdfPatchSerializer.delLanguageTaggedString("http://example.org/show/218",
      "http://example.org/show/localName", "That Seventies Show", "en", null);

    assertThat(result, is("-<http://example.org/show/218> <http://example.org/show/localName> " +
      "\"That Seventies Show\"@en .\n"));
  }
}
