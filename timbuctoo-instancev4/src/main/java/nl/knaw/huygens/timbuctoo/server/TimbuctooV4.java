package nl.knaw.huygens.timbuctoo.server;

import com.codahale.metrics.health.HealthCheck;
import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.search.SearchStore;
import nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudService;
import nl.knaw.huygens.timbuctoo.model.vre.neww.JsonToTinkerpopMappings;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthenticator;
import nl.knaw.huygens.timbuctoo.security.JsonBasedUserStore;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.server.rest.AuthenticationV2_1EndPoint;
import nl.knaw.huygens.timbuctoo.server.rest.DomainCrudCollectionV2_1EndPoint;
import nl.knaw.huygens.timbuctoo.server.rest.DomainCrudEntityV2_1EndPoint;
import nl.knaw.huygens.timbuctoo.server.rest.FacetedSearchV2_1Endpoint;
import nl.knaw.huygens.timbuctoo.server.rest.UserV2_1Endpoint;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;

public class TimbuctooV4 extends Application<TimbuctooConfiguration> {

  public static final String ENCRYPTION_ALGORITHM = "SHA-256";
  private ActiveMQBundle activeMqBundle;

  public static void main(String[] args) throws Exception {
    new TimbuctooV4().run(args);
  }

  @Override
  public void initialize(Bootstrap<TimbuctooConfiguration> bootstrap) {
    activeMqBundle = new ActiveMQBundle();
    bootstrap.addBundle(activeMqBundle);
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
    // Support services
    final Path loginsPath = Paths.get(configuration.getLoginsFilePath());
    final Path usersPath = Paths.get(configuration.getUsersFilePath());

    JsonBasedUserStore userStore = new JsonBasedUserStore(usersPath);
    final LoggedInUserStore loggedInUserStore = new LoggedInUserStore(
      new JsonBasedAuthenticator(loginsPath, ENCRYPTION_ALGORITHM),
      userStore,
      configuration.getAutoLogoutTimeout()
    );

    final TinkerpopGraphManager graphManager = new TinkerpopGraphManager(configuration);
    final PersistenceManager persistenceManager = configuration.getPersistenceManagerFactory().build();
    final HandleAdder handleAdder = new HandleAdder(activeMqBundle, "pids", graphManager, persistenceManager);
    final TinkerpopJsonCrudService crudService = new TinkerpopJsonCrudService(
      graphManager,
      JsonToTinkerpopMappings.getMappings(),
      handleAdder,
      userStore,
      DomainCrudEntityV2_1EndPoint::makeUrl,
      Clock.systemDefaultZone());

    // lifecycle managers
    environment.lifecycle().manage(graphManager);

    // register REST endpoints
    register(environment, new AuthenticationV2_1EndPoint(loggedInUserStore));
    register(environment, new UserV2_1Endpoint(loggedInUserStore));
    register(environment, new FacetedSearchV2_1Endpoint(configuration, graphManager));
    register(environment, new DomainCrudCollectionV2_1EndPoint(crudService));
    register(environment, new DomainCrudEntityV2_1EndPoint(crudService));

    // register health checks
    register(environment, "Encryption algorithm", new EncryptionAlgorithmHealthCheck(ENCRYPTION_ALGORITHM));
    register(environment, "Local logins file", new FileHealthCheck(loginsPath));
    register(environment, "Users file", new FileHealthCheck(usersPath));
    register(environment, "Neo4j database connection", graphManager);
  }

  private void register(Environment environment, String name, HealthCheck healthCheck) {
    environment.healthChecks().register(name, healthCheck);
  }

  private void register(Environment environment, Object component) {
    environment.jersey().register(component);
  }

}
