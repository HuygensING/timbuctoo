package nl.knaw.huygens.timbuctoo.server;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.Lists;
import io.dropwizard.lifecycle.Managed;
import nl.knaw.huygens.timbuctoo.server.databasemigration.DatabaseMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.DatabaseMigrator;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tinkerpop.api.impl.Neo4jGraphAPIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;

public class TinkerPopGraphManager extends HealthCheck implements Managed, GraphWrapper, GraphWaiter {
  private static final SubgraphStrategy LATEST_ELEMENTS =
          SubgraphStrategy.build().edgeCriterion(has("isLatest", true)).vertexCriterion(has("isLatest", true)).create();

  private static final Logger LOG = LoggerFactory.getLogger(TimbuctooV4.class);

  private final TimbuctooConfiguration configuration;
  private final LinkedHashMap<String, DatabaseMigration> migrations;
  protected final List<Consumer<Graph>> graphWaitList;

  private File databasePath;

  protected Neo4jGraph graph;
  protected GraphDatabaseService graphDatabase;

  public TinkerPopGraphManager(TimbuctooConfiguration configuration,
                               LinkedHashMap<String, DatabaseMigration> migrations) {
    this.configuration = configuration;
    graphWaitList = Lists.newArrayList();

    this.migrations = migrations;
  }

  @Override
  public void start() throws Exception {
    synchronized (graphWaitList) {
      initGraphDatabaseService();
      this.graph = Neo4jGraph.open(new Neo4jGraphAPIImpl(graphDatabase));
      new DatabaseMigrator(this, migrations).execute();
      graphWaitList.forEach(consumer -> {
        try {
          consumer.accept(graph);
        } catch (RuntimeException e) {
          LOG.error(e.getMessage(), e);
        }
      });
    }
  }

  protected void initGraphDatabaseService() {
    if (databasePath == null) {
      databasePath = new File(configuration.getDatabasePath());
    }
    if (graphDatabase == null) {
      graphDatabase = new GraphDatabaseFactory()
        .newEmbeddedDatabaseBuilder(databasePath)
        .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
        .newGraphDatabase();
      LOG.info("Using database at " + databasePath.getAbsolutePath());
    }
  }

  @Override
  public void stop() throws Exception {
    graph.close();
    graphDatabase.shutdown();
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
  public void onGraph(Consumer<Graph> graphConsumer) {
    synchronized (graphWaitList) {
      if (graph == null) {
        graphWaitList.add(graphConsumer);
      } else {
        graphConsumer.accept(graph);
      }
    }
  }

  @Override
  public GraphTraversalSource getLatestState() {
    return GraphTraversalSource.build().with(LATEST_ELEMENTS).create(graph);
  }


  @Override
  public GraphTraversal<Vertex, Vertex> getCurrentEntitiesFor(String... entityTypeNames) {
    if (entityTypeNames.length == 1) {
      String type = entityTypeNames[0];
      return getLatestState().V().has(T.label, LabelP.of(type));
    } else {
      P<String> labels = LabelP.of(entityTypeNames[0]);
      for (int i = 1; i < entityTypeNames.length; i++) {
        labels = labels.or(LabelP.of(entityTypeNames[i]));
      }

      return getLatestState().V().has(T.label, labels);
    }
  }

  public GraphDatabaseService getGraphDatabase() {
    initGraphDatabaseService();
    return graphDatabase;
  }
}
