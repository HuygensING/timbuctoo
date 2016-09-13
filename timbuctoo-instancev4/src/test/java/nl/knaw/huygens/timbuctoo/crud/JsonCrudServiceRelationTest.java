package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.crud.JsonCrudServiceBuilder.newJsonCrudService;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;
import static nl.knaw.huygens.timbuctoo.util.AuthorizerHelper.authorizerThrowsAuthorizationUnavailableException;
import static nl.knaw.huygens.timbuctoo.util.AuthorizerHelper.userIsNotAllowedToWriteTheCollection;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JsonCrudServiceRelationTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void canPostRelation() throws Exception {
    String typeId = "10000000-046d-477a-acbb-1c18b2a7c7e9";
    String sourceId = "20000000-742e-4351-9154-b33c10dbf5b2";
    String targetId = "30000000-bc09-4959-a8b9-1cafad9a60f6";
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(typeId)
        .withVre("ww")
        .withType("relation")
        .withProperty("relationtype_regularName", "regularName")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(sourceId)
        .withProperty("rev", 1)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(targetId)
        .withProperty("rev", 1)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
      )
      .build();

    JsonCrudService instance = newJsonCrudService()
      .withClock(Clock.fixed(Instant.ofEpochMilli(1337), ZoneId.systemDefault()))
      .forGraph(graph);

    instance.create("wwrelations", jsnO(
      "accepted", jsn(true),
      "^typeId", jsn(typeId),
      "^sourceId", jsn(sourceId),
      "^targetId", jsn(targetId)
    ), "the user provided to create()");

    Edge result = graph.edges().next();
    assertThat(result.outVertex().value("tim_id"), is(sourceId));
    assertThat(result.inVertex().value("tim_id"), is(targetId));
    assertThat(result.value("typeId"), is(typeId));
    assertThat(result.value("wwrelation_accepted"), is(true));
    assertThat(result.value("types"), is("[\"wwrelation\",\"relation\"]"));
    assertThat(getModificationInfo("created", result), is(jsnO(
      "timeStamp", jsn(1337),
      "userId", jsn("the user provided to create()")
    )));
  }

  @Test
  public void canPutRelation() throws Exception {

    UUID edgeId = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withVre("")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
        .withOutgoingRelation("someName", "target", r -> r
          .withTim_id(edgeId)
          .withAccepted("relation", true)
          .withAccepted("wwrelation", true)
          .withIsLatest(true)
          .withRev(1)
        )
      )
      .withVertex("target", v -> v
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
        .withVre("")
        .withVre("ww")
      )
      .build();

    JsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.replace("wwrelations", edgeId, jsnO(
      "accepted", jsn(false),
      "^rev", jsn(1)
    ), "");

    Edge result = graph.traversal().E().has("isLatest", true).next();
    assertThat(result.value("wwrelation_accepted"), is(false));
    assertThat(result.value("types"), is("[\"relation\",\"wwrelation\"]"));
  }

  @Test
  public void createdRelationsAreShown() throws Exception {
    String typeId = "10000000-046d-477a-acbb-1c18b2a7c7e9";
    String sourceId = "20000000-742e-4351-9154-b33c10dbf5b2";
    String targetId = "30000000-bc09-4959-a8b9-1cafad9a60f6";
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(typeId)
        .withProperty("relationtype_regularName", "regularName")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(sourceId)
        .withType("person")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(targetId)
        .withProperty("rev", 1)
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
      )
      .build();

    JsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.create("wwrelations", jsnO(
      "accepted", jsn(true),
      "^typeId", jsn(typeId),
      "^sourceId", jsn(sourceId),
      "^targetId", jsn(targetId)
    ), "");

    JsonNode getResult = instance.get("wwpersons", UUID.fromString(sourceId));
    assertThat(getResult.get("@relationCount").asInt(), is(1));
  }

  @Test
  public void sourceAndTargetMustBeOfTheSameVre() throws Exception {
    String typeId = "10000000-046d-477a-acbb-1c18b2a7c7e9";
    String sourceId = "20000000-742e-4351-9154-b33c10dbf5b2";
    String targetId = "30000000-bc09-4959-a8b9-1cafad9a60f6";
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(typeId)
        .withProperty("relationtype_regularName", "regularName")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(sourceId)
        .withType("person")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(targetId)
        .withProperty("rev", 1)
        .withType("person")
        .withVre("ckcc")
        .withProperty("isLatest", true)
      )
      .build();
    JsonCrudService instance = newJsonCrudService().forGraph(graph);

    expectedException.expect(IOException.class);
    instance.create("wwrelations", jsnO(
      "accepted", jsn(true),
      "^typeId", jsn(typeId),
      "^sourceId", jsn(sourceId),
      "^targetId", jsn(targetId)
    ), "");
  }

  @Test
  public void onlyValidSourcesCanBeConnected() throws Exception {
    String typeId = "10000000-046d-477a-acbb-1c18b2a7c7e9";
    String sourceId = "20000000-742e-4351-9154-b33c10dbf5b2";
    String targetId = "30000000-bc09-4959-a8b9-1cafad9a60f6";
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(typeId)
        .withProperty("relationtype_sourceTypeName", "wwperson")
        .withProperty("relationtype_targetTypeName", "wwperson")
        .withProperty("relationtype_regularName", "regularName")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(sourceId)
        .withType("person")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(targetId)
        .withProperty("rev", 1)
        .withType("document")
        .withVre("ww")
        .withProperty("isLatest", true)
      )
      .build();
    JsonCrudService instance = newJsonCrudService().forGraph(graph);

    expectedException.expect(IOException.class);
    instance.create("wwrelations", jsnO(
      "accepted", jsn(true),
      "^typeId", jsn(typeId),
      "^sourceId", jsn(sourceId),
      "^targetId", jsn(targetId)
    ), "");
  }


  @Test
  public void onlyValidTargetsCanBeConnected() throws Exception {
    String typeId = "10000000-046d-477a-acbb-1c18b2a7c7e9";
    String sourceId = "20000000-742e-4351-9154-b33c10dbf5b2";
    String targetId = "30000000-bc09-4959-a8b9-1cafad9a60f6";
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(typeId)
        .withProperty("relationtype_sourceTypeName", "wwdocument")
        .withProperty("relationtype_targetTypeName", "wwdocument")
        .withProperty("relationtype_regularName", "regularName")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(sourceId)
        .withType("person")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(targetId)
        .withProperty("rev", 1)
        .withType("document")
        .withVre("ww")
        .withProperty("isLatest", true)
      )
      .build();
    JsonCrudService instance = newJsonCrudService().forGraph(graph);

    expectedException.expect(IOException.class);
    instance.create("wwrelations", jsnO(
      "accepted", jsn(true),
      "^typeId", jsn(typeId),
      "^sourceId", jsn(sourceId),
      "^targetId", jsn(targetId)
    ), "");
  }

  @Test
  public void setsModifiedInfo() throws Exception {
    testRelationUpdate((oldEdge, newEdge, totalEdgeCount) ->
      assertThat(getModificationInfo("modified", newEdge), is(jsnO(
        "timeStamp", jsn(1337),
        "userId", jsn("the user provided to replace()")
      )))
    );
  }

  @Test
  public void setsCreatedInfo() throws Exception {
    String typeId = "10000000-046d-477a-acbb-1c18b2a7c7e9";
    String sourceId = "20000000-742e-4351-9154-b33c10dbf5b2";
    String targetId = "30000000-bc09-4959-a8b9-1cafad9a60f6";
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(typeId)
        .withProperty("relationtype_regularName", "regularName")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex("source", v -> v
        .withTimId(sourceId)
        .withType("person")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex("target", v -> v
        .withTimId(targetId)
        .withProperty("rev", 1)
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
      )
      .build();
    String collectionName = "wwrelations";
    JsonCrudService instance = newJsonCrudService()
      .withClock(Clock.fixed(Instant.ofEpochMilli(1337), ZoneId.systemDefault()))
      .forGraph(graph);

    instance.create(collectionName, jsnO(
      "accepted", jsn(true),
      "^typeId", jsn(typeId),
      "^sourceId", jsn(sourceId),
      "^targetId", jsn(targetId)
    ), "the user provided to create()");

    Edge newEdge = graph.traversal().V().has("tim_id", sourceId).outE().has("isLatest", true).next();
    assertThat(getModificationInfo("modified", newEdge), is(jsnO(
      "timeStamp", jsn(1337),
      "userId", jsn("the user provided to create()")
    )));
    assertThat(getModificationInfo("created", newEdge), is(jsnO(
      "timeStamp", jsn(1337),
      "userId", jsn("the user provided to create()")
    )));
  }

  private ObjectNode getModificationInfo(String prop, Element elm) {
    return getProp(elm, prop, String.class)
      .map(data -> Try.of(() -> (ObjectNode) new ObjectMapper().readTree(data)))
      .orElse(Try.success(null))
      .get();
  }

  @Test
  public void edgeIsDuplicated() throws Exception {
    testRelationUpdate((oldEdge, newEdge, totalEdgeCount) -> assertThat(totalEdgeCount, is(2)));
  }

  @Test
  public void revIsUpdated() throws Exception {
    testRelationUpdate((oldEdge, newEdge, totalEdgeCount) -> assertThat(newEdge.property("rev").value(), is(2)));
  }

  private interface RelationTest {
    void test(Edge oldEdge, Edge newEdge, int totalEdgeCount);
  }

  public void testRelationUpdate(RelationTest test) throws Exception {
    UUID relId = UUID.fromString("00000000-046d-477a-acbb-1c18b2a7c7e9");
    String typeId = "10000000-046d-477a-acbb-1c18b2a7c7e9";
    String sourceId = "20000000-742e-4351-9154-b33c10dbf5b2";
    String targetId = "30000000-bc09-4959-a8b9-1cafad9a60f6";
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(typeId)
        .withProperty("relationtype_regularName", "regularName")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex("source", v -> v
        .withTimId(sourceId)
        .withType("person")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
        .withOutgoingRelation("regularName", "target", e -> e
          .withAccepted("wwrelation", true)
          .withRev(1)
          .withTim_id(relId)
        )
      )
      .withVertex("target", v -> v
        .withTimId(targetId)
        .withProperty("rev", 1)
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
      )
      .build();
    String collectionName = "wwrelations";
    JsonCrudService instance = newJsonCrudService()
      .withClock(Clock.fixed(Instant.ofEpochMilli(1337), ZoneId.systemDefault()))
      .forGraph(graph);

    instance.replace(collectionName, relId, jsnO(
      "^rev", jsn(1),
      "accepted", jsn(false)
    ), "the user provided to replace()");

    int edgeCount = graph.traversal().V().has("tim_id", sourceId).outE().toList().size();
    Edge oldEdge = null;
    Edge newEdge = null;
    try {
      oldEdge = graph.traversal().V().has("tim_id", sourceId).outE().has("isLatest", false).next();
      newEdge = graph.traversal().V().has("tim_id", sourceId).outE().has("isLatest", true).next();
    } catch (NoSuchElementException e) {
      //ignore
    }
    test.test(oldEdge, newEdge, edgeCount);
  }

  // security tests
  @Test
  public void createThrowsAAuthorizationExceptionWhenTheUserIsNotAllowedToAddANewRelation() throws Exception {
    String typeId = "10000000-046d-477a-acbb-1c18b2a7c7e9";
    String sourceId = "20000000-742e-4351-9154-b33c10dbf5b2";
    String targetId = "30000000-bc09-4959-a8b9-1cafad9a60f6";
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(typeId)
        .withProperty("relationtype_regularName", "regularName")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(sourceId)
        .withType("person")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(targetId)
        .withProperty("rev", 1)
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
      )
      .build();
    String collectionName = "wwrelations";
    String userId = "userId";
    Authorizer authorizer = userIsNotAllowedToWriteTheCollection(collectionName, userId);
    JsonCrudService instance = newJsonCrudService().withAuthorizer(authorizer).forGraph(graph);

    expectedException.expect(AuthorizationException.class);

    instance.create(collectionName, jsnO(
      "accepted", jsn(true),
      "^typeId", jsn(typeId),
      "^sourceId", jsn(sourceId),
      "^targetId", jsn(targetId)
    ), userId);

  }

  @Test
  public void replaceThrowsAnAuthorizationExceptionWhenTheUsersIsNotAllowedToChangeTheCollection() throws Exception {
    UUID edgeId = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withVre("")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
        .withOutgoingRelation("someName", "target", r -> r
          .withTim_id(edgeId)
          .withAccepted("relation", true)
          .withAccepted("wwrelation", true)
          .withIsLatest(true)
          .withRev(1)
        )
      )
      .withVertex("target", v -> v
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
        .withVre("")
        .withVre("ww")
      )
      .build();
    String wwrelations = "wwrelations";
    String userId = "userId";
    Authorizer authorizer = userIsNotAllowedToWriteTheCollection(wwrelations, userId);
    JsonCrudService instance = newJsonCrudService().withAuthorizer(authorizer).forGraph(graph);

    expectedException.expect(AuthorizationException.class);

    instance.replace(wwrelations, edgeId, jsnO(
      "accepted", jsn(false),
      "^rev", jsn(1)
    ), userId);

  }

  @Test
  public void createThrowsAnIoExceptionWhenTheAuthorizerThrowsAnAuthorizationUnavailableException() throws Exception {
    String typeId = "10000000-046d-477a-acbb-1c18b2a7c7e9";
    String sourceId = "20000000-742e-4351-9154-b33c10dbf5b2";
    String targetId = "30000000-bc09-4959-a8b9-1cafad9a60f6";
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(typeId)
        .withProperty("relationtype_regularName", "regularName")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(sourceId)
        .withType("person")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(targetId)
        .withProperty("rev", 1)
        .withType("person")
        .withVre("ww")
        .withProperty("isLatest", true)
      )
      .build();
    String collectionName = "wwrelations";
    String userId = "userId";
    Authorizer authorizer = authorizerThrowsAuthorizationUnavailableException();
    JsonCrudService instance = newJsonCrudService().withAuthorizer(authorizer).forGraph(graph);

    expectedException.expect(IOException.class);

    instance.create(collectionName, jsnO(
      "accepted", jsn(true),
      "^typeId", jsn(typeId),
      "^sourceId", jsn(sourceId),
      "^targetId", jsn(targetId)
    ), userId);
  }

  @Test
  public void replaceThrowsAnIoExceptionWhenTheAuthorizerThrowsAnAuthorizationUnavailableException() throws Exception {
    UUID edgeId = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withVre("")
        .withVre("ww")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
        .withOutgoingRelation("someName", "target", r -> r
          .withTim_id(edgeId)
          .withAccepted("relation", true)
          .withAccepted("wwrelation", true)
          .withIsLatest(true)
          .withRev(1)
        )
      )
      .withVertex("target", v -> v
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
        .withVre("")
        .withVre("ww")
      )
      .build();
    String wwrelations = "wwrelations";
    String userId = "userId";
    Authorizer authorizer = authorizerThrowsAuthorizationUnavailableException();
    JsonCrudService instance = newJsonCrudService().withAuthorizer(authorizer).forGraph(graph);

    expectedException.expect(AuthorizationUnavailableException.class);

    instance.replace(wwrelations, edgeId, jsnO(
      "accepted", jsn(false),
      "^rev", jsn(1)
    ), userId);
  }
}
