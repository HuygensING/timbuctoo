package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class GenerateRdfPatchFromJsonLdEntityTest {
  @Test
  public void testGenerateAdditions() throws Exception {
    Entity testEntity = ImmutableEntity.builder()
                                       .entityType("test")
                                       .specializationOf(URI.create("http://example/entity"))
                                       .putAdditions("pred2", new String[]{"value1", "value2"}).build();

    GenerateRdfPatchFromJsonLdEntity generateRdfPatchFromJsonLdEntity =
      new GenerateRdfPatchFromJsonLdEntity(testEntity, "http://example.org/datasetuserid/");


    MyTestRdfPatchSerializer myTestRdfPatchSerializer = new MyTestRdfPatchSerializer();
    generateRdfPatchFromJsonLdEntity.sendQuads(myTestRdfPatchSerializer);

    assertThat(myTestRdfPatchSerializer.results, is("+ http://example.org/datasetuserid/ pred2 value1\n+ " +
      "http://example.org/datasetuserid/ pred2 value2\n"));
  }

  private class MyTestRdfPatchSerializer implements RdfPatchSerializer {
    String results = "";

    @Override
    public void delRelation(String subject, String predicate, String object, String graph)
      throws LogStorageFailedException {
      results += "-" + subject + predicate + object + graph + "\n";
    }

    @Override
    public void delValue(String subject, String predicate, String value, String valueType, String graph)
      throws LogStorageFailedException {
      results += "-" + subject + predicate + value + graph + "\n";
    }

    @Override
    public void delLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
      throws LogStorageFailedException {

    }

    @Override
    public MediaType getMediaType() {
      return null;
    }

    @Override
    public Charset getCharset() {
      return null;
    }

    @Override
    public void onPrefix(String prefix, String iri) throws LogStorageFailedException {

    }

    @Override
    public void onRelation(String subject, String predicate, String object, String graph)
      throws LogStorageFailedException {
      results += "+ " + subject + " " + predicate + " " + object + "\n";
    }

    @Override
    public void onValue(String subject, String predicate, String value, String valueType, String graph)
      throws LogStorageFailedException {
      results += "+ " + subject + " " + predicate + " " + value + "\n";
    }

    @Override
    public void onLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
      throws LogStorageFailedException {

    }

    @Override
    public void close() throws LogStorageFailedException {

    }
  }
}
