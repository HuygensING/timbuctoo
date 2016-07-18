package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

public class RdfImportedDefaultDisplayname extends ReadableProperty {
  public static final String TYPE = "default-rdf-imported-displayname";

  public RdfImportedDefaultDisplayname() {
    super(() -> __.<Vertex, Try<JsonNode>>map(traverser -> {
      final Vertex vertex = traverser.get();
      if (vertex.property("rdfUri").isPresent()) {
        return Try.success(jsn(vertex.value("rdfUri")));
      }
      return Try.success(jsn("<no rdf uri found>"));
    }));
  }


  @Override
  public String getTypeId() {
    return TYPE;
  }

  @Override
  public String getUniqueTypeId() {
    return TYPE;
  }
}
