package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.io.IOException;
import java.util.function.Supplier;

public class TimbuctooProperty {
  private final Supplier<GraphTraversal<?, Try<JsonNode>>> getter;
  private final LambdaExceptionUtil.Function_WithExceptions<JsonNode, GraphTraversal<?, ?>, IOException> setter;

  public TimbuctooProperty(Supplier<GraphTraversal<?, Try<JsonNode>>> getter,
                           LambdaExceptionUtil.Function_WithExceptions<
                             JsonNode, GraphTraversal<?, ?>, IOException> setter) {
    this.getter = getter;
    this.setter = setter;
  }

  public Supplier<GraphTraversal<?, Try<JsonNode>>> get() {
    return getter;
  }

  public GraphTraversal<?, ?> set(JsonNode value) throws IOException {
    return setter.apply(value);
  }
}
