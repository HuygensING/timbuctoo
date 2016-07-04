package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.function.Supplier;

public abstract class ReadableProperty {
  private final Supplier<GraphTraversal<?, Try<JsonNode>>> getter;

  public static final String DATABASE_LABEL = "property";
  public static final String DISPLAY_NAME_PROPERTY_NAME = "@displayName";
  public static final String CLIENT_PROPERTY_NAME = "clientName";
  public static final String PROPERTY_TYPE_NAME = "propertyType";
  public static final String HAS_NEXT_PROPERTY_RELATION_NAME = "hasNextProperty";

  public ReadableProperty(Supplier<GraphTraversal<?, Try<JsonNode>>> getter) {
    this.getter = getter;
  }

  public GraphTraversal<?, Try<JsonNode>> traversal() {
    return getter.get();
  }

  public abstract String getTypeId();

  public Vertex save(Graph graph, String clientPropertyName) {
    Vertex propertyVertex = graph.addVertex(DATABASE_LABEL);
    propertyVertex.property(CLIENT_PROPERTY_NAME, clientPropertyName);
    propertyVertex.property(PROPERTY_TYPE_NAME, getTypeId());

    return propertyVertex;
  }
}
