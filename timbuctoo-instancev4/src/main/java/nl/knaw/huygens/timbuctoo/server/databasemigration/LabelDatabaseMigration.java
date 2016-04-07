package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
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
  public void execute(TimbuctooConfiguration configuration, GraphWrapper graphWrapper) throws IOException {
    Graph graph = graphWrapper.getGraph();
    graph.tx().open();
    GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V();
    while (traversal.hasNext()) {
      Neo4jVertex vertex = (Neo4jVertex) traversal.next();
      if (vertex.property("types").isPresent()) {
        List<String> types =
          MAPPER.readValue((String) vertex.property("types").value(), new TypeReference<List<String>>() {
          });
        for (String type : types) {
          vertex.addLabel(type);

        }
      }
    }
    graph.tx().commit();
  }
}
