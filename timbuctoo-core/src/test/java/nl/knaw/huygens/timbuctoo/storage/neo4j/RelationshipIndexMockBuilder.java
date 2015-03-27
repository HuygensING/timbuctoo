package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.helpers.collection.IteratorUtil;

import com.google.common.collect.Lists;

public class RelationshipIndexMockBuilder {
  private Object value;
  private List<Relationship> relationships;
  private String propertyName;

  private RelationshipIndexMockBuilder() {
    relationships = Lists.newArrayList();
  }

  public static RelationshipIndexMockBuilder aRelationshipIndex() {
    return new RelationshipIndexMockBuilder();
  }

  public RelationshipIndexMockBuilder containsForPropertyWithValue(String propertyName, Object propertyValue) {
    this.propertyName = propertyName;
    this.value = propertyValue;

    return this;
  }

  public RelationshipIndexMockBuilder containsNothingForPropertyWithValue(String propertyName, String propertyValue) {
    this.propertyName = propertyName;
    this.value = propertyValue;

    return this;
  }

  public RelationshipIndexMockBuilder containsForId(String id) {
    containsForPropertyWithValue(ID_PROPERTY_NAME, id);
    return this;
  }

  public RelationshipIndexMockBuilder containsNothingForId(String id) {
    containsForPropertyWithValue(ID_PROPERTY_NAME, id);
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

  public RelationshipIndex build() {
    RelationshipIndex indexMock = mock(RelationshipIndex.class);

    ResourceIterator<Relationship> relationshipIterator = IteratorUtil.asResourceIterator(relationships.iterator());
    @SuppressWarnings("unchecked")
    IndexHits<Relationship> indexHitsMock = mock(IndexHits.class);
    when(indexHitsMock.iterator()).thenReturn(relationshipIterator);
    when(indexMock.get(propertyName, value)).thenReturn(indexHitsMock);

    return indexMock;
  }

}
