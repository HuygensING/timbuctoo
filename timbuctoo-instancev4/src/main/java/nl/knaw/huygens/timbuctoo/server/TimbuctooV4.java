package nl.knaw.huygens.timbuctoo.server;

import com.codahale.metrics.JmxAttributeGauge;
import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.java8.Java8Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudService;
import nl.knaw.huygens.timbuctoo.logging.LoggingFilter;
import nl.knaw.huygens.timbuctoo.model.properties.JsonMetadata;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexDescriptionFactory;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthenticator;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthorizer;
import nl.knaw.huygens.timbuctoo.security.JsonBasedUserStore;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.server.databasemigration.DatabaseMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.LabelDatabaseMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.WwDocumentSortIndexesDatabaseMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.WwPersonSortIndexesDatabaseMigration;
import nl.knaw.huygens.timbuctoo.server.endpoints.RootEndpoint;
import nl.knaw.huygens.timbuctoo.server.endpoints.admin.DatabaseValidationServlet;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Authenticate;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.BulkUpload;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Graph;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Gremlin;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Metadata;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.RelationTypes;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Search;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.Autocomplete;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.Index;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.SingleEntity;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.users.Me;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseHealthCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseValidator;
import nl.knaw.huygens.timbuctoo.server.healthchecks.EncryptionAlgorithmHealthCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.FileHealthCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks.LabelsAddedToVertexDatabaseCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks.SortIndexesDatabaseCheck;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.FacetValueDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.List;
import java.util.Properties;

import static com.codahale.metrics.MetricRegistry.name;
import static nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.rethrowConsumer;

public class TimbuctooV4 extends Application<TimbuctooConfiguration> {

  public static final String ENCRYPTION_ALGORITHM = "SHA-256";
  public static final String HANDLE_QUEUE = "pids";
  private static final Logger LOG = LoggerFactory.getLogger(TimbuctooV4.class);
  private ActiveMQBundle activeMqBundle;

  public static void main(String[] args) throws Exception {
    new TimbuctooV4().run(args);
  }

  @Override
  public void initialize(Bootstrap<TimbuctooConfiguration> bootstrap) {
    //bundles
    activeMqBundle = new ActiveMQBundle();
    bootstrap.addBundle(activeMqBundle);
    bootstrap.addBundle(new Java8Bundle());
    bootstrap.addBundle(new MultiPartBundle());
    bootstrap.addBundle(new AssetsBundle("/static", "/static", "index.html"));
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
    //Make sure we know what version is running
    Properties properties = new Properties();
    properties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
    String currentVersion = properties.getProperty("git.commit.id");

    LoggerFactory.getLogger(this.getClass()).info("Now launching timbuctoo version: " + currentVersion);

    // Support services
    final AuthenticationHandler authHandler = configuration.getFederatedAuthentication().makeHandler(environment);
    final Path loginsPath = Paths.get(configuration.getLoginsFilePath());
    final Path usersPath = Paths.get(configuration.getUsersFilePath());
    final LoginFileMigration loginFileMigration = new LoginFileMigration();

    // Convert login file
    if (!loginFileMigration.isConverted(loginsPath)) {
      LOG.info("Migrating logins file to use byte[] for password property");
      loginFileMigration.convert(loginsPath);
    }

    JsonBasedUserStore userStore = new JsonBasedUserStore(usersPath);
    final LoggedInUserStore loggedInUserStore = new LoggedInUserStore(
      new JsonBasedAuthenticator(loginsPath, ENCRYPTION_ALGORITHM),
      userStore,
      configuration.getAutoLogoutTimeout(),
      authHandler
    );

    // Database migrations
    final List<DatabaseMigration> databaseMigrations = Lists.newArrayList(
      new LabelDatabaseMigration(),
      new WwPersonSortIndexesDatabaseMigration(),
      new WwDocumentSortIndexesDatabaseMigration()
    );

    final TinkerpopGraphManager graphManager = new TinkerpopGraphManager(configuration, databaseMigrations);
    final PersistenceManager persistenceManager = configuration.getPersistenceManagerFactory().build();
    final HandleAdder handleAdder = new HandleAdder(activeMqBundle, HANDLE_QUEUE, graphManager, persistenceManager);
    final Vres vres = HuygensIng.mappings;
    final TinkerpopJsonCrudService crudService = new TinkerpopJsonCrudService(
      graphManager,
      vres,
      handleAdder,
      userStore,
      (coll, id, rev) -> URI.create(configuration.getBaseUri() + SingleEntity.makeUrl(coll, id, rev).getPath()),
      (coll, id, rev) -> URI.create(configuration.getBaseUri() + SingleEntity.makeUrl(coll, id, rev).getPath()),
      (coll, id, rev) -> URI.create(SingleEntity.makeUrl(coll, id, rev).getPath().replaceFirst("^/v2.1/", "")),
      Clock.systemDefaultZone(),
      new IndexDescriptionFactory(),
      new JsonBasedAuthorizer(configuration.getAuthorizationsPath()));
    final JsonMetadata jsonMetadata = new JsonMetadata(vres, graphManager, HuygensIng.keywordTypes);


    // lifecycle managers
    environment.lifecycle().manage(graphManager);


    // register REST endpoints
    register(environment, new RootEndpoint());
    register(environment, new Authenticate(loggedInUserStore));
    register(environment, new Me(loggedInUserStore));
    register(environment, new Search(configuration, graphManager));
    register(environment, new Autocomplete(crudService));
    register(environment, new Index(crudService, loggedInUserStore));
    register(environment, new SingleEntity(crudService, loggedInUserStore));
    register(environment, new Gremlin(graphManager));
    register(environment, new Graph(graphManager));
    register(environment, new BulkUpload(vres, graphManager));
    register(environment, new RelationTypes(graphManager));
    register(environment, new Metadata(jsonMetadata));
    // admin endpoints
    // database validator
    DatabaseValidator databaseValidator = createDatabaseValidator(graphManager);
    environment.admin()
               .addServlet("databasevalidation", new DatabaseValidationServlet(databaseValidator))
               .addMapping("/databasevalidation");

    // register health checks
    register(environment, "Encryption algorithm", new EncryptionAlgorithmHealthCheck(ENCRYPTION_ALGORITHM));
    register(environment, "Local logins file", new FileHealthCheck(loginsPath));
    register(environment, "Users file", new FileHealthCheck(usersPath));

    register(environment, "Neo4j database connection", graphManager);
    register(environment, "Database", new DatabaseHealthCheck(databaseValidator));

    //Log all http requests
    register(environment, new LoggingFilter(1024, currentVersion));
    //Allow all CORS requests
    register(environment, new PromiscuousCorsFilter());

    //Add embedded AMQ (if any) to the metrics
    configuration.getLocalAmqJmxPath(HANDLE_QUEUE).ifPresent(rethrowConsumer(jmxPath -> {
      String dwMetricName = name(this.getClass(), "localAmq");
      ObjectName jmxMetricName = new ObjectName(jmxPath);

      environment.metrics().register(
        dwMetricName + ".enqueueCount",
        new JmxAttributeGauge(jmxMetricName, "EnqueueCount")
      );
      environment.metrics().register(
        dwMetricName + ".dequeueCount",
        new JmxAttributeGauge(jmxMetricName, "DequeueCount")
      );
    }));

    setupObjectMapping(environment);
  }

  public DatabaseValidator createDatabaseValidator(TinkerpopGraphManager graphManager) {
    List<DatabaseCheck> databaseChecks = Lists.newArrayList(
            new LabelsAddedToVertexDatabaseCheck(),
            new SortIndexesDatabaseCheck()
    );
    return new DatabaseValidator(graphManager, 1, Clock.systemUTC(), databaseChecks);
  }


  private void setupObjectMapping(Environment environment) {
    // object mapping
    SimpleModule module = new SimpleModule();
    module.addDeserializer(FacetValue.class, new FacetValueDeserializer());

    environment.getObjectMapper().registerModule(module);
  }

  private void register(Environment environment, String name, HealthCheck healthCheck) {
    environment.healthChecks().register(name, healthCheck);
  }

  private void register(Environment environment, Object component) {
    environment.jersey().register(component);
  }

}
