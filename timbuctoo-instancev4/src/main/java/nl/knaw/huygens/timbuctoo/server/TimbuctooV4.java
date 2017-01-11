package nl.knaw.huygens.timbuctoo.server;

import com.codahale.metrics.JmxAttributeGauge;
import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
import nl.knaw.huygens.timbuctoo.bulkupload.BulkUploadService;
import nl.knaw.huygens.timbuctoo.core.TimbuctooActions;
import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.crud.CrudServiceFactory;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopConfig;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopOperations;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TransactionFilter;
import nl.knaw.huygens.timbuctoo.experimental.womenwriters.WomenWritersEntityGet;
import nl.knaw.huygens.timbuctoo.handle.HandleAdder;
import nl.knaw.huygens.timbuctoo.logging.LoggingFilter;
import nl.knaw.huygens.timbuctoo.model.properties.JsonMetadata;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.DatabaseConfiguredVres;
import nl.knaw.huygens.timbuctoo.rml.jena.JenaBasedReader;
import nl.knaw.huygens.timbuctoo.search.AutocompleteService;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.security.SecurityFactory;
import nl.knaw.huygens.timbuctoo.security.dataaccess.localfile.LocalfileAccessFactory;
import nl.knaw.huygens.timbuctoo.server.databasemigration.DatabaseMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.FixDcarKeywordDisplayNameMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.IndexAllEntityIds;
import nl.knaw.huygens.timbuctoo.server.databasemigration.IndexAllTheDisplaynames;
import nl.knaw.huygens.timbuctoo.server.databasemigration.IndexRelationsById;
import nl.knaw.huygens.timbuctoo.server.databasemigration.MakePidsAbsoluteUrls;
import nl.knaw.huygens.timbuctoo.server.databasemigration.PrepareForBiaImportMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.RelationTypeRdfUriMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.ScaffoldMigrator;
import nl.knaw.huygens.timbuctoo.server.endpoints.RootEndpoint;
import nl.knaw.huygens.timbuctoo.server.endpoints.legacy.LegacyIndexRedirect;
import nl.knaw.huygens.timbuctoo.server.endpoints.legacy.LegacySingleEntityRedirect;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Authenticate;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Graph;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Gremlin;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.ImportRdf;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.JsEnv;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Metadata;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.RelationTypes;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Search;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.VreImage;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.BulkUpload;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.BulkUploadVre;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.DataSourceFactory;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.ExecuteRml;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.RawCollection;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.Autocomplete;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.Index;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.SingleEntity;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.users.Me;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.users.MyVres;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.vres.ListVres;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.vres.SingleVre;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseValidator;
import nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks.FullTextIndexCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks.InvariantsCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks.LabelsAddedToVertexDatabaseCheck;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.FacetValueDeserializer;
import nl.knaw.huygens.timbuctoo.server.security.LocalUserCreator;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import nl.knaw.huygens.timbuctoo.server.tasks.DatabaseValidationTask;
import nl.knaw.huygens.timbuctoo.server.tasks.DbLogCreatorTask;
import nl.knaw.huygens.timbuctoo.server.tasks.UserCreationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.io.InputStream;
import java.net.URI;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static com.codahale.metrics.MetricRegistry.name;
import static nl.knaw.huygens.timbuctoo.handle.HandleAdder.HANDLE_QUEUE;
import static nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.rethrowConsumer;

public class TimbuctooV4 extends Application<TimbuctooConfiguration> {

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
    InputStream gitproperties = getClass().getClassLoader().getResourceAsStream("git.properties");
    String currentVersion;
    if (gitproperties != null) {
      properties.load(gitproperties);
      currentVersion = properties.getProperty("git.commit.id");
    } else {
      currentVersion = "NO-GIT-PROPERTIES-FOUND";
      LoggerFactory.getLogger(this.getClass()).error("NO-GIT-PROPERTIES-FOUND");
    }

    LoggerFactory.getLogger(this.getClass()).info("Now launching timbuctoo version: " + currentVersion);

    // Support services
    SecurityFactory securityConfig;
    if (configuration.getSecurityConfiguration() == null) {
      securityConfig = new SecurityFactory();
      //map old style to new style. Needed until timbuctoo is migrated away from rpm based releases and we can more
      //easily update the configuration
      securityConfig.setLocalfileAccessFactory(new LocalfileAccessFactory(
        configuration.getAuthorizationsPath().toAbsolutePath().toString(),
        configuration.getLoginsFilePath(),
        configuration.getUsersFilePath()
      ));
      securityConfig.setAutoLogoutTimeout(configuration.getAutoLogoutTimeout());
      securityConfig.setAuthHandler(configuration.getFederatedAuthentication().makeHandler(environment));
    } else {
      securityConfig = configuration.getSecurityConfiguration();
    }

    securityConfig.getHealthChecks().forEachRemaining(check -> {
      register(environment, check.getLeft(), check.getRight());
    });

    // Database migrations
    LinkedHashMap<String, DatabaseMigration> migrations = new LinkedHashMap<>();

    migrations.put("fix-dcarkeywords-displayname-migration", new FixDcarKeywordDisplayNameMigration());
    migrations.put("fix-pids-migration", new MakePidsAbsoluteUrls());
    migrations.put("index-all-displaynames", new IndexAllTheDisplaynames());
    migrations.put("index-all-entity-ids", new IndexAllEntityIds());

    final UriHelper uriHelper = new UriHelper(configuration.getBaseUri());

    TinkerPopConfig tinkerPopConfig = configuration.getDatabaseConfiguration();
    if (tinkerPopConfig == null) {
      tinkerPopConfig = new TinkerPopConfig();
      tinkerPopConfig.setDatabasePath(configuration.getDatabasePath());
    }
    final TinkerPopGraphManager graphManager = new TinkerPopGraphManager(tinkerPopConfig, migrations);
    final PersistenceManager persistenceManager = configuration.getPersistenceManagerFactory().build();
    UrlGenerator uriToRedirectToFromPersistentUrls = (coll, id, rev) ->
      uriHelper.fromResourceUri(SingleEntity.makeUrl(coll, id, rev));

    final UrlGenerator pathWithoutVersionAndRevision =
      (coll, id, rev) -> URI.create(SingleEntity.makeUrl(coll, id, null).toString().replaceFirst("^/v2.1/", ""));
    final UrlGenerator uriWithoutRev = (coll, id, rev) ->
      uriHelper.fromResourceUri(SingleEntity.makeUrl(coll, id, null));

    HandleAdder handleAdder = new HandleAdder(persistenceManager, activeMqBundle);

    // TODO make function when TimbuctooActions does not depend on TransactionEnforcer anymore
    TimbuctooActions.TimbuctooActionsFactory timbuctooActionsFactory = new TimbuctooActions.TimbuctooActionsFactoryImpl(
      securityConfig.getAuthorizer(),
      Clock.systemDefaultZone(),
      handleAdder,
      uriToRedirectToFromPersistentUrls,
      () -> new TinkerPopOperations(graphManager)
    );
    TransactionEnforcer transactionEnforcer = new TransactionEnforcer(
      timbuctooActionsFactory
    );
    graphManager.onGraph(g -> new ScaffoldMigrator(graphManager).execute());
    handleAdder.init(transactionEnforcer);

    final Vres vres = new DatabaseConfiguredVres(transactionEnforcer);
    migrations.put("prepare-for-bia-import-migration", new PrepareForBiaImportMigration(vres, graphManager));
    migrations.put("give-existing-relationtypes-rdf-uris", new RelationTypeRdfUriMigration());
    migrations.put("index-relations-by-id", new IndexRelationsById());

    final JsonMetadata jsonMetadata = new JsonMetadata(vres, graphManager);

    final AutocompleteService.AutocompleteServiceFactory autocompleteServiceFactory =
      new AutocompleteService.AutocompleteServiceFactory(
        uriWithoutRev
      );

    environment.lifecycle().manage(graphManager);

    final CrudServiceFactory crudServiceFactory = new CrudServiceFactory(
      vres,
      securityConfig.getUserStore(),
      pathWithoutVersionAndRevision
    );

    // register REST endpoints
    register(environment, new RootEndpoint());
    register(environment, new JsEnv(configuration));
    register(environment, new Authenticate(securityConfig.getLoggedInUsers(environment)));
    register(environment, new Me(securityConfig.getLoggedInUsers(environment)));
    register(environment, new Search(configuration, graphManager));
    register(environment, new Autocomplete(autocompleteServiceFactory, transactionEnforcer));
    register(environment,
      new Index(securityConfig.getLoggedInUsers(environment), crudServiceFactory, transactionEnforcer));
    register(environment,
      new SingleEntity(securityConfig.getLoggedInUsers(environment), crudServiceFactory, transactionEnforcer));
    register(environment, new WomenWritersEntityGet(crudServiceFactory, transactionEnforcer));
    register(environment, new LegacySingleEntityRedirect(uriHelper));
    register(environment, new LegacyIndexRedirect(uriHelper));

    if (configuration.isAllowGremlinEndpoint()) {
      register(environment, new Gremlin(graphManager));
    }
    register(environment, new Graph(graphManager, vres));
    // Bulk upload
    UserPermissionChecker permissionChecker = new UserPermissionChecker(
      securityConfig.getLoggedInUsers(environment),
      securityConfig.getAuthorizer()
    );
    RawCollection rawCollection = new RawCollection(graphManager, uriHelper, permissionChecker);
    register(environment, rawCollection);
    ExecuteRml executeRml = new ExecuteRml(uriHelper, graphManager, vres, new JenaBasedReader(), permissionChecker,
      new DataSourceFactory(graphManager), transactionEnforcer);
    register(environment, executeRml);
    SaveRml saveRml = new SaveRml(uriHelper, permissionChecker, transactionEnforcer);
    register(environment, saveRml);

    BulkUploadVre bulkUploadVre = new BulkUploadVre(graphManager, uriHelper, rawCollection, executeRml,
      permissionChecker, saveRml, transactionEnforcer, 2 * 1024 * 1024);
    register(environment, bulkUploadVre);
    register(environment, new BulkUpload(new BulkUploadService(vres, graphManager, 25_000), bulkUploadVre,
      securityConfig.getLoggedInUsers(environment), securityConfig.getVreAuthorizationCreator(), 20 * 1024 * 1024,
      permissionChecker, transactionEnforcer));

    register(environment, new RelationTypes(graphManager));
    register(environment, new Metadata());
    register(environment, new nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.vres.Metadata(jsonMetadata));
    register(environment, new MyVres(
      securityConfig.getLoggedInUsers(environment),
      securityConfig.getAuthorizer(),
      bulkUploadVre,
      transactionEnforcer,
      uriHelper)
    );
    register(environment, new SingleVre(permissionChecker, transactionEnforcer,
      securityConfig.getVreAuthorizationCreator()));
    register(environment, new ListVres(uriHelper, transactionEnforcer));
    register(environment, new VreImage(transactionEnforcer));

    final ExecutorService rfdExecutorService = environment.lifecycle().executorService("rdf-import").build();
    register(environment, new ImportRdf(graphManager, vres, rfdExecutorService, transactionEnforcer));

    // Admin resources
    environment.admin().addTask(new UserCreationTask(new LocalUserCreator(
      securityConfig.getLoginCreator(),
      securityConfig.getUserCreator(),
      securityConfig.getVreAuthorizationCreator()
    )));

    environment.admin().addTask(
      new DatabaseValidationTask(
        new DatabaseValidator(
          graphManager,
          new LabelsAddedToVertexDatabaseCheck(),
          new InvariantsCheck(vres),
          new FullTextIndexCheck()
        ),
        Clock.systemUTC(),
        5000
      )
    );
    environment.admin().addTask(new DbLogCreatorTask(graphManager));

    // register health checks
    // Dropwizard Health checks are used to check whether requests should be routed to this instance
    // For example, checking if neo4j is in a valid state is not a "HealthCheck" because if the database on one instance
    // is in an invalid state, then this applies to all other instances too. So once the database is in an invalid state
    // timbuctoo will be down.
    //
    // checking whether this instance is part of the neo4j quorum is a good HealthCheck because running a database query
    // on those instances that are not part of the quorum will block forever, while the other instances will respond
    // just fine.
    register(environment, "Neo4j database connection", graphManager);

    //Log all http requests
    register(environment, new LoggingFilter(1024, currentVersion));
    register(environment, new TransactionFilter(graphManager));
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
