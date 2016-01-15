package nl.knaw.huygens.timbuctoo.server;

import com.codahale.metrics.health.HealthCheck;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Neo4jHealthCheck extends HealthCheck {
  public static final Logger LOG = LoggerFactory.getLogger(Neo4jHealthCheck.class);
  private final GraphDatabaseService graphDatabase;
  private final File databasePath;

  public Neo4jHealthCheck(GraphDatabaseService graphDatabase, File databasePath) {

    this.graphDatabase = graphDatabase;
    this.databasePath = databasePath;
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
}
