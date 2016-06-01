package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.experimental.exports.ExcelDescription;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.HasOptions;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

public class LocalProperty extends ReadableProperty {
  private final String propName;
  private final Converter converter;

  public LocalProperty(String propName, Converter converter) {
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

  public GraphTraversal<?, Try<ExcelDescription>> getExcelDescription() {
    Supplier<GraphTraversal<?, Try<ExcelDescription>>> supplier =
      () -> __.<Object, String>values(propName).map(prop -> Try.of(() ->
        converter.tinkerPopToExcel(prop.get(), getGuiTypeId())));
    return supplier.get();
  }
}
