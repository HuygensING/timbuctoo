package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.database.ChangeListener;
import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.database.exceptions.RelationNotPossibleException;
import nl.knaw.huygens.timbuctoo.logging.Logmarkers;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.crud.EdgeManipulator.duplicateEdge;
import static nl.knaw.huygens.timbuctoo.database.VertexDuplicator.duplicateVertex;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;
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
    this.dataAccess = new DataAccess(graphwrapper, entityFetcher, authorizer, listener);
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
      return getVertex(id, rev, collection);
    }
  }

  private JsonNode getRelation(UUID id, Integer rev, Collection collection) throws NotFoundException {
    return jsnO("message", jsn("Getting a wwrelation is not yet supported"));
  }

  private JsonNode getVertex(UUID id, Integer rev, Collection collection) throws NotFoundException {
    final Map<String, ReadableProperty> mapping = collection.getReadableProperties();
    final String entityTypeName = collection.getEntityTypeName();
    final GraphTraversalSource traversalSource = graphwrapper.getGraph().traversal();

    final ObjectNode result = JsonNodeFactory.instance.objectNode();

    result.set("@type", nodeFactory.textNode(entityTypeName));
    result.set("_id", nodeFactory.textNode(id.toString()));

    Vertex entityTs = null;
    try {
      entityTs = entityFetcher.getEntity(traversalSource, id, rev, collection.getCollectionName()).next();
    } catch (NoSuchElementException e) {
      throw new NotFoundException();
    }
    GraphTraversal<Vertex, Vertex> entityT = traversalSource.V(entityTs.id());

    if (!entityT.asAdmin().clone().hasNext()) {
      throw new NotFoundException();
    }

    GraphTraversal[] propertyGetters = mapping
      .entrySet().stream()
      //append error handling and resuling to the traversal
      .map(prop -> prop.getValue().traversal().sideEffect(x ->
        x.get()
         .onSuccess(node -> result.set(prop.getKey(), node))
         .onFailure(e -> {
           if (e.getCause() instanceof IOException) {
             LOG.error(
               databaseInvariant,
               "Property '" + prop.getKey() + "' is not encoded correctly",
               e.getCause()
             );
           } else {
             LOG.error("Something went wrong while reading the property '" + prop.getKey() + "'.", e.getCause());
           }
         })
      ))
      .toArray(GraphTraversal[]::new);

    entityT.asAdmin().clone().union(propertyGetters).forEachRemaining(x -> {
      //Force side effects to happen
    });
    Vertex entity = entityT.asAdmin().clone().next();

    String entityTypesStr = getProp(entity, "types", String.class).orElse("[]");
    if (!entityTypesStr.contains("\"" + collection.getEntityTypeName() + "\"")) {
      throw new NotFoundException();
    }

    Tuple<ObjectNode, Long> relations = getRelations(entity, traversalSource, collection);
    result.set("@relationCount", nodeFactory.numberNode(relations.getRight()));
    result.set("@relations", relations.getLeft());

    result.set(
      "^rev", nodeFactory.numberNode(
        getProp(entity, "rev", Integer.class)
          .orElse(rev == null ? -1 : rev)
      )
    );
    getModification(entity, "modified").ifPresent(val -> result.set("^modified", val));
    getModification(entity, "created").ifPresent(val -> result.set("^created", val));

    result.set("@variationRefs", getVariationRefs(entity, id, entityTypeName));

    result.set("^deleted", nodeFactory.booleanNode(getProp(entity, "deleted", Boolean.class).orElse(false)));

    result.set("^pid", nodeFactory.textNode(getProp(entity, "pid", String.class).orElse(null)));

    return result;
  }

  private Tuple<ObjectNode, Long> getRelations(Vertex entity, GraphTraversalSource traversalSource,
                                               Collection collection) {
    final ObjectMapper mapper = new ObjectMapper();

    final long[] relationCount = new long[1];

    GraphTraversal<Vertex, ObjectNode> realRelations = getRealRelations(entity, traversalSource, collection);

    Map<String, List<ObjectNode>> relations = stream(realRelations)
      .filter(x -> x != null)
      .peek(x -> relationCount[0]++)
      .collect(groupingBy(jsn -> jsn.get("relationType").asText()));

    return new Tuple<>(mapper.valueToTree(relations), relationCount[0]);
  }

  private GraphTraversal<Vertex, ObjectNode> getRealRelations(Vertex entity, GraphTraversalSource traversalSource,
                                                              Collection collection) {
    final Vre vre = collection.getVre();
    final Vre admin = mappings.getVre("Admin");

    Object[] relationTypes = traversalSource.V().has(T.label, LabelP.of("relationtype")).id().toList().toArray();

    return collection.getVre().getCollections().values().stream()
                     .filter(Collection::isRelationCollection)
                     .findAny()
                     .map(Collection::getEntityTypeName)
                     .map(ownRelationType -> traversalSource.V(entity.id())
                                                            .union(
                                                              __.outE()
                                                                .as("edge")
                                                                .label().as("label")
                                                                .select("edge"),
                                                              __.inE()
                                                                .as("edge")
                                                                .label().as("edgeLabel")
                                                                .V(relationTypes)
                                                                .has("relationtype_regularName",
                                                                  __.where(P.eq("edgeLabel")))
                                                                .properties("relationtype_inverseName").value()
                                                                .as("label")
                                                                .select("edge")
                                                            )
                                                            .where(
                                                              //FIXME move to strategy
                                                              __.has("isLatest", true)
                                                                .not(__.has("deleted", true))
                                                                .not(__.hasLabel("VERSION_OF"))
                                                                //The old timbuctoo showed relations from all VRE's.
                                                                // Changing that behaviour caused breakage in the
                                                                //frontend and exposed errors in the database that
                                                                //.has("types", new P<>((val, def) -> val.contains
                                                                // ("\"" + ownRelationType + "\""), ""))
                                                                // FIXME: string concatenating methods like this
                                                                // should be delegated to a configuration clas
                                                                .not(__.has(ownRelationType + "_accepted", false))
                                                            )
                                                            .otherV().as("vertex")
                                                            .select("edge", "vertex", "label")
                                                            .map(r -> {
                                                              try {
                                                                Map<String, Object> val = r.get();
                                                                Edge edge = (Edge) val.get("edge");
                                                                Vertex vertex = (Vertex) val.get("vertex");
                                                                String label = (String) val.get("label");

                                                                String targetEntityType =
                                                                  getOwnEntityType(collection, vertex);
                                                                Collection targetCollection =
                                                                  vre.getCollectionForTypeName(targetEntityType);
                                                                if (targetEntityType == null) {
                                                                  //this means that the edge is of this VRE, but the
                                                                  // Vertex it points to is of another VRE
                                                                  //In that case we use the admin vre
                                                                  targetEntityType =
                                                                    admin.getOwnType(getEntityTypesOrDefault(vertex));
                                                                  targetCollection =
                                                                    admin.getCollectionForTypeName(targetEntityType);
                                                                }

                                                                String displayName =
                                                                  getDisplayname(traversalSource, vertex,
                                                                    targetCollection)
                                                                    .orElse("<No displayname found>");
                                                                String uuid =
                                                                  getProp(vertex, "tim_id", String.class).orElse("");

                                                                URI relatedEntityUri =
                                                                  relationUrlFor
                                                                    .apply(targetCollection.getCollectionName(),
                                                                      UUID.fromString(uuid), null);
                                                                return jsnO(
                                                                  tuple("id", jsn(uuid)),
                                                                  tuple("path", jsn(relatedEntityUri.toString())),
                                                                  tuple("relationType", jsn(label)),
                                                                  tuple("type", jsn(targetEntityType)),
                                                                  tuple("accepted", jsn(
                                                                    getProp(edge, "accepted", Boolean.class)
                                                                      .orElse(true))),
                                                                  tuple("relationId",
                                                                    getProp(edge, "tim_id", String.class)
                                                                      .map(x -> (JsonNode) jsn(x)).orElse(jsn())),
                                                                  tuple("rev",
                                                                    jsn(getProp(edge, "rev", Integer.class).orElse(1))),
                                                                  tuple("displayName", jsn(displayName))
                                                                );
                                                              } catch (Exception e) {
                                                                LOG.error(databaseInvariant,
                                                                  "Something went wrong while formatting the entity",
                                                                  e);
                                                                return null;
                                                              }
                                                            })
                     )
                     .orElse(EmptyGraph.instance().traversal().V().map(x -> jsnO()));
  }

  private Optional<String> getDisplayname(GraphTraversalSource traversalSource, Vertex vertex,
                                          Collection targetCollection) {
    ReadableProperty displayNameProperty = targetCollection.getDisplayName();
    if (displayNameProperty != null) {
      GraphTraversal<Vertex, Try<JsonNode>> displayNameGetter = traversalSource.V(vertex.id()).union(
        targetCollection.getDisplayName().traversal()
      );
      if (displayNameGetter.hasNext()) {
        Try<JsonNode> traversalResult = displayNameGetter.next();
        if (!traversalResult.isSuccess()) {
          LOG.error(databaseInvariant, "Retrieving displayname failed", traversalResult.getCause());
        } else {
          if (traversalResult.get() == null) {
            LOG.error(databaseInvariant, "Displayname was null");
          } else {
            if (!traversalResult.get().isTextual()) {
              LOG.error(databaseInvariant, "Displayname was not a string but " + traversalResult.get().toString());
            } else {
              return Optional.of(traversalResult.get().asText());
            }
          }
        }
      } else {
        LOG.error(databaseInvariant, "Displayname traversal resulted in no results: " + displayNameGetter);
      }
    } else {
      LOG.warn("No displayname configured for " + targetCollection.getEntityTypeName());
    }
    return Optional.empty();
  }

  /* returns the entitytype for the current collection's vre or else the type of the current collection */
  private String getOwnEntityType(Collection collection, Element vertex) throws IOException {
    final Vre vre = collection.getVre();
    return getEntityTypes(vertex)
      .map(x -> x.map(vre::getOwnType))
      .orElse(Try.success(collection.getEntityTypeName()))
      .get(); //throws IOException on failure
  }

  private ArrayNode getVariationRefs(Vertex entity, UUID id, String entityTypeName) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      ArrayNode variationRefs = nodeFactory.arrayNode();
      JsonNode types = mapper.readTree((String) entity.value("types"));
      if (!types.isArray()) {
        throw new IOException("types should be a JSON encoded Array");
      }
      for (int i = 0; i < types.size(); i++) {
        ObjectNode ref = nodeFactory.objectNode();
        ref.set("id", nodeFactory.textNode(id.toString()));
        ref.set("type", nodeFactory.textNode(types.get(i).asText()));
        variationRefs.add(ref);
      }
      return variationRefs;
    } catch (Exception e) {
      //When something goes wrong we log the error and return a functional representation
      LOG.error(databaseInvariant, "Error while generating variation refs", e);
      return jsnA(
        jsnO(
          "id", jsn(id.toString()),
          "type", jsn(entityTypeName)
        )
      );
    }
  }

  private Optional<ObjectNode> getModification(Vertex entity, String propertyName) {
    ObjectMapper mapper = new ObjectMapper();
    return getProp(entity, propertyName, String.class)
      .flatMap(content -> {
        try {
          return Optional.of(mapper.readTree(content));
        } catch (IOException e) {
          return Optional.empty();
        }
      })
      .flatMap(parsed -> parsed instanceof ObjectNode ? Optional.of((ObjectNode) parsed) : Optional.empty())
      .map(modifiedObj -> {
        try {
          userStore.userForId(modifiedObj.get("userId").asText(""))
                   .map(User::getDisplayName)
                   .ifPresent(userName -> modifiedObj.set("username", nodeFactory.textNode(userName)));
        } catch (AuthenticationUnavailableException e) {
          LOG.error(Logmarkers.serviceUnavailable, "could not get user for modifiedObj", e);
          modifiedObj.set("username", nodeFactory.nullNode());
        }
        return modifiedObj;
      });
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

  public List<ObjectNode> fetchCollection(String collectionName, int rows, int start, boolean withRelations)
    throws InvalidCollectionException {
    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));
    final Map<String, ReadableProperty> mapping = collection.getReadableProperties();

    GraphTraversal<Vertex, Vertex> entities =
      graphwrapper.getCurrentEntitiesFor(collection.getEntityTypeName()).range(start, start + rows);
    final GraphTraversalSource traversalSource = graphwrapper.getGraph().traversal();


    return entities.asAdmin().clone().map(entityT -> {
      final ObjectNode result = JsonNodeFactory.instance.objectNode();
      final List<GraphTraversal> propertyGetters = mapping
        .entrySet().stream()
        .map(prop -> prop.getValue().traversal().sideEffect(x -> {
          x.get()
            .onSuccess(node -> result.set(prop.getKey(), node))
            .onFailure(e -> {
              if (e.getCause() instanceof IOException) {
                LOG.error(
                  databaseInvariant,
                  "Property '" + prop.getKey() + "' is not encoded correctly",
                  e.getCause()
                );
              } else {
                LOG.error("Something went wrong while reading the property '" + prop.getKey() + "'.", e.getMessage());
              }
            });
        }))
        .collect(Collectors.toList());

      propertyGetters.add(collection.getDisplayName().traversal().sideEffect( x -> {
        x.get()
          .onSuccess(node -> result.set("@displayName", node))
          .onFailure(e -> LOG.error(databaseInvariant, "Failed to make displayname for {}", collectionName, e));
      }));

      traversalSource.V(entityT.get().id())
        .union(propertyGetters.toArray(new GraphTraversal[propertyGetters.size()])).forEachRemaining(x -> {
          // side effects
        });

      Vertex entity = entityT.get();
      if (withRelations) {
        Tuple<ObjectNode, Long> relations = getRelations(entity, traversalSource, collection);
        result.set("@relationCount", nodeFactory.numberNode(relations.getRight()));
        result.set("@relations", relations.getLeft());
      }

      result.set(
        "^rev", nodeFactory.numberNode(
          getProp(entity, "rev", Integer.class)
            .orElse(0)
        )
      );
      getModification(entity, "modified").ifPresent(val -> result.set("^modified", val));
      getModification(entity, "created").ifPresent(val -> result.set("^created", val));

      result.set("@variationRefs", getVariationRefs(entity, UUID.fromString(entity.value("tim_id")),
        collection.getCollectionName()));

      result.set("^deleted", nodeFactory.booleanNode(getProp(entity, "deleted", Boolean.class).orElse(false)));
      result.set("_id", jsn(entity.value("tim_id")));

      getProp(entity, "pid", String.class)
        .ifPresent(pid -> result.set("^pid", nodeFactory.textNode(pid)));

      return result;
    }).toList();
  }
}
