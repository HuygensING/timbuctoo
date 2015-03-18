package nl.knaw.huygens.timbuctoo.storage.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class RelationshipDuplicator {

  public RelationshipDuplicator(GraphDatabaseService db) {
    // TODO Auto-generated constructor stub
  }

  public void saveDuplicate(Relationship relationship) {
    Node startNode = relationship.getStartNode();
    Node endNode = relationship.getEndNode();
    RelationshipType type = relationship.getType();

    Relationship duplicate = startNode.createRelationshipTo(endNode, type);

    addPropertiesToDuplicate(duplicate, relationship);
  }

  private void addPropertiesToDuplicate(Relationship duplicate, Relationship relationship) {
    for (String key : relationship.getPropertyKeys()) {
      duplicate.setProperty(key, relationship.getProperty(key));
    }
  }

}
