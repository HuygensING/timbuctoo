package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;



public class PropertyOrDefault extends ReadableProperty {

  public PropertyOrDefault(ReadableProperty prop, JsonNode orElse) {
    super(() ->
      __.<Object, Try<JsonNode>>coalesce(
        prop.get(),
        __.map(x -> Try.success(orElse))
      )
    );
  }

  public PropertyOrDefault(ReadableProperty prop, ReadableProperty orElse) {
    super(() ->
      __.<Object, Try<JsonNode>>coalesce(
        prop.get(),
        orElse.get()
      )
    );
  }
}
