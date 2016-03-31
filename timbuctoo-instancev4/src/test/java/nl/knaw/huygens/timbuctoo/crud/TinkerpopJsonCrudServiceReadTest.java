package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.JsonBasedUserStore;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class TinkerpopJsonCrudServiceReadTest {
  public TinkerpopJsonCrudService basicInstance(Graph graph) {
    return customInstance(graph, null, null);
  }

  public TinkerpopJsonCrudService basicInstanceWithUserStore(Graph graph, JsonBasedUserStore userStore) {
    return customInstance(graph, userStore, null);
  }

  public TinkerpopJsonCrudService basicInstanceWithGenerator(Graph graph, UrlGenerator gen) {
    return customInstance(graph, null, gen);
  }

  public TinkerpopJsonCrudService customInstance(Graph graph, JsonBasedUserStore userStore, UrlGenerator gen) {


    if (gen == null) {
      gen = (collection, id, rev) -> URI.create("http://example.com/");
    }
    if (userStore == null) {
      userStore = mock(JsonBasedUserStore.class);
    }
    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    when(graphWrapper.getGraph()).thenReturn(graph);

    HandleAdder handleAdder = mock(HandleAdder.class);

    Clock clock = Clock.systemDefaultZone();

    Vres testVres = new Vres.Builder()
      .withVre("WomenWriters", "ww", vre -> vre
        .withCollection("wwdocuments")
        .withCollection("wwrelations")
        .withCollection("wwlanguages", c -> c
          .withDisplayName(localProperty("wwlanguage_name"))
        )
        .withCollection("wwderivedrelations", c -> c
          .withDerivedRelation("hasPersonLanguage", () -> {
            P<String> isWw = new P<>((types, extra) -> types.contains("\"wwrelation\""), "");
            return __
              .outE("isCreatorOf").has("isLatest", true).not(has("isDeleted", true)).has("types", isWw).inV()
              .outE("hasWorkLanguage").has("isLatest", true).not(has("isDeleted", true)).has("types", isWw).inV();
          })
        )
        .withCollection("wwdisplaynames", c -> c
          .withDisplayName(PropertyTypes.localProperty("wwperson_displayName"))
        )
        .withCollection("wwpersons", c -> c
          .withProperty("name", localProperty("wwperson_name"))
        )
      )
      .build();

    return new TinkerpopJsonCrudService(graphWrapper, testVres, handleAdder, userStore, gen, gen, clock);
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void throwsOnUnknownMappings() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(InvalidCollectionException.class);

    instance.get("anUnknownCollection", UUID.randomUUID());
  }

  /*
   * @type is allowed by the previous implementation, but is always identical to the collectionName
   * We've chosen to not throw a 400 when the collection and the type mismatch
   */
  @Test
  public void echoesAtTypeAndIdProperty() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    assertThat(
      instance.get("wwpersons", id).toString(),
      sameJSONAs(
        JsonBuilder.jsnO(
          "@type", jsn("wwperson"),
          "_id", jsn(id.toString())
        ).toString()
      ).allowingExtraUnexpectedFields()
    );
  }

  @Test
  public void returnsThePropertiesFromTheMapper() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("wwperson_name", "the name")
        .withProperty("wwperson_UnmappedProperty", "shouldn't be returned")
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);


    JsonNode entity = instance.get("wwpersons", id);
    Long normalFieldCount = stream(entity.fields())
      .filter(x -> x.getKey().matches("^[a-zA-Z].*"))
      .collect(Collectors.counting());

    assertThat(normalFieldCount, is(1L));
    assertThat(entity.get("name").asText(""), is("the name"));
  }

  @Test
  public void omitsPropertiesThatAreNotStoredCorrectlyInTheDatabase() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withType("person")
        .withVre("ww")
        .withProperty("wwperson_name", 2) //should be a string, not an int
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);


    JsonNode entity = instance.get("wwpersons", id);

    assertThat(entity.get("name"), is(nullValue()));
  }

  @Test
  public void getsTheLatestEntityWhenNoRevIsSpecified() throws Exception {

    UUID uuid = UUID.randomUUID();

    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(uuid.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("wwperson_name", "old")
        .withProperty("rev", 1)
        .withProperty("isLatest", false)
        .withOutgoingRelation("VERSION_OF", "replacement")
      )
      .withVertex("replacement", v -> v
        .withTimId(uuid.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("wwperson_name", "new")
        .withProperty("rev", 2)
        .withProperty("isLatest", false)
        .withOutgoingRelation("VERSION_OF", "dangling")
      )
      .withVertex("dangling", v -> v
        .withTimId(uuid.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("wwperson_name", "new")
        .withProperty("rev", 2)
        .withProperty("isLatest", true)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    JsonNode entity = instance.get("wwpersons", uuid);

    assertThat(entity.get("^rev").asInt(), is(2));
    assertThat(entity.get("name").asText(), is("new"));
  }

  @Test
  public void throwsNotFoundWhenTheIdIsNotInTheDatabase() throws Exception {
    UUID id = UUID.randomUUID();
    UUID otherId = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(NotFoundException.class);

    instance.get("wwpersons", otherId);
  }

  @Test
  public void getsTheRequestedRevWhenSpecified() throws Exception {

    UUID uuid = UUID.randomUUID();

    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(uuid.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("wwperson_name", "old")
        .withProperty("rev", 1)
        .withProperty("isLatest", false)
        .withOutgoingRelation("VERSION_OF", "replacement")
      )
      .withVertex("replacement", v -> v
        .withTimId(uuid.toString())
        .withProperty("wwperson_name", "new")
        .withProperty("rev", 2)
        .withProperty("isLatest", false)
        .withOutgoingRelation("VERSION_OF", "dangling")
      )
      .withVertex("dangling", v -> v
        .withTimId(uuid.toString())
        .withProperty("wwperson_name", "new")
        .withProperty("rev", 2)
        .withProperty("isLatest", true)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    JsonNode entity = instance.get("wwpersons", uuid, 1);

    assertThat(entity.get("^rev").asInt(), is(1));
    assertThat(entity.get("name").asText(), is("old"));
  }

  @Test
  public void throwsNotFoundWhenTheRevIsNotInTheDatabase() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(NotFoundException.class);

    instance.get("wwpersons", id, 2);
  }

  @Test
  public void showsTheModificationInfoIncludingUserDescription() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("modified", "{\"timeStamp\":1427921175250, \"userId\":\"USER1\"}")
      )
      .build();

    JsonBasedUserStore userStore = mock(JsonBasedUserStore.class);
    when(userStore.userForId("USER1")).thenReturn(Optional.of(new User("Username for USER1")));

    TinkerpopJsonCrudService instance = basicInstanceWithUserStore(graph, userStore);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "^modified", JsonBuilder.jsnO(
        "timeStamp", jsn(1427921175250L),
        "userId", jsn("USER1"),
        "username", jsn("Username for USER1")
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsTheCreationInfo() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withProperty("isLatest", true)
        .withType("person")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("created", "{\"timeStamp\":1427921175250, \"userId\":\"USER1\"}")
      )
      .build();

    JsonBasedUserStore userStore = mock(JsonBasedUserStore.class);
    when(userStore.userForId("USER1")).thenReturn(Optional.of(new User("Username for USER1")));

    TinkerpopJsonCrudService instance = basicInstanceWithUserStore(graph, userStore);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "^created", JsonBuilder.jsnO(
        "timeStamp", jsn(1427921175250L),
        "userId", jsn("USER1"),
        "username", jsn("Username for USER1")
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsTheRelations() throws Exception {
    UUID id = UUID.randomUUID();
    UUID pseudonymId = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withOutgoingRelation("isPseudonymOf", "pseudonym")
        .withVre("ww")
        .withVre("")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("pseudonym", v -> v
        .withVre("ww")
        .withVre("")
        .withType("person")
        .withTimId(pseudonymId.toString())
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@relationCount", jsn(1),
      "@relations", JsonBuilder.jsnO(
        "isPseudonymOf", JsonBuilder.jsnA(
          JsonBuilder.jsnO(
            "id", jsn(pseudonymId.toString())
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void usesInversePropertyNameForIncomingRelations() throws Exception {
    UUID id = UUID.randomUUID();
    UUID workId = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withIncomingRelation("isCreatedBy", "work")
        .withVre("ww")
        .withVre("")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("work", v -> v
        .withVre("ww")
        .withVre("")
        .withType("document")
        .withTimId(workId.toString())
      )
      .withVertex(v -> v
        .withProperty("relationtype_regularName", "isCreatedBy")
        .withProperty("relationtype_inverseName", "isCreatorOf")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@relationCount", jsn(1),
      "@relations", JsonBuilder.jsnO(
        "isCreatorOf", JsonBuilder.jsnA(
          JsonBuilder.jsnO(
            "id", jsn(workId.toString())
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsThePropertiesOfTheRelatedVertex() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withOutgoingRelation("isPseudonymOf", "work")
        .withVre("ww")
        .withVre("")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("work", v -> v
        .withVre("ww")
        .withVre("")
        .withType("person")
        .withTimId("f005ba11-0000-0000-0000-000000000000")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstanceWithGenerator(
      graph,
      (collection, uuid, rev) -> URI.create(String.format("/%s/%s/%s", collection, uuid, rev))
    );

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@relationCount", jsn(1),
      "@relations", JsonBuilder.jsnO(
        "isPseudonymOf", JsonBuilder.jsnA(
          JsonBuilder.jsnO(
            "id", jsn("f005ba11-0000-0000-0000-000000000000"),
            "path", jsn("/wwpersons/f005ba11-0000-0000-0000-000000000000/null")
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void omitsRelationsWithIsLatestIsFalse() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withOutgoingRelation("isPseudonymOf", "work", relation -> relation
          .withRev(1)
          .withAccepted("wwrelation", true)
          .withTim_id(UUID.fromString("deadbeaf-0000-0000-0000-000000000000"))
          .withIsLatest(false)
        )
        .withOutgoingRelation("isPseudonymOf", "work", relation -> relation
          .withRev(2)
          .withIsLatest(true)
        )
        .withVre("ww")
        .withVre("")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("work", v -> v
        .withVre("ww")
        .withVre("")
        .withType("person")
        .withTimId("f005ba11-0000-0000-0000-000000000000")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);
    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@relationCount", jsn(1),
      "@relations", JsonBuilder.jsnO(
        "isPseudonymOf", JsonBuilder.jsnA(
          JsonBuilder.jsnO(
            "rev", jsn(2)
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void omitsRelationsOutsideMyVre() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withOutgoingRelation("isPseudonymOf", "work", relation -> relation
          .withRev(1)
          .addType("ckcc")
          .removeType("ww")
        )
        .withOutgoingRelation("isPseudonymOf", "work", relation -> relation
          .withRev(2)
        )
        .withVre("ww")
        .withVre("")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("work", v -> v
        .withVre("ww")
        .withVre("")
        .withType("person")
        .withTimId("f005ba11-0000-0000-0000-000000000000")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);
    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@relationCount", jsn(1),
      "@relations", JsonBuilder.jsnO(
        "isPseudonymOf", JsonBuilder.jsnA(
          JsonBuilder.jsnO(
            "rev", jsn(2)
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void omitsDeletedRelations() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withOutgoingRelation("isPseudonymOf", "work", relation -> relation
          .withRev(1)
          .withDeleted(null) //when no deleted prop is present, it should assume that the prop is not deleted
        )
        .withOutgoingRelation("isPseudonymOf", "work", relation -> relation
          .withRev(2)
          .withDeleted(false)
        )
        .withOutgoingRelation("isPseudonymOf", "work", relation -> relation
          .withRev(2)
          .withDeleted(true)
        )
        .withVre("ww")
        .withVre("")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("work", v -> v
        .withVre("ww")
        .withVre("")
        .withType("person")
        .withTimId("f005ba11-0000-0000-0000-000000000000")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);
    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@relationCount", jsn(2),
      "@relations", JsonBuilder.jsnO(
        "isPseudonymOf", JsonBuilder.jsnA(
          JsonBuilder.jsnO(
            "rev", jsn(2)
          ),
          JsonBuilder.jsnO(
            "rev", jsn(1)
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsThePropertiesOfTheRelationItself() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withOutgoingRelation("isPseudonymOf", "work", relation -> relation
          .withRev(5)
          .withAccepted("wwrelation", true)
          .withTim_id(UUID.fromString("deadbeaf-0000-0000-0000-000000000000"))
        )
        .withVre("ww")
        .withVre("")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("work", v -> v
        .withVre("ww")
        .withVre("")
        .withType("person")
        .withTimId("f005ba11-0000-0000-0000-000000000000")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);
    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@relationCount", jsn(1),
      "@relations", JsonBuilder.jsnO(
        "isPseudonymOf", JsonBuilder.jsnA(
          JsonBuilder.jsnO(
            "relationId", jsn("deadbeaf-0000-0000-0000-000000000000"),
            "accepted", jsn(true),
            "rev", jsn(5)
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsDisplayName() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withOutgoingRelation("isPseudonymOf", "pseudonym")
        .withVre("ww")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("pseudonym", v -> v
        .withVre("ww")
        .withType("displayname")
        .withProperty("wwperson_displayName", "Pieter van Reigersberch")
        .withTimId("f005ba11-0000-0000-0000-000000000000")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);
    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@relations", JsonBuilder.jsnO(
        "isPseudonymOf", JsonBuilder.jsnA(
          JsonBuilder.jsnO(
            "displayName", jsn("Pieter van Reigersberch")
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsDerivedRelations() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withOutgoingRelation("isCreatorOf", "someWork", r -> r
          .withIsLatest(true)
          .withDeleted(false)
        )
        .withVre("ww")
        .withType("derivedrelation")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("someWork", v -> v
        .withOutgoingRelation("hasWorkLanguage", "dutch", r -> r
          .withIsLatest(true)
          .withDeleted(false)
        )
        .isLatest(true)
        .withVre("ww")
        .withType("document")
      )
      .withVertex("dutch", v -> v
        .withVre("ww")
        .withType("language")
        .withProperty("wwlanguage_name", "Dutch")
        .isLatest(true)
        .withTimId(UUID.randomUUID().toString())
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);
    String resultJson = instance.get("wwderivedrelations", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@relations", JsonBuilder.jsnO(
        "hasPersonLanguage", JsonBuilder.jsnA(
          JsonBuilder.jsnO(
            "displayName", jsn("Dutch")
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsTheVariationRefs() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("types", "[\"wwperson\", \"person\"]")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@variationRefs", JsonBuilder.jsnA(
        JsonBuilder.jsnO(
          "id", jsn(id.toString()),
          "type", jsn("wwperson")
        ),
        JsonBuilder.jsnO(
          "id", jsn(id.toString()),
          "type", jsn("person")
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void throwsNotFoundWhenTypeIsNotPartOfTypes() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withType("person")
        .withVre("ckcc")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(NotFoundException.class);

    instance.get("wwpersons", id).toString();
  }

  @Test
  public void throwsNotFoundWhenDeleted() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("deleted", true)
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(NotFoundException.class);

    instance.get("wwpersons", id).toString();
  }

  @Test
  public void alwaysShowsDeleteProp() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "^deleted", jsn(false)
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsThePid() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("pid", "http://example.com/pid")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "^pid", jsn("http://example.com/pid")
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void bugFix_GettingTheSameResourceTwiceWouldReuseTinkerPopPipelines() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withProperty("isLatest", true)
        .withType("person")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("wwperson_name", "the name")
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.get("wwpersons", id);
    JsonNode entity = instance.get("wwpersons", id);

    assertThat(entity.get("name").asText(""), is("the name"));
  }

  @Test
  public void bugFix_handlesMultiplePropertyDefinitionsCorrectly() throws Exception {
    UUID id = UUID.randomUUID();
    UUID workId = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withIncomingRelation("isCreatedBy", "work")
        .withVre("ww")
        .withVre("")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("work", v -> v
        .withVre("ww")
        .withType("document")
        .withTimId(workId.toString())
      )
      .withVertex(v -> v
        .withProperty("relationtype_regularName", "isCreatedBy")
        .withProperty("relationtype_inverseName", "isCreatorOf")
      )
      .withVertex(v -> v
        .withProperty("relationtype_regularName", "secondPropertyDefinition")
        .withProperty("relationtype_inverseName", "inverseName")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@relationCount", jsn(1),
      "@relations", JsonBuilder.jsnO(
        "isCreatorOf", JsonBuilder.jsnA(
          JsonBuilder.jsnO(
            "id", jsn(workId.toString())
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }


  @Test
  public void bugFix_handlesDeletedCorrectly() throws Exception {
    UUID id = UUID.randomUUID();
    UUID workId = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withIncomingRelation("isCreatedBy", "work", r -> r
          .withDeleted(false)
        )
        .withVre("ww")
        .withVre("")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("work", v -> v
        .withVre("ww")
        .withType("document")
        .withTimId(workId.toString())
      )
      .withVertex(v -> v
        .withProperty("relationtype_regularName", "isCreatedBy")
        .withProperty("relationtype_inverseName", "isCreatorOf")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(JsonBuilder.jsnO(
      "@relationCount", jsn(1),
      "@relations", JsonBuilder.jsnO(
        "isCreatorOf", JsonBuilder.jsnA(
          JsonBuilder.jsnO(
            "id", jsn(workId.toString())
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }


}
