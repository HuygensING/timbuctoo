package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getRevisionProperty;

import java.util.Iterator;
import java.util.List;

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
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TinkerpopStorage implements GraphStorage {

  private final Graph db;
  private final ElementConverterFactory elementConverterFactory;
  private final TinkerpopLowLevelAPI lowLevelAPI;
  private final TypeRegistry typeRegistry;
  private boolean available = true;
  private final StorageIteratorFactory storageIteratorFactory;

  @Inject
  public TinkerpopStorage(Graph db, TypeRegistry typeRegistry) {
    this(db, new ElementConverterFactory(typeRegistry), new TinkerpopLowLevelAPI(db), typeRegistry);
  }

  public TinkerpopStorage(Graph db, ElementConverterFactory elementConverterFactory, TinkerpopLowLevelAPI lowLevelAPI, TypeRegistry typeRegistry) {
    this(db, elementConverterFactory, lowLevelAPI, typeRegistry, new StorageIteratorFactory(elementConverterFactory));
  }

  public TinkerpopStorage(Graph db, ElementConverterFactory elementConverterFactory, TinkerpopLowLevelAPI lowLevelAPI, TypeRegistry typeRegistry, StorageIteratorFactory storageIteratorFactory) {
    this.db = db;
    this.elementConverterFactory = elementConverterFactory;
    this.lowLevelAPI = lowLevelAPI;
    this.typeRegistry = typeRegistry;
    this.storageIteratorFactory = storageIteratorFactory;
  }

  @Override
  public <T extends DomainEntity> void addDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    new RevertableAddition<T>() {
      @Override
      protected VertexConverter<T> createVertexConverter(Class<T> type) {
        return elementConverterFactory.compositeForType(type);
      }
    }.execute(type, entity);
  }

  @Override
  public <T extends SystemEntity> void addSystemEntity(Class<T> type, T entity) throws StorageException {
    new RevertableAddition<T>().execute(type, entity);
  }

  private class RevertableAddition<T extends Entity> {
    public final void execute(Class<T> type, T entity) throws StorageException {
      Vertex vertex = db.addVertex(null);

      VertexConverter<T> converter = createVertexConverter(type);
      try {
        converter.addValuesToElement(vertex, entity);
      } catch (ConversionException e) {
        rollback(vertex);
        throw e;
      }
    }

    protected void rollback(Vertex vertex) {
      db.removeVertex(vertex);
    }

    protected VertexConverter<T> createVertexConverter(Class<T> type) {
      VertexConverter<T> converter = elementConverterFactory.forType(type);
      return converter;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Relation> void addRelation(Class<T> type, Relation relation, Change change) throws StorageException {
    Vertex sourceVertex = getDomainEntityRelationPart(relation.getSourceType(), relation.getSourceId());
    Vertex targetVertex = getDomainEntityRelationPart(relation.getTargetType(), relation.getTargetId());
    Vertex relationTypeVertex = getSystemEntityRelationPart(relation.getTypeType(), relation.getTypeId());

    String regularRelationName = getRegularRelationName(relationTypeVertex);

    Edge edge = sourceVertex.addEdge(regularRelationName, targetVertex);

    EdgeConverter<T> converter = elementConverterFactory.compositeForRelation(type);

    converter.addValuesToElement(edge, (T) relation);
  }

  private String getRegularRelationName(Vertex relationTypeVertex) throws ConversionException {
    VertexConverter<RelationType> relationTypeConverter = elementConverterFactory.forType(RelationType.class);
    RelationType relationType = relationTypeConverter.convertToEntity(relationTypeVertex);

    String relationTypeName = relationType.getRegularName();
    return relationTypeName;
  }

  private Vertex getSystemEntityRelationPart(String typeType, String typeId) throws StorageException {
    return getRelationPart(getSystemEntityType(typeType), typeType, typeId);
  }

  private Class<? extends SystemEntity> getSystemEntityType(String typeType) {
    return typeRegistry.getSystemEntityType(typeType);
  }

  private Vertex getDomainEntityRelationPart(String partType, String partId) throws StorageException {
    return getRelationPart(getDomainEntityType(partType), partType, partId);
  }

  private Class<? extends DomainEntity> getDomainEntityType(String typeString) {
    return typeRegistry.getDomainEntityType(typeString);
  }

  private Vertex getRelationPart(Class<? extends Entity> type, String partName, String partId) throws StorageException {
    Vertex part = lowLevelAPI.getLatestVertexById(type, partId);
    if (part == null) {
      throw new StorageException(createCannotFindString(partName, type, partId));
    }
    return part;
  }

  private String createCannotFindString(String relationPart, Class<? extends Entity> type, String id) {
    return String.format("%s of type \"%s\" with id \"%s\" could not be found.", relationPart, type, id);
  }

  @Override
  public <T extends Entity> T getEntity(Class<T> type, String id) throws StorageException {
    T entity = null;
    Vertex vertex = lowLevelAPI.getLatestVertexById(type, id);

    if (vertex != null) {
      VertexConverter<T> converter = elementConverterFactory.forType(type);

      entity = converter.convertToEntity(vertex);
    }

    return entity;
  }

  @Override
  public <T extends Entity> StorageIterator<T> getEntities(Class<T> type) throws StorageException {
    Iterator<Vertex> vertices = lowLevelAPI.getLatestVerticesOf(type);
    return storageIteratorFactory.create(type, vertices);
  }

  @Override
  public <T extends Relation> T getRelation(Class<T> type, String id) throws StorageException {
    T relation = null;

    Edge edge = lowLevelAPI.getLatestEdgeById(type, id);

    if (edge != null) {
      EdgeConverter<T> converter = elementConverterFactory.forRelation(type);

      relation = converter.convertToEntity(edge);
    }

    return relation;

  }

  @Override
  public <T extends Entity> void updateEntity(Class<T> type, T entity) throws StorageException {
    Vertex vertex = getVertexIfExists(type, entity.getId());

    validateIsMatchingRev(type, entity, vertex);

    VertexConverter<T> converter = elementConverterFactory.forType(type);
    converter.updateModifiedAndRev(vertex, entity);
    converter.updateElement(vertex, entity);

  }

  private <T extends Entity> Vertex getVertexIfExists(Class<T> type, String id) throws NoSuchEntityException {
    Vertex vertex = lowLevelAPI.getLatestVertexById(type, id);

    if (vertex == null) {
      throw new NoSuchEntityException(type, id);
    }
    return vertex;
  }

  private <T extends Entity> void validateIsMatchingRev(Class<T> type, T entity, Element element) throws UpdateException {
    if (!isMatchingRev(entity, element)) {
      throw UpdateException.revisionNotFound(type, entity, getRevisionProperty(element));
    }
  }

  private <T extends Entity> boolean isMatchingRev(T entity, Element element) {
    // The difference between the reference of the entity and the property container should be one.
    // This is because the life cycle management is done outside this class.
    // So the revision should be updated before update is called.
    return (entity.getRev() - getRevisionProperty(element)) == 1;
  }

  @Override
  public <T extends DomainEntity> void addVariant(Class<T> type, T variant) throws StorageException {
    Class<? extends DomainEntity> primitive = TypeRegistry.toBaseDomainEntity(type);

    String id = variant.getId();
    validateEntityDoesNotContainVariant(type, id);

    Vertex vertex = getVertexIfExists(primitive, variant.getId());

    validateIsMatchingRev(type, variant, vertex);

    VertexConverter<T> converter = elementConverterFactory.forType(type);
    converter.updateModifiedAndRev(vertex, variant);
    converter.addValuesToElement(vertex, variant);

  }

  private <T extends DomainEntity> void validateEntityDoesNotContainVariant(Class<T> type, String id) throws UpdateException {
    if (entityExists(type, id)) {
      throw UpdateException.variantAlreadyExists(type, id);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Relation> void updateRelation(Class<T> type, Relation relation, Change change) throws StorageException {
    Edge edge = lowLevelAPI.getLatestEdgeById(type, relation.getId());
    if (edge == null) {
      throw UpdateException.entityNotFound(type, (T) relation);
    }

    validateIsMatchingRev(type, (T) relation, edge);

    EdgeConverter<T> converter = elementConverterFactory.forRelation(type);

    converter.updateModifiedAndRev(edge, relation);
    converter.updateElement(edge, relation);
  }

  @Override
  public long countEntities(Class<? extends Entity> type) {
    Class<? extends Entity> baseClass = TypeRegistry.getBaseClass(type);
    Iterator<Vertex> vertices = lowLevelAPI.getLatestVerticesOf(baseClass);

    return Iterators.size(vertices);
  }

  @Override
  public long countRelations(Class<? extends Relation> relationType) {
    @SuppressWarnings("unchecked")
    Class<? extends Relation> baseClass = (Class<? extends Relation>) TypeRegistry.toBaseDomainEntity(relationType);

    Iterator<Edge> edges = lowLevelAPI.getLatestEdgesOf(baseClass);

    return Iterators.size(edges);
  }

  @Override
  public <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id) throws StorageException {
    if (!TypeRegistry.isPrimitiveDomainEntity(type)) {
      throw new IllegalArgumentException("Only primitive DomainEntities can be deleted. " + type.getSimpleName() + " is not a primitive DomainEntity.");
    }

    Iterator<Vertex> vertices = lowLevelAPI.getVerticesWithId(type, id);

    if (!vertices.hasNext()) {
      throw new NoSuchEntityException(type, id);
    }

    this.deleteEntities(type, id);
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws StorageException {
    int numberOfDeletedEntities = deleteEntities(type, id);

    return numberOfDeletedEntities;
  }

  private <T extends Entity> int deleteEntities(Class<T> type, String id) {
    int numberOfDeletedEntities = 0;

    for (Iterator<Vertex> iterator = lowLevelAPI.getVerticesWithId(type, id); iterator.hasNext();) {
      Vertex vertex = iterator.next();

      for (Edge edge : vertex.getEdges(Direction.BOTH)) {
        db.removeEdge(edge);
      }

      db.removeVertex(vertex);
      numberOfDeletedEntities++;
    }
    return numberOfDeletedEntities;
  }

  @Override
  public <T extends DomainEntity> T getDomainEntityRevision(Class<T> type, String id, int revision) throws StorageException {
    Vertex vertex = lowLevelAPI.getVertexWithRevision(type, id, revision);

    if (vertex == null) {
      return null;
    }

    VertexConverter<T> converter = elementConverterFactory.forType(type);

    T entity = converter.convertToEntity(vertex);

    return hasPID(entity) ? entity : null;
  }

  private <T extends DomainEntity> boolean hasPID(T entity) {
    return entity.getPid() != null;
  }

  @Override
  public <T extends Relation> T getRelationRevision(Class<T> type, String id, int revision) throws StorageException {
    Edge edge = lowLevelAPI.getEdgeWithRevision(type, id, revision);

    if (edge == null) {
      return null;
    }

    EdgeConverter<T> converter = elementConverterFactory.forRelation(type);
    T relation = converter.convertToEntity(edge);

    return hasPID(relation) ? relation : null;
  }

  @Override
  public <T extends DomainEntity> void setDomainEntityPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException {
    Vertex vertex = lowLevelAPI.getLatestVertexById(type, id);

    if (vertex == null) {
      throw new NoSuchEntityException(type, id);
    }

    VertexConverter<T> converter = elementConverterFactory.forType(type);

    T entity = converter.convertToEntity(vertex);
    validateEntityHasNoPID(type, entity);

    entity.setPid(pid);
    // TODO create a method to update the pid. see TIM-178
    converter.addValuesToElement(vertex, entity);

    lowLevelAPI.duplicate(vertex);

  }

  private <T extends DomainEntity> void validateEntityHasNoPID(Class<T> type, T entity) {
    if (hasPID(entity)) {
      throw new IllegalStateException(String.format("%s with %s already has a pid: %s", type.getSimpleName(), entity.getId(), entity.getPid()));
    }
  }

  @Override
  public <T extends Relation> void setRelationPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException {
    Edge edge = lowLevelAPI.getLatestEdgeById(type, id);
    if (edge == null) {
      throw new NoSuchEntityException(type, id);
    }

    EdgeConverter<T> converter = elementConverterFactory.forRelation(type);
    T relation = converter.convertToEntity(edge);

    validateEntityHasNoPID(type, relation);

    relation.setPid(pid);
    converter.addValuesToElement(edge, relation);

    lowLevelAPI.duplicate(edge);

  }

  @Override
  public void close() {
    db.shutdown();
    available = false;
  }

  @Override
  public boolean isAvailable() {
    // Tinkerpop has no way to check if the database is available, 
    // so we use a boolean that is set when close is called.
    return available;
  }

  @Override
  public <T extends Entity> T findEntityByProperty(Class<T> type, String field, String value) throws StorageException {
    T entity = null;
    VertexConverter<T> converter = elementConverterFactory.forType(type);
    String propertyName = converter.getPropertyName(field);

    Iterator<Vertex> iterator = lowLevelAPI.findVerticesByProperty(type, propertyName, value);

    if (iterator.hasNext()) {
      entity = converter.convertToEntity(iterator.next());
    }

    return entity;
  }

  @Override
  public <T extends Relation> T findRelationByProperty(Class<T> type, String field, String value) throws StorageException {
    T relation = null;
    EdgeConverter<T> converter = elementConverterFactory.forRelation(type);
    String propertyName = converter.getPropertyName(field);

    Iterator<Edge> edges = null;

    switch (propertyName) {
      case Relation.SOURCE_ID:
        edges = lowLevelAPI.findEdgesBySource(type, value);
        break;
      case Relation.TARGET_ID:
        edges = lowLevelAPI.findEdgesByTarget(type, value);
        break;
      default:
        edges = lowLevelAPI.findEdgesByProperty(type, propertyName, value);
        break;
    }

    if (edges.hasNext()) {
      relation = converter.convertToEntity(edges.next());
    }

    return relation;
  }

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public boolean entityExists(Class<? extends Entity> type, String id) {
    return lowLevelAPI.getLatestVertexById(type, id) != null;
  }

  @Override
  public boolean relationExists(Class<? extends Relation> relationType, String id) {
    return lowLevelAPI.getLatestEdgeById(relationType, id) != null;
  }

  @Override
  public <T extends Relation> T findRelation(Class<T> relationType, String sourceId, String targetId, String relationTypeId) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends DomainEntity> List<String> getIdsOfNonPersistentDomainEntities(Class<T> type) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> List<String> getIdsOfNonPersistentRelations(Class<T> type) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Entity> T getDefaultVariation(Class<T> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
