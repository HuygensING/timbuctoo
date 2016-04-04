package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;



public class PropertyOrDefault extends ReadOnlyProperty {

  public PropertyOrDefault(ReadOnlyProperty prop, JsonNode orElse) {
    super(() ->
      __.<Object, Try<JsonNode>>coalesce(
        prop.get(),
        __.map(x -> Try.success(orElse))
      )
    );
  }

  public PropertyOrDefault(ReadOnlyProperty prop, ReadOnlyProperty orElse) {
    super(() ->
      __.<Object, Try<JsonNode>>coalesce(
        prop.get(),
        orElse.get()
      )
    );
  }
}
