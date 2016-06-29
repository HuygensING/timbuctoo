package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.properties.converters.HasOptions;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.function.Supplier;

public abstract class ReadableProperty {
  private final Supplier<GraphTraversal<?, Try<JsonNode>>> getter;

  public ReadableProperty(Supplier<GraphTraversal<?, Try<JsonNode>>> getter) {
    this.getter = getter;
  }

  public GraphTraversal<?, Try<JsonNode>> traversal() {
    return getter.get();
  }

  public abstract String getTypeId();

  public Vertex save(GraphWrapper graphWrapper, String clientPropertyName) {
    Graph graph = graphWrapper.getGraph();
    Vertex propertyVertex = graph.addVertex("property");
    propertyVertex.property("clientName", clientPropertyName);
    final String typeId = getTypeId();
    if (typeId != null) {
      propertyVertex.property("propertyType", typeId);
    }
    return propertyVertex;
  }
}
