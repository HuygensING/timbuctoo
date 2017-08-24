package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class GenerateRdfPatchFromJsonLdEntityTest {
  @Test
  public void testGenerateAdditions() throws Exception {
    Entity testEntity = ImmutableEntity.builder()
                                       .entityType("test")
                                       .specializationOf(URI.create("http://example.org/datasetuserid/"))
                                       .putAdditions("pred2", new String[]{"value1", "value2"}).build();

    GenerateRdfPatchFromJsonLdEntity generateRdfPatchFromJsonLdEntity =
      new GenerateRdfPatchFromJsonLdEntity(testEntity);


    MyTestRdfPatchSerializer myTestRdfPatchSerializer = new MyTestRdfPatchSerializer();
    generateRdfPatchFromJsonLdEntity.generateAdditions(myTestRdfPatchSerializer);

    assertThat(myTestRdfPatchSerializer.results, is("+ http://example.org/datasetuserid/ pred2 value1\n+ " +
      "http://example.org/datasetuserid/ pred2 value2\n"));
  }


  @Test
  public void testGenerateDeletions() throws Exception {
    Entity testEntity = ImmutableEntity.builder()
                                       .entityType("test")
                                       .specializationOf(URI.create("http://example.org/datasetuserid/"))
                                       .putDeletions("pred2", new String[]{"value1", "value2"}).build();

    GenerateRdfPatchFromJsonLdEntity generateRdfPatchFromJsonLdEntity =
      new GenerateRdfPatchFromJsonLdEntity(testEntity);


    MyTestRdfPatchSerializer myTestRdfPatchSerializer = new MyTestRdfPatchSerializer();
    generateRdfPatchFromJsonLdEntity.generateDeletions(myTestRdfPatchSerializer);

    assertThat(myTestRdfPatchSerializer.results, is("- http://example.org/datasetuserid/ pred2 value1\n- " +
      "http://example.org/datasetuserid/ pred2 value2\n"));
  }

  @Test
  public void testGenerateReplacements() throws Exception {
    Entity testEntity = ImmutableEntity.builder()
                                       .entityType("test")
                                       .specializationOf(URI.create("http://example/datasetuserid"))
                                       .putReplacements("pred2", new String[]{"value1", "value2"}).build();

    GenerateRdfPatchFromJsonLdEntity generateRdfPatchFromJsonLdEntity =
      new GenerateRdfPatchFromJsonLdEntity(testEntity,
        (subject, predicate, direction, cursor) -> {
          if (subject.equals("http://example/datasetuserid") && predicate.equals("pred2")) {
            return Stream.of(
              CursorQuad.create(subject, predicate, Direction.OUT, "oldvalue1", STRING, null, ""),
              CursorQuad.create(subject, predicate, Direction.OUT, "oldvalue2", STRING, null, "")
            );
          } else {
            return Stream.empty();
          }
        });


    MyTestRdfPatchSerializer myTestRdfPatchSerializer = new MyTestRdfPatchSerializer();
    generateRdfPatchFromJsonLdEntity.generateReplacements(myTestRdfPatchSerializer);

    assertThat(myTestRdfPatchSerializer.results, is("- http://example/datasetuserid pred2 oldvalue1\n" +
      "- http://example/datasetuserid pred2 oldvalue2\n" +
      "+ http://example/datasetuserid pred2 value1\n" +
      "+ http://example/datasetuserid pred2 value2\n"));
  }

  private class MyTestRdfPatchSerializer implements RdfPatchSerializer {
    String results = "";

    @Override
    public void delRelation(String subject, String predicate, String object, String graph)
      throws LogStorageFailedException {
      results += "- " + subject + " " + predicate + " " + object + "\n";
    }

    @Override
    public void delValue(String subject, String predicate, String value, String valueType, String graph)
      throws LogStorageFailedException {
      results += "- " + subject + " " + predicate + " " + value + "\n";
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
