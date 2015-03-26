package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_ID;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_ID;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SystemRelationshipType.VERSION_OF;
import static org.neo4j.graphdb.Direction.INCOMING;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import com.google.common.collect.Lists;

class Neo4JLowLevelAPI {
  static final String RELATIONSHIP_ID_INDEX = "Relationship id";
  static final String RELATIONSHIP_START_ID_INDEX = "Relationship start id";
  static final String RELATIONSHIP_END_ID_INDEX = "Relationship end id";
  private final GraphDatabaseService db;

  public Neo4JLowLevelAPI(GraphDatabaseService db) {
    this.db = db;
  }

  public static int getRevisionProperty(PropertyContainer propertyContainer) {
    return propertyContainer != null && propertyContainer.hasProperty(REVISION_PROPERTY_NAME) ? //
    (int) propertyContainer.getProperty(REVISION_PROPERTY_NAME)
        : 0;
  }

  /**
   * Retrieves all of {@code type} with {@code id} 
   * and returns the one with the highest revision number.
   * @param type the type to get the latest from
   * @param id the id to get the latest from
   * @return the node of type and id with the highest revision.
   */
  public <T extends Entity> Node getLatestNodeById(Class<T> type, String id) {
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Node> iterator = findByProperty(type, ID_PROPERTY_NAME, id);

      Node nodeWithHighestRevision = null;

      for (; iterator.hasNext();) {
        Node next = iterator.next();

        if (newNodeHasHigherRevision(nodeWithHighestRevision, next) && !next.hasRelationship(INCOMING, VERSION_OF)) {
          nodeWithHighestRevision = next;
        }
      }

      transaction.success();
      return nodeWithHighestRevision;
    }
  }

  private boolean newNodeHasHigherRevision(Node nodeWithHighestRevision, Node next) {
    return getRevisionProperty(next) > getRevisionProperty(nodeWithHighestRevision);
  }

  public <T extends Entity> Node getNodeWithRevision(Class<T> type, String id, int revision) {
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Node> iterator = findByProperty(type, ID_PROPERTY_NAME, id);

      Node nodeWithRevision = null;

      for (; iterator.hasNext();) {
        Node next = iterator.next();

        if (revision == getRevisionProperty(next)) {
          nodeWithRevision = next;
          break;
        }
      }

      transaction.success();
      return nodeWithRevision;
    }
  }

  private <T extends Entity> ResourceIterator<Node> findByProperty(Class<T> type, String propertyName, String value) {
    Label internalNameLabel = DynamicLabel.label(TypeNames.getInternalName(type));
    ResourceIterable<Node> foundNodes = db.findNodesByLabelAndProperty(internalNameLabel, propertyName, value);

    ResourceIterator<Node> iterator = foundNodes.iterator();
    return iterator;
  }

  public <T extends Entity> List<Node> getNodesWithId(Class<T> type, String id) {
    List<Node> nodes = Lists.newArrayList();
    ResourceIterator<Node> iterator = findByProperty(type, ID_PROPERTY_NAME, id);

    for (; iterator.hasNext();) {
      nodes.add(iterator.next());
    }

    return nodes;
  }

  public Relationship getLatestRelationship(String id) {
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Relationship> iterator = getFromIndex(id);

      Relationship relationshipWithHighestRevision = null;

      for (; iterator.hasNext();) {
        Relationship next = iterator.next();

        if (getRevisionProperty(next) > getRevisionProperty(relationshipWithHighestRevision)) {
          relationshipWithHighestRevision = next;
        }
      }
      transaction.success();
      return relationshipWithHighestRevision;
    }
  }

  public <T extends Relation> Relationship getRelationshipWithRevision(Class<T> relationType, String id, int revision) {
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Relationship> iterator = getFromIndex(id);

      Relationship relationshipWithRevsion = null;

      for (; iterator.hasNext();) {
        Relationship next = iterator.next();
        if (getRevisionProperty(next) == revision) {

          relationshipWithRevsion = next;
          break;
        }
      }
      transaction.success();
      return relationshipWithRevsion;
    }
  }

  private ResourceIterator<Relationship> getFromIndex(String id) {
    Index<Relationship> index = db.index().forRelationships(RELATIONSHIP_ID_INDEX);

    IndexHits<Relationship> indexHits = index.get(ID_PROPERTY_NAME, id);

    ResourceIterator<Relationship> iterator = indexHits.iterator();
    return iterator;
  }

  public void addRelationship(Relationship relationship, String id) {
    db.index().forRelationships(RELATIONSHIP_ID_INDEX).add(relationship, ID_PROPERTY_NAME, id);
    db.index().forRelationships(RELATIONSHIP_START_ID_INDEX).add(relationship, SOURCE_ID, getNodeId(relationship.getStartNode()));
    db.index().forRelationships(RELATIONSHIP_END_ID_INDEX).add(relationship, TARGET_ID, getNodeId(relationship.getEndNode()));
  }

  private Object getNodeId(Node node) {
    return node.getProperty(ID_PROPERTY_NAME);
  }

  /**
   * Finds the first node without incoming versionOf relationships. 
   * @param type the type of the label the node has to have
   * @param propertyName the name of the property that should contain the value
   * @param value the value the property should have
   * @return the first node found or null if none are found
   */
  public Node findNodeByProperty(Class<? extends Entity> type, String propertyName, String value) {
    try (Transaction transaction = db.beginTx()) {

      ResourceIterator<Node> foundNodes = findByProperty(type, propertyName, value);
      Node firstNodeWithoutIncommingVersionOfRelations = null;

      for (; foundNodes.hasNext();) {
        Node node = foundNodes.next();

        if (!node.hasRelationship(INCOMING, VERSION_OF)) {
          firstNodeWithoutIncommingVersionOfRelations = node;
          break;
        }

      }

      transaction.success();
      return firstNodeWithoutIncommingVersionOfRelations;
    }
  }

}
