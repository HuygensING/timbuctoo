package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.model.JsonToTinkerpopPropertyMap;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.crud.VertexDuplicator.duplicateVertex;
import static nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.rethrowConsumer;

public class TinkerpopJsonCrudService {

  private final Map<String, String> collectionToAbstractCollection = ImmutableMap.of(
    "wwperson", "person"
  );

  private final GraphWrapper graphwrapper;
  private final HandleAdder handleAdder;
  private final Map<String, Map<String, JsonToTinkerpopPropertyMap>> mappingPerJson;
  private final Clock clock;
  private final JsonNodeFactory nodeFactory;

  public TinkerpopJsonCrudService(GraphWrapper graphwrapper, Map<String, List<JsonToTinkerpopPropertyMap>> mappings,
                                  HandleAdder handleAdder) {
    this.graphwrapper = graphwrapper;
    this.handleAdder = handleAdder;
    this.mappingPerJson = makeIndexed(mappings, JsonToTinkerpopPropertyMap::getJsonName);
    nodeFactory = JsonNodeFactory.instance;

    this.clock = Clock.systemDefaultZone();
  }

  public TinkerpopJsonCrudService(GraphWrapper graphwrapper, Map<String, List<JsonToTinkerpopPropertyMap>> mappings,
                                  HandleAdder handleAdder, Clock clock) {
    this.graphwrapper = graphwrapper;
    this.handleAdder = handleAdder;
    this.mappingPerJson = makeIndexed(mappings, JsonToTinkerpopPropertyMap::getJsonName);
    nodeFactory = JsonNodeFactory.instance;

    this.clock = clock;
  }

  public UUID create(String collectionName, ObjectNode input, String userId, BiFunction<UUID, Integer, URI> urlFor)
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
    handleAdder.add(new HandleAdderParameters(vertex.id(), urlFor.apply(id, 1)));
    //Make sure this is the last line of the method. We don't want to commit if an exception happens halfway
    //the return statement below should return a variable directly without any additional logic
    graph.tx().commit();
    return id;
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
