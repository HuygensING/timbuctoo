package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.HasOptions;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

public class ReadWriteProperty extends ReadableProperty {
  private final String propName;
  private final Converter converter;

  public ReadWriteProperty(String propName, Converter converter) {
    super(() -> __.<Object, String>values(propName).map(prop -> Try.of(() -> converter.tinkerpopToJson(prop.get()))));
    this.propName = propName;
    this.converter = converter;
  }

  public String getGuiTypeId() {
    return converter.getTypeIdentifier();
  }

  public Optional<Collection<String>> getOptions() {
    if (converter instanceof HasOptions) {
      return Optional.of(((HasOptions) converter).getOptions());
    } else {
      return Optional.empty();
    }
  }

  public Optional<Collection<String>> getParts() {
    if (converter instanceof HasParts) {
      return Optional.of(((HasParts) converter).getParts());
    } else {
      return Optional.empty();
    }
  }

  public void setJson(Vertex vertex, JsonNode value) throws IOException {
    if (value == null) {
      vertex.property(propName).remove();
    } else {
      vertex.property(propName, converter.jsonToTinkerpop(value));
    }
  }
}
