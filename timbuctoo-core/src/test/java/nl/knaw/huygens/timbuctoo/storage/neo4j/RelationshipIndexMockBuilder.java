package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.helpers.collection.IteratorUtil;

import com.google.common.collect.Lists;

public class RelationshipIndexMockBuilder {
  private String id;
  private List<Relationship> relationships;
  private String indexName;

  private RelationshipIndexMockBuilder(String indexName) {
    this.indexName = indexName;
    relationships = Lists.newArrayList();
  }

  public static RelationshipIndexMockBuilder aRelationshipIndexForName(String indexName) {
    return new RelationshipIndexMockBuilder(indexName);
  }

  public RelationshipIndexMockBuilder containsForId(String id) {
    this.id = id;
    return this;
  }

  public RelationshipIndexMockBuilder containsNothingForId(String id) {
    this.id = id;
    return this;
  }

  public RelationshipIndexMockBuilder relationship(Relationship relationship) {
    addRelationship(relationship);
    return this;
  }

  public RelationshipIndexMockBuilder andRelationship(Relationship relationship) {
    addRelationship(relationship);
    return this;
  }

  private void addRelationship(Relationship relationship) {
    relationships.add(relationship);
  }

  public RelationshipIndex foundInDB(GraphDatabaseService dbMock) {
    RelationshipIndex indexMock = mock(RelationshipIndex.class);
    dbHasRelationshipIndexWithName(dbMock, indexMock);

    ResourceIterator<Relationship> relationshipIterator = IteratorUtil.asResourceIterator(relationships.iterator());
    @SuppressWarnings("unchecked")
    IndexHits<Relationship> indexHitsMock = mock(IndexHits.class);
    when(indexHitsMock.iterator()).thenReturn(relationshipIterator);
    when(indexMock.get(ID_PROPERTY_NAME, id)).thenReturn(indexHitsMock);

    return indexMock;
  }

  private RelationshipIndex dbHasRelationshipIndexWithName(GraphDatabaseService dbMock, RelationshipIndex indexMock) {
    IndexManager indexManagerMock = mock(IndexManager.class);

    when(indexManagerMock.forRelationships(indexName)).thenReturn(indexMock);
    when(dbMock.index()).thenReturn(indexManagerMock);

    return indexMock;
  }

}
