package nl.knaw.huygens.timbuctoo.search.description.indexes;


import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockIndexUtil {

  public static List<Object> makeIndexMocks(Vertex vertex, String timId) {
    IndexManager mockIndexManager = mock(IndexManager.class);
    IndexHits mockIndexHits = mock(IndexHits.class);

    GraphDatabaseService mockDatabaseService = mock(GraphDatabaseService.class);
    Index mockIndex = mock(Index.class);
    Node removeNode = mock(Node.class);
    Node addNode = mock(Node.class);

    when(mockIndexHits.hasNext()).thenReturn(true).thenReturn(false);
    when(mockIndexHits.next()).thenReturn(removeNode);
    given(mockIndex.get("tim_id", timId)).willReturn(mockIndexHits);
    given(mockIndexManager.forNodes(anyString(), anyMap())).willReturn(mockIndex);
    given(mockDatabaseService.index()).willReturn(mockIndexManager);
    given(mockDatabaseService.getNodeById((long) vertex.id())).willReturn(addNode);

    return Lists.newArrayList(
            mockDatabaseService,
            mockIndex,
            removeNode,
            addNode
    );
  }
}
