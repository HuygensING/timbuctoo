package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;

import com.google.common.collect.Maps;

public class IndexManagerMockBuilder {
  private Map<String, RelationshipIndex> relationshipIndexMap;

  private IndexManagerMockBuilder() {
    relationshipIndexMap = Maps.newHashMap();
  }

  public static IndexManagerMockBuilder anIndexManager() {
    return new IndexManagerMockBuilder();
  }

  public IndexManagerMockBuilder containsRelationshipIndexWithName(RelationshipIndex index, String name) {
    relationshipIndexMap.put(name, index);
    return this;
  }

  public void foundInDB(GraphDatabaseService dbMock) {
    IndexManager indexManagerMock = mock(IndexManager.class);
    when(dbMock.index()).thenReturn(indexManagerMock);

    for (Entry<String, RelationshipIndex> entry : relationshipIndexMap.entrySet()) {
      when(indexManagerMock.forRelationships(entry.getKey())).thenReturn(entry.getValue());
    }
  }
}
