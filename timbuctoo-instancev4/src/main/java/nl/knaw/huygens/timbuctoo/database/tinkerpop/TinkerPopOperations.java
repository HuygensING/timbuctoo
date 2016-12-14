package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.core.AlreadyUpdatedException;
import nl.knaw.huygens.timbuctoo.core.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.core.NotFoundException;
import nl.knaw.huygens.timbuctoo.core.RelationNotPossibleException;
import nl.knaw.huygens.timbuctoo.core.dto.CreateCollection;
import nl.knaw.huygens.timbuctoo.core.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.core.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.core.dto.DataStream;
import nl.knaw.huygens.timbuctoo.core.dto.DirectionalRelationType;
import nl.knaw.huygens.timbuctoo.core.dto.EntityRelation;
import nl.knaw.huygens.timbuctoo.core.dto.ImmutableEntityRelation;
import nl.knaw.huygens.timbuctoo.core.dto.QuickSearch;
import nl.knaw.huygens.timbuctoo.core.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.core.dto.RelationType;
import nl.knaw.huygens.timbuctoo.core.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.core.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.CollectionBuilder;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.ImmutableVresDto;
import nl.knaw.huygens.timbuctoo.core.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.changelistener.ChangeListener;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.conversion.TinkerPopPropertyConverter;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.conversion.TinkerPopToEntityMapper;
import nl.knaw.huygens.timbuctoo.logging.Logmarkers;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.VreBuilder;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.rdf.SystemPropertyModifier;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import nl.knaw.huygens.timbuctoo.server.databasemigration.DatabaseMigrator;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.ERROR_PREFIX;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.SAVED_MAPPING_STATE;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.COLLECTION_ENTITIES_LABEL;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.COLLECTION_IS_UNKNOWN_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.IS_RELATION_COLLECTION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.database.tinkerpop.EdgeManipulator.duplicateEdge;
import static nl.knaw.huygens.timbuctoo.database.tinkerpop.VertexDuplicator.VERSION_OF;
import static nl.knaw.huygens.timbuctoo.database.tinkerpop.VertexDuplicator.duplicateVertex;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.configurationFailure;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.arrayToEncodedArray;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.BulkUploadedDataSource.HAS_NEXT_ERROR;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;

public class TinkerPopOperations implements DataStoreOperations {
  private static final String RDF_URI_PROP = "rdfUri";
  private static final Logger LOG = LoggerFactory.getLogger(TinkerPopOperations.class);
  private final Transaction transaction;
  private final ChangeListener listener;
  private final EntityFetcher entityFetcher;
  private final GraphTraversalSource traversal;
  private final GraphTraversalSource latestState;
  private final Vres mappings;
  private final Graph graph;
  private final IndexHandler indexHandler;
  private final SystemPropertyModifier systemPropertyModifier;
  private boolean requireCommit = false; //we only need an explicit success() call when the database is changed
  private Optional<Boolean> isSuccess = Optional.empty();

  public TinkerPopOperations(TinkerPopGraphManager graphManager, ChangeListener listener,
                             GremlinEntityFetcher entityFetcher, Vres mappings, IndexHandler indexHandler) {
    graph = graphManager.getGraph();
    this.indexHandler = indexHandler;
    this.transaction = graph.tx();
    this.listener = listener;
    this.entityFetcher = entityFetcher;

    if (!transaction.isOpen()) {
      transaction.open();
    }
    this.traversal = graph.traversal();
    this.latestState = graphManager.getLatestState();
    this.mappings = mappings == null ? loadVres() : mappings;
    this.systemPropertyModifier = new SystemPropertyModifier(Clock.systemDefaultZone());
  }

  private static UUID asUuid(String input, Element source) {
    try {
      return UUID.fromString(input);
    } catch (IllegalArgumentException e) {
      LOG.error(databaseInvariant, "wrongly formatted UUID as tim_id: " + input + " on " +
        source.id());
      return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
  }

  private static EntityRelation makeEntityRelation(Edge edge, Collection collection) {
    final String acceptedPropName = collection.getEntityTypeName() + "_accepted";

    return ImmutableEntityRelation.builder()
                                  .isAccepted(getRequiredProp(edge, acceptedPropName, false))
                                  .timId(asUuid(getRequiredProp(edge, "tim_id", ""), edge))
                                  .revision(getRequiredProp(edge, "rev", -1))
                                  .build();
  }

  @SuppressWarnings("unchecked")
  private static <V> V getRequiredProp(final Element element, final String key, V valueOnException) {
    try {
      Iterator<? extends Property<Object>> revProp = element.properties(key);
      if (revProp.hasNext()) {
        Object value = revProp.next().value();
        return (V) valueOnException.getClass().cast(value);
      } else {
        LOG.error(databaseInvariant, "Value is missing for property " + key + " on element with id " + element.id());
        return valueOnException;
      }
    } catch (RuntimeException e) {
      LOG.error(databaseInvariant, "Something went wrong while getting the property " + key + " from the element " +
        "with id " + (element != null ? element.id() : "<NULL>") + ": " + e.getMessage());
      return valueOnException;
    }
  }

  private static Optional<Vertex> getEntityByFullIteration(GraphTraversalSource traversal, UUID id) {
    return getFirst(traversal
      .V()
      .has("tim_id", id.toString())
      .not(has("deleted", true))
      .has("isLatest", true));
  }

  private static <T> Optional<T> getFirst(Traversal<?, T> traversal) {
    if (traversal.hasNext()) {
      return Optional.of(traversal.next());
    } else {
      return Optional.empty();
    }
  }

  private static Edge getExpectedEdge(GraphTraversalSource traversal, String timId) {
    GraphTraversal<Edge, Edge> edge = traversal.E().has("tim_id", timId);
    if (edge.hasNext()) {
      return edge.next();
    } else {
      throw new ObjectSuddenlyDisappearedException("The code assumes that the edge with id " + timId + " is " +
        "available, but it isn't!");
    }
  }

  private static Optional<RelationType> getRelationDescription(GraphTraversalSource traversal, UUID typeId) {
    return getFirst(traversal
      .V()
      //.has(T.label, LabelP.of("relationtype"))
      .has("tim_id", typeId.toString())
    )
      .map(RelationType::relationType);
  }

  private static String[] getEntityTypes(Element element) {
    try {
      String typesProp = getRequiredProp(element, "types", "");
      if (typesProp.equals("[]")) {
        LOG.error(databaseInvariant, "Entitytypes not present on vertex with ID " + element.id());
        return new String[0];
      } else {
        return arrayToEncodedArray.tinkerpopToJava(typesProp, String[].class);
      }
    } catch (IOException e) {
      LOG.error(databaseInvariant, "Could not parse entitytypes property on vertex with ID " + element.id());
      return new String[0];
    }
  }

  @Override
  public void clearMappingErrors(String vreName) {
    getRawCollectionsTraversal(vreName)
      .emit()
      .repeat(__.out(HAS_NEXT_ERROR))
      .forEachRemaining(vertex -> {
        vertex.edges(Direction.IN, HAS_NEXT_ERROR).forEachRemaining(Edge::remove);
        vertex.properties().forEachRemaining(property -> {
          if (property.key().startsWith(ERROR_PREFIX)) {
            property.remove();
          }
        });
      });
  }


  private GraphTraversal<Vertex, Vertex> getRawCollectionsTraversal(String vreName) {
    return traversal.V()
                    .hasLabel(Vre.DATABASE_LABEL)
                    .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
                    .out(RAW_COLLECTION_EDGE_NAME);
  }

  @Override
  public boolean hasMappingErrors(String vreName) {
    return getRawCollectionsTraversal(vreName).outE(HAS_NEXT_ERROR).hasNext();
  }

  @Override
  public void saveRmlMappingState(String vreName, String rdfData) {
    final GraphTraversal<Vertex, Vertex> vreT = traversal.V()
                                                         .hasLabel(Vre.DATABASE_LABEL)
                                                         .has(Vre.VRE_NAME_PROPERTY_NAME, vreName);
    if (vreT.hasNext()) {
      vreT.next().property(SAVED_MAPPING_STATE, rdfData);
    }
  }

  @Override
  public void success() {
    isSuccess = Optional.of(true);
  }

  @Override
  public void rollback() {
    isSuccess = Optional.of(false);
  }

  @Override
  public void close() {
    if (isSuccess.isPresent()) {
      if (isSuccess.get()) {
        transaction.commit();
      } else {
        transaction.rollback();
      }
    } else {
      transaction.rollback();
      if (requireCommit) {
        LOG.error("Transaction was not closed, rolling back. Please add an explicit rollback so that we know this " +
          "was not a missing success()");
      }
    }
    transaction.close();
  }

  @Override
  public UUID acceptRelation(Collection collection, CreateRelation createRelation) throws RelationNotPossibleException {
    UUID typeId = createRelation.getTypeId();
    String userId = createRelation.getCreated().getUserId();
    Instant instant = Instant.ofEpochMilli(createRelation.getCreated().getTimeStamp());

    requireCommit = true;
    RelationType descs = getRelationDescription(traversal, typeId)
      .orElseThrow(notPossible("Relation type " + typeId + " does not exist"));
    Vertex sourceV = getEntityByFullIteration(traversal, createRelation.getSourceId())
      .orElseThrow(notPossible("source is not present"));
    Vertex targetV = getEntityByFullIteration(traversal, createRelation.getTargetId())
      .orElseThrow(notPossible("target is not present"));

    //check if the relation already exists
    final Optional<EntityRelation> existingEdgeOpt = getEntityRelation(sourceV, targetV, typeId, collection);

    if (existingEdgeOpt.isPresent()) {
      final EntityRelation existingEdge = existingEdgeOpt.get();
      if (!existingEdge.isAccepted()) {
        //if not already an active relation
        updateRelation(existingEdge, collection, userId, true, instant);
      }
      return existingEdge.getTimId();
    } else {
      Collection sourceCollection = getOwnCollectionOfElement(collection.getVre(), sourceV)
        .orElseThrow(notPossible("Source vertex is not part of the VRE of " + collection.getCollectionName()));
      Collection targetCollection = getOwnCollectionOfElement(collection.getVre(), targetV)
        .orElseThrow(notPossible("Target vertex is not part of the VRE of " + collection.getCollectionName()));
      DirectionalRelationType desc = descs.getForDirection(sourceCollection, targetCollection)
                                          .orElseThrow(notPossible(
                                            "You can't have a " + descs.getOutName() + " relation from " +
                                              sourceCollection.getEntityTypeName() + " to " +
                                              targetCollection.getEntityTypeName() + " or vice versa"));

      return createRelation(sourceV, targetV, desc, userId, collection, true, instant);
    }
  }

  @Override
  public void createEntity(Collection col, Optional<Collection> baseCollection, CreateEntity input)
    throws IOException {

    requireCommit = true;

    Map<String, LocalProperty> mapping = col.getWriteableProperties();
    TinkerPopPropertyConverter colConverter = new TinkerPopPropertyConverter(col);
    Map<String, LocalProperty> baseMapping = baseCollection.isPresent() ?
      baseCollection.get().getWriteableProperties() : Maps.newHashMap();
    TinkerPopPropertyConverter baseColConverter = baseCollection.isPresent() ? // converter not needed without mapping
      new TinkerPopPropertyConverter(baseCollection.get()) : null;

    GraphTraversal<Vertex, Vertex> traversalWithVertex = traversal.addV();

    Vertex vertex = traversalWithVertex.next();
    for (TimProperty<?> property : input.getProperties()) {
      String fieldName = property.getName();
      if (mapping.containsKey(fieldName)) {
        try {
          String dbName = mapping.get(fieldName).getDatabasePropertyName();
          Tuple<String, Object> convertedProp = property.convert(colConverter);
          vertex.property(dbName, convertedProp.getRight());
        } catch (IOException e) {
          throw new IOException(fieldName + " could not be saved. " + e.getMessage(), e);
        }
      } else {
        throw new IOException(String.format("Items of %s have no property %s", col.getCollectionName(),
          fieldName));
      }

      if (baseMapping.containsKey(fieldName)) {
        try {
          property.convert(baseColConverter);
          Tuple<String, Object> convertedProp = property.convert(baseColConverter);
          baseMapping.get(fieldName).setValue(vertex, convertedProp.getRight());
        } catch (IOException e) {
          LOG.error(configurationFailure, "Field could not be parsed by Admin VRE converter {}_{}",
            baseCollection.get().getCollectionName(), fieldName);
        }
      }

    }

    setAdministrativeProperties(col, vertex, input);

    listener.onCreate(col, vertex);
    listener.onAddToCollection(col, Optional.empty(), vertex);
    baseCollection.ifPresent(baseCol -> listener.onAddToCollection(baseCol, Optional.empty(), vertex));

    duplicateVertex(traversal, vertex);
  }

  @Override
  public ReadEntity getEntity(UUID id, Integer rev, Collection collection,
                              CustomEntityProperties customEntityProperties,
                              CustomRelationProperties customRelationProperties) throws NotFoundException {
    GraphTraversal<Vertex, Vertex> fetchedEntity = entityFetcher.getEntity(
      traversal,
      id,
      rev,
      collection.getCollectionName()
    );

    if (!fetchedEntity.hasNext()) {
      throw new NotFoundException();
    }

    Vertex entityVertex = entityFetcher.getEntity(traversal, id, rev, collection.getCollectionName()).next();
    GraphTraversal<Vertex, Vertex> entityT = traversal.V(entityVertex.id());

    if (!entityT.asAdmin().clone().hasNext()) {
      throw new NotFoundException();
    }

    String entityTypesStr = getProp(entityT.asAdmin().clone().next(), "types", String.class).orElse("[]");
    if (!entityTypesStr.contains("\"" + collection.getEntityTypeName() + "\"")) {
      throw new NotFoundException();
    }

    return new TinkerPopToEntityMapper(collection, traversal, mappings, customEntityProperties,
      customRelationProperties).mapEntity(entityT, true);
  }

  @Override
  public Optional<ReadEntity> searchEntityByRdfUri(Collection collection, String uri, boolean withRelations) {
    GraphTraversal<Vertex, Vertex> entityT = latestState.V().has("rdfUri", uri).has("isLatest", true);

    if (!entityT.asAdmin().clone().hasNext()) {
      return Optional.empty();
    }

    String entityTypesStr = getProp(entityT.asAdmin().clone().next(), "types", String.class).orElse("[]");
    if (!entityTypesStr.contains("\"" + collection.getEntityTypeName() + "\"")) {
      return Optional.empty();
    }

    return Optional.of(new TinkerPopToEntityMapper(collection, traversal, mappings).mapEntity(entityT, withRelations));
  }

  @Override
  public List<RelationType> getRelationTypes() {
    return traversal.V().has(T.label, LabelP.of("relationtype")).toList().stream()
                    .map(RelationType::relationType).collect(Collectors.toList());
  }


  @Override
  public DataStream<ReadEntity> getCollection(Collection collection, int start, int rows,
                                              boolean withRelations,
                                              CustomEntityProperties customEntityProperties,
                                              CustomRelationProperties customRelationProperties) {
    GraphTraversal<Vertex, Vertex> entities =
      getCurrentEntitiesFor(collection.getEntityTypeName()).range(start, start + rows);

    TinkerPopToEntityMapper tinkerPopToEntityMapper =
      new TinkerPopToEntityMapper(collection, traversal, mappings, customEntityProperties, customRelationProperties);

    return new TinkerPopGetCollection(
      entities.toStream().map(vertex -> tinkerPopToEntityMapper.mapEntity(vertex, withRelations))
    );
  }

  @Override
  public List<ReadEntity> doQuickSearch(Collection collection, QuickSearch quickSearch, int limit) {
    GraphTraversal<Vertex, Vertex> result = indexHandler.findByQuickSearch(collection, quickSearch);

    return asReadEntityList(collection, result.limit(limit));
  }

  @Override
  public List<ReadEntity> doKeywordQuickSearch(Collection collection, String keywordType, QuickSearch quickSearch,
                                               int limit) {
    GraphTraversal<Vertex, Vertex> result =
      indexHandler.findKeywordsByQuickSearch(collection, quickSearch, keywordType);

    return asReadEntityList(collection, result.limit(limit));
  }

  private List<ReadEntity> asReadEntityList(Collection collection, GraphTraversal<Vertex, Vertex> result) {
    TinkerPopToEntityMapper tinkerPopToEntityMapper = new TinkerPopToEntityMapper(
      collection,
      traversal,
      mappings,
      (traversalSource, vre) -> {

      },
      (entity1, entityVertex, target, relationRef) -> {

      });

    return result.map(vertex -> tinkerPopToEntityMapper.mapEntity(vertex.get(), false)).toList();
  }

  @Override
  public int replaceEntity(Collection collection, UpdateEntity updateEntity)
    throws NotFoundException, AlreadyUpdatedException, IOException {

    requireCommit = true;

    GraphTraversal<Vertex, Vertex> entityTraversal = entityFetcher.getEntity(
      this.traversal,
      updateEntity.getId(),
      null,
      collection.getCollectionName()
    );


    if (!entityTraversal.hasNext()) {
      throw new NotFoundException();
    }

    Vertex entityVertex = entityTraversal.next();

    int curRev = getProp(entityVertex, "rev", Integer.class).orElse(1);
    if (curRev != updateEntity.getRev()) {
      throw new AlreadyUpdatedException();
    }

    int newRev = updateEntity.getRev() + 1;
    entityVertex.property("rev", newRev);

    // update properties
    TinkerPopPropertyConverter tinkerPopPropertyConverter = new TinkerPopPropertyConverter(collection);
    for (TimProperty<?> property : updateEntity.getProperties()) {
      try {
        Tuple<String, Object> nameValue = property.convert(tinkerPopPropertyConverter);

        collection.getWriteableProperties().get(nameValue.getLeft()).setValue(entityVertex, nameValue.getRight());
      } catch (IOException e) {
        throw new IOException(property.getName() + " could not be saved. " + e.getMessage(), e);
      }
    }

    // Set removed values to null.
    Set<String> propertyNames = updateEntity.getProperties().stream()
                                            .map(prop -> prop.getName())
                                            .collect(Collectors.toSet());
    for (String name : Sets.difference(collection.getWriteableProperties().keySet(),
      propertyNames)) {
      collection.getWriteableProperties().get(name).setJson(entityVertex, null);
    }

    String entityTypesStr = getProp(entityVertex, "types", String.class).orElse("[]");
    boolean wasAddedToCollection = false;
    if (!entityTypesStr.contains("\"" + collection.getEntityTypeName() + "\"")) {
      try {
        ArrayNode entityTypes = arrayToEncodedArray.tinkerpopToJson(entityTypesStr);
        entityTypes.add(collection.getEntityTypeName());

        entityVertex.property("types", entityTypes.toString());
        wasAddedToCollection = true;
      } catch (IOException e) {
        // FIXME potential bug?
        LOG.error(Logmarkers.databaseInvariant, "property 'types' was not parseable: " + entityTypesStr);
      }
    }

    setModified(entityVertex, updateEntity.getModified());
    entityVertex.property("pid").remove();

    Optional<Vertex> prevVertex = getPrevVertex(collection, entityVertex);
    listener.onPropertyUpdate(collection, prevVertex, entityVertex);
    if (wasAddedToCollection) {
      listener.onAddToCollection(collection, prevVertex, entityVertex);
    }

    duplicateVertex(traversal, entityVertex);
    return newRev;
  }

  @Override
  public void replaceRelation(Collection collection, UpdateRelation updateRelation) throws NotFoundException {
    replaceRelation(collection, updateRelation.getId(), updateRelation.getRev(), updateRelation.getAccepted(),
      updateRelation.getModified().getUserId(),
      Instant.ofEpochMilli(updateRelation.getModified().getTimeStamp()));
  }

  private void replaceRelation(Collection collection, UUID id, int rev, boolean accepted, String userId,
                               Instant instant)
    throws NotFoundException {

    requireCommit = true;

    // FIXME: string concatenating methods like this should be delegated to a configuration class
    final String acceptedPropName = collection.getEntityTypeName() + "_accepted";


    // FIXME: throw a AlreadyUpdatedException when the rev of the client is not the latest
    Edge origEdge;
    try {
      origEdge = traversal.E()
                          .has("tim_id", id.toString())
                          .has("isLatest", true)
                          .has("rev", rev)
                          .next();
    } catch (NoSuchElementException e) {
      throw new NotFoundException();
    }

    //FIXME: throw a distinct Exception when the client tries to save a relation with wrong source, target or type.

    Edge edge = duplicateEdge(origEdge);
    edge.property(acceptedPropName, accepted);
    edge.property("rev", getProp(origEdge, "rev", Integer.class).orElse(1) + 1);
    setModified(edge, userId, instant);
  }

  @Override
  public int deleteEntity(Collection collection, UUID id, Change modified)
    throws NotFoundException {

    requireCommit = true;

    GraphTraversal<Vertex, Vertex> entityTraversal = entityFetcher.getEntity(traversal, id, null,
      collection.getCollectionName());

    if (!entityTraversal.hasNext()) {
      throw new NotFoundException();
    }

    Vertex entity = entityTraversal.next();
    String entityTypesStr = getProp(entity, "types", String.class).orElse("[]");
    boolean wasRemoved = false;
    if (entityTypesStr.contains("\"" + collection.getEntityTypeName() + "\"")) {
      try {
        ArrayNode entityTypes = arrayToEncodedArray.tinkerpopToJson(entityTypesStr);
        if (entityTypes.size() == 1) {
          entity.property("deleted", true);
          wasRemoved = true;
        } else {
          for (int i = entityTypes.size() - 1; i >= 0; i--) {
            JsonNode val = entityTypes.get(i);
            if (val != null && val.asText("").equals(collection.getEntityTypeName())) {
              entityTypes.remove(i);
              wasRemoved = true;
            }
          }
          entity.property("types", entityTypes.toString());
        }
      } catch (IOException e) {
        LOG.error(Logmarkers.databaseInvariant, "property 'types' was not parseable: " + entityTypesStr);
      }
    } else {
      throw new NotFoundException();
    }

    int newRev = getProp(entity, "rev", Integer.class).orElse(1) + 1;
    entity.property("rev", newRev);

    entity.edges(Direction.BOTH).forEachRemaining(edge -> {
      // Skip the hasEntity and the VERSION_OF edge, which are not real relations, but system edges
      if (edge.label().equals(HAS_ENTITY_RELATION_NAME) || edge.label().equals(VERSION_OF)) {
        return;
      }
      Optional<Collection> ownEdgeCol = getOwnCollectionOfElement(collection.getVre(), edge);
      if (ownEdgeCol.isPresent()) {
        edge.property(ownEdgeCol.get().getEntityTypeName() + "_accepted", false);
      }
    });

    setModified(entity, modified);
    entity.property("pid").remove();
    if (wasRemoved) {
      listener.onRemoveFromCollection(collection, getPrevVertex(collection, entity), entity);
    }

    duplicateVertex(traversal, entity);

    return newRev;
  }

  @Override
  public Vres loadVres() {
    ImmutableVresDto.Builder builder = ImmutableVresDto.builder();

    traversal.V().has(T.label, LabelP.of(Vre.DATABASE_LABEL)).forEachRemaining(vreVertex -> {
      final Vre vre = Vre.load(vreVertex);
      builder.putVres(vre.getVreName(), vre);
    });

    return builder.build();
  }

  @Override
  public boolean databaseIsEmptyExceptForMigrations() {
    return !traversal.V()
                     .not(has("type", DatabaseMigrator.EXECUTED_MIGRATIONS_TYPE))
                     .hasNext();
  }

  @Override
  public void initDb(Vres mappings, RelationType... relationTypes) {
    requireCommit = true;
    //FIXME: add security
    saveVres(mappings);
    saveRelationTypes(relationTypes);
  }

  @Override
  public void saveRelationTypes(RelationType... relationTypes) {
    for (RelationType relationType : relationTypes) {
      saveRelationType(relationType);
    }
  }

  @Override
  public void saveVre(Vre vre) {
    vre.save(graph);
  }

  @Override
  public Vre ensureVreExists(String vreName) {
    Vre vre = VreBuilder.vre(vreName, vreName)
                        .withCollection(vreName + "relations", CollectionBuilder::isRelationCollection)
                        .build();
    saveVre(vre);
    return vre;
  }

  @Override
  public void removeCollectionsAndEntities(String vreName) {
    traversal
      .V()
      .hasLabel(Vre.DATABASE_LABEL)
      .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
      .out("hasCollection")
      .not(has(Collection.IS_RELATION_COLLECTION_PROPERTY_NAME, true))
      .union(
        __.out("hasDisplayName"),
        __.out("hasProperty"),
        __.out("hasEntityNode")
          .union(
            __.out("hasEntity"), //the entities
            __.identity() //the entityNodes container
          ),
        __.identity() //the collection
      )
      .drop()
      .toList();//force traversal and thus side-effects
  }

  /*******************************************************************************************************************
   * Support methods:
   ******************************************************************************************************************/
  private Supplier<RelationNotPossibleException> notPossible(String message) {
    return () -> new RelationNotPossibleException(message);
  }

  private Optional<Collection> getOwnCollectionOfElement(Vre vre, Element sourceV) {
    String ownType = vre.getOwnType(getEntityTypes(sourceV));
    if (ownType == null) {
      return Optional.empty();
    }
    return Optional.of(vre.getCollectionForTypeName(ownType));
  }

  private Optional<EntityRelation> getEntityRelation(Vertex sourceV, Vertex targetV, UUID typeId,
                                                     Collection collection) {
    return stream(sourceV.edges(Direction.BOTH))
      .filter(e ->
        (e.inVertex().id().equals(targetV.id()) || e.outVertex().id().equals(targetV.id())) &&
          getRequiredProp(e, "typeId", "").equals(typeId.toString())
      )
      //sort by rev (ascending)
      .sorted((o1, o2) -> getRequiredProp(o1, "rev", -1).compareTo(getRequiredProp(o2, "rev", -1)))
      //get last element, i.e. with the highest rev, i.e. the most recent
      .reduce((o1, o2) -> o2)
      .map(edge -> makeEntityRelation(edge, collection));
  }

  private void updateRelation(EntityRelation existingEdge, Collection collection, String userId, boolean accepted,
                              Instant time) {
    final Edge origEdge = getExpectedEdge(traversal, existingEdge.getTimId().toString());
    final Edge newEdge = EdgeManipulator.duplicateEdge(origEdge);
    newEdge.property(collection.getEntityTypeName() + "_accepted", accepted);
    newEdge.property("rev", existingEdge.getRevision() + 1);
    setModified(newEdge, userId, time);
  }

  private UUID createRelation(Vertex source, Vertex target, DirectionalRelationType relationType,
                              String userId, Collection collection, boolean accepted, Instant time) {
    UUID id = UUID.randomUUID();
    Edge edge = source.addEdge(
      relationType.getDbName(),
      target,
      collection.getEntityTypeName() + "_accepted", accepted,
      "types", jsnA(
        jsn(collection.getEntityTypeName()),
        jsn(collection.getAbstractType())
      ).toString(),
      "typeId", relationType.getTimId(),
      "tim_id", id.toString(),
      "isLatest", true,
      "rev", 1
    );
    setCreated(edge, userId, time);
    return id;
  }

  private void setAdministrativeProperties(Collection col, Vertex vertex, CreateEntity input) {
    vertex.property("isLatest", true);
    vertex.property("tim_id", input.getId().toString());
    vertex.property("rev", 1);
    vertex.property("types", String.format(
      "[\"%s\", \"%s\"]",
      col.getEntityTypeName(),
      col.getAbstractType()
    ));

    setCreated(vertex, input.getCreated().getUserId(), Instant.ofEpochMilli(input.getCreated().getTimeStamp()));
  }

  // TODO let accept a Change
  private void setCreated(Element element, String userId, Instant instant) {
    final String value = jsnO(
      "timeStamp", jsn(instant.toEpochMilli()),
      "userId", jsn(userId)
    ).toString();
    element.property("created", value);
    element.property("modified", value);
  }

  // TODO let accept a Change
  private void setModified(Element element, String userId, Instant instant) {
    final String value = jsnO(
      "timeStamp", jsn(instant.toEpochMilli()),
      "userId", jsn(userId)
    ).toString();
    element.property("modified", value);
  }

  private void setModified(Element element, Change modified) {
    final String value = jsnO(
      "timeStamp", jsn(modified.getTimeStamp()),
      "userId", jsn(modified.getUserId())
    ).toString();
    element.property("modified", value);
  }

  private GraphTraversal<Vertex, Vertex> getCurrentEntitiesFor(String... entityTypeNames) {
    if (entityTypeNames.length == 1) {
      String type = entityTypeNames[0];
      return latestState.V().has(T.label, LabelP.of(type));
    } else {
      P<String> labels = LabelP.of(entityTypeNames[0]);
      for (int i = 1; i < entityTypeNames.length; i++) {
        labels = labels.or(LabelP.of(entityTypeNames[i]));
      }

      return latestState.V().has(T.label, labels);
    }
  }

  private Optional<Vertex> getPrevVertex(Collection collection, Vertex entity) {
    final Iterator<Edge> prevEdges = entity.edges(Direction.IN, "VERSION_OF");
    Optional<Vertex> old = Optional.empty();
    if (prevEdges.hasNext()) {
      old = Optional.of(prevEdges.next().outVertex());
    } else {
      LOG.error(Logmarkers.databaseInvariant, "Vertex {} has no previous version", entity.id());
    }
    return old;
  }

  private void saveVres(Vres mappings) {
    //Save admin VRE first, the rest will link to it
    Optional.ofNullable(mappings.getVre("Admin")).ifPresent(this::saveVre);

    mappings
      .getVres()
      .values()
      .stream()
      .filter(vre -> !vre.getVreName().equals("Admin"))
      .forEach(this::saveVre);
  }

  private void saveRelationType(RelationType relationType) {
    final Vertex vertex = graph.addVertex(
      T.label, "relationtype",
      "rev", 1,
      "types", jsnA(jsn("relationtype")).toString(),
      "isLatest", true,
      "tim_id", relationType.getTimId().toString(),

      "relationtype_regularName", relationType.getOutName(),
      "relationtype_inverseName", relationType.getInverseName(),
      "relationtype_sourceTypeName", relationType.getSourceTypeName(),
      "relationtype_targetTypeName", relationType.getTargetTypeName(),

      "relationtype_reflexive", relationType.isReflexive(),
      "relationtype_symmetric", relationType.isSymmetric(),
      "relationtype_derived", relationType.isDerived(),

      "rdfUri", "http://timbuctoo.huygens.knaw.nl/" + relationType.getOutName()
    );

    systemPropertyModifier.setCreated(vertex, "timbuctoo", "timbuctoo");
  }

  @Override
  public void addPid(UUID id, int rev, URI pidUri) throws NotFoundException {
    /*
     * EntityFetcher does not work here, because it does not return all the vertices with a certain id en revision. It
     * will only return the vertex with the revision with "isLatest" set to false.
     */
    GraphTraversal<Vertex, Vertex> vertices = traversal.V().has("tim_id", id.toString()).has("rev", rev);
    if (!vertices.hasNext()) {
      throw new NotFoundException();
    }
    vertices.forEachRemaining(vertex -> {
        LOG.info("Setting pid for " + vertex.id() + " to " + pidUri.toString());
        vertex.property("pid", pidUri.toString());
      }
    );
  }

  @Override
  public void addCollectionToVre(Vre vre, CreateCollection createCollection) {
    // FIXME think of a default way to add collections to VRE's.
    boolean vreHasCollection = graph.traversal().V()
                                    .hasLabel(Vre.DATABASE_LABEL)
                                    .has(Vre.VRE_NAME_PROPERTY_NAME, vre.getVreName())
                                    .outE(Vre.HAS_COLLECTION_RELATION_NAME).otherV()
                                    .has(COLLECTION_NAME_PROPERTY_NAME, createCollection.getCollectionName(vre))
                                    .hasNext();

    if (vreHasCollection) {
      return;
    }

    Vertex collectionVertex = graph.addVertex(DATABASE_LABEL);
    collectionVertex.property(COLLECTION_NAME_PROPERTY_NAME, createCollection.getCollectionName(vre));
    collectionVertex.property(ENTITY_TYPE_NAME_PROPERTY_NAME, createCollection.getEntityTypeName(vre));
    collectionVertex.property(RDF_URI_PROP, createCollection.getRdfUri(vre));
    collectionVertex.property(IS_RELATION_COLLECTION_PROPERTY_NAME, false);
    collectionVertex.property(COLLECTION_IS_UNKNOWN_PROPERTY_NAME, createCollection.isUknownCollection());

    // add entities vertex
    Vertex containerVertex = graph.addVertex(COLLECTION_ENTITIES_LABEL);
    collectionVertex.addEdge(HAS_ENTITY_NODE_RELATION_NAME, containerVertex);

    // add collection to VRE
    Vertex vreVertex = graph.traversal().V()
                            .hasLabel(Vre.DATABASE_LABEL)
                            .has(Vre.VRE_NAME_PROPERTY_NAME, vre.getVreName())
                            .next();
    vreVertex.addEdge(Vre.HAS_COLLECTION_RELATION_NAME, collectionVertex);
  }

}
