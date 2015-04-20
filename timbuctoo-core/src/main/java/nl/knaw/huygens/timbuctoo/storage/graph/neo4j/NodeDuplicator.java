package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.SystemRelationshipType.VERSION_OF;

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
  private final Neo4JLowLevelAPI neo4jLowLevelAPI;

  public NodeDuplicator(GraphDatabaseService db, Neo4JLowLevelAPI neo4jLowLevelAPI) {
    this.db = db;
    this.neo4jLowLevelAPI = neo4jLowLevelAPI;
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

    neo4jLowLevelAPI.index(duplicate);
  }

  private void addLabelsToDuplicate(Node duplicatedNode, Node original) {
    for (Label label : original.getLabels()) {
      duplicatedNode.addLabel(label);
    }
  }

  private void addPropertiesToDuplicate(Node duplicatedNode, Node original) {
    for (String propertyKey : original.getPropertyKeys()) {
      duplicatedNode.setProperty(propertyKey, original.getProperty(propertyKey));
    }
  }

  private void addRelationshipsToDuplicate(Node duplicatedNode, Node original) {
    duplicateRelationships(duplicatedNode, original, new IncomingRelationshipDuplicator());
    duplicateRelationships(duplicatedNode, original, new OutgoingRelationshipDuplicator());

  }

  private void duplicateRelationships(Node duplicatedNode, Node original, RelationshipDuplicator relDuplicator) {
    for (Relationship relationship : original.getRelationships(relDuplicator.getDirection())) {
      relDuplicator.addRelationshipToNode(duplicatedNode, relationship);
    }
  }

  private void addVersionOfRelationToDuplicate(Node duplicateNode, Node original) {
    duplicateNode.createRelationshipTo(original, VERSION_OF);
  }

  private static interface RelationshipDuplicator {
    Direction getDirection();

    void addRelationshipToNode(Node duplicatedNode, Relationship relationship);
  }

  private static class IncomingRelationshipDuplicator implements RelationshipDuplicator {

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
