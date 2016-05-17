package nl.knaw.huygens.timbuctoo.search.description.indexes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudService;
import nl.knaw.huygens.timbuctoo.crud.changelistener.DenormalizedSortFieldUpdater;
import nl.knaw.huygens.timbuctoo.crud.changelistener.FulltextIndexChangeListener;
import nl.knaw.huygens.timbuctoo.model.Change;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.crud.JsonCrudServiceBuilder.newJsonCrudService;
import static nl.knaw.huygens.timbuctoo.search.description.indexes.MockIndexUtil.makeIndexMocks;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WwDocumentIndexDescriptionTest {

  @Test
  public void getSortIndexPropertyNamesReturnsPropertyNamesForAllTypesAndFields() {
    WwDocumentIndexDescription instance = new WwDocumentIndexDescription();

    Set<String> results = instance.getSortFieldDescriptions().stream()
            .map(IndexerSortFieldDescription::getSortPropertyName)
            .collect(Collectors.toSet());

    assertThat(results, containsInAnyOrder(
            "wwdocument_creator_sort",
            "modified_sort"
    ));
  }

  @Test
  public void addIndexedSortPropertiesSetsTheSortIndexProperties() throws JsonProcessingException {
    long timeStampOnJan20th2016 = 1453290593000L;
    Graph graph = newGraph()
            .withVertex("creator", v -> v
                    .withVre("ww")
                    .withType("person")
                    .withProperty("wwperson_names_sort", "testsur2, testfore")
            )
            .withVertex(v -> v
                    .withVre("ww")
                    .withType("document")
                    .withTimId("123")
                    .withProperty("modified", getChange(timeStampOnJan20th2016))
                    .withOutgoingRelation("isCreatedBy", "creator")
            )
            .build();

    WwDocumentIndexDescription instance = new WwDocumentIndexDescription();
    Vertex vertex = graph.traversal().V().has("tim_id", "123").toList().get(0);

    instance.addIndexedSortProperties(vertex);

    assertThat(vertex.property("wwdocument_creator_sort").value(), equalTo("testsur2, testfore"));
    assertThat(vertex.property("modified_sort").value(), equalTo(timeStampOnJan20th2016));

  }

  @Test
  public void addIndexedSortPropertiesSetsTheSortIndexPropertyToEmptyStringWhenPropertyIsMissing() {
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withVre("ww")
                    .withType("document"))
            .build();
    WwDocumentIndexDescription instance = new WwDocumentIndexDescription();
    Vertex vertex = graph.traversal().V().toList().get(0);

    instance.addIndexedSortProperties(vertex);


    assertThat(vertex.property("wwdocument_creator_sort").value(), equalTo(""));
    assertThat(vertex.property("modified_sort").value(), equalTo(0L));
  }

  @Test
  public void addToFulltextIndexCorrectlyInvokesLegacyIndexerService() {
    final String timId = "123";
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withVre("ww")
                    .withType("document")
                    .withTimId(timId)
                    .withProperty("wwdocument_title", "Title")
                    .withProperty("wwdocument_date", "1234")
            )
            .build();

    WwDocumentIndexDescription instance = new WwDocumentIndexDescription();
    Vertex vertex = graph.traversal().V().toList().get(0);

    List<Object> mocks = makeIndexMocks(vertex, timId);

    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) mocks.get(0);
    Index mockIndex = (Index) mocks.get(1);
    Node removeNode = (Node) mocks.get(2);
    Node addNode = (Node) mocks.get(3);

    instance.addToFulltextIndex(vertex, mockDatabaseService);

    verify(mockIndex, times(1)).remove(removeNode);
    verify(mockIndex, times(1)).add(addNode, "tim_id", timId);
    verify(mockIndex, times(1)).add(addNode, "displayName", "Title (1234)");
  }

  @Test
  public void crudServiceInvokesIndexDescriptionAddToFulltextIndexForWwDocumentsOnCreate() throws Exception {
    Graph graph = newGraph().build();

    List<Object> mocks = makeIndexMocks();

    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) mocks.get(0);
    Index mockIndex = (Index) mocks.get(1);
    Node addNode = (Node) mocks.get(3);


    TinkerpopJsonCrudService instance = newJsonCrudService()
            .withChangeListener(new FulltextIndexChangeListener(mockDatabaseService, new IndexDescriptionFactory()))
            .forGraph(graph);

    instance.create("wwdocuments", jsnO(
            "title", jsn("Title"),
            "date", jsn("1234")
    ), "");
    Vertex vertex = graph.vertices().next();


    verify(mockIndex, times(1)).add(addNode, "tim_id", vertex.property("tim_id").value());
    verify(mockIndex, times(1)).add(addNode, "displayName", "Title (1234)");
  }


  @Test
  public void crudServiceInvokesIndexDescriptionAddToFulltextIndexForWwDocumentsOnUpdate() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withTimId(id)
                    .withProperty("types", "[\"wwdocument\"]")
                    .withProperty("isLatest", true)
                    .withProperty("rev", 1)
                    .withProperty("wwdocument_title", "origTitle")
                    .withProperty("wwdocument_date", "2013")
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

    instance.replace("wwdocuments", UUID.fromString(id),
            jsnO(
                    "^rev", jsn(1),
                    "title", jsn("newTitle"),
                    "date", jsn("1234")
            ), "");

    verify(mockIndex, times(1)).remove(removeNode);
    verify(mockIndex, times(1)).add(addNode, "tim_id", id);
    verify(mockIndex, times(1)).add(addNode, "displayName", "newTitle (1234)");
  }

  @Test
  public void crudServiceInvokesIndexDescriptionRemoveFromFulltextIndexForWwDocumentsOnDelete() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withTimId(id)
                    .withProperty("types", "[\"document\", \"wwdocument\"]")
                    .withProperty("isLatest", true)
                    .withProperty("rev", 1)
                    .withProperty("wwdocument_title", "origTitle")
                    .withIncomingRelation("VERSION_OF", "orig")
            )
            .withVertex("orig", v -> v
                    .withTimId(id)
                    .withProperty("types", "[\"document\", \"wwdocument\"]")
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


    instance.delete("wwdocuments", UUID.fromString(id), "");


    verify(mockIndex, times(1)).remove(removeNode);
    verify(mockIndex, never()).add(any(), any(), any());
  }



  @Test
  public void crudServiceInvokesIndexDescriptionAddIndexedSortPropertiesForWwDocumentsOnCreate() throws Exception {
    Graph graph = newGraph().build();

    TinkerpopJsonCrudService instance = newJsonCrudService()
      .withChangeListener(new DenormalizedSortFieldUpdater(new IndexDescriptionFactory()))
      .forGraph(graph);

    instance.create("wwdocuments", jsnO(), "");

    Vertex vertex = graph.vertices().next();

    assertThat(vertex.property("wwdocument_creator_sort").value(), equalTo(""));
    assertThat(vertex.property("modified_sort").value(), instanceOf(Long.class));
  }

  @Test
  public void crudServiceInvokesIndexDescriptionAddIndexedSortPropertiesForWwDocumentsOnUpdate() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withTimId(id)
                    .withProperty("types", "[\"wwdocument\"]")
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

    instance.replace("wwdocuments", UUID.fromString(id), jsnO("^rev", jsn(1)), "");


    Vertex vertex = graph.traversal().V().has("tim_id", id).has("isLatest", true).next();

    MatcherAssert.assertThat(vertex.property("wwdocument_creator_sort").value(), equalTo(""));
    MatcherAssert.assertThat(vertex.property("modified_sort").value(), instanceOf(Long.class));
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
