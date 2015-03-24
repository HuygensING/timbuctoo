package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.DomainEntity.PID;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.google.common.collect.Maps;

public class RelationshipMockBuilder {
  private Node endNode;
  private RelationshipType relationshipType;
  private Node startNode;
  private Map<String, Object> properties;

  private RelationshipMockBuilder() {
    properties = Maps.newHashMap();

  }

  public static RelationshipMockBuilder aRelationship() {
    return new RelationshipMockBuilder();
  }

  public Relationship build() {
    Relationship relationshipMock = mock(Relationship.class);
    addProperties(relationshipMock);
    when(relationshipMock.getEndNode()).thenReturn(endNode);
    when(relationshipMock.getStartNode()).thenReturn(startNode);
    when(relationshipMock.getType()).thenReturn(relationshipType);
    return relationshipMock;
  }

  private void addProperties(Relationship relationshipMock) {
    when(relationshipMock.getPropertyKeys()).thenReturn(properties.keySet());
    for (Entry<String, Object> entry : properties.entrySet()) {
      String key = entry.getKey();
      when(relationshipMock.getProperty(key)).thenReturn(entry.getValue());
      when(relationshipMock.hasProperty(key)).thenReturn(true);
    }
  }

  public RelationshipMockBuilder withEndNode(Node endNode) {
    this.endNode = endNode;
    return this;
  }

  public RelationshipMockBuilder withType(RelationshipType relationshipType) {
    this.relationshipType = relationshipType;
    return this;
  }

  public RelationshipMockBuilder withStartNode(Node startNode) {
    this.startNode = startNode;
    return this;
  }

  public RelationshipMockBuilder withAPID() {
    return withProperty(PID, "pid");
  }

  public RelationshipMockBuilder withRevision(int revision) {
    return withProperty(REVISION_PROPERTY_NAME, revision);
  }

  public RelationshipMockBuilder withProperty(String key, Object value) {
    properties.put(key, value);
    return this;
  }
}
