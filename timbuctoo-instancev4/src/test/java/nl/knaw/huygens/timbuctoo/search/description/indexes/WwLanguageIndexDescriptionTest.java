package nl.knaw.huygens.timbuctoo.search.description.indexes;


import nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudService;
import nl.knaw.huygens.timbuctoo.crud.changelistener.FulltextIndexChangeListener;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import java.util.List;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.crud.JsonCrudServiceBuilder.newJsonCrudService;
import static nl.knaw.huygens.timbuctoo.search.description.indexes.MockIndexUtil.makeIndexMocks;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WwLanguageIndexDescriptionTest {


  @Test
  public void addToFulltextIndexCorrectlyInvokesLegacyIndexerService() {
    final String timId = "123";
    String testValue = "testValue";
    String testType = "testType";
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withVre("ww")
                    .withType("language")
                    .withTimId(timId)
                    .withProperty("wwlanguage_name", testValue)
            )
            .build();

    WwLanguageIndexDescription instance = new WwLanguageIndexDescription();
    Vertex vertex = graph.traversal().V().toList().get(0);

    List<Object> mocks = makeIndexMocks(vertex, timId);

    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) mocks.get(0);
    Index mockIndex = (Index) mocks.get(1);
    Node removeNode = (Node) mocks.get(2);
    Node addNode = (Node) mocks.get(3);

    instance.addToFulltextIndex(vertex, mockDatabaseService);

    verify(mockIndex, times(1)).remove(removeNode);
    verify(mockIndex, times(1)).add(addNode, "tim_id", timId);
    verify(mockIndex, times(1)).add(addNode, "displayName", testValue);
  }


  @Test
  public void crudServiceInvokesIndexDescriptionAddToFulltextIndexForWwLanguagesOnCreate() throws Exception {
    Graph graph = newGraph().build();

    List<Object> mocks = makeIndexMocks();

    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) mocks.get(0);
    Index mockIndex = (Index) mocks.get(1);
    Node addNode = (Node) mocks.get(3);


    TinkerpopJsonCrudService instance = newJsonCrudService()
            .withChangeListener(new FulltextIndexChangeListener(mockDatabaseService, new IndexDescriptionFactory()))
            .forGraph(graph);

    instance.create("wwlanguages", jsnO(
            "name", jsn("testValue")
    ), "");
    Vertex vertex = graph.vertices().next();


    verify(mockIndex, times(1)).add(addNode, "tim_id", vertex.property("tim_id").value());
    verify(mockIndex, times(1)).add(addNode, "displayName", "testValue");
  }

  @Test
  public void crudServiceInvokesIndexDescriptionAddToFulltextIndexForWwLanguagesOnUpdate() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withTimId(id)
                    .withProperty("types", "[\"language\", \"wwlanguage\"]")
                    .withProperty("isLatest", true)
                    .withProperty("rev", 1)
                    .withProperty("wwlanguage_name", "origValue")
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

    instance.replace("wwlanguages", UUID.fromString(id),
            jsnO(
                    "^rev", jsn(1),
                    "name", jsn("newValue")
            ), "");

    verify(mockIndex, times(1)).remove(removeNode);
    verify(mockIndex, times(1)).add(addNode, "tim_id", id);
    verify(mockIndex, times(1)).add(addNode, "displayName", "newValue");
  }

  @Test
  public void crudServiceInvokesIndexDescriptionRemoveFromFulltextIndexForWwLanguagesOnDelete() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withTimId(id)
                    .withProperty("types", "[\"language\", \"wwlanguage\"]")
                    .withProperty("isLatest", true)
                    .withProperty("rev", 1)
                    .withProperty("wwlanguage_name", "origValue")
                    .withIncomingRelation("VERSION_OF", "orig")
            )
            .withVertex("orig", v -> v
                    .withTimId(id)
                    .withProperty("types", "[\"language\", \"wwlanguage\"]")
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


    instance.delete("wwlanguages", UUID.fromString(id), "");


    verify(mockIndex, times(1)).remove(removeNode);
    verify(mockIndex, never()).add(any(), any(), any());
  }
}
