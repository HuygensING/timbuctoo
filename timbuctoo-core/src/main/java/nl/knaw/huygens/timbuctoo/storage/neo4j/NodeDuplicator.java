package nl.knaw.huygens.timbuctoo.storage.neo4j;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Only use this class from within a Neo4J transaction. 
 */
public class NodeDuplicator {

  private final GraphDatabaseService db;

  public NodeDuplicator(GraphDatabaseService db) {
    this.db = db;
  }

  /**
   * Duplicates the node and all its relationships and saves it to the database.
   * @param node the node to duplicate.
   */
  public void saveDuplicate(Node node) {
    Node duplicate = db.createNode();

    addLabelsToDuplicate(duplicate, node);
    addPropertiesToDuplicate(duplicate, node);
    addRelationshipsToDuplicate(duplicate, node);
  }

  private void addLabelsToDuplicate(Node duplicatedNode, Node node) {
    for (Label label : node.getLabels()) {
      duplicatedNode.addLabel(label);
    }
  }

  private void addPropertiesToDuplicate(Node duplicatedNode, Node node) {
    for (String propertyKey : node.getPropertyKeys()) {
      duplicatedNode.setProperty(propertyKey, node.getProperty(propertyKey));
    }
  }

  private void addRelationshipsToDuplicate(Node duplicatedNode, Node node) {
    duplicateRelationships(duplicatedNode, node, new IncommingRelationshipDuplicator());
    duplicateRelationships(duplicatedNode, node, new OutgoingRelationshipDuplicator());

  }

  private void duplicateRelationships(Node duplicatedNode, Node node, RelationshipDuplicator relDuplicator) {
    for (Relationship relationship : node.getRelationships(relDuplicator.getDirection())) {
      relDuplicator.addRelationshipToNode(duplicatedNode, relationship);
    }
  }

  private static interface RelationshipDuplicator {
    Direction getDirection();

    void addRelationshipToNode(Node nodeToDuplicate, Relationship relationship);
  }

  private static class IncommingRelationshipDuplicator implements RelationshipDuplicator {

    @Override
    public Direction getDirection() {
      return Direction.INCOMING;
    }

    @Override
    public void addRelationshipToNode(Node duplicatedNode, Relationship relationship) {
      Node startNode = relationship.getStartNode();
      startNode.createRelationshipTo(duplicatedNode, relationship.getType());
    }
  }

  private static class OutgoingRelationshipDuplicator implements RelationshipDuplicator {

    @Override
    public Direction getDirection() {
      return Direction.OUTGOING;
    }

    @Override
    public void addRelationshipToNode(Node duplicatedNode, Relationship relationship) {
      Node endNode = relationship.getEndNode();
      duplicatedNode.createRelationshipTo(endNode, relationship.getType());
    }
  }
}
