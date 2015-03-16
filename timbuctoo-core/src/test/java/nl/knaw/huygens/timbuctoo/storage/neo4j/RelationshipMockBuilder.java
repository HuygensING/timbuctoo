package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.neo4j.graphdb.Relationship;

public class RelationshipMockBuilder {
  private int revision;

  private RelationshipMockBuilder() {

  }

  public static RelationshipMockBuilder aRelationship() {
    return new RelationshipMockBuilder();
  }

  public Relationship build() {
    Relationship relationshipMock = mock(Relationship.class);
    when(relationshipMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(revision);
    return relationshipMock;
  }

  public RelationshipMockBuilder withRevision(int revision) {
    this.revision = revision;
    return this;
  }
}
