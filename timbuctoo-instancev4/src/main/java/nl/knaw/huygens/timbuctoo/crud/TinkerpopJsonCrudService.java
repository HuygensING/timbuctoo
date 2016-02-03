package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.model.JsonToTinkerpopPropertyMap;
import nl.knaw.huygens.timbuctoo.security.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.JsonBasedUserStore;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

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
import static nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.rethrowConsumer;
import static nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.rethrowFunction;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;

public class TinkerpopJsonCrudService {

  private final Map<String, String> collectionToAbstractCollection = ImmutableMap.of(
    "wwperson", "person"//FIXME add all collections
  );
  private final Map<String, String> vreForEntityType = ImmutableMap.of(
    "wwperson", "neww",
    "wwdocument", "neww"
  );
  private final Map<String, Set<String>> entityTypePerVRE = ImmutableMap.of(
    "neww", Sets.newHashSet("wwperson", "wwdocument")
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
    handleAdder.add(new HandleAdderParameters(vertex.id(), urlFor.apply(collectionName, id, 1)));
    //Make sure this is the last line of the method. We don't want to commit if an exception happens halfway
    //the return statement below should return a variable directly without any additional logic
    graph.tx().commit();
    return id;
  }

  public JsonNode get(String collectionName, UUID id) throws InvalidCollectionException {
    return get(collectionName, id, null);
  }

  public JsonNode get(String collectionName, UUID id, Integer rev) throws InvalidCollectionException {
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
      entity = getEntity(id, rev).get(); //FIXME throw when not found
    } else {
      entity = getEntity(id).get(); //FIXME throw when not found
    }

    String[] mappedPropertyNames = mappings.keySet().stream().toArray(String[]::new);
    entity.properties(mappedPropertyNames).forEachRemaining(prop -> {
      JsonToTinkerpopPropertyMap propertyMap = mappings.get(prop.key());
      try {
        JsonNode jsonNode = propertyMap.tinkerpopToJson(prop.value());
        result.set(propertyMap.getJsonName(), jsonNode);
      } catch (IOException e) {
        //FIXME log
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

    Stream<Tuple<Vertex, String>> outgoing = stream(entity.edges(Direction.OUT))
      .filter(edge -> !edge.label().equals("VERSION_OF"))
      .map(edge -> new Tuple<>(edge.inVertex(), edge.label()));

    Stream<Tuple<Vertex, String>> incoming = stream(entity.edges(Direction.IN))
      .filter(edge -> !edge.label().equals("VERSION_OF"))
      .map(edge -> new Tuple<>(edge.outVertex(), edge.label()))
      .map(edge -> new Tuple<>(edge.getLeft(), (String) graphwrapper.getGraph().traversal()
        .V()
        .has("relationtype_regularName", edge.getRight())
        .next()
        .value("relationtype_inverseName"))
      );

    Map<String, List<ObjectNode>> relations = concat(incoming, outgoing)
      .map(edge -> {
        Vertex vertex = edge.getLeft();
        String label = edge.getRight();
        String uuid = vertex.value("tim_id");
        try {
          Set<String> types = getProp(vertex, "types", String.class)
            .map(rethrowFunction(ty -> (Set<String>) mapper.readValue(ty, new TypeReference<Set<String>>(){})))
            .orElseThrow(IOException::new);
          String curVreVariant = Sets.intersection(entityTypePerVRE.get(curVre), types).iterator().next();
          return Optional.of(jsn(
            "id", jsn(uuid),
            "path", jsn(urlFor.apply(curVreVariant + "s", UUID.fromString(uuid), null).toString()),
            "type", jsn(label)
          ));
        } catch (Exception e) {
          return Optional.<ObjectNode>empty();
        }
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(groupingBy(jsn -> jsn.get("type").asText()));

    long count = relations.values().stream().flatMap(Collection::stream).count();
    return new Tuple<>(mapper.valueToTree(relations), count);
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
      ArrayNode variationRefs = nodeFactory.arrayNode();
      //When something goes wrong we log the error and return a functional representation
      //FIXME: log
      ObjectNode ownRef = nodeFactory.objectNode();
      ownRef.set("id", nodeFactory.textNode(id.toString()));
      ownRef.set("type", nodeFactory.textNode(entityTypeName));
      variationRefs.add(ownRef);
      return variationRefs;
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
            .ifPresent(userName -> {
              modifiedObj.set("username", nodeFactory.textNode(userName));
            });
        } catch (AuthenticationUnavailableException e) {
          modifiedObj.set("username", nodeFactory.nullNode());
        }
        return modifiedObj;
      });
  }

  public <V> Optional<V> getProp(final Vertex vertex, final String key, Class<? extends V> clazz) {
    try {
      Iterator<VertexProperty<Object>> revProp = vertex.properties(key);
      if (revProp.hasNext()) {
        return Optional.of(clazz.cast(revProp.next().value()));
      } else {
        return Optional.empty();
      }
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

  private Optional<Vertex> getEntity(UUID id/*, String entityTypeName*/) {
    Optional<Vertex> resultEntity;
    GraphTraversal<Vertex, Vertex> resultSet = graphwrapper.getGraph().traversal()
      .V()
      //.has("types", P.test((types, o2) -> types instanceof String && ((String) types).contains(entityTypeName), null))
      .has("tim_id", id.toString())
      .has("isLatest", true);
    if (resultSet.hasNext()) {
      resultEntity = Optional.of(resultSet.next());
    } else {
      resultEntity = Optional.empty();
    }
    return resultEntity;
  }

  private Optional<Vertex> getEntity(UUID id, int rev/*, String entityTypeName*/) {
    Optional<Vertex> resultEntity;
    GraphTraversal<Vertex, Vertex> resultSet = graphwrapper.getGraph().traversal()
      .V()
      //.has("types", P.test((types, o2) -> types instanceof String && ((String) types).contains(entityTypeName), null))
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
      x -> x.getKey(),
      x -> x.getValue().stream().collect(Collectors.toMap(
        keySelector,
        y -> y
      ))
    ));

  }

}
