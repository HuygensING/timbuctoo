package nl.knaw.huygens.timbuctoo.search.description.indexes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudService;
import nl.knaw.huygens.timbuctoo.crud.changelistener.DenormalizedSortFieldUpdater;
import nl.knaw.huygens.timbuctoo.crud.changelistener.FulltextIndexChangeListener;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.crud.JsonCrudServiceBuilder.newJsonCrudService;
import static nl.knaw.huygens.timbuctoo.search.description.indexes.MockIndexUtil.makeIndexMocks;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WwPersonIndexDescriptionTest {

  @Test
  public void getSortIndexPropertyNamesReturnsPropertyNamesForAllTypesAndFields() {
    WwPersonIndexDescription instance = new WwPersonIndexDescription();

    Set<String> results = instance.getSortFieldDescriptions().stream()
            .map(IndexerSortFieldDescription::getSortPropertyName)
            .collect(Collectors.toSet());

    assertThat(results, containsInAnyOrder(
            "wwperson_names_sort",
            "wwperson_deathDate_sort",
            "wwperson_birthDate_sort",
            "modified_sort"
    ));
  }

  @Test
  public void addIndexedSortPropertiesSetsTheSortIndexProperties() throws JsonProcessingException {
    long timeStampOnJan20th2016 = 1453290593000L;
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withVre("ww")
                    .withType("person")
                    .withProperty("wwperson_names", getPersonName("testfore", "testsur2"))
                    .withProperty("wwperson_deathDate", "\"2015-05-01\"")
                    .withProperty("wwperson_birthDate", "\"2010-05-01\"")
                    .withProperty("modified", getChange(timeStampOnJan20th2016))
            )
            .build();
    WwPersonIndexDescription instance = new WwPersonIndexDescription();
    Vertex vertex = graph.traversal().V().toList().get(0);

    instance.addIndexedSortProperties(vertex);

    assertThat(vertex.property("wwperson_names_sort").value(), equalTo("testsur2, testfore"));
    assertThat(vertex.property("wwperson_birthDate_sort").value(), equalTo(2010));
    assertThat(vertex.property("wwperson_deathDate_sort").value(), equalTo(2015));
    assertThat(vertex.property("modified_sort").value(), equalTo(timeStampOnJan20th2016));

  }

  @Test
  public void addIndexedSortPropertiesSetsTheSortIndexPropertyToEmptyStringWhenPropertyIsMissing() {
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withVre("ww")
                    .withType("person"))
            .build();
    WwPersonIndexDescription instance = new WwPersonIndexDescription();
    Vertex vertex = graph.traversal().V().toList().get(0);

    instance.addIndexedSortProperties(vertex);

    assertThat(vertex.property("wwperson_names_sort").value(), equalTo(""));
    assertThat(vertex.property("wwperson_birthDate_sort").value(), equalTo(0));
    assertThat(vertex.property("wwperson_deathDate_sort").value(), equalTo(0));
    assertThat(vertex.property("modified_sort").value(), equalTo(0L));

  }

  @Test
  public void addToFulltextIndexCorrectlyInvokesLegacyIndexerService() {
    final String timId = "123";
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withVre("ww")
                    .withType("person")
                    .withTimId(timId)
                    .withProperty("wwperson_names", getPersonName("testfore", "testsur2"))
            )
            .build();

    WwPersonIndexDescription instance = new WwPersonIndexDescription();
    Vertex vertex = graph.traversal().V().toList().get(0);

    List<Object> mocks = makeIndexMocks(vertex, timId);

    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) mocks.get(0);
    Index mockIndex = (Index) mocks.get(1);
    Node removeNode = (Node) mocks.get(2);
    Node addNode = (Node) mocks.get(3);

    instance.addToFulltextIndex(vertex, mockDatabaseService);

    verify(mockIndex, times(1)).remove(removeNode);
    verify(mockIndex, times(1)).add(addNode, "tim_id", timId);
    verify(mockIndex, times(1)).add(addNode, "displayName", "testfore testsur2");
  }



  @Test
  public void crudServiceInvokesIndexDescriptionAddToFulltextIndexForWwPersonsOnCreate() throws Exception {
    Graph graph = newGraph().build();

    List<Object> mocks = makeIndexMocks();

    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) mocks.get(0);
    Index mockIndex = (Index) mocks.get(1);
    Node addNode = (Node) mocks.get(3);


    TinkerpopJsonCrudService instance = newJsonCrudService()
            .withChangeListener(new FulltextIndexChangeListener(mockDatabaseService, new IndexDescriptionFactory()))
            .forGraph(graph);

    instance.create("wwpersons", jsnO(
            "names", jsnA(jsnO("components", jsnA(jsnO("type", jsn("FORENAME"), "value", jsn("testing")))))
    ), "");
    Vertex vertex = graph.vertices().next();


    verify(mockIndex, times(1)).add(addNode, "tim_id", vertex.property("tim_id").value());
    verify(mockIndex, times(1)).add(addNode, "displayName", "testing");
  }

  @Test
  public void crudServiceInvokesIndexDescriptionRemoveFromFulltextIndexForWwDocumentsOnDelete() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withTimId(id)
                    .withProperty("types", "[\"person\", \"wwperson\"]")
                    .withProperty("isLatest", true)
                    .withProperty("rev", 1)
                    .withIncomingRelation("VERSION_OF", "orig")
            )
            .withVertex("orig", v -> v
                    .withTimId(id)
                    .withProperty("types", "[\"person\", \"wwperson\"]")
                    .withProperty("isLatest", false)
                    .withProperty("rev", 1)
            )
            .build();

    Vertex origVertex = graph.traversal().V().has("tim_id", id).has("isLatest", true).next();
    List<Object> mocks = makeIndexMocks(origVertex, id);

    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) mocks.get(0);
    Index mockIndex = (Index) mocks.get(1);
    Node removeNode = (Node) mocks.get(2);


    TinkerpopJsonCrudService instance = newJsonCrudService()
            .withChangeListener(new FulltextIndexChangeListener(mockDatabaseService, new IndexDescriptionFactory()))
            .forGraph(graph);


    instance.delete("wwpersons", UUID.fromString(id), "");

    verify(mockIndex, times(1)).remove(removeNode);
    verify(mockIndex, never()).add(any(), any(), any());
  }



  @Test
  public void crudServiceInvokesIndexDescriptionAddToFulltextIndexForWwPersonsOnUpdate() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withTimId(id)
                    .withProperty("types", "[\"person\", \"wwperson\"]")
                    .withProperty("isLatest", true)
                    .withProperty("rev", 1)
                    .withProperty("wwperson_names", getPersonName("testfore", "testsur2"))
                    .withIncomingRelation("VERSION_OF", "orig")
            )
            .withVertex("orig", v -> v
                    .withTimId(id)
                    .withProperty("isLatest", false)
                    .withProperty("rev", 1)
            )
            .build();
    Vertex origVertex = graph.traversal().V().has("tim_id", id).has("isLatest", true).next();
    List<Object> mocks = makeIndexMocks(origVertex, id);

    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) mocks.get(0);
    Index mockIndex = (Index) mocks.get(1);
    Node removeNode = (Node) mocks.get(2);
    Node addNode = (Node) mocks.get(3);


    TinkerpopJsonCrudService instance = newJsonCrudService()
            .withChangeListener(new FulltextIndexChangeListener(mockDatabaseService, new IndexDescriptionFactory()))
            .forGraph(graph);

    instance.replace("wwpersons", UUID.fromString(id),
            jsnO(
                    "^rev", jsn(1),
                    "names", jsnA(jsnO("components", jsnA(jsnO("type", jsn("FORENAME"), "value", jsn("testing")))))
            ), "");

    verify(mockIndex, times(1)).remove(removeNode);
    verify(mockIndex, times(1)).add(addNode, "tim_id", id);
    verify(mockIndex, times(1)).add(addNode, "displayName", "testing");
  }




  @Test
  public void crudServiceInvokesIndexDescriptionAddIndexedSortPropertiesForWwPersonsOnUpdate() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withTimId(id)
                    .withProperty("types", "[\"person\", \"wwperson\"]")
                    .withProperty("isLatest", true)
                    .withProperty("rev", 1)
                    .withIncomingRelation("VERSION_OF", "orig")
            )
            .withVertex("orig", v -> v
                    .withTimId(id)
                    .withProperty("isLatest", false)
                    .withProperty("rev", 1)
            )
            .build();
    TinkerpopJsonCrudService instance = newJsonCrudService()
      .withChangeListener(new DenormalizedSortFieldUpdater(new IndexDescriptionFactory()))
      .forGraph(graph);

    instance.replace("wwpersons", UUID.fromString(id), jsnO("^rev", jsn(1)), "");


    Vertex vertex = graph.traversal().V().has("tim_id", id).has("isLatest", true).next();

    MatcherAssert.assertThat(vertex.property("wwperson_names_sort").value(), equalTo(""));
    MatcherAssert.assertThat(vertex.property("wwperson_birthDate_sort").value(), equalTo(0));
    MatcherAssert.assertThat(vertex.property("wwperson_deathDate_sort").value(), equalTo(0));
    MatcherAssert.assertThat(vertex.property("modified_sort").value(), Matchers.instanceOf(Long.class));
  }

  @Test
  public void crudServiceInvokesIndexDescriptionAddIndexedSortPropertiesForWwPersonsOnCreate() throws Exception {
    Graph graph = newGraph().build();

    TinkerpopJsonCrudService instance = newJsonCrudService()
      .withChangeListener(new DenormalizedSortFieldUpdater(new IndexDescriptionFactory()))
      .forGraph(graph);

    instance.create("wwpersons", JsonBuilder.jsnO(), "");

    Vertex vertex = graph.vertices().next();

    assertThat(vertex.property("wwperson_names_sort").value(), equalTo(""));
    assertThat(vertex.property("wwperson_birthDate_sort").value(), equalTo(0));
    assertThat(vertex.property("wwperson_deathDate_sort").value(), equalTo(0));
    assertThat(vertex.property("modified_sort").value(), instanceOf(Long.class));
  }

  private String getPersonName(String foreName, String surName) {
    PersonName name = new PersonName();
    name.addNameComponent(PersonNameComponent.Type.FORENAME, foreName);
    name.addNameComponent(PersonNameComponent.Type.SURNAME, surName);
    String nameProp;
    try {
      nameProp = new ObjectMapper().writeValueAsString(name);
    } catch (IOException e) {
      nameProp = "";
    }

    return "{\"list\": [" + nameProp + "]}";
  }



  private String getChange(long timeStamp) {
    Change change = new Change(timeStamp, "user", "vre");
    String changeString;
    try {
      changeString = new ObjectMapper().writeValueAsString(change);
    } catch (JsonProcessingException e) {
      changeString = "";
    }
    return changeString;
  }
}
