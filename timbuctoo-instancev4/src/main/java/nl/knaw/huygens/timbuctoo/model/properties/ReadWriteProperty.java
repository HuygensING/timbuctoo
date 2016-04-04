package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.io.IOException;
import java.util.function.Supplier;

public abstract class ReadWriteProperty extends ReadOnlyProperty {
  private final LambdaExceptionUtil.Function_WithExceptions<JsonNode, GraphTraversal<?, ?>, IOException> setter;

  public ReadWriteProperty(Supplier<GraphTraversal<?, Try<JsonNode>>> getter, LambdaExceptionUtil.Function_WithExceptions<JsonNode, GraphTraversal<?, ?>, IOException> setter) {
    super(getter);
    this.setter = setter;
  }

  public GraphTraversal<?, ?> set(JsonNode value) throws IOException {
    return setter.apply(value);
  }

  public abstract String getGuiTypeId();

  public abstract java.util.Collection<String> getOptions();

}
