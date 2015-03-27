package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import org.neo4j.graphdb.Relationship;

public class RelationshipIndexes {

  public boolean containsIndexFor(String propertyName) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  public List<Relationship> getRelationshipsBy(String property, String propertyValue) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  public boolean isLatestVersion(Relationship relationship) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
