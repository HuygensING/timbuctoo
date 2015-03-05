package nl.knaw.huygens.timbuctoo.storage.neo4j;

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

import com.google.inject.Inject;

public class Neo4JStorage implements Storage {

  public static final String RELATION_SHIP_ID_INDEX = "RelationShip id";

  private static final Class<Relationship> RELATIONSHIP_TYPE = Relationship.class;
  private static final Class<Node> NODE_TYPE = Node.class;
  private final EntityConverterFactory entityConverterFactory;
  private final GraphDatabaseService db;
  private final EntityInstantiator entityInstantiator;
  private final IdGenerator idGenerator;
  private final TypeRegistry typeRegistry;

  @Inject
  public Neo4JStorage(GraphDatabaseService db, EntityConverterFactory entityTypeWrapperFactory, EntityInstantiator entityInstantiator, IdGenerator idGenerator, TypeRegistry typeRegistry) {
    this.db = db;
    this.entityConverterFactory = entityTypeWrapperFactory;
    this.entityInstantiator = entityInstantiator;
    this.idGenerator = idGenerator;
    this.typeRegistry = typeRegistry;
  }

  @Override
  public void createIndex(boolean unique, Class<? extends Entity> type, String... fields) throws StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends Entity> String getStatistics(Class<T> type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      try {
        String id = addAdministrativeValues(type, entity);

        EntityConverter<T, Node> objectWrapper = entityConverterFactory.createForTypeAndPropertyContainer(type, NODE_TYPE);
        Node node = db.createNode();

        objectWrapper.addValuesToPropertyContainer(node, entity);

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
      Node source = getLatestById(typeRegistry.getDomainEntityType(relation.getSourceType()), relation.getSourceId());
      if (source == null) {
        transaction.failure();
        throw new StorageException(createCannotFindString("Source", relation.getSourceType(), relation.getSourceId()));
      }

      Node target = getLatestById(typeRegistry.getDomainEntityType(relation.getTargetType()), relation.getTargetId());
      if (target == null) {
        transaction.failure();
        throw new StorageException(createCannotFindString("Target", relation.getSourceType(), relation.getSourceId()));
      }

      Node relationType = getLatestById(typeRegistry.getSystemEntityType(relation.getTypeType()), relation.getTypeId());
      if (relationType == null) {
        transaction.failure();
        throw new StorageException(createCannotFindString("RelationType", relation.getTypeType(), relation.getTypeId()));
      }

      EntityConverter<T, Relationship> relationConverter = entityConverterFactory.createForTypeAndPropertyContainer(type, RELATIONSHIP_TYPE);
      EntityConverter<? super T, Relationship> primitiveRelationConverter = entityConverterFactory.createForPrimitive(type, RELATIONSHIP_TYPE);

      String id = addAdministrativeValues(type, (T) relation);

      // TODO get the relationTypeName via an entityConverter 
      String relationTypeName = (String) relationType.getProperty(String.format("%s:%s", relation.getTypeType(), RelationType.REGULAR_NAME));
      Relationship relationship = source.createRelationshipTo(target, DynamicRelationshipType.withName(relationTypeName));

      try {
        relationConverter.addValuesToPropertyContainer(relationship, (T) relation);
        primitiveRelationConverter.addValuesToPropertyContainer(relationship, (T) relation);

        db.index().forRelationships(RELATION_SHIP_ID_INDEX).add(relationship, ID_PROPERTY_NAME, id);
        transaction.success();
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      }

      return id;
    }
  }

  private String createCannotFindString(String relationPart, String typeName, String id) {
    return String.format("%s of type \"%s\" with id \"%s\" could not be found.", relationPart, typeName, id);
  }

  private <T extends DomainEntity> String addRegularDomainEntity(Class<T> type, T entity) throws ConversionException {
    try (Transaction transaction = db.beginTx()) {
      String id = addAdministrativeValues(type, entity);
      Node node = db.createNode();

      EntityConverter<T, Node> domainEntityConverter = entityConverterFactory.createForTypeAndPropertyContainer(type, NODE_TYPE);
      EntityConverter<? super T, Node> primitiveEntityConverter = entityConverterFactory.createForPrimitive(type, NODE_TYPE);

      try {
        domainEntityConverter.addValuesToPropertyContainer(node, entity);
        primitiveEntityConverter.addValuesToPropertyContainer(node, entity);
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
        throw new UpdateException(String.format("\"%s\" with id \"%s\" cannot be found.", type.getSimpleName(), entity.getId()));
      }

      int rev = getRevision(node);
      if (rev != entity.getRev()) {
        transaction.failure();
        throw new UpdateException(String.format("\"%s\" with id \"%s\" and revision \"%d\".", type.getSimpleName(), entity.getId(), entity.getRev()));
      }

      updateAdministrativeValues(entity);

      try {
        EntityConverter<T, Node> entityConverter = entityConverterFactory.createForTypeAndPropertyContainer(type, NODE_TYPE);

        /* split the update and the update of modified and rev, 
         * to be sure the administrative values can only be changed by the system
         */
        entityConverter.updatePropertyContainer(node, entity);
        entityConverter.updateModifiedAndRev(node, entity);

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

  @Override
  public <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    updateEntity(type, entity);
  }

  @Override
  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws StorageException {
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public <T extends SystemEntity> int deleteByModifiedDate(Class<T> type, Date dateValue) throws StorageException {
    // TODO Auto-generated method stub
    return 0;
  }

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
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteVariation(Class<? extends DomainEntity> type, String id, Change change) throws IllegalArgumentException, NoSuchEntityException, StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteRelationsOfEntity(Class<Relation> type, String id) throws StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public void declineRelationsOfEntity(Class<? extends Relation> type, String id) throws IllegalArgumentException, StorageException {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends Entity> boolean entityExists(Class<T> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public <T extends Entity> T getEntityOrDefaultVariation(Class<T> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Entity> T getEntity(Class<T> type, String id) throws StorageException {
    PropertyContainer propertyContainerWithHighestRevision = null;

    try (Transaction transaction = db.beginTx()) {
      if (Relation.class.isAssignableFrom(type)) {
        propertyContainerWithHighestRevision = getLatestFromIndex(id, transaction);
      } else {
        propertyContainerWithHighestRevision = getLatestById(type, id);
      }

      if (propertyContainerWithHighestRevision == null) {
        transaction.success();
        return null;
      }

      try {
        T entity = convertEnity(type, propertyContainerWithHighestRevision);

        transaction.success();
        return entity;
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      } catch (IllegalAccessException | IllegalArgumentException | InstantiationException e) {
        transaction.failure();
        throw new StorageException(e);
      }
    }

  }

  private Relationship getLatestFromIndex(String id, Transaction transaction) {
    Index<Relationship> index = db.index().forRelationships(RELATION_SHIP_ID_INDEX);

    IndexHits<Relationship> indexHits = index.get(ID_PROPERTY_NAME, id);

    ResourceIterator<Relationship> iterator = indexHits.iterator();
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

  private <T extends Entity, U extends PropertyContainer> T convertEnity(Class<T> type, U propertyContainer) throws InstantiationException, IllegalAccessException, ConversionException {
    T entity = entityInstantiator.createInstanceOf(type);
    @SuppressWarnings("unchecked")
    Class<U> propertyContainerType = (Class<U>) propertyContainer.getClass();

    EntityConverter<T, U> entityConverter = entityConverterFactory.createForTypeAndPropertyContainer(type, propertyContainerType);
    entityConverter.addValuesToEntity(entity, propertyContainer);
    return entity;
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends DomainEntity> StorageIterator<T> getDomainEntities(Class<T> type) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Entity> StorageIterator<T> getEntitiesByProperty(Class<T> type, String field, String value) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Entity> long count(Class<T> type) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public <T extends Entity> T findItemByProperty(Class<T> type, String field, String value) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  private <T extends Entity> ResourceIterator<Node> findByProperty(Class<T> type, String propertyName, String id) {
    Label internalNameLabel = DynamicLabel.label(TypeNames.getInternalName(type));
    ResourceIterable<Node> foundNodes = db.findNodesByLabelAndProperty(internalNameLabel, propertyName, id);

    ResourceIterator<Node> iterator = foundNodes.iterator();
    return iterator;
  }

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends DomainEntity> List<T> getAllRevisions(Class<T> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Relation> T findRelation(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Relation> StorageIterator<T> findRelations(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getRelationIds(List<String> ids) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Relation> List<T> getRelationsByType(Class<T> type, List<String> relationTypeIds) throws StorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean doesVariationExist(Class<? extends DomainEntity> type, String id) throws StorageException {
    // TODO Auto-generated method stub
    return false;
  }

}
