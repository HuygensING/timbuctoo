package nl.knaw.huygens.timbuctoo.server.healthchecks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;

public class LabelsAddedToVertexDatabaseCheck implements DatabaseCheck {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public ValidationResult check(Vertex vertex) {
    if (!(vertex instanceof Neo4jVertex)) {
      return new ElementValidationResult(false, "Is not a Neo4jVertex.");
    }
    if (vertex.property("types").isPresent()) {
      Set<String> types = null;
      try {
        types = OBJECT_MAPPER.readValue((String) vertex.property("types").value(), new TypeReference<Set<String>>() {
        });

        Sets.SetView<String> difference = Sets.difference(types, ((Neo4jVertex) vertex).labels());

        if (!difference.isEmpty()) {
          return new ElementValidationResult(
            false,
            String.format("Vertex with tim_id %s misses labels %s\n",
              getProp(vertex, "tim_id", String.class).orElse("<UNKNOWN>"),
              difference
            )
          );
        }
      } catch (IOException e) {
        return new ElementValidationResult(false, e.getMessage());
      }

    }
    return new ElementValidationResult(true,
      String.format("Vertex with tim_id %s is valid.",
        getProp(vertex, "tim_id", String.class).orElse("<UNKNOWN>"))
    );
  }
}
