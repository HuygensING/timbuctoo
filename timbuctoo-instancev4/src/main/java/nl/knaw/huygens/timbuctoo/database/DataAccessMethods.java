package nl.knaw.huygens.timbuctoo.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.crud.AlreadyUpdatedException;
import nl.knaw.huygens.timbuctoo.crud.EdgeManipulator;
import nl.knaw.huygens.timbuctoo.crud.EntityFetcher;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.HandleAdderParameters;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.converters.tinkerpop.TinkerPopPropertyConverter;
import nl.knaw.huygens.timbuctoo.database.converters.tinkerpop.TinkerPopToEntityMapper;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.DirectionalRelationType;
import nl.knaw.huygens.timbuctoo.database.dto.EntityRelation;
import nl.knaw.huygens.timbuctoo.database.dto.ImmutableEntityRelation;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.RelationType;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.CollectionBuilder;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.ImmutableVresDto;
import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.database.exceptions.ObjectSuddenlyDisappearedException;
import nl.knaw.huygens.timbuctoo.database.exceptions.RelationNotPossibleException;
import nl.knaw.huygens.timbuctoo.logging.Logmarkers;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.VreBuilder;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
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
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.crud.EdgeManipulator.duplicateEdge;
import static nl.knaw.huygens.timbuctoo.database.VertexDuplicator.duplicateVertex;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.configurationFailure;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.arrayToEncodedArray;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;

public class DataAccessMethods implements AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger(DataAccessMethods.class);
  private final Transaction transaction;
  private final Authorizer authorizer;
  private final ChangeListener listener;
  private final EntityFetcher entityFetcher;
  private final GraphTraversalSource traversal;
  private final GraphTraversalSource latestState;
  private final Vres mappings;
  private final Graph graph;
  private final HandleAdder handleAdder;
  private final List<HandleAdderParameters> handlesToAdd;
  private boolean requireCommit = false; //we only need an explicit success() call when the database is changed
  private Optional<Boolean> isSuccess = Optional.empty();

  DataAccessMethods(GraphWrapper graphWrapper, Authorizer authorizer, ChangeListener listener,
                    EntityFetcher entityFetcher, Vres mappings, HandleAdder handleAdder) {
    graph = graphWrapper.getGraph();
    this.handleAdder = handleAdder;
    this.transaction = graph.tx();
    this.authorizer = authorizer;
    this.listener = listener;
    this.entityFetcher = entityFetcher;

    if (!transaction.isOpen()) {
      transaction.open();
    }
    this.traversal = graph.traversal();
    this.latestState = graphWrapper.getLatestState();
    this.mappings = mappings == null ? loadVres() : mappings;
    handlesToAdd = Lists.newArrayList();
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
      .not(__.has("deleted", true))
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

  private static void checkIfAllowedToWrite(Authorizer authorizer, String userId, Collection collection) throws
    AuthorizationException, AuthorizationUnavailableException {
    if (!authorizer.authorizationFor(collection, userId).isAllowedToWrite()) {
      throw AuthorizationException.notAllowedToCreate(collection.getCollectionName());
    }
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

  public void success() {
    isSuccess = Optional.of(true);
  }

  public void rollback() {
    isSuccess = Optional.of(false);
  }

  @Override
  public void close() {
    if (isSuccess.isPresent()) {
      if (isSuccess.get()) {
        handlesToAdd.forEach(handleAdder::add);
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

  /**
   * Creates a relation between two entities.
   * <p>If a relation already exists, it will not create a new one.</p>
   *
   * @param sourceId   Id of the source Entity
   * @param typeId     Id of the relation type
   * @param targetId   Id of the target Entity
   * @param collection the relation collection (not the collection of the source or target vertices)
   * @param userId     the user under which the relation is created. Will be validated and written to the database
   * @param instant    the time under which the acceptance should be recorded
   * @return the UUID of the relation
   * @throws RelationNotPossibleException      if a relation is not possible
   * @throws AuthorizationUnavailableException if the relation datafile cannot be read
   * @throws AuthorizationException            if the user is not authorized
   */
  public UUID acceptRelation(UUID sourceId, UUID typeId, UUID targetId, Collection collection, String userId,
                             Instant instant) throws RelationNotPossibleException {

    requireCommit = true;
    RelationType descs = getRelationDescription(traversal, typeId)
      .orElseThrow(notPossible("Relation type " + typeId + " does not exist"));
    Vertex sourceV = getEntityByFullIteration(traversal, sourceId).orElseThrow(notPossible("source is not present"));
    Vertex targetV = getEntityByFullIteration(traversal, targetId).orElseThrow(notPossible("target is not present"));

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
                                            "You can't have a " + descs.getOutName() + " from " +
                                              sourceCollection.getEntityTypeName() + " to " +
                                              targetCollection.getEntityTypeName() + " or vice versa"));

      return createRelation(sourceV, targetV, desc, userId, collection, true, instant);
    }
  }

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

    listener.onCreate(vertex);

    duplicateVertex(traversal, vertex);
  }

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

  public Stream<ReadEntity> getCollection(Collection collection, int start, int rows, boolean withRelations,
                                          CustomEntityProperties customEntityProperties,
                                          CustomRelationProperties customRelationProperties) {
    GraphTraversal<Vertex, Vertex> entities =
      getCurrentEntitiesFor(collection.getEntityTypeName()).range(start, start + rows);

    TinkerPopToEntityMapper tinkerPopToEntityMapper =
      new TinkerPopToEntityMapper(collection, traversal, mappings, customEntityProperties, customRelationProperties);

    return entities.toStream().map(vertex -> tinkerPopToEntityMapper.mapEntity(vertex, withRelations));
  }

  /**
   * Sets the new values of the entity contained in replaceEntity and removes the other values.
   *
   * @return the new revision of entity
   * @throws NotFoundException       when the entity does not exist in the database
   * @throws AlreadyUpdatedException when the entity is updated in between the read and this update
   */
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
    if (!entityTypesStr.contains("\"" + collection.getEntityTypeName() + "\"")) {
      try {
        ArrayNode entityTypes = arrayToEncodedArray.tinkerpopToJson(entityTypesStr);
        entityTypes.add(collection.getEntityTypeName());

        entityVertex.property("types", entityTypes.toString());
      } catch (IOException e) {
        // FIXME potential bug?
        LOG.error(Logmarkers.databaseInvariant, "property 'types' was not parseable: " + entityTypesStr);
      }
    }

    setModified(entityVertex, updateEntity.getModified());
    entityVertex.property("pid").remove();

    callUpdateListener(entityVertex);

    duplicateVertex(traversal, entityVertex);
    return newRev;
  }

  public void replaceRelation(Collection collection, UUID id, int rev, boolean accepted, String userId,
                              Instant instant)
    throws NotFoundException, AuthorizationUnavailableException, AuthorizationException {

    checkIfAllowedToWrite(authorizer, userId, collection);
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

  public void deleteEntity(Collection collection, UUID id, Change modified)
    throws NotFoundException {

    requireCommit = true;

    GraphTraversal<Vertex, Vertex> entityTraversal = entityFetcher.getEntity(traversal, id, null,
      collection.getCollectionName());

    if (!entityTraversal.hasNext()) {
      throw new NotFoundException();
    }

    Vertex entity = entityTraversal.next();
    String entityTypesStr = getProp(entity, "types", String.class).orElse("[]");
    if (entityTypesStr.contains("\"" + collection.getEntityTypeName() + "\"")) {
      try {
        ArrayNode entityTypes = arrayToEncodedArray.tinkerpopToJson(entityTypesStr);
        if (entityTypes.size() == 1) {
          entity.property("deleted", true);
        } else {
          for (int i = entityTypes.size() - 1; i >= 0; i--) {
            JsonNode val = entityTypes.get(i);
            if (val != null && val.asText("").equals(collection.getEntityTypeName())) {
              entityTypes.remove(i);
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
      Optional<Collection> ownEdgeCol = getOwnCollectionOfElement(collection.getVre(), edge);
      if (ownEdgeCol.isPresent()) {
        edge.property(ownEdgeCol.get().getEntityTypeName() + "_accepted", false);
      }
    });

    setModified(entity, modified);
    entity.property("pid").remove();
    callUpdateListener(entity);
    duplicateVertex(traversal, entity);

    handleAdder.add(new HandleAdderParameters(collection.getCollectionName(), id, newRev));
  }

  public Vres loadVres() {
    ImmutableVresDto.Builder builder = ImmutableVresDto.builder();

    traversal.V().has(T.label, LabelP.of(Vre.DATABASE_LABEL)).forEachRemaining(vreVertex -> {
      final Vre vre = Vre.load(vreVertex);
      builder.putVres(vre.getVreName(), vre);
    });

    return builder.build();
  }

  public boolean databaseIsEmptyExceptForMigrations() {
    return !traversal.V()
                     .not(__.has("type", DatabaseMigrator.EXECUTED_MIGRATIONS_TYPE))
                     .hasNext();
  }

  public void initDb(Vres mappings, RelationType... relationTypes) {
    requireCommit = true;
    //FIXME: add security
    saveVres(mappings);
    for (RelationType relationType : relationTypes) {
      saveRelationType(relationType);
    }
  }

  public void saveVre(Vre vre) {
    vre.save(graph);
  }

  public Vre ensureVreExists(String vreName) {
    Vre vre = VreBuilder.vre(vreName, vreName)
                        .withCollection(vreName + "relations", CollectionBuilder::isRelationCollection)
                        .build();
    saveVre(vre);
    return vre;
  }

  public void removeCollectionsAndEntities(String vreName) {
    traversal
      .V()
      .hasLabel(Vre.DATABASE_LABEL)
      .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
      .out("hasCollection")
      .not(__.has(Collection.IS_RELATION_COLLECTION_PROPERTY_NAME, true))
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

  private void callUpdateListener(Vertex entity) {
    final Iterator<Edge> prevEdges = entity.edges(Direction.IN, "VERSION_OF");
    Optional<Vertex> old = Optional.empty();
    if (prevEdges.hasNext()) {
      old = Optional.of(prevEdges.next().outVertex());
    } else {
      LOG.error(Logmarkers.databaseInvariant, "Vertex {} has no previous version", entity.id());
    }
    listener.onUpdate(old, entity);
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
    graph.addVertex(
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

      "rdfUri", "http://timbuctoo.com/" + relationType.getOutName()
    );
  }

}