package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.DomainEntity.PID;
import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;

import java.util.Date;
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
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.apache.commons.lang.StringUtils;
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
import com.google.inject.Inject;

public class Neo4JStorage implements Storage {

  public static final String RELATIONSHIP_ID_INDEX = "RelationShip id";

  private final PropertyContainerConverterFactory propertyContainerConverterFactory;
  private final GraphDatabaseService db;
  private final EntityInstantiator entityInstantiator;
  private final IdGenerator idGenerator;
  private final TypeRegistry typeRegistry;
  private final NodeDuplicator nodeDuplicator;
  private final RelationshipDuplicator relationshipDuplicator;

  @Inject
  public Neo4JStorage(GraphDatabaseService db, PropertyContainerConverterFactory propertyContainerConverterFactory, EntityInstantiator entityInstantiator, IdGenerator idGenerator,
      TypeRegistry typeRegistry, NodeDuplicator nodeDuplicator, RelationshipDuplicator relationshipDuplicator) {
    this.db = db;
    this.propertyContainerConverterFactory = propertyContainerConverterFactory;
    this.entityInstantiator = entityInstantiator;
    this.idGenerator = idGenerator;
    this.typeRegistry = typeRegistry;
    this.nodeDuplicator = nodeDuplicator;
    this.relationshipDuplicator = relationshipDuplicator;
  }

  @Override
  public void createIndex(boolean unique, Class<? extends Entity> type, String... fields) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");

  }

  @Override
  public <T extends Entity> String getStatistics(Class<T> type) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException("Yet to be implemented");

  }

  @Override
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

  @SuppressWarnings("unchecked")
  @Override
  public <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    if (Relation.class.isAssignableFrom(type)) {
      return addRelationDomainEntity((Class<? extends Relation>) type, (Relation) entity);
    } else {
      return addRegularDomainEntity(type, entity);
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Relation> String addRelationDomainEntity(Class<T> type, Relation relation) throws StorageException {
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
    RelationType relationType = entityInstantiator.createInstanceOf(RelationType.class);

    relationTypeConverter.addValuesToEntity(relationType, relationTypeNode);

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

  private <T extends DomainEntity> String addRegularDomainEntity(Class<T> type, T entity) throws ConversionException {
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

  @Override
  public <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws StorageException {
    updateEntity(type, entity);

  }

  private <T extends Entity> void updateEntity(Class<T> type, T entity) throws UpdateException, ConversionException {
    try (Transaction transaction = db.beginTx()) {
      Node node = getLatestById(type, entity.getId());

      if (node == null) {
        transaction.failure();
        throw new UpdateException(entityNotFoundMessageFor(type, entity));
      }

      int rev = getRevision(node);
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

  private <T extends Entity> String revisionNotFoundMessage(Class<T> type, T entity, int actualLatestRev) {
    return String.format("\"%s\" with id \"%s\" and revision \"%d\" found. Revision \"%d\" wanted.", type.getSimpleName(), entity.getId(), entity.getRev(), actualLatestRev);
  }

  private <T extends Entity> String entityNotFoundMessageFor(Class<T> type, T entity) {
    return String.format("\"%s\" with id \"%s\" cannot be found.", type.getSimpleName(), entity.getId());
  }

  private <T extends Entity> void updateAdministrativeValues(T entity) {
    entity.setModified(Change.newInternalInstance());
    updateRevision(entity);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    removePID(entity);
    if (Relation.class.isAssignableFrom(type)) {
      updateRelation((Class<? extends Relation>) type, (Relation) entity);
    } else {
      updateEntity(type, entity);
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Relation> void updateRelation(Class<T> type, Relation relation) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Relationship relationship = getLatestFromIndex(relation.getId(), transaction);

      T entity = (T) relation;
      if (relationship == null) {
        transaction.failure();
        throw new UpdateException(entityNotFoundMessageFor(type, entity));
      }

      int rev = getRevision(relationship);
      if (rev != relation.getRev()) {
        transaction.failure();
        throw new UpdateException(revisionNotFoundMessage(type, entity, rev));
      }

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

  private <T extends DomainEntity> void removePID(T entity) {
    entity.setPid(null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws StorageException {
    if (Relation.class.isAssignableFrom(type)) {
      setRelationPID((Class<? extends Relation>) type, id, pid);
    } else {
      setDomainEntityPID(type, id, pid);
    }
  }

  private <T extends Relation> void setRelationPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException {
    try (Transaction transaction = db.beginTx()) {
      Relationship relationship = getLatestFromIndex(id, transaction);

      if (relationship == null) {
        transaction.failure();
        throw new NoSuchEntityException(type, id);
      }

      try {
        RelationshipConverter<T> converter = propertyContainerConverterFactory.createForRelation(type);

        T entity = convertRelationshipToRelation(type, relationship);

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

  private <T extends DomainEntity> void setDomainEntityPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node node = getLatestById(type, id);

      if (node == null) {
        transaction.failure();
        throw new NoSuchEntityException(type, id);
      }

      try {
        NodeConverter<T> converter = propertyContainerConverterFactory.createForType(type);
        T entity = convertNodeToEntity(type, node, converter);

        validateEntityHasNoPID(type, id, pid, transaction, entity);

        updateNodeWithPID(type, pid, node, entity, converter);

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

  private <T extends DomainEntity> void validateEntityHasNoPID(Class<T> type, String id, String pid, Transaction transaction, T entity) {
    if (!StringUtils.isBlank(entity.getPid())) {
      transaction.failure();
      throw new IllegalStateException(String.format("%s with %s already has a pid: %s", type.getSimpleName(), id, pid));
    }
  }

  private <T extends DomainEntity> void updateNodeWithPID(Class<T> type, String pid, Node node, T entity, NodeConverter<T> converter) throws ConversionException {

    entity.setPid(pid);

    converter.addValuesToPropertyContainer(node, entity);
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws StorageException {
    int numDeleted = 0;
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Node> nodes = findByProperty(type, ID_PROPERTY_NAME, id);
      numDeleted = deleteEntity(nodes);
      transaction.success();
    }

    return numDeleted;
  }

  private int deleteEntity(ResourceIterator<Node> nodes) {
    int numDeleted = 0;
    for (; nodes.hasNext();) {
      Node node = nodes.next();

      for (Iterator<Relationship> relationships = node.getRelationships().iterator(); relationships.hasNext();) {
        relationships.next().delete();
      }

      node.delete();
      numDeleted++;
    }
    return numDeleted;
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntities(Class<T> type) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends SystemEntity> int deleteByModifiedDate(Class<T> type, Date dateValue) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  // TODO: Make equal to deleteSystemEntity see TIM-54
  @Override
  public <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id, Change change) throws StorageException {
    if (!TypeRegistry.isPrimitiveDomainEntity(type)) {
      throw new IllegalArgumentException("Only primitive DomainEntities can be deleted. " + type.getSimpleName() + " is not a primitive DomainEntity.");
    }

    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Node> foundNodes = findByProperty(type, ID_PROPERTY_NAME, id);
      if (!foundNodes.hasNext()) {
        transaction.failure();
        throw new NoSuchEntityException(type, id);
      }

      deleteEntity(foundNodes);

      transaction.success();
    }
  }

  @Override
  public <T extends DomainEntity> void deleteNonPersistent(Class<T> type, List<String> ids) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");

  }

  @Override
  public void deleteVariation(Class<? extends DomainEntity> type, String id, Change change) throws IllegalArgumentException, NoSuchEntityException, StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");

  }

  @Override
  public void deleteRelationsOfEntity(Class<Relation> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");

  }

  @Override
  public void declineRelationsOfEntity(Class<? extends Relation> type, String id) throws IllegalArgumentException, StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");

  }

  @Override
  public <T extends Entity> boolean entityExists(Class<T> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Entity> T getEntityOrDefaultVariation(Class<T> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Entity> T getEntity(Class<T> type, String id) throws StorageException {
    if (Relation.class.isAssignableFrom(type)) {
      @SuppressWarnings("unchecked")
      T relationDomainEntity = (T) getRelationDomainEntity((Class<Relation>) type, id);
      return relationDomainEntity;
    } else {
      return getRegularEntity(type, id);
    }

  }

  private <T extends Relation> T getRelationDomainEntity(Class<T> type, String id) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Relationship propertyContainerWithHighestRevision = getLatestFromIndex(id, transaction);

      if (propertyContainerWithHighestRevision == null) {
        transaction.success();
        return null;
      }

      try {
        T entity = convertRelationshipToRelation(type, propertyContainerWithHighestRevision);

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

  private <T extends Entity> T getRegularEntity(Class<T> type, String id) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node propertyContainerWithHighestRevision = getLatestById(type, id);

      if (propertyContainerWithHighestRevision == null) {
        transaction.success();
        return null;
      }

      try {
        T entity = convertNodeToEntity(type, propertyContainerWithHighestRevision);

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

  private Relationship getLatestFromIndex(String id, Transaction transaction) {
    ResourceIterator<Relationship> iterator = getFromIndex(id);
    if (!iterator.hasNext()) {
      return null;
    }
    Relationship relationshipWithHighestRevision = iterator.next();

    for (; iterator.hasNext();) {
      Relationship next = iterator.next();

      if (getRevision(next) > getRevision(relationshipWithHighestRevision)) {
        relationshipWithHighestRevision = next;
      }
    }
    return relationshipWithHighestRevision;
  }

  private ResourceIterator<Relationship> getFromIndex(String id) {
    Index<Relationship> index = db.index().forRelationships(RELATIONSHIP_ID_INDEX);

    IndexHits<Relationship> indexHits = index.get(ID_PROPERTY_NAME, id);

    ResourceIterator<Relationship> iterator = indexHits.iterator();
    return iterator;
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

      if (getRevision(next) > getRevision(nodeWithHighestRevision)) {
        nodeWithHighestRevision = next;
      }
    }

    return nodeWithHighestRevision;
  }

  private int getRevision(PropertyContainer propertyContainer) {
    return (int) propertyContainer.getProperty(REVISION_PROPERTY_NAME);
  }

  @Override
  public <T extends SystemEntity> StorageIterator<T> getSystemEntities(Class<T> type) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends DomainEntity> StorageIterator<T> getDomainEntities(Class<T> type) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Entity> StorageIterator<T> getEntitiesByProperty(Class<T> type, String field, String value) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Entity> long count(Class<T> type) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Entity> T findItemByProperty(Class<T> type, String field, String value) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  private <T extends Entity> ResourceIterator<Node> findByProperty(Class<T> type, String propertyName, String id) {
    Label internalNameLabel = DynamicLabel.label(TypeNames.getInternalName(type));
    ResourceIterable<Node> foundNodes = db.findNodesByLabelAndProperty(internalNameLabel, propertyName, id);

    ResourceIterator<Node> iterator = foundNodes.iterator();
    return iterator;
  }

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revision) throws StorageException {
    if (Relation.class.isAssignableFrom(type)) {
      return (T) getRelationRevision((Class<? extends Relation>) type, id, revision);
    } else {
      return getDomainEntityRevision(type, id, revision);
    }
  }

  private <T extends Relation> T getRelationRevision(Class<T> type, String id, int revision) throws StorageException {
    try (Transaction transaction = db.beginTx()) {

      Relationship relationship = getRevisionRelationship(type, id, revision);

      if (relationship == null) {
        transaction.success();
        return null;
      }

      try {
        T entity = convertRelationshipToRelation(type, relationship);

        if (hasPID(entity)) {
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

  private <T extends Relation> T convertRelationshipToRelation(Class<T> type, Relationship relationship) throws InstantiationException, ConversionException {

    RelationshipConverter<T> converter = propertyContainerConverterFactory.createForRelation(type);

    return convertRelationshipToRelation(type, relationship, converter);
  }

  private <T extends Relation> T convertRelationshipToRelation(Class<T> type, Relationship relationship, RelationshipConverter<T> converter) throws InstantiationException, ConversionException {
    T entity = entityInstantiator.createInstanceOf(type);

    converter.addValuesToEntity(entity, relationship);

    return entity;
  }

  private <T extends Relation> boolean hasPID(T entity) {
    return Strings.isBlank(entity.getPid());
  }

  private <T extends Relation> Relationship getRevisionRelationship(Class<T> type, String id, int revision) {
    ResourceIterator<Relationship> iterator = getFromIndex(id);
    for (; iterator.hasNext();) {
      Relationship next = iterator.next();
      if (getRevision(next) == revision) {
        return next;
      }
    }

    return null;
  }

  private <T extends DomainEntity> T getDomainEntityRevision(Class<T> type, String id, int revision) throws ConversionException, StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node node = getRevisionNode(type, id, revision);

      if (node == null) {
        transaction.success();
        return null;
      }

      try {
        T entity = convertNodeToEntity(type, node);

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

  private <T extends Entity> T convertNodeToEntity(Class<T> type, Node node) throws InstantiationException, ConversionException {
    return convertNodeToEntity(type, node, propertyContainerConverterFactory.createForType(type));
  }

  private <T extends Entity> T convertNodeToEntity(Class<T> type, Node node, NodeConverter<T> converter) throws InstantiationException, ConversionException {
    T entity = entityInstantiator.createInstanceOf(type);
    converter.addValuesToEntity(entity, node);
    return entity;
  }

  private <T extends Entity> Node getRevisionNode(Class<T> type, String id, int revision) {
    ResourceIterator<Node> iterator = findByProperty(type, ID_PROPERTY_NAME, id);

    if (!iterator.hasNext()) {
      return null;
    }

    Node nodeWithRevision = null;

    for (; iterator.hasNext();) {
      Node next = iterator.next();

      if (Objects.equal(revision, next.getProperty(REVISION_PROPERTY_NAME))) {
        nodeWithRevision = next;
        break;
      }
    }

    // Needed to mimic the separate collections used in the Mongo storage.
    // getRevision only returns objects with a PID.
    return nodeWithRevision != null && nodeWithRevision.hasProperty(PID) ? nodeWithRevision : null;
  }

  @Override
  public <T extends DomainEntity> List<T> getAllRevisions(Class<T> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> T findRelation(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> StorageIterator<T> findRelations(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public List<String> getRelationIds(List<String> ids) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> List<T> getRelationsByType(Class<T> type, List<String> relationTypeIds) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public boolean doesVariationExist(Class<? extends DomainEntity> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
