package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class PropertyOrDefault extends ReadableProperty {

  public PropertyOrDefault(ReadableProperty prop, JsonNode orElse) {
    super(() ->
      __.<Object, Try<JsonNode>>coalesce(
        prop.traversal(),
        __.map(x -> Try.success(orElse))
      )
    );
  }

  public PropertyOrDefault(ReadableProperty prop, ReadableProperty orElse) {
    super(() ->
      __.<Object, Try<JsonNode>>coalesce(
        prop.traversal(),
        orElse.traversal()
      )
    );
  }

  @Override
  public String getTypeId() {
    return "property-or-default";
  }
}
