package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.HasOptions;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public class LocalProperty extends ReadableProperty {
  private static final Logger LOG = LoggerFactory.getLogger(ReadableProperty.class);

  private final String propName;
  private final Converter converter;

  public LocalProperty(String propName, Converter converter) {
    super(() -> __.<Object, String>values(propName).map(prop -> Try.of(() -> converter.tinkerpopToJson(prop.get()))));
    this.propName = propName;
    this.converter = converter;
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
        converter.tinkerPopToExcel(prop.get(), getTypeId())));
    return supplier.get();
  }

  @Override
  public String getTypeId() {
    return converter.getTypeIdentifier();
  }

  @Override
  public Vertex save(GraphWrapper graphWrapper, String clientPropertyName) {
    Graph graph = graphWrapper.getGraph();
    Vertex propertyVertex = graph.addVertex("property");
    propertyVertex.property("clientName", clientPropertyName);
    propertyVertex.property("dbName", propName);
    final String typeId = getTypeId();
    
    if (typeId != null) {
      propertyVertex.property("propertyType", typeId);
    }

    if (converter instanceof HasOptions) {
      try {
        propertyVertex.property("options",
          new ObjectMapper().writeValueAsString(((HasOptions) converter).getOptions()));
      } catch (JsonProcessingException e) {
        LOG.error("Failed to write options to database for property {}", propName);
      }
    }
    return propertyVertex;
  }
}
