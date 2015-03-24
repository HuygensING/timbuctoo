package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.apache.commons.lang.StringUtils;
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
import org.neo4j.helpers.Strings;

import com.google.common.base.Objects;

public class Neo4JStorage {

  public static final String RELATIONSHIP_ID_INDEX = "RelationShip id";
  private GraphDatabaseService db;
  private PropertyContainerConverterFactory propertyContainerConverterFactory;
  private final NodeDuplicator nodeDuplicator;
  private RelationshipDuplicator relationshipDuplicator;

  public Neo4JStorage(GraphDatabaseService db, PropertyContainerConverterFactory propertyContainerConverterFactory) {
    this(db, propertyContainerConverterFactory, new NodeDuplicator(db), new RelationshipDuplicator(db));
  }

  public Neo4JStorage(GraphDatabaseService db, PropertyContainerConverterFactory propertyContainerConverterFactory, NodeDuplicator nodeDuplicator, RelationshipDuplicator relationshipDuplicator) {
    this.db = db;
    this.propertyContainerConverterFactory = propertyContainerConverterFactory;
    this.nodeDuplicator = nodeDuplicator;
    this.relationshipDuplicator = relationshipDuplicator;
  }

  public <T extends DomainEntity> T getDomainEntityRevision(Class<T> type, String id, int revision) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node node = getRevisionNode(type, id, revision);

      if (node == null) {
        transaction.success();
        return null;
      }

      try {
        NodeConverter<T> nodeConverter = propertyContainerConverterFactory.createForType(type);
        T entity = nodeConverter.convertToEntity(node);

        // Needed to mimic the separate collections used in the Mongo storage.
        // getRevision only returns objects with a PID.
        if (!hasPID(entity)) {
          transaction.success();
          return null;
        }

        transaction.success();
        return entity;
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      } catch (InstantiationException e) {
        transaction.failure();
        throw new StorageException(e);
      }
    }
  }

  public <T extends Relation> T getRelationRevision(Class<T> type, String id, int revision) throws StorageException {
    try (Transaction transaction = db.beginTx()) {

      Relationship relationship = getRevisionRelationship(type, id, revision);

      if (relationship == null) {
        transaction.success();
        return null;
      }

      try {
        RelationshipConverter<T> converter = propertyContainerConverterFactory.createForRelation(type);
        T entity = converter.convertToEntity(relationship);

        if (!hasPID(entity)) {
          transaction.success();
          return null;
        }

        transaction.success();
        return entity;
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      } catch (InstantiationException e) {
        transaction.failure();
        throw new StorageException(e);
      }

    }
  }

  private <T extends DomainEntity> boolean hasPID(T entity) {
    return !Strings.isBlank(entity.getPid());
  }

  private ResourceIterator<Relationship> getFromIndex(String id) {
    Index<Relationship> index = db.index().forRelationships(RELATIONSHIP_ID_INDEX);

    IndexHits<Relationship> indexHits = index.get(ID_PROPERTY_NAME, id);

    ResourceIterator<Relationship> iterator = indexHits.iterator();
    return iterator;
  }

  private Relationship getLatestRelationship(String id) {
    ResourceIterator<Relationship> iterator = getFromIndex(id);
    if (!iterator.hasNext()) {
      return null;
    }
    Relationship relationshipWithHighestRevision = iterator.next();

    for (; iterator.hasNext();) {
      Relationship next = iterator.next();

      if (getRevisionProperty(next) > getRevisionProperty(relationshipWithHighestRevision)) {
        relationshipWithHighestRevision = next;
      }
    }
    return relationshipWithHighestRevision;
  }

  private int getRevisionProperty(PropertyContainer propertyContainer) {
    return (int) propertyContainer.getProperty(REVISION_PROPERTY_NAME);
  }

  public <T extends DomainEntity> void setDomainEntityPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node node = getLatestById(type, id);

      if (node == null) {
        transaction.failure();
        throw new NoSuchEntityException(type, id);
      }

      try {
        NodeConverter<T> converter = propertyContainerConverterFactory.createForType(type);
        T entity = converter.convertToEntity(node);

        validateEntityHasNoPID(type, id, pid, transaction, entity);

        entity.setPid(pid);
        converter.addValuesToPropertyContainer(node, entity);

        // FIXME functionality should be part of the repository class.
        nodeDuplicator.saveDuplicate(node);

        transaction.success();
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      } catch (InstantiationException e) {
        transaction.failure();
        throw new StorageException(e);
      }
    }
  }

  public <T extends Relation> void setRelationPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException {
    try (Transaction transaction = db.beginTx()) {
      Relationship relationship = getLatestRelationship(id);

      if (relationship == null) {
        transaction.failure();
        throw new NoSuchEntityException(type, id);
      }

      try {
        RelationshipConverter<T> converter = propertyContainerConverterFactory.createForRelation(type);

        T entity = converter.convertToEntity(relationship);

        validateEntityHasNoPID(type, id, pid, transaction, entity);

        entity.setPid(pid);

        converter.addValuesToPropertyContainer(relationship, entity);

        relationshipDuplicator.saveDuplicate(relationship);

        transaction.success();
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      } catch (InstantiationException e) {
        transaction.failure();
        throw new StorageException(e);
      }
    }
  }

  private <T extends DomainEntity> void validateEntityHasNoPID(Class<T> type, String id, String pid, Transaction transaction, T entity) {
    if (!StringUtils.isBlank(entity.getPid())) {
      transaction.failure();
      throw new IllegalStateException(String.format("%s with %s already has a pid: %s", type.getSimpleName(), id, pid));
    }
  }

  /* *************************************************************************************
   * Low level API
   * ************************************************************************************/
  /**
   * Retrieves all of {@code type} with {@code id} 
   * and returns the one with the highest revision number.
   * @param type the type to get the latest from
   * @param id the id to get the latest from
   * @return the node of type and id with the highest revision.
   */
  private <T extends Entity> Node getLatestById(Class<T> type, String id) {
    ResourceIterator<Node> iterator = findByProperty(type, ID_PROPERTY_NAME, id);

    if (!iterator.hasNext()) {
      return null;
    }

    Node nodeWithHighestRevision = iterator.next();

    for (; iterator.hasNext();) {
      Node next = iterator.next();

      if (getRevisionProperty(next) > getRevisionProperty(nodeWithHighestRevision)) {
        nodeWithHighestRevision = next;
      }
    }

    return nodeWithHighestRevision;
  }

  private <T extends Relation> Relationship getRevisionRelationship(Class<T> type, String id, int revision) {
    ResourceIterator<Relationship> iterator = getFromIndex(id);
    for (; iterator.hasNext();) {
      Relationship next = iterator.next();
      if (getRevisionProperty(next) == revision) {
        return next;
      }
    }

    return null;
  }

  private <T extends Entity> Node getRevisionNode(Class<T> type, String id, int revision) {
    ResourceIterator<Node> iterator = findByProperty(type, ID_PROPERTY_NAME, id);

    if (!iterator.hasNext()) {
      return null;
    }

    Node nodeWithRevision = null;

    for (; iterator.hasNext();) {
      Node next = iterator.next();

      if (Objects.equal(revision, getRevisionProperty(next))) {
        nodeWithRevision = next;
        break;
      }
    }

    return nodeWithRevision;
  }

  private <T extends Entity> ResourceIterator<Node> findByProperty(Class<T> type, String propertyName, String id) {
    Label internalNameLabel = DynamicLabel.label(TypeNames.getInternalName(type));
    ResourceIterable<Node> foundNodes = db.findNodesByLabelAndProperty(internalNameLabel, propertyName, id);

    ResourceIterator<Node> iterator = foundNodes.iterator();
    return iterator;
  }

}
