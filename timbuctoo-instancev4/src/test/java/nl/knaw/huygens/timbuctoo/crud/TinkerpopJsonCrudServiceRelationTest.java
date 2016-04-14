package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.crud.JsonCrudServiceBuilder.newJsonCrudService;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TinkerpopJsonCrudServiceRelationTest {

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
        .withProperty("relationtype_regularName", "regularName")
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(sourceId)
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .withVertex(v -> v
        .withTimId(targetId)
        .withProperty("rev", 1)
        .withProperty("isLatest", true)
      )
      .build();

    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.create("wwrelations", JsonBuilder.jsnO(
      "accepted", jsn(true),
      "^typeId", jsn(typeId),
      "^sourceId", jsn(sourceId),
      "^targetId", jsn(targetId)
    ), "");

    Edge result = graph.edges().next();
    assertThat(result.outVertex().value("tim_id"), is(sourceId));
    assertThat(result.inVertex().value("tim_id"), is(targetId));
    assertThat(result.value("typeId"), is(typeId));
    assertThat(result.value("wwrelation_accepted"), is(true));
    assertThat(result.value("types"), is("[\"wwrelation\",\"relation\"]"));
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

    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.replace("wwrelations", edgeId, JsonBuilder.jsnO(
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

    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.create("wwrelations", JsonBuilder.jsnO(
      "accepted", jsn(true),
      "^typeId", jsn(typeId),
      "^sourceId", jsn(sourceId),
      "^targetId", jsn(targetId)
    ), "");

    JsonNode getResult = instance.get("wwpersons", UUID.fromString(sourceId));
    assertThat(getResult.get("@relationCount").asInt(), is(1));
  }

  //FIXME:modified, created
  //FIXME:update rev
  //FIXME: duplicate edge
  //FIXME:add pid
  //FIXME:check if allowed for source and target to be connected using this relation
  //FIXME:source and target must be of this VRE

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
    Authorizer authorizer = AuthorizerHelper.userIsNotAllowedToWriteTheCollection(collectionName, userId);
    TinkerpopJsonCrudService instance = newJsonCrudService().withAuthorizer(authorizer).forGraph(graph);

    expectedException.expect(AuthorizationException.class);

    instance.create(collectionName, JsonBuilder.jsnO(
      "accepted", jsn(true),
      "^typeId", jsn(typeId),
      "^sourceId", jsn(sourceId),
      "^targetId", jsn(targetId)
    ), userId);

  }
}
