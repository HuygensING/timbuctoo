package nl.knaw.huygens.timbuctoo.server.databasemigration;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;

public class LabelDatabaseMigration implements DatabaseMigration {
  public static final Logger LOG = LoggerFactory.getLogger(LabelDatabaseMigration.class);


  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public void generateIndexes(Neo4jGraph graph, Transaction transaction) {
    LOG.info("This task does not create new indexes");
  }

  @Override
  public void beforeMigration(TinkerpopGraphManager graphManager) {
    // before hook not needed
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    Neo4jVertex neo4jVertex = (Neo4jVertex) vertex;
    if (neo4jVertex.property("types").isPresent()) {
      String[] types = getEntityTypes(vertex)
              .orElseGet(() -> Try.success(new String[0]))
              .getOrElse(() -> new String[0]);

      for (String type : types) {
        neo4jVertex.addLabel(type);
      }
    }
  }
}
