package nl.knaw.huygens.timbuctoo.model.properties;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.HasOptions;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Collection;
import java.util.Optional;

public class LocalProperty extends ReadWriteProperty {
  private final Converter converter;

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
    this.converter = converter;
  }

  @Override
  public String getGuiTypeId() {
    return converter.getTypeIdentifier();
  }

  @Override
  public Optional<Collection<String>> getOptions() {
    if (converter instanceof HasOptions) {
      return Optional.of(((HasOptions) converter).getOptions());
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<Collection<String>> getParts() {
    if (converter instanceof HasParts) {
      return Optional.of(((HasParts) converter).getParts());
    } else {
      return Optional.empty();
    }
  }
}
