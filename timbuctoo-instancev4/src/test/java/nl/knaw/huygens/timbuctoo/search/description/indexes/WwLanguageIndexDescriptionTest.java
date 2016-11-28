package nl.knaw.huygens.timbuctoo.search.description.indexes;


import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.search.description.indexes.MockIndexUtil.makeIndexMocks;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WwLanguageIndexDescriptionTest {


  @Test
  public void addToFulltextIndexCorrectlyInvokesLegacyIndexerService() {
    final String timId = "123";
    String testValue = "testValue";
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

}
