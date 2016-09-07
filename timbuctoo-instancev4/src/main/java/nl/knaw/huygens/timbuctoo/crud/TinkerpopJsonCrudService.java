package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.database.ChangeListener;
import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.database.dto.Entity;
import nl.knaw.huygens.timbuctoo.database.dto.RelationRef;
import nl.knaw.huygens.timbuctoo.database.dto.property.JsonPropertyConverter;
import nl.knaw.huygens.timbuctoo.database.exceptions.RelationNotPossibleException;
import nl.knaw.huygens.timbuctoo.logging.Logmarkers;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.helpers.Strings;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Clock;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.crud.EdgeManipulator.duplicateEdge;
import static nl.knaw.huygens.timbuctoo.database.VertexDuplicator.duplicateVertex;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.arrayToEncodedArray;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class TinkerpopJsonCrudService {

  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(TinkerpopJsonCrudService.class);

  private final GraphWrapper graphwrapper;
  private final Vres mappings;
  private final HandleAdder handleAdder;
  private final UrlGenerator handleUrlFor;
  private final UrlGenerator autoCompleteUrlFor;
  private final UrlGenerator relationUrlFor;
  private final Clock clock;
  private final JsonNodeFactory nodeFactory;
  private final UserStore userStore;
  private final ChangeListener listener;
  private final DataAccess dataAccess;
  private Authorizer authorizer;
  private EntityFetcher entityFetcher;

  public TinkerpopJsonCrudService(GraphWrapper graphwrapper, Vres mappings,
                                  HandleAdder handleAdder, UserStore userStore, UrlGenerator handleUrlFor,
                                  UrlGenerator autoCompleteUrlFor, UrlGenerator relationUrlFor, Clock clock,
                                  ChangeListener listener, Authorizer authorizer, EntityFetcher entityFetcher) {
    this.graphwrapper = graphwrapper;
    this.mappings = mappings;
    this.handleAdder = handleAdder;
    this.handleUrlFor = handleUrlFor;
    this.autoCompleteUrlFor = autoCompleteUrlFor;
    this.relationUrlFor = relationUrlFor;
    this.userStore = userStore;
    this.clock = clock;
    this.listener = listener;
    this.authorizer = authorizer;
    this.entityFetcher = entityFetcher;
    this.dataAccess = new DataAccess(graphwrapper, entityFetcher, authorizer, listener, mappings);
    nodeFactory = JsonNodeFactory.instance;
  }

  public UUID create(String collectionName, ObjectNode input, String userId)
    throws InvalidCollectionException, IOException, AuthorizationException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    try {
      Authorization authorization = authorizer.authorizationFor(collection, userId);
      if (!authorization.isAllowedToWrite()) {
        throw AuthorizationException.notAllowedToCreate(collectionName);
      }

      if (collection.isRelationCollection()) {
        return createRelation(collection, input, userId);
      } else {
        return createEntity(collection, input, userId);
      }
    } catch (AuthorizationUnavailableException e) {
      throw new IOException(e.getMessage());
    }
  }

  private UUID asUuid(ObjectNode input, String fieldName) throws IOException {
    try {
      return UUID.fromString(input.get(fieldName).asText(""));
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IOException(fieldName + " must contain a valid UUID", e);
    }
  }

  private UUID createRelation(Collection collection, ObjectNode input, String userId)
    throws IOException, AuthorizationException, AuthorizationUnavailableException {

    UUID sourceId = asUuid(input, "^sourceId");
    UUID targetId = asUuid(input, "^targetId");
    UUID typeId = asUuid(input, "^typeId");

    try (DataAccess.DataAccessMethods db = dataAccess.start()) {
      UUID relationId = db.acceptRelation(
        sourceId,
        typeId,
        targetId,
        collection,
        userId,
        clock.instant()
      );
      db.success();
      return relationId;
    } catch (RelationNotPossibleException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  private UUID createEntity(Collection collection, ObjectNode input, String userId)
    throws IOException, AuthorizationException, AuthorizationUnavailableException {

    Optional<Collection> baseCollection = mappings.getCollectionForType(collection.getAbstractType());

    UUID id;
    try (DataAccess.DataAccessMethods db = dataAccess.start()) {
      try {
        id = db.createEntity(collection, baseCollection, input, userId, clock.instant());
        db.success();
      } catch (IOException | AuthorizationException | AuthorizationUnavailableException e) {
        db.rollback();
        throw e;
      }
    }

    //but out of process commands that require our changes need to come after a commit of course :)
    handleAdder.add(new HandleAdderParameters(id, 1, handleUrlFor.apply(collection.getCollectionName(), id, 1)));
    return id;
  }

  public JsonNode get(String collectionName, UUID id) throws InvalidCollectionException, NotFoundException {
    return get(collectionName, id, null);
  }

  public JsonNode get(String collectionName, UUID id, Integer rev)
    throws InvalidCollectionException, NotFoundException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    if (collection.isRelationCollection()) {
      return getRelation(id, rev, collection);
    } else {
      return getEntity(id, rev, collection);
    }
  }

  private JsonNode getRelation(UUID id, Integer rev, Collection collection) throws NotFoundException {
    return jsnO("message", jsn("Getting a wwrelation is not yet supported"));
  }

  private JsonNode getEntity(UUID id, Integer rev, Collection collection) throws NotFoundException {
    try (DataAccess.DataAccessMethods dataAccessMethods = dataAccess.start()) {

      try {
        Entity entity = dataAccessMethods.getEntity(id, rev, collection);

        ObjectNode result = mapEntity(collection, entity, true);

        dataAccessMethods.success();
        return result;
      } catch (NotFoundException e) {
        dataAccessMethods.rollback();
        throw e;
      }
    }

  }

  public List<ObjectNode> getCollection(String collectionName, int rows, int start, boolean withRelations)
    throws InvalidCollectionException {
    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    try (DataAccess.DataAccessMethods dataAccessMethods = dataAccess.start()) {
      Iterator<Entity> entities = dataAccessMethods.getCollection(collection, rows, start);
      List<ObjectNode> result = Lists.newArrayList();
      entities.forEachRemaining(entity -> {
        final ObjectNode mappedEntity = mapEntity(collection, entity, withRelations);

        result.add(mappedEntity);
      });

      dataAccessMethods.success();
      return result;
    }
  }

  private ObjectNode mapEntity(Collection collection, Entity entity, boolean withRelations) {
    final ObjectNode mappedEntity = JsonNodeFactory.instance.objectNode();
    String id = entity.getId().toString();

    mappedEntity.set("@type", nodeFactory.textNode(collection.getEntityTypeName()));
    mappedEntity.set("_id", nodeFactory.textNode(id));

    mappedEntity.set("^rev", jsn(entity.getRev()));
    mappedEntity.set("^deleted", jsn(entity.getDeleted()));
    mappedEntity.set("^pid", jsn(entity.getPid()));

    JsonNode variationRefs = jsnA(entity.getTypes()
                                        .stream()
                                        .map(type -> {
                                          ObjectNode variationRef = jsnO();
                                          variationRef.set("id", jsn(id));
                                          variationRef.set("type", jsn(type));
                                          return variationRef;
                                        }));
    mappedEntity.set("@variationRefs", variationRefs);

    Change modified = entity.getModified();
    mappedEntity.set("^modified", mapChange(modified));
    Change created = entity.getCreated();
    mappedEntity.set("^created", mapChange(created));

    // translate TimProperties to Json
    JsonPropertyConverter jsonPropertyConverter = new JsonPropertyConverter(collection);
    entity.getProperties().forEach(prop -> {
      Tuple<String, JsonNode> convertedProperty = null;
      try {
        convertedProperty = prop.convert(jsonPropertyConverter);
      } catch (IOException e) {
        LOG.error(
          databaseInvariant,
          "Property '" + prop.getName() + "' is not encoded correctly",
          e.getCause()
        );
      }
      mappedEntity.set(convertedProperty.getLeft(), convertedProperty.getRight());
    });

    if (!Strings.isBlank(entity.getDisplayName())) {
      mappedEntity.set("@displayName", jsn(entity.getDisplayName()));
    }

    if (withRelations) {
      mappedEntity.set("@relationCount", nodeFactory.numberNode(entity.getRelations().size()));
      mappedEntity.set("@relations", mapRelations(entity.getRelations()));
    }
    return mappedEntity;
  }


  private JsonNode mapRelations(List<RelationRef> relations) {
    ObjectNode relationsNode = jsnO();
    relations.stream().map(relationRef -> jsnO(
      tuple("id", jsn(relationRef.getEntityId())),
      tuple("path", jsn(relationUrlFor.apply(relationRef.getCollectionName(),
        UUID.fromString(relationRef.getEntityId()), null).toString())),
      tuple("relationType", jsn(relationRef.getRelationType())),
      tuple("type", jsn(relationRef.getEntityType())),
      tuple("accepted", jsn(relationRef.isRelationAccepted())),
      tuple("relationId", jsn(relationRef.getRelationId())),
      tuple("rev", jsn(relationRef.getRelationRev())),
      tuple("displayName", jsn(relationRef.getDisplayName()))
    )).collect(groupingBy(x -> x.get("relationType").asText())).entrySet().forEach(relationsType ->
      relationsNode.set(relationsType.getKey(), jsnA(relationsType.getValue().stream())));

    return relationsNode;
  }

  private JsonNode mapChange(Change change) {
    String userId = change.getUserId();
    ObjectNode changeNode = new ObjectMapper().valueToTree(change);

    try {
      userStore.userForId(userId).ifPresent(user -> changeNode.set("username", jsn(user.getDisplayName())));
    } catch (AuthenticationUnavailableException e) {
      LOG.error("Could not retrieve user store", e);
    }

    return changeNode;
  }

  /* returns the entitytype for the current collection's vre or else the type of the current collection */

  private String getOwnEntityType(Collection collection, Element vertex) throws IOException {
    final Vre vre = collection.getVre();
    return getEntityTypes(vertex)
      .map(x -> x.map(vre::getOwnType))
      .orElse(Try.success(collection.getEntityTypeName()))
      .get(); //throws IOException on failure
  }

  private void setModified(Element element, String userId) {
    String value = String.format("{\"timeStamp\":%s,\"userId\":%s}",
      clock.millis(),
      nodeFactory.textNode(userId)
    );
    element.property("modified", value);
  }

  public void replace(String collectionName, UUID id, ObjectNode data, String userId)
    throws InvalidCollectionException, IOException, NotFoundException, AlreadyUpdatedException, AuthorizationException,
    AuthorizationUnavailableException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    Authorization authorization = authorizer.authorizationFor(collection, userId);
    if (!authorization.isAllowedToWrite()) {
      throw AuthorizationException.notAllowedToEdit(collectionName, id);
    }

    if (collection.isRelationCollection()) {
      replaceRelation(collection, id, data, userId);
    } else {
      replaceEntity(collection, id, data, userId);
    }
  }

  private void replaceRelation(Collection collection, UUID id, ObjectNode data, String userId)
    throws IOException, NotFoundException {

    // FIXME: string concatenating methods like this should be delegated to a configuration class
    final String acceptedPropName = collection.getEntityTypeName() + "_accepted";

    JsonNode accepted = data.get("accepted");
    if (accepted == null || !accepted.isBoolean()) {
      throw new IOException("Only the property accepted can be updated. It must be a boolean");
    }
    JsonNode rev = data.get("^rev");
    if (rev == null || !rev.isNumber()) {
      throw new IOException("^rev must be a number");
    }
    for (Iterator<String> fieldNames = data.fieldNames(); fieldNames.hasNext(); ) {
      String name = fieldNames.next();
      if (!name.startsWith("^") && !name.startsWith("@") && !name.equals("_id") && !name.equals("accepted")) {
        throw new IOException("Only 'accepted' is a changeable property");
      }
    }

    Graph graph = graphwrapper.getGraph();
    Edge origEdge;
    try {
      origEdge = graph.traversal().E()
                      .has("tim_id", id.toString())
                      .has("isLatest", true)
                      .has("rev", rev.intValue())
                      .next();
    } catch (NoSuchElementException e) {
      throw new NotFoundException();
    }

    Edge edge = duplicateEdge(origEdge);
    edge.property(acceptedPropName, accepted.booleanValue());
    edge.property("rev", getProp(origEdge, "rev", Integer.class).orElse(1) + 1);
    setModified(edge, userId);

    //Make sure this is the last line of the method. We don't want to commit half our changes
    //this also means checking each function that we call to see if they don't call commit()
    graph.tx().commit();
  }

  private void replaceEntity(Collection collection, UUID id, ObjectNode data, String userId)
    throws NotFoundException, IOException, AlreadyUpdatedException {

    final Graph graph = graphwrapper.getGraph();
    final GraphTraversalSource traversalSource = graph.traversal();

    GraphTraversal<Vertex, Vertex> entityTraversal = entityFetcher.getEntity(traversalSource, id, null,
      collection.getCollectionName());

    if (!entityTraversal.hasNext()) {
      throw new NotFoundException();
    }
    Vertex entity = entityTraversal.next();
    int curRev = getProp(entity, "rev", Integer.class).orElse(1);
    if (data.get("^rev") == null) {
      throw new IOException("data object should have a ^rev property indicating the revision this update was based on");
    }
    if (curRev != data.get("^rev").asInt()) {
      throw new AlreadyUpdatedException();
    }

    int newRev = curRev + 1;
    entity.property("rev", newRev);

    String entityTypesStr = getProp(entity, "types", String.class).orElse("[]");
    if (!entityTypesStr.contains("\"" + collection.getEntityTypeName() + "\"")) {
      try {
        ArrayNode entityTypes = arrayToEncodedArray.tinkerpopToJson(entityTypesStr);
        entityTypes.add(collection.getEntityTypeName());

        entity.property("types", entityTypes.toString());
      } catch (IOException e) {
        LOG.error(Logmarkers.databaseInvariant, "property 'types' was not parseable: " + entityTypesStr);
      }
    }

    final Map<String, LocalProperty> collectionProperties = collection.getWriteableProperties();

    final Set<String> dataFields = stream(data.fieldNames())
      .filter(x -> !x.startsWith("@"))
      .filter(x -> !x.startsWith("^"))
      .filter(x -> !Objects.equals(x, "_id"))
      .collect(toSet());

    for (String name : dataFields) {
      if (!collectionProperties.containsKey(name)) {
        graph.tx().rollback();
        throw new IOException(name + " is not a valid property");
      }
      try {
        collectionProperties.get(name).setJson(entity, data.get(name));
      } catch (IOException e) {
        graph.tx().rollback();
        throw new IOException(name + " could not be saved. " + e.getMessage(), e);
      }
    }

    for (String name : Sets.difference(collectionProperties.keySet(), dataFields)) {
      collectionProperties.get(name).setJson(entity, null);
    }

    setModified(entity, userId);
    entity.property("pid").remove();

    callUpdateListener(entity);

    duplicateVertex(graph, entity);

    //Make sure this is at the last line of the method. We don't want to commit half our changes
    //this also means checking each function that we call to see if they don't call commit()
    graph.tx().commit();

    //but out of process commands that require our changes need to come after a commit of course :)
    handleAdder.add(new HandleAdderParameters(
      id,
      newRev,
      handleUrlFor.apply(collection.getCollectionName(), id, newRev)
    ));
  }

  public void delete(String collectionName, UUID id, String userId)
    throws InvalidCollectionException, NotFoundException, AuthorizationException, IOException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    final Authorization authorization;
    try {
      authorization = authorizer.authorizationFor(collection, userId);
      if (!authorization.isAllowedToWrite()) {
        throw AuthorizationException.notAllowedToDelete(collectionName, id);
      }
    } catch (AuthorizationUnavailableException e) {
      throw new IOException(e.getMessage());
    }

    final Graph graph = graphwrapper.getGraph();
    final GraphTraversalSource traversalSource = graph.traversal();
    GraphTraversal<Vertex, Vertex> entityTraversal = entityFetcher.getEntity(traversalSource, id, null, collectionName);

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
      try {
        String entityType = getOwnEntityType(collection, edge);
        if (entityType != null) {
          edge.property(entityType + "_accepted", false);
        }
      } catch (IOException e) {
        LOG.error(Logmarkers.databaseInvariant, "property 'types' was not parseable");
      }
    });

    setModified(entity, userId);
    entity.property("pid").remove();
    callUpdateListener(entity);
    duplicateVertex(graph, entity);


    handleAdder.add(new HandleAdderParameters(id, newRev, handleUrlFor.apply(collectionName, id, newRev)));

    //Make sure this is the last line of the method. We don't want to commit half our changes
    //this also means checking each function that we call to see if they don't call commit()
    graph.tx().commit();
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


}
