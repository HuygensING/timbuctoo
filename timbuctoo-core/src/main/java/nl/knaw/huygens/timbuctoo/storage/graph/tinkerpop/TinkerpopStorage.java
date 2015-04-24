package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getRevisionProperty;

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

import com.google.inject.Inject;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TinkerpopStorage implements GraphStorage {

  private final Graph db;
  private final ElementConverterFactory elementConverterFactory;
  private final TinkerpopLowLevelAPI lowLevelAPI;
  private final TypeRegistry typeRegistry;

  @Inject
  public TinkerpopStorage(Graph db, TypeRegistry typeRegistry) {
    this(db, new ElementConverterFactory(typeRegistry), new TinkerpopLowLevelAPI(db), typeRegistry);
  }

  public TinkerpopStorage(Graph db, ElementConverterFactory elementConverterFactory, TinkerpopLowLevelAPI lowLevelAPI, TypeRegistry typeRegistry) {
    this.db = db;
    this.elementConverterFactory = elementConverterFactory;
    this.lowLevelAPI = lowLevelAPI;
    this.typeRegistry = typeRegistry;
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

    Edge edge = db.addEdge(null, sourceVertex, targetVertex, regularRelationName);
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
    throw new UnsupportedOperationException("Yet to be implemented");
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
    Vertex vertex = lowLevelAPI.getLatestVertexById(type, entity.getId());

    if (vertex == null) {
      throw UpdateException.entityNotFound(type, entity);
    }

    if (!isMatchingRev(entity, vertex)) {
      throw UpdateException.revisionNotFound(type, entity, entity.getRev());
    }

    VertexConverter<T> converter = elementConverterFactory.forType(type);
    converter.updateModifiedAndRev(vertex, entity);
    converter.updateElement(vertex, entity);

  }

  private <T extends Entity> boolean isMatchingRev(T entity, Element element) {
    // The difference between the reference of the entity and the property container should be one.
    // This is because the life cycle management is done outside this class.
    // So the revision should be updated before update is called.
    return (entity.getRev() - getRevisionProperty(element)) == 1;
  }

  @Override
  public <T extends DomainEntity> void addVariant(Class<T> type, T variant, Change change) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> void updateRelation(Class<T> type, Relation relation, Change change) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public long countEntities(Class<? extends Entity> type) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public long countRelations(Class<? extends Relation> relationType) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id, Change change) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
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
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends DomainEntity> void setDomainEntityPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> void setRelationPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public boolean isAvailable() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Entity> T findEntityByProperty(Class<T> type, String field, String value) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> T findRelationByProperty(Class<T> type, String field, String value) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
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
    throw new UnsupportedOperationException("Yet to be implemented");
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
