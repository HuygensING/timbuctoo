package nl.knaw.huygens;


import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tinkerpop.api.impl.Neo4jGraphAPIImpl;

import java.io.File;

public class GraphManager implements GraphWrapper {
  private static final SubgraphStrategy LATEST_ELEMENTS = SubgraphStrategy.build()
          .edgeCriterion(__.<Edge>has("isLatest", true)).vertexCriterion(__.<Vertex>has("isLatest", true)).create();

  private final GraphDatabaseService graphDatabase;

  private final Neo4jGraph graph;

  public GraphManager() {
    final File databasePath = new File("../timbuctoo-instancev4/src/spec/resources/database");
    graphDatabase = new GraphDatabaseFactory()
            .newEmbeddedDatabaseBuilder(databasePath)
            .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
            .newGraphDatabase();
    graph = Neo4jGraph.open(new Neo4jGraphAPIImpl(graphDatabase));
  }

  @Override
  public Graph getGraph() {
    return graph;
  }

  @Override
  public GraphTraversalSource getLatestState() {
    return GraphTraversalSource.build().with(LATEST_ELEMENTS).create(graph);
  }

  @Override
  public GraphTraversal<Vertex, Vertex> getCurrentEntitiesFor(String... entityTypeNames) {
    String type = entityTypeNames[0];
    return getLatestState().V().has(T.label, LabelP.of(type));
  }

  public void close() throws Exception {
    graph.close();
    graphDatabase.shutdown();
  }
}
