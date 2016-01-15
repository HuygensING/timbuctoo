package nl.knaw.huygens.timbuctoo.server;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthenticator;
import nl.knaw.huygens.timbuctoo.security.JsonBasedUserStore;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.server.rest.AuthenticationV2_1EndPoint;
import nl.knaw.huygens.timbuctoo.server.rest.FacetedSearchV2_1Endpoint;
import nl.knaw.huygens.timbuctoo.server.rest.Searcher;
import nl.knaw.huygens.timbuctoo.server.rest.UserV2_1Endpoint;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tinkerpop.api.impl.Neo4jGraphAPIImpl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TimbuctooV4 extends Application<TimbuctooConfiguration> {

  public static final String ENCRYPTION_ALGORITHM = "SHA-256";

  public static void main(String[] args) throws Exception {
    new TimbuctooV4().run(args);
  }

  @Override
  public void initialize(Bootstrap<TimbuctooConfiguration> bootstrap) {
    /*
     * Make it possible to use environment variables in the config.
     * see: http://www.dropwizard.io/0.9.1/docs/manual/core.html#environment-variables
     */
    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor(false)));
  }

  @Override
  public void run(TimbuctooConfiguration configuration, Environment environment) throws Exception {
    Path loginsPath = Paths.get(configuration.getLoginsFilePath());
    JsonBasedAuthenticator authenticator = new JsonBasedAuthenticator(loginsPath, ENCRYPTION_ALGORITHM);

    Path usersPath = Paths.get(configuration.getUsersFilePath());
    JsonBasedUserStore userStore = new JsonBasedUserStore(usersPath);

    LoggedInUserStore loggedInUserStore = new LoggedInUserStore(
      authenticator,
      userStore,
      configuration.getAutoLogoutTimeout());

    // register REST endpoints
    environment.jersey().register(new AuthenticationV2_1EndPoint(loggedInUserStore));
    environment.jersey().register(new UserV2_1Endpoint(loggedInUserStore));
    environment.jersey().register(new FacetedSearchV2_1Endpoint(
      new Searcher(getGraph(environment, configuration), configuration.getSearchResultAvailabilityTimeout())));

    // register health checks
    registerHealthCheck(environment, "Encryption algorithm", new EncryptionAlgorithmHealthCheck(ENCRYPTION_ALGORITHM));
    registerHealthCheck(environment, "Local logins file", new FileHealthCheck(loginsPath));
    registerHealthCheck(environment, "Users file", new FileHealthCheck(usersPath));
  }



  private Neo4jGraph getGraph(Environment environment, TimbuctooConfiguration configuration) {
    File databasePath = new File(configuration.getDatabasePath());
    GraphDatabaseService graphDatabase = new GraphDatabaseFactory()
      .newEmbeddedDatabaseBuilder(databasePath)
      .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
      .newGraphDatabase();

    registerHealthCheck(environment, "Neo4j database connection", new Neo4jHealthCheck(graphDatabase, databasePath));

    return Neo4jGraph.open(new Neo4jGraphAPIImpl(graphDatabase));
  }

  private void registerHealthCheck(Environment environment, String name, HealthCheck healthCheck) {
    environment.healthChecks().register(name, healthCheck);
  }


}
