package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public abstract class ReadableProperty {
  private final Supplier<GraphTraversal<?, Try<JsonNode>>> getter;

  public static final String DATABASE_LABEL = "property";
  public static final String DISPLAY_NAME_PROPERTY_NAME = "@displayName";
  public static final String CLIENT_PROPERTY_NAME = "clientName";
  public static final String PROPERTY_TYPE_NAME = "propertyType";
  public static final String HAS_NEXT_PROPERTY_RELATION_NAME = "hasNextProperty";

  public ReadableProperty(Supplier<GraphTraversal<?, Try<JsonNode>>> getter) {
    this.getter = getter;
  }

  public GraphTraversal<?, Try<JsonNode>> traversal() {
    return getter.get();
  }

  public abstract String getTypeId();

  public abstract String getUniqueTypeId();

  public Vertex save(Graph graph, String clientPropertyName) {
    Vertex propertyVertex = graph.addVertex(DATABASE_LABEL);
    propertyVertex.property(CLIENT_PROPERTY_NAME, clientPropertyName);
    propertyVertex.property(PROPERTY_TYPE_NAME, getUniqueTypeId());

    return propertyVertex;
  }

  public static ReadableProperty load(Vertex propertyVertex)
    throws IOException, NoSuchMethodException, InstantiationException, IllegalAccessException,
    InvocationTargetException {
    final String type = propertyVertex.value(PROPERTY_TYPE_NAME);

    if (type.equals(WwDocumentDisplayName.TYPE)) {
      return new WwDocumentDisplayName();
    } else if (type.equals(WwPersonDisplayName.TYPE)) {
      return new WwPersonDisplayName();
    } else if (type.equals(RdfImportedDefaultDisplayname.TYPE)) {
      return new RdfImportedDefaultDisplayname();
    }

    String[] options = null;
    if (propertyVertex.property(LocalProperty.OPTIONS_PROPERTY_NAME).isPresent()) {
      final String optionsJson = propertyVertex.value(LocalProperty.OPTIONS_PROPERTY_NAME);
      options = new ObjectMapper().readValue(optionsJson, new TypeReference<String[]>() { });
    }

    if (propertyVertex.property(LocalProperty.DATABASE_PROPERTY_NAME).isPresent()) {
      final String dbName = propertyVertex.value(LocalProperty.DATABASE_PROPERTY_NAME);
      return new LocalProperty(dbName, Converters.forType(type, options));
    }

    throw new IOException("Unknown property configuration");
  }
}
