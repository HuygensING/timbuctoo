package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.PropertyContainerHelper.getIdProperty;
import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.PropertyContainerHelper.getRevisionProperty;

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
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.PropertyContainerConverterFactory;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.Strings;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class Neo4JStorage implements GraphStorage {

  private static final String PID_PROPERTY_NAME = DomainEntity.PID;

  static final long REQUEST_TIMEOUT = 5000;

  private final GraphDatabaseService db;
  private final PropertyContainerConverterFactory propertyContainerConverterFactory;
  private final NodeDuplicator nodeDuplicator;
  private final RelationshipDuplicator relationshipDuplicator;
  private final TypeRegistry typeRegistry;
  private final Neo4JLowLevelAPI neo4jLowLevelAPI;
  private final Neo4JStorageIteratorFactory neo4jStorageIteratorFactory;

  @Inject
  public Neo4JStorage(GraphDatabaseService db, PropertyContainerConverterFactory propertyContainerConverterFactory, TypeRegistry typeRegistry) {
    this(db, propertyContainerConverterFactory, new IdGenerator(), typeRegistry, new Neo4JLowLevelAPI(db), new Neo4JStorageIteratorFactory(propertyContainerConverterFactory));
  }

  public Neo4JStorage(GraphDatabaseService db, PropertyContainerConverterFactory propertyContainerConverterFactory, NodeDuplicator nodeDuplicator, RelationshipDuplicator relationshipDuplicator,
      IdGenerator idGenerator, TypeRegistry typeRegistry, Neo4JLowLevelAPI neo4jLowLevelAPI, Neo4JStorageIteratorFactory neo4jStorageIteratorFactory) {
    this.db = db;
    this.propertyContainerConverterFactory = propertyContainerConverterFactory;
    this.nodeDuplicator = nodeDuplicator;
    this.relationshipDuplicator = relationshipDuplicator;
    this.typeRegistry = typeRegistry;
    this.neo4jLowLevelAPI = neo4jLowLevelAPI;
    this.neo4jStorageIteratorFactory = neo4jStorageIteratorFactory;
  }

  public Neo4JStorage(GraphDatabaseService db, PropertyContainerConverterFactory propertyContainerConverterFactory, IdGenerator idGenerator, TypeRegistry typeRegistry,
      Neo4JLowLevelAPI neo4jLowLevelAPI, Neo4JStorageIteratorFactory neo4jStorageIteratorFactory) {
    this(db, propertyContainerConverterFactory, new NodeDuplicator(db, neo4jLowLevelAPI), new RelationshipDuplicator(db), idGenerator, typeRegistry, neo4jLowLevelAPI, neo4jStorageIteratorFactory);
  }

  @Override
  public <T extends DomainEntity> void addDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node node = db.createNode();

      NodeConverter<? super T> compositeNodeConverter = propertyContainerConverterFactory.createCompositeForType(type);

      try {
        compositeNodeConverter.addValuesToPropertyContainer(node, entity);
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      }
      neo4jLowLevelAPI.index(node);

      transaction.success();
    }
  }

  @Override
  public <T extends SystemEntity> void addSystemEntity(Class<T> type, T entity) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      try {

        NodeConverter<T> propertyContainerConverter = propertyContainerConverterFactory.createForType(type);
        Node node = db.createNode();

        propertyContainerConverter.addValuesToPropertyContainer(node, entity);

        neo4jLowLevelAPI.index(node);
        transaction.success();
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Relation> void addRelation(Class<T> type, Relation relation, Change change) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node source = getRelationPart(transaction, typeRegistry.getDomainEntityType(relation.getSourceType()), "Source", relation.getSourceId());
      Node target = getRelationPart(transaction, typeRegistry.getDomainEntityType(relation.getTargetType()), "Target", relation.getTargetId());
      Node relationTypeNode = getRelationPart(transaction, typeRegistry.getSystemEntityType(relation.getTypeType()), "RelationType", relation.getTypeId());

      RelationshipConverter<T> relationConverter = propertyContainerConverterFactory.createCompositeForRelation(type);

      try {
        String relationTypeName = getRegularRelationName(relationTypeNode);
        Relationship relationship = source.createRelationshipTo(target, DynamicRelationshipType.withName(relationTypeName));

        relationConverter.addValuesToPropertyContainer(relationship, (T) relation);

        neo4jLowLevelAPI.addRelationship(relationship, relation.getId());
        transaction.success();
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      } catch (InstantiationException | IllegalAccessException e) {
        transaction.failure();
        throw new StorageException(e);
      }

    }
  }

  private String getRegularRelationName(Node relationTypeNode) throws ConversionException, InstantiationException, IllegalAccessException {
    NodeConverter<RelationType> relationTypeConverter = propertyContainerConverterFactory.createForType(RelationType.class);
    RelationType relationType = relationTypeConverter.convertToEntity(relationTypeNode);

    String relationTypeName = relationType.getRegularName();
    return relationTypeName;
  }

  private Node getRelationPart(Transaction transaction, Class<? extends Entity> type, String partName, String partId) throws StorageException {
    Node part = neo4jLowLevelAPI.getLatestNodeById(type, partId);
    if (part == null) {
      transaction.failure();
      throw new StorageException(createCannotFindString(partName, type, partId));
    }
    return part;
  }

  private String createCannotFindString(String relationPart, Class<? extends Entity> type, String id) {
    return String.format("%s of type \"%s\" with id \"%s\" could not be found.", relationPart, type, id);
  }

  @Override
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

  @Override
  public <T extends Entity> StorageIterator<T> getEntities(Class<T> type) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Node> nodes = neo4jLowLevelAPI.getNodesOfType(type);

      try {
        StorageIterator<T> storageIterator = neo4jStorageIteratorFactory.forNode(type, nodes);

        transaction.success();
        return storageIterator;
      } catch (StorageException e) {
        transaction.failure();

        throw e;
      }
    }
  }

  @Override
  public <T extends Relation> T getRelation(Class<T> type, String id) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Relationship relationshipWithHighestRevision = neo4jLowLevelAPI.getLatestRelationshipById(id);

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

  @Override
  public <T extends Entity> void updateEntity(Class<T> type, T entity) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node node = neo4jLowLevelAPI.getLatestNodeById(type, entity.getId());

      if (node == null) {
        transaction.failure();
        throw new UpdateException(entityNotFoundMessageFor(type, entity));
      }

      if (isMatchingRev(entity, node)) {
        transaction.failure();
        throw new UpdateException(revisionNotFoundMessage(type, entity, getRevisionProperty(node)));
      }

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

  /**
   * Update a DomainEntity with a new variant.
   * @param type the type of the variant
   * @param variant the variant to add
   * @throws StorageException when the variant cannot be added
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends DomainEntity> void addVariant(Class<T> type, T variant) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      String id = variant.getId();

      if (entityExists(type, id)) {
        transaction.failure();
        throw new UpdateException(String.format("Variant \"%s\" cannot be added when it already exists.", type));
      }

      Class<? extends DomainEntity> baseType = TypeRegistry.toBaseDomainEntity(type);
      Node node = neo4jLowLevelAPI.getLatestNodeById(baseType, id);

      if (node == null) {
        transaction.failure();
        throw new NoSuchEntityException(baseType, id);
      }

      if (isMatchingRev(variant, node)) {
        transaction.failure();
        throw new UpdateException(revisionNotFoundMessage((Class<? super T>) baseType, variant, variant.getRev()));
      }

      // update administrative values
      NodeConverter<T> converter = propertyContainerConverterFactory.createForType(type);
      converter.addValuesToPropertyContainer(node, variant);
      converter.updateModifiedAndRev(node, variant);

      transaction.success();
    }
  }

  private <T extends Entity> boolean isMatchingRev(T entity, PropertyContainer propertyContainer) {
    // The difference between the reference of the entity and the property container should be one.
    // This is because the life cycle management is done outside this class.
    // So the revision should be updated before update is called.
    return (entity.getRev() - getRevisionProperty(propertyContainer)) != 1;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Relation> void updateRelation(Class<T> type, Relation relation, Change change) throws StorageException {

    try (Transaction transaction = db.beginTx()) {
      Relationship relationship = neo4jLowLevelAPI.getLatestRelationshipById(relation.getId());

      T entity = (T) relation;
      if (relationship == null) {
        transaction.failure();
        throw new UpdateException(entityNotFoundMessageFor(type, entity));
      }

      if (isMatchingRev(entity, relationship)) {
        transaction.failure();
        throw new UpdateException(revisionNotFoundMessage(type, entity, getRevisionProperty(relationship)));
      }

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

  private <T extends Entity> String revisionNotFoundMessage(Class<? super T> type, T entity, int actualLatestRev) {
    return String.format("\"%s\" with id \"%s\" and revision \"%d\" found. Revision \"%d\" wanted.", type.getSimpleName(), entity.getId(), entity.getRev() - 1, actualLatestRev);
  }

  private <T extends Entity> String entityNotFoundMessageFor(Class<T> type, T entity) {
    return String.format("\"%s\" with id \"%s\" cannot be found.", type.getSimpleName(), entity.getId());
  }

  @Override
  public long countEntities(Class<? extends Entity> type) {
    Class<? extends Entity> primitiveType = TypeRegistry.getBaseClass(type);
    Label label = DynamicLabel.label(TypeNames.getInternalName(primitiveType));

    return neo4jLowLevelAPI.countNodesWithLabel(label);
  }

  @Override
  public long countRelations(Class<? extends Relation> relationType) {
    return neo4jLowLevelAPI.countRelationships();
  }

  // TODO: Make equal to deleteSystemEntity see TIM-54
  @Override
  public <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id) throws StorageException {
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

  @Override
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

  @Override
  public <T extends DomainEntity> T getDomainEntityRevision(Class<T> type, String id, int revision) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node node = neo4jLowLevelAPI.getNodeWithRevision(type, id, revision);

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

  @Override
  public <T extends Relation> T getRelationRevision(Class<T> type, String id, int revision) throws StorageException {
    try (Transaction transaction = db.beginTx()) {

      Relationship relationship = neo4jLowLevelAPI.getRelationshipWithRevision(type, id, revision);

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

  @Override
  public <T extends DomainEntity> void setDomainEntityPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException {
    try (Transaction transaction = db.beginTx()) {
      Node node = neo4jLowLevelAPI.getLatestNodeById(type, id);

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

  @Override
  public <T extends Relation> void setRelationPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException {
    try (Transaction transaction = db.beginTx()) {
      Relationship relationship = neo4jLowLevelAPI.getLatestRelationshipById(id);

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

  @Override
  public void close() {
    db.shutdown();
  }

  @Override
  public boolean isAvailable() {
    return db.isAvailable(REQUEST_TIMEOUT);
  }

  @Override
  public <T extends Entity> T findEntityByProperty(Class<T> type, String field, String value) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      NodeConverter<T> converter = propertyContainerConverterFactory.createForType(type);

      String propertyName = converter.getPropertyName(field);

      Node node = neo4jLowLevelAPI.findNodeByProperty(type, propertyName, value);

      if (node == null) {
        transaction.success();
        return null;
      }

      try {
        T entity = converter.convertToEntity(node);
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

  @Override
  public <T extends Relation> T findRelationByProperty(Class<T> type, String field, String value) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      RelationshipConverter<T> relationshipConverter = propertyContainerConverterFactory.createForRelation(type);
      String propertyName = relationshipConverter.getPropertyName(field);
      Relationship relationship = neo4jLowLevelAPI.findRelationshipByProperty(type, propertyName, value);

      if (relationship == null) {
        transaction.success();
        return null;
      }

      try {
        T relation = relationshipConverter.convertToEntity(relationship);

        transaction.success();
        return relation;
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      } catch (InstantiationException e) {
        transaction.failure();
        throw new StorageException(e);
      }
    }
  }

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws StorageException {
    Preconditions.checkArgument(TypeRegistry.isPrimitiveDomainEntity(type), "Nonprimitive type %s", type);

    try (Transaction transaction = db.beginTx()) {
      Node node = neo4jLowLevelAPI.getLatestNodeById(type, id);

      List<T> variations = Lists.newArrayList();

      for (Label label : node.getLabels()) {
        Class<? extends DomainEntity> domainEntityType = typeRegistry.getDomainEntityType(label.name());
        NodeConverter<? extends DomainEntity> converter = propertyContainerConverterFactory.createForType(domainEntityType);

        try {
          variations.add(type.cast(converter.convertToEntity(node)));
        } catch (ConversionException e) {
          transaction.failure();
          throw e;
        } catch (InstantiationException e) {
          transaction.failure();
          throw new StorageException(e);
        }
      }

      transaction.success();
      return variations;
    }

  }

  @Override
  public <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      List<Relationship> relationships = neo4jLowLevelAPI.getRelationshipsByNodeId(id);

      try {
        StorageIterator<T> iterator = neo4jStorageIteratorFactory.forRelationship(type, relationships);
        transaction.success();
        return iterator;
      } catch (StorageException e) {
        transaction.failure();
        throw e;
      }
    }
  }

  /**
   * Checks if a certain variant with a certain id exists.
   * @param type the type of the variant
   * @param id the id of the variant
   * @return true if it exists, false if not
   */
  @Override
  public boolean entityExists(Class<? extends Entity> type, String id) {
    try (Transaction transaction = db.beginTx()) {
      boolean exists = propertyContainerExists(neo4jLowLevelAPI.getLatestNodeById(type, id));

      transaction.success();
      return exists;
    }
  }

  @Override
  public boolean relationExists(Class<? extends Relation> relationType, String id) {
    try (Transaction transaction = db.beginTx()) {
      boolean exists = propertyContainerExists(neo4jLowLevelAPI.getLatestRelationshipById(id));

      transaction.success();
      return exists;
    }
  }

  private boolean propertyContainerExists(PropertyContainer propertyContainer) {
    return propertyContainer != null;
  }

  @Override
  public <T extends Relation> T findRelation(Class<T> relationType, String sourceId, String targetId, String relationTypeId) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      Relationship relationship = neo4jLowLevelAPI.findLatestRelationshipFor(relationType, sourceId, targetId, relationTypeId);

      if (relationship == null) {
        transaction.success();
        return null;
      }

      RelationshipConverter<T> converter = propertyContainerConverterFactory.createForRelation(relationType);

      try {
        T relation = converter.convertToEntity(relationship);
        transaction.success();
        return relation;
      } catch (ConversionException e) {
        transaction.failure();
        throw e;
      } catch (InstantiationException e) {
        transaction.failure();
        throw new StorageException(e);
      }
    }
  }

  @Override
  public <T extends DomainEntity> List<String> getIdsOfNonPersistentDomainEntities(Class<T> type) {
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Node> foundNodes = neo4jLowLevelAPI.getNodesOfType(type);

      Predicate<Node> isNonPersistent = new IsNonPersistent<Node>();

      List<String> ids = Lists.newArrayList();
      for (; foundNodes.hasNext();) {
        Node node = foundNodes.next();
        if (isNonPersistent.apply(node)) {
          ids.add("" + getIdProperty(node));
        }
      }
      transaction.success();
      return ids;
    }
  }

  // FIXME filter with projectType see TIM-143
  @Override
  public <T extends Relation> List<String> getIdsOfNonPersistentRelations(Class<T> type) {
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Node> allNodes = neo4jLowLevelAPI.getAllNodes();
      List<String> ids = Lists.newArrayList();

      Predicate<Relationship> isNonPersistent = new IsNonPersistent<Relationship>();

      for (; allNodes.hasNext();) {
        Node node = allNodes.next();

        for (Iterator<Relationship> iterator = node.getRelationships(Direction.OUTGOING).iterator(); iterator.hasNext();) {
          Relationship relationship = iterator.next();
          if (isNonPersistent.apply(relationship)) {
            ids.add("" + getIdProperty(relationship));
          }
        }
      }

      transaction.success();
      return ids;
    }
  }

  private static final class IsNonPersistent<T extends PropertyContainer> implements Predicate<T> {
    @Override
    public boolean apply(T input) {
      return !input.hasProperty(PID_PROPERTY_NAME);
    }
  }

  // TODO make only available for DomainEntities see TIM-162
  @Override
  public <T extends Entity> T getDefaultVariation(Class<T> type, String id) throws StorageException {
    try (Transaction transaction = db.beginTx()) {
      T entity = null;
      Class<? extends Entity> primitiveType = TypeRegistry.getBaseClass(type);
      Node node = neo4jLowLevelAPI.getLatestNodeById(primitiveType, id);

      if (node != null) {

        NodeConverter<? super T> converter = propertyContainerConverterFactory.createForPrimitive(type);
        try {
          entity = converter.convertToSubType(type, node);
        } catch (ConversionException ex) {
          transaction.failure();
          throw ex;
        }
      }

      transaction.success();
      return entity;
    }

  }
}
