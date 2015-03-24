package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;

import java.util.Iterator;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
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
  private final GraphDatabaseService db;
  private final PropertyContainerConverterFactory propertyContainerConverterFactory;
  private final NodeDuplicator nodeDuplicator;
  private final RelationshipDuplicator relationshipDuplicator;
  private final IdGenerator idGenerator;
  private final TypeRegistry typeRegistry;
  private final Neo4JLowLevelAPI neo4jLowLevelAPI;

  public Neo4JStorage(GraphDatabaseService db, PropertyContainerConverterFactory propertyContainerConverterFactory, TypeRegistry typeRegistry) {
    this(db, propertyContainerConverterFactory, new NodeDuplicator(db), new RelationshipDuplicator(db), new IdGenerator(), typeRegistry, new Neo4JLowLevelAPI(db));
  }

  public Neo4JStorage(GraphDatabaseService db, PropertyContainerConverterFactory propertyContainerConverterFactory, NodeDuplicator nodeDuplicator, RelationshipDuplicator relationshipDuplicator,
      IdGenerator idGenerator, TypeRegistry typeRegistry, Neo4JLowLevelAPI neo4jLowLevelAPI) {
    this.db = db;
    this.propertyContainerConverterFactory = propertyContainerConverterFactory;
    this.nodeDuplicator = nodeDuplicator;
    this.relationshipDuplicator = relationshipDuplicator;
    this.idGenerator = idGenerator;
    this.typeRegistry = typeRegistry;
    this.neo4jLowLevelAPI = neo4jLowLevelAPI;
  }

  public <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      removePID(entity);
      String id = addAdministrativeValues(type, entity);
      Node node = db.createNode();

      NodeConverter<? super T> compositeNodeConverter = propertyContainerConverterFactory.createCompositeForType(type);

      try {
        compositeNodeConverter.addValuesToPropertyContainer(node, entity);
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      }

      transaction.success();

      return id;
    }
  }

  public <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      try {
        String id = addAdministrativeValues(type, entity);

        NodeConverter<T> propertyContainerConverter = propertyContainerConverterFactory.createForType(type);
        Node node = db.createNode();

        propertyContainerConverter.addValuesToPropertyContainer(node, entity);

        transaction.success();
        return id;
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Relation> String addRelation(Class<T> type, Relation relation, Change change) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node source = getRelationPart(transaction, typeRegistry.getDomainEntityType(relation.getSourceType()), "Source", relation.getSourceId());
      Node target = getRelationPart(transaction, typeRegistry.getDomainEntityType(relation.getTargetType()), "Target", relation.getTargetId());
      Node relationTypeNode = getRelationPart(transaction, typeRegistry.getSystemEntityType(relation.getTypeType()), "RelationType", relation.getTypeId());

      RelationshipConverter<T> relationConverter = propertyContainerConverterFactory.createCompositeForRelation(type);

      String id = addAdministrativeValues(type, (T) relation);

      try {
        String relationTypeName = getRegularRelationName(relationTypeNode);
        Relationship relationship = source.createRelationshipTo(target, DynamicRelationshipType.withName(relationTypeName));

        relationConverter.addValuesToPropertyContainer(relationship, (T) relation);

        db.index().forRelationships(RELATIONSHIP_ID_INDEX).add(relationship, ID_PROPERTY_NAME, id);
        transaction.success();
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      } catch (InstantiationException | IllegalAccessException e) {
        transaction.failure();
        throw new StorageException(e);
      }

      return id;
    }
  }

  private String getRegularRelationName(Node relationTypeNode) throws ConversionException, InstantiationException, IllegalAccessException {
    NodeConverter<RelationType> relationTypeConverter = propertyContainerConverterFactory.createForType(RelationType.class);
    RelationType relationType = relationTypeConverter.convertToEntity(relationTypeNode);

    String relationTypeName = relationType.getRegularName();
    return relationTypeName;
  }

  private Node getRelationPart(Transaction transaction, Class<? extends Entity> type, String partName, String partId) throws StorageException {
    Node part = getLatestById(type, partId);
    if (part == null) {
      transaction.failure();
      throw new StorageException(createCannotFindString(partName, type, partId));
    }
    return part;
  }

  private String createCannotFindString(String relationPart, Class<? extends Entity> type, String id) {
    return String.format("%s of type \"%s\" with id \"%s\" could not be found.", relationPart, type, id);
  }

  /**
   * Adds the administrative values to the entity.
   * @param type the type to generate the id for
   * @param entity the entity to add the values to
   * @return the generated id
   */
  private <T extends Entity> String addAdministrativeValues(Class<T> type, T entity) {
    String id = idGenerator.nextIdFor(type);
    Change change = Change.newInternalInstance();

    entity.setCreated(change);
    entity.setModified(change);
    entity.setId(id);
    updateRevision(entity);

    return id;
  }

  private <T extends Entity> void updateRevision(T entity) {
    int rev = entity.getRev();
    entity.setRev(++rev);
  }

  private <T extends DomainEntity> void removePID(T entity) {
    entity.setPid(null);
  }

  public <T extends Entity> T getEntity(Class<T> type, String id) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node nodeWithHighestRevision = neo4jLowLevelAPI.getLatestNodeById(type, id);

      if (nodeWithHighestRevision == null) {
        transaction.success();
        return null;
      }

      try {
        NodeConverter<T> nodeConverter = propertyContainerConverterFactory.createForType(type);
        T entity = nodeConverter.convertToEntity(nodeWithHighestRevision);

        transaction.success();
        return entity;
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      } catch (IllegalArgumentException | InstantiationException e) {
        transaction.failure();
        throw new StorageException(e);
      }
    }
  }

  public <T extends Relation> T getRelation(Class<T> type, String id) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Relationship relationshipWithHighestRevision = getLatestRelationship(id);

      if (relationshipWithHighestRevision == null) {
        transaction.success();
        return null;
      }

      try {

        RelationshipConverter<T> relationshipConverter = propertyContainerConverterFactory.createForRelation(type);
        T entity = relationshipConverter.convertToEntity(relationshipWithHighestRevision);

        transaction.success();
        return entity;
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      } catch (IllegalArgumentException | InstantiationException e) {
        transaction.failure();
        throw new StorageException(e);
      }
    }
  }

  public <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    removePID(entity);
    updateEntity(type, entity, change);
  }

  public <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws StorageException {
    updateEntity(type, entity, Change.newInternalInstance());
  }

  private <T extends Entity> void updateEntity(Class<T> type, T entity, Change change) throws UpdateException, ConversionException {
    try (Transaction transaction = db.beginTx()) {
      Node node = neo4jLowLevelAPI.getLatestNodeById(type, entity.getId());

      if (node == null) {
        transaction.failure();
        throw new UpdateException(entityNotFoundMessageFor(type, entity));
      }

      int rev = getRevisionProperty(node);
      if (rev != entity.getRev()) {
        transaction.failure();
        throw new UpdateException(revisionNotFoundMessage(type, entity, rev));
      }

      updateAdministrativeValues(entity);

      try {
        NodeConverter<T> propertyContainerConverter = propertyContainerConverterFactory.createForType(type);

        /* split the update and the update of modified and rev, 
         * to be sure the administrative values can only be changed by the system
         */
        propertyContainerConverter.updatePropertyContainer(node, entity);
        propertyContainerConverter.updateModifiedAndRev(node, entity);

        transaction.success();
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Relation> void updateRelation(Class<T> type, Relation relation, Change change) throws StorageException {

    try (Transaction transaction = db.beginTx()) {
      Relationship relationship = getLatestRelationship(relation.getId());

      T entity = (T) relation;
      if (relationship == null) {
        transaction.failure();
        throw new UpdateException(entityNotFoundMessageFor(type, entity));
      }

      int rev = getRevisionProperty(relationship);
      if (rev != relation.getRev()) {
        transaction.failure();
        throw new UpdateException(revisionNotFoundMessage(type, entity, rev));
      }

      removePID(relation);
      updateAdministrativeValues(relation);

      RelationshipConverter<T> converter = propertyContainerConverterFactory.createForRelation(type);
      try {
        converter.updatePropertyContainer(relationship, entity);
        converter.updateModifiedAndRev(relationship, entity);
        transaction.success();
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      }

    }
  }

  private <T extends Entity> void updateAdministrativeValues(T entity) {
    entity.setModified(Change.newInternalInstance());
    updateRevision(entity);
  }

  private <T extends Entity> String revisionNotFoundMessage(Class<T> type, T entity, int actualLatestRev) {
    return String.format("\"%s\" with id \"%s\" and revision \"%d\" found. Revision \"%d\" wanted.", type.getSimpleName(), entity.getId(), entity.getRev(), actualLatestRev);
  }

  private <T extends Entity> String entityNotFoundMessageFor(Class<T> type, T entity) {
    return String.format("\"%s\" with id \"%s\" cannot be found.", type.getSimpleName(), entity.getId());
  }

  // TODO: Make equal to deleteSystemEntity see TIM-54
  public <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id, Change change) throws StorageException {
    if (!TypeRegistry.isPrimitiveDomainEntity(type)) {
      throw new IllegalArgumentException("Only primitive DomainEntities can be deleted. " + type.getSimpleName() + " is not a primitive DomainEntity.");
    }

    try (Transaction transaction = db.beginTx()) {
      List<Node> foundNodes = neo4jLowLevelAPI.getNodesWithId(type, id);
      if (foundNodes.isEmpty()) {
        transaction.failure();
        throw new NoSuchEntityException(type, id);
      }

      deleteEntity(foundNodes);

      transaction.success();
    }
  }

  public <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws StorageException {
    int numDeleted = 0;
    try (Transaction transaction = db.beginTx()) {
      List<Node> foundNodes = neo4jLowLevelAPI.getNodesWithId(type, id);
      numDeleted = deleteEntity(foundNodes);
      transaction.success();
    }

    return numDeleted;
  }

  private int deleteEntity(List<Node> nodes) {
    int numDeleted = 0;
    for (Node node : nodes) {
      for (Iterator<Relationship> relationships = node.getRelationships().iterator(); relationships.hasNext();) {
        relationships.next().delete();
      }

      node.delete();
      numDeleted++;
    }
    return numDeleted;
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
    if (hasPID(entity)) {
      transaction.failure();
      throw new IllegalStateException(String.format("%s with %s already has a pid: %s", type.getSimpleName(), id, pid));
    }
  }

  /* *************************************************************************************
   * Low level API
   * ************************************************************************************/

  private int getRevisionProperty(PropertyContainer propertyContainer) {
    return (int) propertyContainer.getProperty(REVISION_PROPERTY_NAME);
  }

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

  private ResourceIterator<Relationship> getFromIndex(String id) {
    Index<Relationship> index = db.index().forRelationships(RELATIONSHIP_ID_INDEX);

    IndexHits<Relationship> indexHits = index.get(ID_PROPERTY_NAME, id);

    ResourceIterator<Relationship> iterator = indexHits.iterator();
    return iterator;
  }

}
