package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class LabelDatabaseMigration implements DatabaseMigration {

  public static final Logger LOG = LoggerFactory.getLogger(LabelDatabaseMigration.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    Neo4jVertex neo4jVertex = (Neo4jVertex) vertex;
    if (neo4jVertex.property("types").isPresent()) {
      List<String> types =
        MAPPER.readValue((String) neo4jVertex.property("types").value(), new TypeReference<List<String>>() {
        });
      for (String type : types) {
        neo4jVertex.addLabel(type);
      }
    }
  }
}
