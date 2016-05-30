package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CorruptChangeFix implements DatabaseMigration {

  public CorruptChangeFix() {
  }

  @Override
  public String getName() {
    return "CorruptChangeFix";
  }

  @Override
  public void generateIndexes(Neo4jGraph neo4jGraph, Transaction transaction) {
    // No indices needed
  }

  @Override
  public void beforeMigration(TinkerpopGraphManager graphManager) {
    // No preparation needed
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    VertexProperty<String> property = vertex.property("modified");
    if (property.isPresent()) {
      String value = property.value();
      // LoggerFactory.getLogger(CorruptChangeFix.class).info("modified {}", value);
      if (value.contains("\"\"timbuctoo\"\"")) {
        LoggerFactory.getLogger(CorruptChangeFix.class).info("modified corrupt {}", value);
        String newValue = value.replace("\"\"timbuctoo\"\"", "\"timbuctoo\"");
        vertex.property("modified", newValue);
      }
    }
  }
}
