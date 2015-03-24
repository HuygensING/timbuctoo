package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
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

public class Neo4JLowLevelAPI {
  public static final String RELATIONSHIP_ID_INDEX = "RelationShip id";
  private final GraphDatabaseService db;

  public Neo4JLowLevelAPI(GraphDatabaseService db) {
    this.db = db;
  }

  public int getRevisionProperty(PropertyContainer propertyContainer) {
    return propertyContainer.hasProperty(REVISION_PROPERTY_NAME) ? //
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

      if (!iterator.hasNext()) {
        transaction.success();
        return null;
      }

      Node nodeWithHighestRevision = iterator.next();

      for (; iterator.hasNext();) {
        Node next = iterator.next();

        if (getRevisionProperty(next) > getRevisionProperty(nodeWithHighestRevision)) {
          nodeWithHighestRevision = next;
        }
      }

      transaction.success();
      return nodeWithHighestRevision;
    }
  }

  public <T extends Entity> Node getNodeWithRevision(Class<T> type, String id, int revision) {
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Node> iterator = findByProperty(type, ID_PROPERTY_NAME, id);

      if (!iterator.hasNext()) {
        transaction.success();
        return null;
      }

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

  private <T extends Entity> ResourceIterator<Node> findByProperty(Class<T> type, String propertyName, String id) {
    Label internalNameLabel = DynamicLabel.label(TypeNames.getInternalName(type));
    ResourceIterable<Node> foundNodes = db.findNodesByLabelAndProperty(internalNameLabel, propertyName, id);

    ResourceIterator<Node> iterator = foundNodes.iterator();
    return iterator;
  }

  public Relationship getLatestRelationship(String id) {
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Relationship> iterator = getFromIndex(id);
      if (!iterator.hasNext()) {
        transaction.success();
        return null;
      }
      Relationship relationshipWithHighestRevision = iterator.next();

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
      for (; iterator.hasNext();) {
        Relationship next = iterator.next();
        if (getRevisionProperty(next) == revision) {
          transaction.success();
          return next;
        }
      }
      transaction.success();
      return null;
    }
  }

  private ResourceIterator<Relationship> getFromIndex(String id) {
    Index<Relationship> index = db.index().forRelationships(RELATIONSHIP_ID_INDEX);

    IndexHits<Relationship> indexHits = index.get(ID_PROPERTY_NAME, id);

    ResourceIterator<Relationship> iterator = indexHits.iterator();
    return iterator;
  }
}
