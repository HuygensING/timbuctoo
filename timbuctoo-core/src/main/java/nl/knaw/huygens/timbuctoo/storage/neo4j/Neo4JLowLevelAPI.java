package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_ID;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_ID;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.PropertyContainerHelper.getRevisionProperty;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.PropertyNotIndexedException.propertyHasNoIndex;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SystemRelationshipType.VERSION_OF;
import static org.neo4j.graphdb.Direction.INCOMING;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

class Neo4JLowLevelAPI {
  private final GraphDatabaseService db;
  private final RelationshipIndexes relationshipIndexes;
  private final GlobalGraphOperations globalGraphOperations;

  public Neo4JLowLevelAPI(GraphDatabaseService db) {
    this(db, new RelationshipIndexes(db), GlobalGraphOperations.at(db));
  }

  Neo4JLowLevelAPI(GraphDatabaseService db, RelationshipIndexes relationshipIndexesMock, GlobalGraphOperations globalGraphOperations) {
    this.db = db;
    this.relationshipIndexes = relationshipIndexesMock;
    this.globalGraphOperations = globalGraphOperations;
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

  public ResourceIterator<Node> getNodesOfType(Class<? extends Entity> type) {
    try (Transaction transaction = db.beginTx()) {

      ResourceIterator<Node> allNodesWithLabel = db.findNodes(labelFor(type));
      transaction.success();
      return allNodesWithLabel;
    }
  }

  private Label labelFor(Class<? extends Entity> type) {
    return DynamicLabel.label(TypeNames.getInternalName(type));
  }

  private <T extends Entity> ResourceIterator<Node> findByProperty(Class<T> type, String propertyName, String value) {
    Label internalNameLabel = labelFor(type);
    ResourceIterator<Node> foundNodes = db.findNodes(internalNameLabel, propertyName, value);

    return foundNodes;
  }

  public <T extends Entity> List<Node> getNodesWithId(Class<T> type, String id) {
    List<Node> nodes = Lists.newArrayList();
    ResourceIterator<Node> iterator = findByProperty(type, ID_PROPERTY_NAME, id);

    for (; iterator.hasNext();) {
      nodes.add(iterator.next());
    }

    return nodes;
  }

  public Relationship getLatestRelationshipById(String id) {
    return relationshipIndexes.getLatestRelationshipById(id);
  }

  public <T extends Relation> Relationship getRelationshipWithRevision(Class<T> relationType, String id, int revision) {
    return relationshipIndexes.getRelationshipWithRevision(id, revision);
  }

  public void addRelationship(Relationship relationship, String id) {
    relationshipIndexes.indexByField(relationship, ID_PROPERTY_NAME, id);
    relationshipIndexes.indexByField(relationship, SOURCE_ID, getNodeId(relationship.getStartNode()));
    relationshipIndexes.indexByField(relationship, TARGET_ID, getNodeId(relationship.getEndNode()));
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

  public Relationship findRelationshipByProperty(Class<? extends Relation> type, String propertyName, String propertyValue) {
    if (!relationshipIndexes.containsIndexFor(propertyName)) {
      throw propertyHasNoIndex(type, propertyName);
    }

    List<Relationship> foundRelationships = relationshipIndexes.getRelationshipsBy(propertyName, propertyValue);

    for (Relationship relationship : foundRelationships) {
      if (relationshipIndexes.isLatestVersion(relationship)) {
        return relationship;
      }
    }

    return null;
  }

  public long countNodesWithLabel(Label label) {
    try (Transaction transaction = db.beginTx()) {
      Set<Node> foundNodes = Sets.newHashSet(db.findNodes(label));

      Set<Node> latestNodes = Sets.filter(foundNodes, new Predicate<Node>() {

        @Override
        public boolean apply(Node node) {
          return !node.hasRelationship(INCOMING, VERSION_OF);
        }
      });

      int count = latestNodes.size();

      transaction.success();
      return count;
    }
  }

  public long countRelationships() {
    try (Transaction transaction = db.beginTx()) {
      Set<Relationship> foundRelationships = Sets.newHashSet(globalGraphOperations.getAllRelationships().iterator());

      Set<Relationship> latestRelationships = Sets.filter(foundRelationships, new Predicate<Relationship>() {
        private Set<Object> uniqueRelIds = Sets.newHashSet();

        @Override
        public boolean apply(Relationship relationship) {
          Object id = relationship.hasProperty(ID_PROPERTY_NAME) ? relationship.getProperty(ID_PROPERTY_NAME) : null;
          if (!uniqueRelIds.contains(id)) {
            uniqueRelIds.add(id);
            return true;
          }

          return false;
        }
      });

      transaction.success();
      return latestRelationships.size();
    }
  }

  public List<Relationship> getRelationshipsByNodeId(String id) {
    List<Relationship> relationships = Lists.newArrayList();
    relationships.addAll(relationshipIndexes.getRelationshipsBy(SOURCE_ID, id));
    relationships.addAll(relationshipIndexes.getRelationshipsBy(TARGET_ID, id));

    return relationships;
  }

  public Relationship findLatestRelationshipFor(Class<? extends Relation> relationType, String sourceId, String targetId, String relationTypeId) {
    return relationshipIndexes.findLatestRelationshipFor(sourceId, targetId, relationTypeId);
  }

}
