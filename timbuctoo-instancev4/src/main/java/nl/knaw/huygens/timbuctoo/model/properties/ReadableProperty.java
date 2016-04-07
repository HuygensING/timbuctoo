package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.function.Supplier;

public abstract class ReadableProperty {
  private final Supplier<GraphTraversal<?, Try<JsonNode>>> getter;

  public ReadableProperty(Supplier<GraphTraversal<?, Try<JsonNode>>> getter) {
    this.getter = getter;
  }

  public GraphTraversal<?, Try<JsonNode>> traversal() {
    return getter.get();
  }

}
