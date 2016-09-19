package nl.knaw.huygens.timbuctoo.server;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;

import java.util.LinkedHashMap;

import static org.slf4j.LoggerFactory.getLogger;

public class TestableTinkerpopGraphManager extends TinkerpopGraphManager {
  private static final Logger LOG = getLogger(TestableTinkerpopGraphManager.class);

  public TestableTinkerpopGraphManager(GraphDatabaseService graphDatabaseService, Neo4jGraph graph) {
    super(new TimbuctooConfiguration(), new LinkedHashMap<>());
    this.graphDatabase = graphDatabaseService;
    this.graph = graph;
  }

  @Override
  public void start() throws Exception {
    graphWaitList.forEach(consumer -> {
      try {
        consumer.accept(graph);
      } catch (RuntimeException e) {
        LOG.error(e.getMessage(), e);
      }
    });
  }

  @Override
  protected Result check() throws Exception {
    return Result.healthy();
  }

  @Override
  protected void initGraphDatabaseService() {
  }
}
