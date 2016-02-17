package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.io.IOException;

public class TimbuctooProperty {
  private final GraphTraversal<? extends Object, Try<JsonNode>> getter;
  private final LambdaExceptionUtil.Function_WithExceptions<JsonNode, GraphTraversal<?, ?>, IOException> setter;

  public TimbuctooProperty(GraphTraversal<? extends Object, Try<JsonNode>> getter,
                           LambdaExceptionUtil.Function_WithExceptions<
                             JsonNode, GraphTraversal<?, ?>, IOException> setter) {
    this.getter = getter;
    this.setter = setter;
  }

  public GraphTraversal<? extends Object, Try<JsonNode>> get() {
    return getter.asAdmin().clone();
  }

  public GraphTraversal<? extends Object, ? extends  Object> set(JsonNode value) throws IOException {
    return setter.apply(value);
  }
}
