package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.logmarkers.Logmarkers;
import nl.knaw.huygens.timbuctoo.model.JsonToTinkerpopPropertyMap;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.security.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.JsonBasedUserStore;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import nl.knaw.huygens.timbuctoo.util.Triple;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Clock;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Stream.concat;
import static nl.knaw.huygens.timbuctoo.crud.VertexDuplicator.duplicateVertex;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.rethrowConsumer;
import static nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.rethrowFunction;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;

public class TinkerpopJsonCrudService {

  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(TinkerpopJsonCrudService.class);

  private final Map<String, String> collectionToAbstractCollection = ImmutableMap.<String, String>builder()
    .put("wwcollective", "collective")
    .put("wwdocument", "document")
    .put("wwkeyword", "keyword")
    .put("wwlanguage", "language")
    .put("wwlocation", "location")
    .put("wwperson", "person")
    .put("wwrelation", "relation")
    .build();
  private final Map<String, Function<Vertex, String>> displayNames = ImmutableMap
    .<String, Function<Vertex, String>>builder()
    .put("wwperson", value -> {
      try {
        String encodedPersonNames = getProp(value, "wwperson_names", String.class).orElse("");
        PersonNames personNames = new ObjectMapper().readValue(encodedPersonNames, PersonNames.class);
        return personNames.defaultName().getShortName();
      } catch (IOException e) {
        return "";
      }
    })
    .put("wwlanguage", value -> this.getProp(value, "wwlanguage_name", String.class).orElse(""))
    .put("wwdocument", value -> "some document")
    .put("wwcollective", value -> "collective")
    .put("wwkeyword", value -> "keyword")
    .put("wwlocation", value -> "location")
    .put("wwrelation", value -> "relation")
    .build();

  private final Map<String, String> vreForEntityType = ImmutableMap.<String, String>builder()
    .put("wwcollective", "neww")
    .put("wwdocument", "neww")
    .put("wwkeyword", "neww")
    .put("wwlanguage", "neww")
    .put("wwlocation", "neww")
    .put("wwperson", "neww")
    .put("wwrelation", "neww")
    .build();

  private final Map<String, Set<String>> entityTypePerVre = ImmutableMap.of(
    "neww", Sets.newHashSet(
      "wwcollective",
      "wwdocument",
      "wwkeyword",
      "wwlanguage",
      "wwlocation",
      "wwperson",
      "wwrelation"
    )
  );
  private final Map<String, Map<String, Function<GraphTraversal<Vertex, Vertex>, Iterator<Vertex>>>> derivedRelations =
    ImmutableMap.of(
      "wwperson", ImmutableMap.of(
        "hasPersonLanguage", t -> {
          P<String> isWw = new P<>((types, extra) -> types.contains("\"wwrelation\""), "");
          return t
            .outE("isCreatorOf").has("isLatest", true).not(has("isDeleted", true)).has("types", isWw).inV()
            .outE("hasWorkLanguage").has("isLatest", true).not(has("isDeleted", true)).has("types", isWw).inV();
        }
      )
    );

  private final GraphWrapper graphwrapper;
  private final HandleAdder handleAdder;
  private final UrlGenerator urlFor;
  private final Map<String, Map<String, JsonToTinkerpopPropertyMap>> mappingPerJson;
  private final Clock clock;
  private final JsonNodeFactory nodeFactory;
  private final Map<String, Map<String, JsonToTinkerpopPropertyMap>> mappingPerTinkerpop;
  private final JsonBasedUserStore userStore;

  public TinkerpopJsonCrudService(GraphWrapper graphwrapper, Map<String, List<JsonToTinkerpopPropertyMap>> mappings,
                                  HandleAdder handleAdder, JsonBasedUserStore userStore, UrlGenerator urlFor,
                                  Clock clock) {
    this.graphwrapper = graphwrapper;
    this.handleAdder = handleAdder;
    this.urlFor = urlFor;
    this.mappingPerJson = makeIndexed(mappings, JsonToTinkerpopPropertyMap::getJsonName);
    this.mappingPerTinkerpop = makeIndexed(mappings, JsonToTinkerpopPropertyMap::getTinkerpopName);
    nodeFactory = JsonNodeFactory.instance;
    this.userStore = userStore;

    this.clock = clock;
  }

  public UUID create(String collectionName, ObjectNode input, String userId)
    throws InvalidCollectionException, IOException {

    Map<String, JsonToTinkerpopPropertyMap> mappings = this.mappingPerJson.get(collectionName);
    if (mappings == null) {
      throw new InvalidCollectionException();
    }

    UUID id = UUID.randomUUID();

    Graph graph = graphwrapper.getGraph();
    Vertex vertex = graph.addVertex();

    input.fieldNames().forEachRemaining(rethrowConsumer(fieldName -> {
      if (fieldName.equals("@type")) {
        return;
      }
      if (mappings.containsKey(fieldName)) {
        JsonToTinkerpopPropertyMap map = mappings.get(fieldName);
        try {
          vertex.property(map.getTinkerpopName(), map.jsonToTinkerpop(input.get(fieldName)));
        } catch (IOException e) {
          throw new IOException(fieldName + " could not be saved. " + e.getMessage(), e);
        }
      } else {
        throw new IOException(String.format("Items of %s have no property %s", collectionName, fieldName));
      }
    }));

    vertex.property("tim_id", id.toString());
    vertex.property("rev", 1);
    vertex.property("types", String.format(
      "[\"%s\", \"%s\"]",
      typeNameOf(collectionName),
      collectionToAbstractCollection.get(typeNameOf(collectionName))
    ));
    setCreated(vertex, userId);

    duplicateVertex(graph, vertex);
    handleAdder.add(new HandleAdderParameters(id, 1, urlFor.apply(collectionName, id, 1)));
    //Make sure this is the last line of the method. We don't want to commit if an exception happens halfway
    //the return statement below should return a variable directly without any additional logic
    graph.tx().commit();
    return id;
  }

  public JsonNode get(String collectionName, UUID id) throws InvalidCollectionException, NotFoundException {
    return get(collectionName, id, null);
  }

  public JsonNode get(String collectionName, UUID id, Integer rev)
    throws InvalidCollectionException, NotFoundException {
    Map<String, JsonToTinkerpopPropertyMap> mappings = this.mappingPerTinkerpop.get(collectionName);
    if (mappings == null) {
      throw new InvalidCollectionException();
    }
    String entityTypeName = typeNameOf(collectionName);

    ObjectNode result = JsonNodeFactory.instance.objectNode();
    result.set("@type", nodeFactory.textNode(entityTypeName));
    result.set("_id", nodeFactory.textNode(id.toString()));

    Vertex entity;
    if (rev != null) {
      entity = getEntity(id, rev).orElseThrow(NotFoundException::new);
    } else {
      entity = getEntity(id).orElseThrow(NotFoundException::new);
    }

    String[] mappedPropertyNames = mappings.keySet().stream().toArray(String[]::new);
    entity.properties(mappedPropertyNames).forEachRemaining(prop -> {
      JsonToTinkerpopPropertyMap propertyMap = mappings.get(prop.key());
      try {
        JsonNode jsonNode = propertyMap.tinkerpopToJson(prop.value());
        result.set(propertyMap.getJsonName(), jsonNode);
      } catch (IOException e) {
        LOG.error(Logmarkers.databaseInvariant, "Error while exporting " + prop.key(), e);
        //ignore
      }
    });

    result.set(
      "^rev", nodeFactory.numberNode(
        getProp(entity, "rev", Integer.class)
          .orElse(rev == null ? -1 : rev)
      )
    );
    getModification(entity, "modified").ifPresent(val -> result.set("^modified", val));
    getModification(entity, "created").ifPresent(val -> result.set("^created", val));
    //getRelations();
    result.set("@variationRefs", getVariationRefs(entity, id, entityTypeName));

    Tuple<ObjectNode, Long> relations = getRelations(entity, entityTypeName);

    result.set("@relationCount", nodeFactory.numberNode(relations.getRight()));
    result.set("@relations", relations.getLeft());

    result.set("^deleted", nodeFactory.booleanNode(getProp(entity, "deleted", Boolean.class).orElse(false)));

    getProp(entity, "pid", String.class)
      .ifPresent(pid -> result.set("^pid", nodeFactory.textNode(pid)));

    return result;
  }

  private Tuple<ObjectNode, Long> getRelations(Vertex entity, String entityTypeName) {
    ObjectMapper mapper = new ObjectMapper();
    String curVre = vreForEntityType.get(entityTypeName);
    Set<String> entitypesForVre = entityTypePerVre.get(curVre);

    GraphTraversalSource traversal = graphwrapper.getGraph().traversal();

    Map<String, Function<GraphTraversal<Vertex, Vertex>, Iterator<Vertex>>> stringFunctionMap =
      this.derivedRelations.get(entityTypeName);
    Stream<Optional<ObjectNode>> derivedRelations = Stream.empty();
    if (stringFunctionMap != null) {
      derivedRelations = stringFunctionMap.entrySet().stream()
        .flatMap(entry -> stream(entry.getValue().apply(
          traversal.V(entity))).map(vertex -> new Tuple<>(entry.getKey(), vertex))
        )
        .map(tuple -> {
          Vertex vertex = tuple.getRight();
          try {
            Set<String> types = getTypes(mapper, vertex).orElseThrow(IOException::new);
            String curVreVariant = Sets.intersection(entitypesForVre, types).iterator().next();

            String uuid = getProp(vertex, "tim_id", String.class).orElse("");
            return Optional.of(JsonBuilder.jsnO(
              "id", jsn(uuid),
              "path", jsn(urlFor.apply(curVreVariant + "s", UUID.fromString(uuid), null).toString()),
              "type", jsn(tuple.getLeft()),
              "accepted", jsn(true),
              "relationId", jsn("derived"),
              "rev", jsn(1),
              "displayName", jsn(displayNames.get(curVreVariant).apply(vertex))
            ));
          } catch (Exception e) {
            LOG.error(Logmarkers.databaseInvariant, "Error while generating derived-relation data", e);
            return Optional.empty();
          }
        });
    }

    Stream<Triple<Vertex, Edge, String>> outgoing = stream(entity.edges(Direction.OUT))
      .filter(edge -> !edge.label().equals("VERSION_OF"))
      .map(edge -> new Triple<>(edge.inVertex(), edge, edge.label()));

    Stream<Triple<Vertex, Edge, String>> incoming = stream(entity.edges(Direction.IN))
      .filter(edge -> !edge.label().equals("VERSION_OF"))
      .map(edge -> new Tuple<>(edge.outVertex(), edge))
      .map(edge -> new Triple<>(edge.getLeft(), edge.getRight(), (String) traversal
        .V()
        .has("relationtype_regularName", edge.getRight().label())
        .next()
        .value("relationtype_inverseName"))
      );

    Map<String, List<ObjectNode>> relations = concat(derivedRelations, concat(incoming, outgoing)
      .filter(triple -> getProp(triple.getMiddle(), "isLatest", Boolean.class).orElse(false))
      .filter(triple -> !(getProp(triple.getMiddle(), "deleted", Boolean.class).orElse(false)))
      .filter(triple -> getTypes(mapper, triple.getMiddle()).map(
        types -> Sets.intersection(types, entitypesForVre).iterator().hasNext()).orElse(true)
      )
      .map(triple -> {
        Vertex vertex = triple.getLeft();
        Edge edge = triple.getMiddle();
        String label = triple.getRight();
        String uuid = getProp(vertex, "tim_id", String.class).orElse("");
        try {
          Set<String> types = getTypes(mapper, vertex).orElseThrow(IOException::new);
          String curVreVariant = Sets.intersection(entitypesForVre, types).iterator().next();
          return Optional.of(JsonBuilder.jsnO(
            "id", jsn(uuid),
            "path", jsn(urlFor.apply(curVreVariant + "s", UUID.fromString(uuid), null).toString()),
            "type", jsn(label),
            "accepted", jsn(getProp(edge, "accepted", Boolean.class).orElse(true)),
            "relationId", getProp(edge, "tim_id", String.class).map(x -> (JsonNode) jsn(x)).orElse(jsn()),
            "rev", jsn(getProp(edge, "rev", Integer.class).orElse(1)),
            "displayName", jsn(displayNames.get(curVreVariant).apply(vertex))
          ));
        } catch (Exception e) {
          LOG.error(Logmarkers.databaseInvariant, "Error while generating relation data", e);
          return Optional.<ObjectNode>empty();
        }
      }))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(groupingBy(jsn -> jsn.get("type").asText()));

    long count = relations.values().stream().flatMap(Collection::stream).count();
    return new Tuple<>(mapper.valueToTree(relations), count);
  }

  private Optional<Set<String>> getTypes(ObjectMapper mapper, Element vertex) {
    try {
      return getProp(vertex, "types", String.class)
        .map(rethrowFunction(ty -> mapper.<Set<String>>readValue(ty, new TypeReference<Set<String>>() {})));
    } catch (Exception e) {
      LOG.error(Logmarkers.databaseInvariant, "Could not read types from vertex", e);
      return Optional.empty();
    }
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
      LOG.error(Logmarkers.databaseInvariant, "Error while generating variation refs", e);
      return jsnA(
        JsonBuilder.jsnO(
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

  public <V> Optional<V> getProp(final Element vertex, final String key, Class<? extends V> clazz) {
    try {
      Iterator<? extends Property<Object>> revProp = vertex.properties(key);
      if (revProp.hasNext()) {
        return Optional.of(clazz.cast(revProp.next().value()));
      } else {
        return Optional.empty();
      }
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

  private Optional<Vertex> getEntity(UUID id) {
    Optional<Vertex> resultEntity;
    GraphTraversal<Vertex, Vertex> resultSet = graphwrapper.getGraph().traversal()
                                                           .V()
                                                           .has("tim_id", id.toString())
                                                           .has("isLatest", true);
    if (resultSet.hasNext()) {
      resultEntity = Optional.of(resultSet.next());
    } else {
      resultEntity = Optional.empty();
    }
    return resultEntity;
  }

  private Optional<Vertex> getEntity(UUID id, int rev) {
    Optional<Vertex> resultEntity;
    GraphTraversal<Vertex, Vertex> resultSet = graphwrapper.getGraph().traversal()
                                                           .V()
                                                           .has("tim_id", id.toString())
                                                           .has("rev", rev)
                                                           .has("isLatest", false);
    if (resultSet.hasNext()) {
      resultEntity = Optional.of(resultSet.next());
    } else {
      resultEntity = Optional.empty();
    }
    return resultEntity;
  }

  private void setCreated(Vertex vertex, String userId) {
    String value = String.format("{\"timeStamp\":%s,\"userId\":%s}",
      clock.millis(),
      nodeFactory.textNode(userId)
    );
    vertex.property("created", value);
    vertex.property("modified", value);
  }

  private String typeNameOf(String collection) {
    return collection.substring(0, collection.length() - 1);
  }

  private Map<String, Map<String, JsonToTinkerpopPropertyMap>> makeIndexed(
    Map<String, List<JsonToTinkerpopPropertyMap>> input,
    Function<JsonToTinkerpopPropertyMap, String> keySelector) {

    return input.entrySet().stream().collect(Collectors.toMap(
      Map.Entry::getKey,
      x -> x.getValue().stream().collect(Collectors.toMap(
        keySelector,
        y -> y
      ))
    ));

  }

}
