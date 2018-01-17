package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jsonldjava.core.DocumentLoader;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.BasicRdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.TestBasicRdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.v5.jsonldimport.JsonProvenanceToRdfPatch.PROV_SPECIALIZATION_OF;
import static nl.knaw.huygens.timbuctoo.v5.jsonldimport.JsonProvenanceToRdfPatch.fromCurrentState;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_LATEST_REVISION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;


public class JsonProvenanceToRdfPatchTest {

  private static final String EDITOR_URI = "http://example.org/user";
  private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"));
  private static final String TIMESTAMP = CLOCK.instant().toString();
  private static final String context = "  \"@context\": {\n" +
    "    \"@vocab\": \"http://example.org/UNKNOWN#\",\n" +
    "    \"prov\": \"http://www.w3.org/ns/prov#\",\n" +
    "    \"tim\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#\",\n" +
    "    \"ex\": \"http://example.org/\",\n" +
    "\t\t\"predicate\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#predicate\",\n" +
    "\t\t\"value\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#value\",\n" +
    "    \"additions\": {\n" +
    "      \"@id\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#additions\"\n" +
    "    },\n" +
    "    \"deletions\": {\n" +
    "      \"@id\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#deletions\"\n" +
    "    },\n" +
    "    \"replacements\": {\n" +
    "      \"@id\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#replacements\"\n" +
    "    },\n" +
    "    \"latestRevision\": {\n" +
    "      \"@id\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#latestRevision\",\n" +
    "      \"@type\": \"@id\"\n" +
    "    },\n" +
    "    \"used\": {\n" +
    "      \"@id\": \"http://www.w3.org/ns/prov#used\"\n" +
    "    },\n" +
    "    \"generates\": {\n" +
    "      \"@id\": \"http://www.w3.org/ns/prov#generates\"\n" +
    "    },\n" +
    "    \"qualifiedAssociation\": {\n" +
    "      \"@id\": \"http://www.w3.org/ns/prov#qualifiedAssociation\"\n" +
    "    },\n" +
    "    \"agent\": {\n" +
    "      \"@id\": \"http://www.w3.org/ns/prov#agent\",\n" +
    "      \"@type\": \"@id\"\n" +
    "    },\n" +
    "    \"hadRole\": {\n" +
    "      \"@id\": \"http://www.w3.org/ns/prov#hadRole\",\n" +
    "      \"@type\": \"@id\"\n" +
    "    },\n" +
    "    \"specializationOf\": {\n" +
    "      \"@id\": \"http://www.w3.org/ns/prov#specializationOf\",\n" +
    "      \"@type\": \"@id\"\n" +
    "    },\n" +
    "    \"entityType\": {\n" +
    "      \"@id\": \"http://www.w3.org/ns/prov#entityType\",\n" +
    "      \"@type\": \"@id\"\n" +
    "    },\n" +
    "    \"entity\": {\n" +
    "      \"@id\": \"http://www.w3.org/ns/prov#entity\",\n" +
    "      \"@type\": \"@id\"\n" +
    "    },\n" +
    "    \"wasRevisionOf\": {\n" +
    "      \"@id\": \"http://www.w3.org/ns/prov#wasRevisionOf\",\n" +
    "      \"@type\": \"@id\"\n" +
    "    }\n" +
    "  }\n";
  String defaultGraph = "http://example.org/graph";
  ObjectMapper objectMapper = new ObjectMapper();
  Map<String, String> frame = new HashMap<>();
  private List<String> result = new ArrayList<>();
  BasicRdfPatchSerializer basicRdfPatchSerializer = new TestBasicRdfPatchSerializer(this::stringWriter,
    defaultGraph);

  public ObjectNode wrap(String uri, String revisionId, ArrayNode additions, ArrayNode deletions,
                         ArrayNode replacements) {
    return jsnO(
      "@id", jsn(revisionId),
      PROV_SPECIALIZATION_OF, jsnA(jsnO("@id", jsn(uri))),
      JsonProvenanceToRdfPatch.TIM_ADDITIONS, additions,
      JsonProvenanceToRdfPatch.TIM_DELETIONS, deletions,
      JsonProvenanceToRdfPatch.TIM_REPLACEMENTS, replacements
    );
  }

  public void stringWriter(String string) {
    result.add(string);
  }

  @Test
  public void testSingleAddition() throws Exception {
    String testAdditionWithType = "{\n" +
      "   \"@type\":\"prov:Activity\",\n" +
      "   \"http://www.w3.org/ns/prov#generates\":[\n" +
      "      {\n" +
      "         \"@type\":\"prov:Entity\",\n" +
      "         \"specializationOf\":{\n" +
      "            \"@id\":\"http://example.com/the/actual/entitys\"\n" +
      "         },\n" +
      "         \"additions\":[\n" +
      "            {\n" +
      "               \"@type\":\"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\",\n" +
      "               \"predicate\":\"http://example.org/pred2\",\n" +
      "               \"value\":\"extra value\"\n" +
      "            },\n" +
      "            {\n" +
      "               \"@type\":\"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\",\n" +
      "               \"predicate\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\",\n" +
      "               \"value\":{\n" +
      "                  \"@id\":\"http://timbuctoo.huygens.knaw.nl/static/v5/Persons\"\n" +
      "               }\n" +
      "            }\n" +
      "         ]\n" +
      "      }\n" +
      "   ],\n" + context +
      "}";


    JsonProvenanceToRdfPatch creator = fromCurrentState(
      new DocumentLoader(), testAdditionWithType,
      new DummyQuadStore(), EDITOR_URI, "test", CLOCK);

    creator.sendQuads(basicRdfPatchSerializer);

    List<String> filteredResult = Lists.newArrayList(Collections2.filter(
      result, Predicates.containsPattern("http://example.org/pred2")));

    assertThat(filteredResult, containsInAnyOrder(
      "+<http://example.com/the/actual/entitys> <http://example.org/pred2> \"extra value\"" +
        "^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n",
      "+<http://timbuctoo.huygens.knaw.nl/static/v5/skolemized/test/b2> " +
        "<http://timbuctoo.huygens.knaw.nl/v5/vocabulary#predicate> " +
        "\"http://example.org/pred2\"^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n")
    );
  }

  @Test
  public void testMultiAddition() throws Exception {
    String testMultiAddition = "{\n" +
      "   \"@type\":\"prov:Activity\",\n" +
      "   \"http://www.w3.org/ns/prov#generates\":[\n" +
      "      {\n" +
      "         \"@type\":\"prov:Entity\",\n" +
      "         \"specializationOf\":{\n" +
      "            \"@id\":\"http://example.com/the/actual/entitys\"\n" +
      "         },\n" +
      "         \"additions\":[\n" +
      "            {\n" +
      "               \"@type\":\"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\",\n" +
      "               \"predicate\":\"http://example.org/pred1\",\n" +
      "               \"value\":\"value1\"\n" +
      "            },\n" +
      "            {\n" +
      "               \"@type\":\"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\",\n" +
      "               \"predicate\":\"http://example.org/pred2\",\n" +
      "               \"value\":\"value2\"\n" +
      "            }\n" +
      "         ]\n" +
      "      }\n" +
      "   ],\n" + context +
      "}";


    JsonProvenanceToRdfPatch creator = fromCurrentState(
      new DocumentLoader(), testMultiAddition,
      new DummyQuadStore(), EDITOR_URI, "test", CLOCK);

    creator.sendQuads(basicRdfPatchSerializer);

    List<String> filteredResult = Lists.newArrayList(Collections2.filter(
      result, Predicates.containsPattern("http://example.org/pred")));

    assertThat(filteredResult, hasItems(
      "+<http://example.com/the/actual/entitys> <http://example.org/pred1> \"value1\"" +
        "^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n",
      "+<http://example.com/the/actual/entitys> <http://example.org/pred2> \"value2\"" +
        "^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n")
    );
  }

  @Test
  public void testAdditionsWithMultiValues() throws Exception {
    String testMultiValueAdditions = "{\n" +
      "  \"@type\": \"prov:Activity\",\n" +
      "  \"generates\": [{\n" +
      "\t  \"@type\": \"prov:Entity\",\n" +
      "    \"specializationOf\": {\"@id\": \"http://example.com/the/actual/entitys\"},\n" +
      "    \"additions\": [\n" +
      "      {\"@type\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\", " +
      "\"predicate\": \"http://example.org/pred1\", \"value\": \"value1\"},\n" +
      "      {\"@type\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\"," +
      " \"predicate\": \"http://example.org/pred1\", \"value\": \"value2\"},\n" +
      "      {\"@type\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\", " +
      "\"predicate\": \"http://example.org/pred2\", \"value\": \"value3\"},\n" +
      "      {\"@type\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\", " +
      "\"predicate\": \"http://example.org/pred2\", \"value\": \"value4\"}\n" +
      "    ]\n" +
      "  }],\n" + context + "}";

    JsonProvenanceToRdfPatch creator = fromCurrentState(new DocumentLoader(),
      testMultiValueAdditions, new DummyQuadStore(), EDITOR_URI, "test", CLOCK);

    creator.sendQuads(basicRdfPatchSerializer);

    List<String> filteredResult = Lists.newArrayList(Collections2.filter(
      result, Predicates.containsPattern("http://example.org/pred")));


    assertThat(filteredResult, hasItems(
      "+<http://example.com/the/actual/entitys> <http://example.org/pred1> \"value1\"" +
        "^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n",
      "+<http://example.com/the/actual/entitys> <http://example.org/pred1> \"value2\"" +
        "^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n",
      "+<http://example.com/the/actual/entitys> <http://example.org/pred2> \"value3\"" +
        "^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n",
      "+<http://example.com/the/actual/entitys> <http://example.org/pred2> \"value4\"" +
        "^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n")
    );

  }

  @Test
  public void testDeletions() throws Exception {
    String testDeletions = "{\n" +
      "   \"@type\":\"prov:Activity\",\n" +
      "   \"http://www.w3.org/ns/prov#generates\":[\n" +
      "      {\n" +
      "         \"@type\":\"prov:Entity\",\n" +
      "         \"specializationOf\":{\n" +
      "            \"@id\":\"http://example.com/the/actual/entitys\"\n" +
      "         },\n" +
      "         \"deletions\":[\n" +
      "            {\n" +
      "               \"@type\":\"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\",\n" +
      "               \"predicate\":\"http://example.org/pred1\",\n" +
      "               \"value\":\"value1\"\n" +
      "            },\n" +
      "            {\n" +
      "               \"@type\":\"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\",\n" +
      "               \"predicate\":\"http://example.org/pred2\",\n" +
      "               \"value\":\"value2\"\n" +
      "            }\n" +
      "         ]\n" +
      "      }\n" +
      "   ],\n" + context +
      "}";


    JsonProvenanceToRdfPatch creator = fromCurrentState(new DocumentLoader(), testDeletions,
      new DummyQuadStore(), EDITOR_URI, "test", CLOCK);

    creator.sendQuads(basicRdfPatchSerializer);

    List<String> filteredResult = Lists.newArrayList(Collections2.filter(
      result, Predicates.containsPattern("http://example.org/pred")));

    assertThat(filteredResult, hasItems(
      "-<http://example.com/the/actual/entitys> <http://example.org/pred1> \"value1\"" +
        "^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n",
      "-<http://example.com/the/actual/entitys> <http://example.org/pred2> \"value2\"" +
        "^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n")
    );
  }

  @Test
  public void testReplacement() throws Exception {
    String testReplacement = "{\n" +
      "   \"@type\":\"prov:Activity\",\n" +
      "   \"http://www.w3.org/ns/prov#generates\":[\n" +
      "      {\n" +
      "         \"@type\":\"prov:Entity\",\n" +
      "         \"specializationOf\":{\n" +
      "            \"@id\":\"http://example.com/the/actual/entitys\"\n" +
      "         },\n" +
      "         \"replacements\":[\n" +
      "            {\n" +
      "               \"@type\":\"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\",\n" +
      "               \"predicate\":\"http://example.org/pred1\",\n" +
      "               \"value\":\"value1\"\n" +
      "            }\n" +
      "         ]\n" +
      "      }\n" +
      "   ],\n" + context +
      "}";

    QuadStore testQuadStore = new DummyQuadStore().with("http://example.com/the/actual/entitys",
      "http://example.org/pred1", "old value", "http://www.w3.org/2001/XMLSchema#string");

    JsonProvenanceToRdfPatch creator = fromCurrentState(new DocumentLoader(), testReplacement,
      testQuadStore, EDITOR_URI, "test", CLOCK);

    creator.sendQuads(basicRdfPatchSerializer);

    List<String> filteredResult = Lists.newArrayList(Collections2.filter(
      result, Predicates.containsPattern("http://example.org/pred")));

    filteredResult = Lists.newArrayList(Collections2.filter(filteredResult,
      Predicates.not(Predicates.containsPattern("skolemized"))));

    assertThat(filteredResult, containsInAnyOrder(
      "-<http://example.com/the/actual/entitys> <http://example.org/pred1> \"old value\"" +
        "^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n",
      "+<http://example.com/the/actual/entitys> <http://example.org/pred1> \"value1\"" +
        "^^<http://www.w3.org/2001/XMLSchema#string> <" + defaultGraph + "> .\n")
    );
  }

  @Test
  public void retractsPreviousRevision() throws Exception {
    String testRetractsRevision = "{\n" +
      "   \"@type\":\"prov:Activity\",\n" +
      "   \"http://www.w3.org/ns/prov#generates\":[\n" +
      "      {\n" +
      "         \"@type\":\"prov:Entity\",\n" +
      "         \"@id\":\"_:b2\",\n" +
      "         \"specializationOf\":{\n" +
      "            \"@id\":\"http://example.com/the/actual/entitys\"\n" +
      "         },\n" +
      "         \"wasRevisionOf\":{\n" +
      "            \"@id\":\"http://example.org/revision1\"\n" +
      "         },\n" +
      "         \"replacements\":[\n" +
      "            {\n" +
      "               \"@type\":\"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\",\n" +
      "               \"predicate\":\"http://example.org/pred1\",\n" +
      "               \"value\":\"value1\"\n" +
      "            }\n" +
      "         ]\n" +
      "      }\n" +
      "   ],\n" + context +
      "}";

    QuadStore testQuadStore = new DummyQuadStore().with("http://example.org/revision1",
      PROV_SPECIALIZATION_OF, "http://example.com/the/actual/entitys", null)
      .with("http://example.com/the/actual/entitys",
        TIM_LATEST_REVISION, "http://example.org/revision1");


    JsonProvenanceToRdfPatch creator = fromCurrentState(new DocumentLoader(), testRetractsRevision,
      testQuadStore, EDITOR_URI, "test", CLOCK);

    creator.sendQuads(basicRdfPatchSerializer);

    List<String> filteredResult = Lists.newArrayList(Collections2.filter(
      result, Predicates.containsPattern("latestRevision")));

    assertThat(filteredResult, containsInAnyOrder(
      "-<http://example.com/the/actual/entitys> <" + TIM_LATEST_REVISION + "> <http://example.org/revision1" +
        "> <" + defaultGraph + "> .\n",
      "+<http://example.com/the/actual/entitys> <" + TIM_LATEST_REVISION +
        "> <http://timbuctoo.huygens.knaw.nl/static/v5/skolemized/test/b1> <" + defaultGraph + "> .\n"
      )
    );
  }

  @Test
  public void doesOptimisticLocking() throws Exception {
    String examplePatch = "{\n" +
      "   \"@type\":\"prov:Activity\",\n" +
      "   \"http://www.w3.org/ns/prov#generates\":[\n" +
      "      {\n" +
      "         \"@type\":\"prov:Entity\",\n" +
      "         \"specializationOf\":{\n" +
      "            \"@id\":\"http://example.com/the/actual/entity1\"\n" +
      "         },\n" +
      "        \"wasRevisionOf\":{\n" +
      "            \"@id\":\"http://example.org/revision1\"\n" +
      "         },\n" +
      "         \"additions\":[\n" +
      "            {\n" +
      "               \"@type\":\"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\",\n" +
      "               \"predicate\":\"http://example.org/pred1\",\n" +
      "               \"value\":\"value1\"\n" +
      "            }\n" +
      "         ]\n" +
      "      }\n" +
      "   ],\n" + context +
      "}";

    QuadStore testQuadStore = new DummyQuadStore()
      .with("http://example.org/entity1",
        RdfConstants.TIM_LATEST_REVISION, "http://example.org/revision2", null);

    boolean exceptionWasThrown = false;
    try {
      fromCurrentState(new DocumentLoader(), examplePatch, testQuadStore,
        "http://example.org/users/myUser", UUID.randomUUID().toString(), CLOCK);
    } catch (ConcurrentUpdateException e) {
      exceptionWasThrown = true;
    }
    assertThat(exceptionWasThrown, is(true));
  }


  private class DummyQuadStore implements QuadStore {
    Map<String, Map<String, ArrayList<CursorQuad>>> quads = new HashMap<>();

    public DummyQuadStore with(String subject, String predicate, String value, String valueType) {
      quads.computeIfAbsent(subject, k -> new HashMap<>())
        .computeIfAbsent(predicate + "\nOUT", k -> new ArrayList<>())
        .add(CursorQuad.create(subject, predicate, Direction.OUT, value, valueType, null, ""));
      return this;
    }

    public DummyQuadStore with(String subject, String predicate, String object) {
      final Map<String, ArrayList<CursorQuad>> subjectQuads = quads.computeIfAbsent(subject, k -> new HashMap<>());
      subjectQuads
        .computeIfAbsent(predicate + "\nOUT", k -> new ArrayList<>())
        .add(CursorQuad.create(subject, predicate, Direction.OUT, object, null, null, ""));
      subjectQuads
        .computeIfAbsent(predicate + "\nIN", k -> new ArrayList<>())
        .add(CursorQuad.create(object, predicate, Direction.IN, subject, null, null, ""));
      return this;
    }

    @Override
    public Stream<CursorQuad> getQuads(String subject, String predicate, Direction direction, String cursor) {
      return Stream.of(quads
        .getOrDefault(subject, new HashMap<>())
        .getOrDefault(predicate + "\n" + direction.name(), new ArrayList<>())
        .toArray(new CursorQuad[0])
      );
    }

    @Override
    public Stream<CursorQuad> getQuads(String subject) {
      return quads
        .getOrDefault(subject, new HashMap<>())
        .values().stream().flatMap(Collection::stream);
    }

    @Override
    public void close() {

    }

    @Override
    public void commit() {
      
    }
  }

}
