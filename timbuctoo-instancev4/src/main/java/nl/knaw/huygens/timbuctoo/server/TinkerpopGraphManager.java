package nl.knaw.huygens.timbuctoo.server;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.lifecycle.Managed;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tinkerpop.api.impl.Neo4jGraphAPIImpl;

import java.io.File;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;

public class TinkerpopGraphManager extends HealthCheck implements Managed, GraphWrapper {
  private static final SubgraphStrategy LATEST_ELEMENTS =
    SubgraphStrategy.build().edgeCriterion(has("isLatest", true)).vertexCriterion(has("isLatest", true)).create();

  final TimbuctooConfiguration configuration;
  private Neo4jGraph graph;
  private File databasePath;
  private GraphDatabaseService graphDatabase;

  public TinkerpopGraphManager(TimbuctooConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void start() throws Exception {
    databasePath = new File(configuration.getDatabasePath());
    graphDatabase = new GraphDatabaseFactory()
      .newEmbeddedDatabaseBuilder(databasePath)
      .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
      .newGraphDatabase();

    this.graph = Neo4jGraph.open(new Neo4jGraphAPIImpl(graphDatabase));
  }

  @Override
  public void stop() throws Exception {
    graph.close();
  }

  @Override
  protected Result check() throws Exception {
    /*
     * TODO find a better way to check the database is available.
     * Neo4j says it is still available when the database directory is removed.
     * It seems like isAvailable only checks the database is shutdown or not.
     * Trying to retrieve nodes from the non-existing database does not result in an Exception.
     */
    if (graphDatabase.isAvailable(1000)) {
      if (databasePath.exists()) {
        return Result.healthy();
      }
      return Result.unhealthy("Path to database [%s] does not exist", databasePath);
    }

    return Result.unhealthy("Database is unavailable.");
  }

  @Override
  public Graph getGraph() {
    return this.graph;
  }

  @Override
  public GraphTraversalSource getLatestState() {
    return GraphTraversalSource.build().with(LATEST_ELEMENTS).create(graph);
  }

}
