package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.io.IOException;
import java.util.Collection;



public class PropertyOrDefault extends TimbuctooProperty {

  public PropertyOrDefault(TimbuctooProperty prop, JsonNode orElse) {
    super(() ->
        __.<Object, Try<JsonNode>>coalesce(
          prop.get().get(),
          __.map(x -> Try.success(orElse))
        ),
      (value) -> {
        throw new IOException("This property cannot be set.");
      }
    );
  }

  public PropertyOrDefault(TimbuctooProperty prop, TimbuctooProperty orElse, String setterException) {
    super(() ->
        __.<Object, Try<JsonNode>>coalesce(
          prop.get().get(),
          orElse.get().get()
        ),
      (value) -> {
        throw new IOException(setterException);
      }
    );
  }


  //cannot be edited in the GUI
  @Override
  public String getGuiTypeId() {
    return null;
  }

  @Override
  public Collection<String> getOptions() {
    return null;
  }
}
