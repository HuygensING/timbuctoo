package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getIdProperty;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getRevisionProperty;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getTypes;

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
import nl.knaw.huygens.timbuctoo.storage.NoSuchRelationException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TinkerPopStorage implements GraphStorage {

  private boolean available = true;
  private final Graph db;
  private final ElementConverterFactory elementConverterFactory;
  private final TinkerPopLowLevelAPI lowLevelAPI;
  private final TypeRegistry typeRegistry;
  private final TinkerPopStorageIteratorFactory storageIteratorFactory;
  private final TinkerPopQueryFactory queryFactory;

  @Inject
  public TinkerPopStorage(Graph db, TypeRegistry typeRegistry) {
    this(db, new ElementConverterFactory(typeRegistry), new TinkerPopLowLevelAPI(db), typeRegistry);
  }

  public TinkerPopStorage(Graph db, ElementConverterFactory elementConverterFactory, TinkerPopLowLevelAPI lowLevelAPI, TypeRegistry typeRegistry) {
    this(db, elementConverterFactory, lowLevelAPI, typeRegistry, new TinkerPopStorageIteratorFactory(elementConverterFactory), new TinkerPopQueryFactory());
  }

  public TinkerPopStorage(Graph db, ElementConverterFactory elementConverterFactory, TinkerPopLowLevelAPI lowLevelAPI, TypeRegistry typeRegistry,
      TinkerPopStorageIteratorFactory storageIteratorFactory, TinkerPopQueryFactory queryFactory) {
    this.db = db;
    this.elementConverterFactory = elementConverterFactory;
    this.lowLevelAPI = lowLevelAPI;
    this.typeRegistry = typeRegistry;
    this.storageIteratorFactory = storageIteratorFactory;
    this.queryFactory = queryFactory;
  }

  @Override
  public <T extends DomainEntity> void addDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    Vertex vertex = db.addVertex(null);
    VertexAddAction<T> addAction = new VertexAddAction<T>(type, entity, vertex) {
      @Override
      protected VertexConverter<T> converter() {
        return elementConverterFactory.compositeForType(getType());
      }
    };

    VertexRollbackAction rollbackAction = new VertexRollbackAction(vertex);

    new RevertableAddition().execute(addAction, rollbackAction);

  }

  @Override
  public <T extends SystemEntity> void addSystemEntity(Class<T> type, T entity) throws StorageException {
    Vertex vertex = db.addVertex(null);

    VertexAddAction<T> addAction = new VertexAddAction<T>(type, entity, vertex);
    VertexRollbackAction rollbackAction = new VertexRollbackAction(vertex);

    new RevertableAddition().execute(addAction, rollbackAction);
  }

  private class VertexRollbackAction implements RollbackAction {
    private final Vertex vertex;

    public VertexRollbackAction(Vertex vertex) {
      this.vertex = vertex;
    }

    @Override
    public void execute() {
      db.removeVertex(vertex);
    }
  }

  private class VertexAddAction<T extends Entity> implements AddAction {
    private final Class<T> type;
    private final T entity;
    private final Vertex vertex;

    public VertexAddAction(Class<T> type, T entity, Vertex vertex) {
      this.type = type;
      this.entity = entity;
      this.vertex = vertex;
    }

    @Override
    public void execute() throws ConversionException {
      converter().addValuesToElement(vertex, entity);
    }

    protected VertexConverter<T> converter() {
      VertexConverter<T> converter = elementConverterFactory.forType(getType());
      return converter;
    }

    protected Class<T> getType() {
      return type;
    }

  }

  @Override
  public <T extends Relation> void addRelation(Class<T> type, Relation relation, Change change) throws StorageException {
    Vertex sourceVertex = getDomainEntityRelationPart(relation.getSourceType(), relation.getSourceId());
    Vertex targetVertex = getDomainEntityRelationPart(relation.getTargetType(), relation.getTargetId());
    Vertex relationTypeVertex = getSystemEntityRelationPart(relation.getTypeType(), relation.getTypeId());

    String regularRelationName = getRegularRelationName(relationTypeVertex);

    Edge edge = sourceVertex.addEdge(regularRelationName, targetVertex);

    EdgeAddAction<T> addAction = new EdgeAddAction<T>(type, type.cast(relation), edge);
    EdgeRollbackAction rollbackAction = new EdgeRollbackAction(edge);

    new RevertableAddition().execute(addAction, rollbackAction);
  }

  private class EdgeRollbackAction implements RollbackAction {
    private final Edge edge;

    public EdgeRollbackAction(Edge edge) {
      this.edge = edge;
    }

    @Override
    public void execute() {
      db.removeEdge(edge);
    }
  }

  private static interface RollbackAction {
    public void execute();
  }

  private class EdgeAddAction<T extends Relation> implements AddAction {
    private final Class<T> type;
    private final T entity;
    private final Edge edge;

    public EdgeAddAction(Class<T> type, T entity, Edge edge) {
      this.type = type;
      this.entity = entity;
      this.edge = edge;
    }

    @Override
    public void execute() throws ConversionException {
      EdgeConverter<T> converter = elementConverterFactory.compositeForRelation(type);
      converter.addValuesToElement(edge, entity);
    }
  }

  private static interface AddAction {
    public void execute() throws ConversionException;
  }

  private class RevertableAddition {
    public final void execute(AddAction addAction, RollbackAction rollbackAction) throws StorageException {
      try {
        addAction.execute();
      } catch (ConversionException e) {
        rollbackAction.execute();
        throw e;
      }
    }
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
  public <T extends DomainEntity> void deleteVariant(T variant) throws NoSuchEntityException, StorageException {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) variant.getClass();

    String id = variant.getId();
    Vertex vertex = lowLevelAPI.getLatestVertexById(type, id);

    if (vertex == null) {
      throw new NoSuchEntityException(type, id);
    }

    VertexConverter<T> converter = elementConverterFactory.forType(type);
    converter.updateModifiedAndRev(vertex, variant);
    converter.removeVariant(vertex);
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
    validateIsPrimitive(type);

    List<T> variations = Lists.newArrayList();
    Vertex vertex = lowLevelAPI.getLatestVertexById(type, id);

    if (vertex != null) {
      List<String> typeNames = getTypes(vertex);

      for (String typeName : typeNames) {
        Class<? extends DomainEntity> domainEntityType = typeRegistry.getDomainEntityType(typeName);
        VertexConverter<? extends DomainEntity> converter = elementConverterFactory.forType(domainEntityType);

        variations.add(type.cast(converter.convertToEntity(vertex)));
      }
    }

    return variations;
  }

  @Override
  public <T extends Relation> List<T> getAllVariationsOfRelation(Class<T> type, String id) throws StorageException {
    validateIsPrimitive(type);
    List<T> relations = Lists.newArrayList();

    Edge edge = lowLevelAPI.getLatestEdgeById(type, id);

    if (edge != null) {
      for (String typeName : getTypes(edge)) {
        EdgeConverter<? extends Relation> converter = elementConverterFactory.forRelation(getRelationType(typeName));
        relations.add(type.cast(converter.convertToEntity(edge)));
      }
    }

    return relations;
  }

  // TODO move to TypeRegistry
  @SuppressWarnings("unchecked")
  private Class<? extends Relation> getRelationType(String typeName) {
    return (Class<? extends Relation>) typeRegistry.getDomainEntityType(typeName);
  }

  private <T extends Entity> void validateIsPrimitive(Class<T> type) {
    Preconditions.checkArgument(TypeRegistry.isPrimitiveDomainEntity(type), "Nonprimitive type %s", type);
  }

  @Override
  public <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException {
    Vertex vertex = lowLevelAPI.getLatestVertexById(id);

    Iterable<Edge> edges = null;
    if (vertex == null) {
      edges = Lists.newArrayList();
    } else {
      edges = vertex.getEdges(Direction.BOTH);
    }

    return storageIteratorFactory.createForRelation(type, edges);
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
    T relation = null;
    Vertex source = lowLevelAPI.getLatestVertexById(sourceId);
    Vertex target = lowLevelAPI.getLatestVertexById(targetId);

    String edgeLabel = getRegularRelationName(lowLevelAPI.getLatestVertexById(RelationType.class, relationTypeId));

    if (source != null) {
      Edge latestEdge = null;
      for (Edge edge : source.getEdges(Direction.OUT, edgeLabel)) {
        if (edge.getVertex(Direction.IN) == target) {
          if (latestEdge == null || getRevisionProperty(edge) > getRevisionProperty(latestEdge)) {
            latestEdge = edge;
          }
        }
      }

      EdgeConverter<T> converter = elementConverterFactory.forRelation(relationType);

      if (latestEdge != null) {
        relation = converter.convertToEntity(latestEdge);
      }
    }

    return relation;
  }

  @Override
  public <T extends Relation> StorageIterator<T> findRelations(Class<T> relationType, String sourceId, String targetId, String relationTypeId) {
    List<Edge> edges = Lists.newArrayList();

    TinkerPopQuery query = queryFactory.newQuery();
    query.hasNotNullProperty(Relation.TYPE_ID, relationTypeId);

    Vertex source = getVertexIfIdIsNotNull(sourceId);
    Vertex target = getVertexIfIdIsNotNull(targetId);

    Iterator<Edge> foundEdges = lowLevelAPI.findLatestEdges(query);

    for (; foundEdges.hasNext();) {
      Edge edge = foundEdges.next();
      if (vertexIsEqualOrNull(source, edge, Direction.OUT) && vertexIsEqualOrNull(target, edge, Direction.IN)) {
        edges.add(edge);
      }
    }

    return storageIteratorFactory.createForRelation(relationType, edges);
  }

  private Vertex getVertexIfIdIsNotNull(String targetId) {
    return targetId != null ? lowLevelAPI.getLatestVertexById(targetId) : null;
  }

  private boolean vertexIsEqualOrNull(Vertex vertex, Edge edge, Direction direction) {
    return vertex == null || edge.getVertex(direction).equals(vertex);
  }

  @Override
  public <T extends DomainEntity> List<String> getIdsOfNonPersistentDomainEntities(Class<T> type) {
    List<String> ids = Lists.newArrayList();
    Iterator<Vertex> vertices = lowLevelAPI.findVerticesWithoutProperty(type, DomainEntity.PID);

    for (; vertices.hasNext();) {
      ids.add(getIdProperty(vertices.next()));
    }

    return ids;
  }

  @Override
  public <T extends Relation> List<String> getIdsOfNonPersistentRelations(Class<T> type) {
    List<String> ids = Lists.newArrayList();
    Iterator<Edge> edges = lowLevelAPI.findEdgesWithoutProperty(type, DomainEntity.PID);

    for (; edges.hasNext();) {
      ids.add(getIdProperty(edges.next()));
    }

    return ids;
  }

  // TODO make only available for DomainEntities see TIM-162
  @Override
  public <T extends Entity> T getDefaultVariation(Class<T> type, String id) throws StorageException {
    T entity = null;
    Class<? extends Entity> primitiveType = TypeRegistry.getBaseClass(type);

    Vertex vertex = lowLevelAPI.getLatestVertexById(primitiveType, id);

    if (vertex != null) {
      VertexConverter<? super T> converter = elementConverterFactory.forPrimitiveOf(type);

      entity = converter.convertToSubType(type, vertex);
    }

    return entity;
  }

  @Override
  public <T extends DomainEntity> void removePropertyFromEntity(Class<T> type, String id, String fieldName) throws NoSuchEntityException {
    Vertex vertex = getVertexIfExists(type, id);

    VertexConverter<T> converter = elementConverterFactory.forType(type);

    converter.removePropertyByFieldName(vertex, fieldName);
  }

  @Override
  public <T extends Relation> void removePropertyFromRelation(Class<T> type, String id, String fieldName) throws NoSuchRelationException {
    Edge edge = lowLevelAPI.getLatestEdgeById(type, id);

    if (edge == null) {
      throw new NoSuchRelationException(type, id);
    }

    EdgeConverter<T> converter = elementConverterFactory.forRelation(type);

    converter.removePropertyByFieldName(edge, fieldName);

  }

}
