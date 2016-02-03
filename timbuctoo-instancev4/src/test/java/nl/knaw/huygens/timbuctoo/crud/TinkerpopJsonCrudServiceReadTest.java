package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.JsonToTinkerpopPropertyMap;
import nl.knaw.huygens.timbuctoo.security.JsonBasedUserStore;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONObjectAs;

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

    Map<String, List<JsonToTinkerpopPropertyMap>> onlyWwPersons = ImmutableMap.of(
      "wwpersons", Lists.newArrayList(
        new JsonToTinkerpopPropertyMap("name", "wwperson_name")
      )
    );

    return new TinkerpopJsonCrudService(graphWrapper, onlyWwPersons, handleAdder, userStore, gen, clock);
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void throwsOnUnknownMappings() throws IOException, InvalidCollectionException {
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
  public void echoesAtTypeAndIdProperty() throws IOException, InvalidCollectionException {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    assertThat(
      instance.get("wwpersons", id).toString(),
      sameJSONAs(
        jsn(
          "@type", jsn("wwperson"),
          "_id", jsn(id.toString())
        ).toString()
      ).allowingExtraUnexpectedFields()
    );
  }

  @Test
  public void returnsThePropertiesFromTheMapper() throws IOException, InvalidCollectionException {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
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
  public void getsTheLatestEntityWhenNoRevIsSpecified() throws InvalidCollectionException {

    UUID uuid = UUID.randomUUID();

    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(uuid.toString())
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

    JsonNode entity = instance.get("wwpersons", uuid);

    assertThat(entity.get("^rev").asInt(), is(2));
    assertThat(entity.get("name").asText(), is("new"));
  }

  @Test
  public void getsTheRequestedRevWhenSpecified() throws InvalidCollectionException {

    UUID uuid = UUID.randomUUID();

    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(uuid.toString())
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
  public void showsTheModificationInfoIncludingUserDescription() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("modified", "{\"timeStamp\":1427921175250, \"userId\":\"USER1\"}")
      )
      .build();

    JsonBasedUserStore userStore = mock(JsonBasedUserStore.class);
    when(userStore.userForId("USER1")).thenReturn(Optional.of(new User("Username for USER1")));

    TinkerpopJsonCrudService instance = basicInstanceWithUserStore(graph, userStore);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(jsn(
      "^modified", jsn(
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
        .withProperty("rev", 1)
        .withProperty("created", "{\"timeStamp\":1427921175250, \"userId\":\"USER1\"}")
      )
      .build();

    JsonBasedUserStore userStore = mock(JsonBasedUserStore.class);
    when(userStore.userForId("USER1")).thenReturn(Optional.of(new User("Username for USER1")));

    TinkerpopJsonCrudService instance = basicInstanceWithUserStore(graph, userStore);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(jsn(
      "^created", jsn(
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
        .withType("wwperson")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("pseudonym", v -> v
        .withType("wwperson")
        .withType("person")
        .withTimId(pseudonymId.toString())
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(jsn(
      "@relationCount", jsn(1),
      "@relations", jsn(
        "isPseudonymOf", jsn(
          jsn(
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
        .withType("wwperson")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("work", v -> v
        .withType("wwdocument")
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

    assertThat(resultJson, sameJSONAs(jsn(
      "@relationCount", jsn(1),
      "@relations", jsn(
        "isCreatorOf", jsn(
          jsn(
            "id", jsn(workId.toString())
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsTheOtherVerticesProperties() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex("source", v -> v
        .withOutgoingRelation("isPseudonymOf", "work")
        .withType("wwperson")
        .withType("person")
        .isLatest(true)
        .withTimId(id.toString())
      )
      .withVertex("work", v -> v
        .withType("wwperson")
        .withType("person")
        .withTimId("f005ba11-0000-0000-0000-000000000000")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstanceWithGenerator(
      graph,
      (collection, uuid, rev) -> URI.create(String.format("/%s/%s/%s", collection, uuid, rev))
    );

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(jsn(
      "@relationCount", jsn(1),
      "@relations", jsn(
        "isPseudonymOf", jsn(
          jsn(
            "id", jsn("f005ba11-0000-0000-0000-000000000000"),
            "path", jsn("/wwpersons/f005ba11-0000-0000-0000-000000000000/null")
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  //toon de properties van de andere vertex
  //path is de url van de andere vertex
  //derived relations moet je ook doen. die moet je ergens configureren
  //doesn't show accepted false
  //FIXME displayname


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

    assertThat(resultJson, sameJSONAs(jsn(
      "@variationRefs", jsn(
        jsn(
          "id", jsn(id.toString()),
          "type", jsn("wwperson")
        ),
        jsn(
          "id", jsn(id.toString()),
          "type", jsn("person")
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsOwnVariationRefWhenTypesPropertyIsNotSet() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(jsn(
      "@variationRefs", jsn(
        jsn(
          "id", jsn(id.toString()),
          "type", jsn("wwperson")
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsIfDeleted() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("deleted", true)
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(jsn(
      "^deleted", jsn(true)
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void alwaysShowsDeleteProp() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(jsn(
      "^deleted", jsn(false)
    ).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void showsThePid() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("pid", "http://example.com/pid")
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    String resultJson = instance.get("wwpersons", id).toString();

    assertThat(resultJson, sameJSONAs(jsn(
      "^pid", jsn("http://example.com/pid")
    ).toString()).allowingExtraUnexpectedFields());
  }
}
