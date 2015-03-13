package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Mockito.mock;

import org.neo4j.graphdb.Relationship;

public class RelationshipBuilder {
  private RelationshipBuilder() {

  }

  public static RelationshipBuilder aRelationship() {
    return new RelationshipBuilder();
  }

  public Relationship build() {
    return mock(Relationship.class);
  }
}
