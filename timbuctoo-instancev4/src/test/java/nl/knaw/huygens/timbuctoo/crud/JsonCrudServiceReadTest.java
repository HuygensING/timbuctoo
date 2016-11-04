package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.crud.JsonCrudServiceBuilder.newJsonCrudService;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class JsonCrudServiceReadTest {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void throwsOnUnknownMappings() throws Exception {
    Graph graph = newGraph().build();
    JsonCrudService instance = newJsonCrudService().forGraph(graph);

    expectedException.expect(InvalidCollectionException.class);

    instance.get("anUnknownCollection", UUID.randomUUID());
  }

  @Test
  public void getCollectionReturnsAllTheLatestEntitiesOfACollection() throws Exception {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID id3 = UUID.randomUUID();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withLabel("wwperson")
        .withVre("ww")
        .withType("wwperson")
        .isLatest(true)
        .withTimId(id1.toString())
      )
      .withVertex(v -> v
        .withLabel("wwperson")
        .withVre("ww")
        .withType("wwperson")
        .isLatest(true)
        .withTimId(id2.toString())
      )
      .withVertex(v -> v
        .withLabel("wwperson")
        .withVre("ww")
        .withType("wwperson")
        .isLatest(true)
        .withTimId(id3.toString())
      )
      .wrap();
    JsonCrudService instance = newJsonCrudService().withGraphWrapper(graphWrapper).build();

    List<ObjectNode> wwPersons = instance.getCollection("wwpersons", 2, 0, false);

    assertThat(wwPersons, hasSize(2));
  }

  @Test
  public void getCollectionsReturnsTheRelationsOfEntityWhenRequested() throws InvalidCollectionException {
    UUID id1 = UUID.randomUUID();
    UUID workId = UUID.randomUUID();
    GraphWrapper graphWrapper = newGraph()
      .withVertex("v1", v -> v
        .withLabel("wwperson")
        .withVre("ww")
        .withType("wwperson")
        .isLatest(true)
        .withTimId(id1.toString())
        .withOutgoingRelation("isCreatorOf", "work")
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
      .wrap();
    JsonCrudService instance = newJsonCrudService().withGraphWrapper(graphWrapper).build();

    List<ObjectNode> wwPersons = instance.getCollection("wwpersons", 1, 0, true);

    assertThat(wwPersons.toString(), sameJSONAs(jsnA(jsnO(
      "@relationCount", jsn(1),
      "@relations", jsnO(
        "isCreatorOf", jsnA(
          jsnO(
            "id", jsn(workId.toString())
          )
        )
      )
    )).toString()).allowingExtraUnexpectedFields());
  }

  @Test
  public void getCollectionReturnsTheKnownDisplayNameForEachItem() throws Exception {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withLabel("wwperson")
        .withVre("ww")
        .withType("person")
        .isLatest(true)
        .withTimId(id1.toString())
        .withProperty("displayName", "displayName1") // configured in JsonCrudServiceBuilder
      )
      .withVertex(v -> v
        .withLabel("wwperson")
        .withVre("ww")
        .withType("person")
        .isLatest(true)
        .withTimId(id2.toString())
        .withProperty("displayName", "displayName2") // configured in JsonCrudServiceBuilder
      )
      .wrap();
    JsonCrudService instance = newJsonCrudService().withGraphWrapper(graphWrapper).build();

    List<ObjectNode> wwPersons = instance.getCollection("wwpersons", 2, 0, false);

    List<String> wwPersonsStrings = wwPersons.stream().map(person -> person.toString()).collect(toList());
    assertThat(wwPersonsStrings, containsInAnyOrder(
      sameJSONAs(withIdAndDisplayName(id1, "displayName1")).allowingExtraUnexpectedFields(),
      sameJSONAs(withIdAndDisplayName(id2, "displayName2")).allowingExtraUnexpectedFields()
    ));
  }

  private String withIdAndDisplayName(UUID id, String displayName) {
    return jsnO("_id", jsn(id.toString()), "@displayName", jsn(displayName)).toString();
  }

  @Test
  public void getCollectionDoesNotAddADisplayNameWhenNoneIsKnown() throws Exception {
    UUID id1 = UUID.randomUUID();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withLabel("wwperson")
        .withVre("ww")
        .withType("person")
        .isLatest(true)
        .withTimId(id1.toString())
      )
      .wrap();
    JsonCrudService instance = newJsonCrudService().withGraphWrapper(graphWrapper).build();

    List<ObjectNode> wwPersons = instance.getCollection("wwpersons", 2, 0, false);

    assertThat(wwPersons.get(0).has("@displayName"), is(false));
  }

}
