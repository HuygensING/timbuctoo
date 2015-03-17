package nl.knaw.huygens.timbuctoo.storage.neo4j;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Only use this class from within a Neo4J transaction. 
 */
public class NodeDuplicator {

  private final GraphDatabaseService db;
  static final DynamicRelationshipType VERSION_OF_RELATIONSHIP_TYPE = DynamicRelationshipType.withName("versionOf");

  public NodeDuplicator(GraphDatabaseService db) {
    this.db = db;
  }

  /**
   * Duplicates the node and all its relationships and saves it to the database.
   * @param original the node to duplicate.
   */
  public void saveDuplicate(Node original) {
    Node duplicate = db.createNode();

    addLabelsToDuplicate(duplicate, original);
    addPropertiesToDuplicate(duplicate, original);
    addRelationshipsToDuplicate(duplicate, original);
    addVersionOfRelationToDuplicate(duplicate, original);
  }

  private void addLabelsToDuplicate(Node duplicatedNode, Node orginal) {
    for (Label label : orginal.getLabels()) {
      duplicatedNode.addLabel(label);
    }
  }

  private void addPropertiesToDuplicate(Node duplicatedNode, Node original) {
    for (String propertyKey : original.getPropertyKeys()) {
      duplicatedNode.setProperty(propertyKey, original.getProperty(propertyKey));
    }
  }

  private void addRelationshipsToDuplicate(Node duplicatedNode, Node original) {
    duplicateRelationships(duplicatedNode, original, new IncommingRelationshipDuplicator());
    duplicateRelationships(duplicatedNode, original, new OutgoingRelationshipDuplicator());

  }

  private void duplicateRelationships(Node duplicatedNode, Node original, RelationshipDuplicator relDuplicator) {
    for (Relationship relationship : original.getRelationships(relDuplicator.getDirection())) {
      relDuplicator.addRelationshipToNode(duplicatedNode, relationship);
    }
  }

  private void addVersionOfRelationToDuplicate(Node duplicateNode, Node original) {
    duplicateNode.createRelationshipTo(original, VERSION_OF_RELATIONSHIP_TYPE);
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
