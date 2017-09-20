package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.MyTestRdfPatchSerializer;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class GenerateRdfPatchFromJsonLdEntityTest {
  @Test
  public void testGenerateAdditions() throws Exception {
    Entity testEntity =
      createAdditionEntity("test", "http://example/entity", "pred1", new String[]{"value1", "value2"});

    Entity testEntity2 =
      createAdditionEntity("test", "http://example/entity", "pred2", new String[]{"value3", "value4"});

    Entity[] testEntities = new Entity[2];

    testEntities[0] = testEntity;
    testEntities[1] = testEntity2;

    GenerateRdfPatchFromJsonLdEntity generateRdfPatchFromJsonLdEntity =
      new GenerateRdfPatchFromJsonLdEntity(testEntities);


    MyTestRdfPatchSerializer basicRdfPatchSerializer = new MyTestRdfPatchSerializer();
    generateRdfPatchFromJsonLdEntity.generateAdditions(basicRdfPatchSerializer);

    assertThat(basicRdfPatchSerializer.getResults(), is("+<http://example/entity> " +
      "<http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred1> " +
      "\"\"value1\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n" +
      "+<http://example/entity> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred1> " +
      "\"\"value2\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n" +
      "+<http://example/entity> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred2> " +
      "\"\"value3\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n" +
      "+<http://example/entity> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred2> " +
      "\"\"value4\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n"));
  }

  private ImmutableEntity createAdditionEntity(String entityType, String uri, String predicate, String[] values) {
    return ImmutableEntity.builder()
      .entityType(entityType)
      .specializationOf(URI.create(uri))
      .putAdditions(predicate, values).build();
  }

  private ImmutableEntity createDeletionEntity(String entityType, String uri, String predicate, String[] values) {
    return ImmutableEntity.builder()
      .entityType(entityType)
      .specializationOf(URI.create(uri))
      .putDeletions(predicate, values).build();
  }

  private ImmutableEntity createReplacementEntity(String entityType, String uri, String predicate, String[] values) {
    return ImmutableEntity.builder()
      .entityType(entityType)
      .specializationOf(URI.create(uri))
      .putReplacements(predicate, values).build();
  }


  @Test
  public void testGenerateDeletions() throws Exception {
    Entity testEntity =
      createDeletionEntity("test", "http://example/entity", "pred1", new String[]{"value1", "value2"});

    Entity testEntity2 =
      createDeletionEntity("test", "http://example/entity", "pred2", new String[]{"value3", "value4"});


    Entity[] testEntities = new Entity[2];

    testEntities[0] = testEntity;
    testEntities[1] = testEntity2;

    GenerateRdfPatchFromJsonLdEntity generateRdfPatchFromJsonLdEntity =
      new GenerateRdfPatchFromJsonLdEntity(testEntities);


    MyTestRdfPatchSerializer basicRdfPatchSerializer = new MyTestRdfPatchSerializer();
    generateRdfPatchFromJsonLdEntity.generateDeletions(basicRdfPatchSerializer);

    assertThat(basicRdfPatchSerializer.getResults(), is("-<http://example/entity> " +
      "<http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred1> " +
      "\"\"value1\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n" +
      "-<http://example/entity> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred1> " +
      "\"\"value2\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n" +
      "-<http://example/entity> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred2> " +
      "\"\"value3\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n" +
      "-<http://example/entity> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred2> " +
      "\"\"value4\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n"));
  }

  @Test
  public void testGenerateReplacements() throws Exception {
    Entity testEntity =
      createReplacementEntity("test", "http://example/entity", "pred", new String[]{"value1", "value2"});

    Entity testEntity2 =
      createReplacementEntity("test", "http://example/entity", "pred2", new String[]{"value3", "value4"});

    Entity[] testEntities = new Entity[2];

    testEntities[0] = testEntity;
    testEntities[1] = testEntity2;


    GenerateRdfPatchFromJsonLdEntity generateRdfPatchFromJsonLdEntity =
      new GenerateRdfPatchFromJsonLdEntity(testEntities,
        (subject, predicate, direction, cursor) -> {
          if (subject.equals("http://example/entity") &&
            (predicate.equals("pred2") || predicate.equals("pred"))) {
            return Stream.of(
              CursorQuad.create(subject, predicate, Direction.OUT, "oldvalue1", STRING, null, ""),
              CursorQuad.create(subject, predicate, Direction.OUT, "oldvalue2", STRING, null, "")
            );
          } else {
            return Stream.empty();
          }
        });


    MyTestRdfPatchSerializer basicRdfPatchSerializer = new MyTestRdfPatchSerializer();
    generateRdfPatchFromJsonLdEntity.generateReplacements(basicRdfPatchSerializer);

    assertThat(basicRdfPatchSerializer.getResults(), is("-<http://example/entity> " +
      "<pred> " +
      "<oldvalue1> .\\n" +
      "-<http://example/entity> <pred> " +
      "<oldvalue2> .\\n" +
      "-<http://example/entity> " +
      "<pred2> " +
      "<oldvalue1> .\\n" +
      "-<http://example/entity> <pred2> " +
      "<oldvalue2> .\\n" +
      "+<http://example/entity> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred> " +
      "\"\"value1\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n" +
      "+<http://example/entity> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred> " +
      "\"\"value2\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n" +
      "+<http://example/entity> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred2> " +
      "\"\"value3\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n" +
      "+<http://example/entity> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#propertyId/pred2> " +
      "\"\"value4\"\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n"));
  }

  @Test
  public void testGenerateRevisionInfo() throws Exception {
    HashMap<String, URI> revisionOf = new HashMap<>();

    revisionOf.put("@id", URI.create("htttp://previous/mutation"));

    Entity testEntity = ImmutableEntity.builder()
      .entityType("test")
      .specializationOf(URI.create("http://example/entity"))
      .wasRevisionOf(revisionOf)
      .putAdditions("pred1", new String[]{"value1", "value2"}).build();


    Entity[] testEntities = new Entity[1];

    testEntities[0] = testEntity;

    GenerateRdfPatchFromJsonLdEntity generateRdfPatchFromJsonLdEntity =
      new GenerateRdfPatchFromJsonLdEntity(testEntities);


    MyTestRdfPatchSerializer basicRdfPatchSerializer = new MyTestRdfPatchSerializer();
    generateRdfPatchFromJsonLdEntity.generateRevisionInfo(basicRdfPatchSerializer);

    assertThat(basicRdfPatchSerializer.getResults(), is("+<http://example/entity> " +
      "<http://timbuctoo.huygens.knaw.nl/v5/vocabulary#specialization> \"<http://example/entity>" +
      "\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n" +
      "+<http://example/entity> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#latestrevision> " +
      "\"<htttp://previous/mutation>\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n" +
      "-<htttp://previous/mutation> <http://timbuctoo.huygens.knaw.nl/v5/vocabulary#latestrevision> " +
      "\"htttp://previous/mutation\"^^<http://www.w3.org/2001/XMLSchema#string> .\\n"));
  }


}
