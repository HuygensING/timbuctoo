package nl.knaw.huygens.timbuctoo.model.properties;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.HasOptions;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Collection;

public class LocalProperty extends TimbuctooProperty {
  private final String guiTypeId;
  private final Collection<String> options;

  public LocalProperty(String propName, Converter converter) {
    super(() -> __.<Object, String>values(propName).map(prop -> Try.of(() -> converter.tinkerpopToJson(prop.get()))),
      (value) -> {
        if (value == null) {
          return __.sideEffect(vertex -> ((Vertex) vertex.get()).property(propName).remove());
        } else {
          return __.property(propName, converter.jsonToTinkerpop(value));
        }
      }
    );
    guiTypeId = converter.getTypeIdentifier();
    if (converter instanceof HasOptions) {
      options = ((HasOptions) converter).getOptions();
    } else {
      options = null;
    }
  }

  @Override
  public String getGuiTypeId() {
    return guiTypeId;
  }

  @Override
  public Collection<String> getOptions() {
    return options;
  }
}
