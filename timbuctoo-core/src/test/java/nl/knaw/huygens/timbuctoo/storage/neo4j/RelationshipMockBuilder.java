package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class RelationshipMockBuilder {
  private int revision;
  private Node endNode;
  private DynamicRelationshipType relationshipType;
  private Node startNode;

  private RelationshipMockBuilder() {

  }

  public static RelationshipMockBuilder aRelationship() {
    return new RelationshipMockBuilder();
  }

  public Relationship build() {
    Relationship relationshipMock = mock(Relationship.class);
    when(relationshipMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(revision);
    when(relationshipMock.getEndNode()).thenReturn(endNode);
    when(relationshipMock.getStartNode()).thenReturn(startNode);
    when(relationshipMock.getType()).thenReturn(relationshipType);
    return relationshipMock;
  }

  public RelationshipMockBuilder withRevision(int revision) {
    this.revision = revision;
    return this;
  }

  public RelationshipMockBuilder withEndNode(Node endNode) {
    this.endNode = endNode;
    return this;
  }

  public RelationshipMockBuilder withType(DynamicRelationshipType relationshipType) {
    this.relationshipType = relationshipType;
    return this;
  }

  public RelationshipMockBuilder withStartNode(Node startNode) {
    this.startNode = startNode;
    return this;
  }
}
