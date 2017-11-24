package nl.knaw.huygens.timbuctoo.server;

import com.codahale.metrics.JmxAttributeGauge;
import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.persistence.PersistenceManager;
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
import nl.knaw.huygens.timbuctoo.remote.rs.ResourceSyncService;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.rml.jena.JenaBasedReader;
import nl.knaw.huygens.timbuctoo.search.AutocompleteService;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.security.OldStyleSecurityFactory;
import nl.knaw.huygens.timbuctoo.server.databasemigration.DatabaseMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.FixDcarKeywordDisplayNameMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.MakePidsAbsoluteUrls;
import nl.knaw.huygens.timbuctoo.server.databasemigration.MoveIndicesToIsLatestVertexMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.PrepareForBiaImportMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.RelationTypeRdfUriMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.RemoveSearchResultsMigration;
import nl.knaw.huygens.timbuctoo.server.databasemigration.ScaffoldMigrator;
import nl.knaw.huygens.timbuctoo.server.endpoints.RootEndpoint;
import nl.knaw.huygens.timbuctoo.server.endpoints.legacy.LegacyIndexRedirect;
import nl.knaw.huygens.timbuctoo.server.endpoints.legacy.LegacySingleEntityRedirect;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Authenticate;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Graph;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.Gremlin;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.ImportRdf;
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
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.SingleEntityNTriple;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.remote.rs.Discover;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.remote.rs.Import;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.users.Me;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.users.MyVres;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.vres.ListVres;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.vres.SingleVre;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseValidator;
import nl.knaw.huygens.timbuctoo.server.healthchecks.LambdaHealthCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks.FullTextIndexCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks.InvariantsCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks.LabelsAddedToVertexDatabaseCheck;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.FacetValueDeserializer;
import nl.knaw.huygens.timbuctoo.server.security.LocalUserCreator;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import nl.knaw.huygens.timbuctoo.server.tasks.BdbDumpTask;
import nl.knaw.huygens.timbuctoo.server.tasks.DatabaseValidationTask;
import nl.knaw.huygens.timbuctoo.server.tasks.DbLogCreatorTask;
import nl.knaw.huygens.timbuctoo.server.tasks.UserCreationTask;
import nl.knaw.huygens.timbuctoo.solr.Webhooks;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.DataSetFactoryManager;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes.CsvWriter;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes.GraphVizWriter;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes.JsonLdWriter;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes.JsonWriter;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes.SerializerWriterRegistry;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.CreateDataSet;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.ErrorResponseHelper;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.GraphQl;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.JsonLdEditEndpoint;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.RdfUpload;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.ResourceSyncEndpoint;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.Rml;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.TabularUpload;
import nl.knaw.huygens.timbuctoo.v5.security.twitterexample.TwitterLogin;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.WellKnown;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.RdfWiringFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.DerivedSchemaTypeGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.RootQuery;
import nl.knaw.huygens.timbuctoo.v5.security.SecurityFactory;
import nl.knaw.huygens.timbuctoo.v5.security.twitterexample.TwitterSecurityFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.ServerSocketChannel;
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
    bootstrap.addBundle(new MultiPartBundle());
    bootstrap.addBundle(new AssetsBundle("/static", "/static", "index.html"));
    /*
     * Make it possible to use environment variables in the config.
     * see: http://www.dropwizard.io/0.9.1/docs/manual/core.html#environment-variables
     */
    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor(true)
      ));
  }

  @Override
  public void run(TimbuctooConfiguration configuration, Environment environment) throws Exception {
    // environment.jersey().property(ServerProperties.TRACING, "ALL");
    // // environment.jersey().property(ServerProperties.TRACING_THRESHOLD, "VERBOSE");

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

    HttpClientBuilder apacheHttpClientBuilder = new HttpClientBuilder(environment)
      .using(configuration.getHttpClientConfiguration());
    CloseableHttpClient httpClient = apacheHttpClientBuilder.build("httpclient");
    // Support services
    SecurityFactory securityConfig = configuration.getSecurityConfiguration().createNewSecurityFactory(
      httpClient
    );

    securityConfig.getHealthChecks().forEachRemaining(check -> {
      register(environment, check.getLeft(), new LambdaHealthCheck(check.getRight()));
    });

    // Database migrations
    LinkedHashMap<String, DatabaseMigration> migrations = new LinkedHashMap<>();

    migrations.put("fix-dcarkeywords-displayname-migration", new FixDcarKeywordDisplayNameMigration());
    migrations.put("fix-pids-migration", new MakePidsAbsoluteUrls());

    UriHelper uriHelper = configuration.getUriHelper();
    environment.lifecycle().addServerLifecycleListener(new BaseUriDeriver(configuration));

    TinkerPopConfig tinkerPopConfig = configuration.getDatabaseConfiguration();
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
      securityConfig.getPermissionFetcher(),
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
    migrations.put("remove-search-results", new RemoveSearchResultsMigration());
    migrations.put("move-indices-to-isLatest-vertex", new MoveIndicesToIsLatestVertexMigration(vres));

    final ResourceSyncService resourceSyncService = new ResourceSyncService(httpClient, new ResourceSyncContext());
    final JsonMetadata jsonMetadata = new JsonMetadata(vres, graphManager);

    final AutocompleteService.AutocompleteServiceFactory autocompleteServiceFactory =
      new AutocompleteService.AutocompleteServiceFactory(
        uriWithoutRev
      );

    environment.lifecycle().manage(graphManager);

    final CrudServiceFactory crudServiceFactory = new CrudServiceFactory(
      vres,
      securityConfig.getUserValidator(),
      pathWithoutVersionAndRevision
    );

    final Webhooks webhooks = configuration.getWebhooks().getWebHook(environment);
    DataSetRepository dataSetRepository = configuration.getDataSetConfiguration().createRepository(
      environment.lifecycle().executorService("dataSet").build(),
      securityConfig.getPermissionFetcher(),
      configuration.getDatabases(),
      configuration.getRdfIdHelper(),
      (combinedId -> {
        try {
          webhooks.dataSetUpdated(combinedId);
        } catch (IOException e) {
          LOG.error("Webhook call failed", e);
        }
      })
    );

    environment.lifecycle().manage(new DataSetFactoryManager(dataSetRepository));

    ErrorResponseHelper errorResponseHelper = new ErrorResponseHelper();
    AuthCheck authCheck = new AuthCheck(
      securityConfig.getUserValidator(),
      securityConfig.getPermissionFetcher(),
      dataSetRepository
    );

    register(environment, new RdfUpload(authCheck));

    register(environment, new TabularUpload(
      authCheck,
      dataSetRepository,
      errorResponseHelper
    ));

    register(environment, new Rml(
      dataSetRepository,
      errorResponseHelper
    ));

    register(environment, new Rml(
      dataSetRepository,
      errorResponseHelper
    ));

    SerializerWriterRegistry serializerWriterRegistry = new SerializerWriterRegistry(
      new CsvWriter(),
      new JsonLdWriter(),
      new JsonWriter(),
      new GraphVizWriter()
    );

    final PaginationArgumentsHelper argHelper = new PaginationArgumentsHelper(configuration.getCollectionFilters());
    final GraphQl graphQlEndpoint = new GraphQl(
      new RootQuery(
        dataSetRepository,
        serializerWriterRegistry,
        configuration.getArchetypesSchema(),
        new RdfWiringFactory(dataSetRepository, argHelper),
        new DerivedSchemaTypeGenerator(argHelper),
        environment.getObjectMapper()
      ),
      serializerWriterRegistry,
      securityConfig.getUserValidator(),
      uriHelper
    );
    register(environment, graphQlEndpoint);

    if (securityConfig instanceof TwitterSecurityFactory) {
      final TwitterLogin twitterLogin = new TwitterLogin();
      register(environment, twitterLogin);
    }

    register(environment, new CreateDataSet(authCheck));
    register(environment, new DataSet(
        securityConfig.getUserValidator(),
        securityConfig.getPermissionFetcher(), dataSetRepository
      )
    );
    register(environment, new JsonLdEditEndpoint(
      securityConfig.getUserValidator(),
      securityConfig.getPermissionFetcher(),
      dataSetRepository,
      new HttpClientBuilder(environment).build("json-ld")
    ));
    register(environment, new RootEndpoint(uriHelper, configuration.getUserRedirectUrl()));
    if (securityConfig instanceof OldStyleSecurityFactory) {
      register(environment, new Authenticate(((OldStyleSecurityFactory) securityConfig).getLoggedInUsers()));
    }
    register(environment, new Me(securityConfig.getUserValidator()));
    register(environment, new Search(configuration, uriHelper, graphManager));
    register(environment, new Autocomplete(autocompleteServiceFactory, transactionEnforcer));
    register(
      environment,
      new Index(securityConfig.getUserValidator(), crudServiceFactory, transactionEnforcer)
    );
    register(
      environment,
      new SingleEntity(securityConfig.getUserValidator(), crudServiceFactory, transactionEnforcer)
    );
    register(environment, new SingleEntityNTriple(transactionEnforcer, uriHelper));
    register(environment, new WomenWritersEntityGet(crudServiceFactory, transactionEnforcer));
    register(environment, new LegacySingleEntityRedirect(uriHelper));
    register(environment, new LegacyIndexRedirect(uriHelper));
    register(environment, new Discover(resourceSyncService));

    if (configuration.isAllowGremlinEndpoint()) {
      register(environment, new Gremlin(graphManager));
    }
    register(environment, new Graph(graphManager, vres));
    // Bulk upload
    UserPermissionChecker permissionChecker = new UserPermissionChecker(
      securityConfig.getUserValidator(),
      securityConfig.getPermissionFetcher()
    );
    RawCollection rawCollection = new RawCollection(graphManager, uriHelper, permissionChecker, errorResponseHelper);
    register(environment, rawCollection);
    ExecuteRml executeRml = new ExecuteRml(uriHelper, graphManager, vres, new JenaBasedReader(), permissionChecker,
      new DataSourceFactory(graphManager), transactionEnforcer,
      webhooks
    );
    register(environment, executeRml);
    SaveRml saveRml = new SaveRml(uriHelper, permissionChecker, transactionEnforcer);
    register(environment, saveRml);

    BulkUploadVre bulkUploadVre = new BulkUploadVre(graphManager, uriHelper, rawCollection, executeRml,
      permissionChecker, saveRml, transactionEnforcer, 2 * 1024 * 1024
    );
    register(environment, bulkUploadVre);
    if (securityConfig instanceof OldStyleSecurityFactory) {
      final OldStyleSecurityFactory oldStyleSecurityFactory = (OldStyleSecurityFactory) securityConfig;
      register(environment, new BulkUpload(new BulkUploadService(vres, graphManager, 25_000), bulkUploadVre,
        securityConfig.getUserValidator(), oldStyleSecurityFactory.getVreAuthorizationCreator(), 20 * 1024 * 1024,
        permissionChecker, transactionEnforcer, 50
      ));
    }

    register(environment, new RelationTypes(graphManager));
    register(environment, new Metadata());
    register(environment, new nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.vres.Metadata(jsonMetadata));
    register(environment, new MyVres(
        securityConfig.getUserValidator(),
        securityConfig.getPermissionFetcher(),
        bulkUploadVre,
        transactionEnforcer,
        uriHelper
      )
    );
    register(environment, new SingleVre(permissionChecker, transactionEnforcer,
      securityConfig.getPermissionFetcher()
    ));
    register(environment, new ListVres(uriHelper, transactionEnforcer));
    register(environment, new VreImage(transactionEnforcer));

    final ExecutorService rfdExecutorService = environment.lifecycle().executorService("rdf-import").build();
    register(environment, new ImportRdf(graphManager, vres, rfdExecutorService, transactionEnforcer));
    register(environment, new Import(
      new ResourceSyncFileLoader(httpClient),
      authCheck
    ));

    register(environment, new WellKnown());
    register(environment, new ResourceSyncEndpoint(configuration.getResourceSync(), configuration.getUriHelper()));

    // Admin resources
    if (securityConfig instanceof OldStyleSecurityFactory) {
      final OldStyleSecurityFactory oldStyleSecurityFactory = (OldStyleSecurityFactory) securityConfig;
      environment.admin().addTask(new UserCreationTask(new LocalUserCreator(
        oldStyleSecurityFactory.getLoginCreator(),
        oldStyleSecurityFactory.getUserCreator(),
        oldStyleSecurityFactory.getVreAuthorizationCreator()
      )));
    }

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
    environment.admin().addTask(new BdbDumpTask(configuration.getDatabases()));

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

  private class BaseUriDeriver implements ServerLifecycleListener {
    private final TimbuctooConfiguration timbuctooConfiguration;

    public BaseUriDeriver(TimbuctooConfiguration timbuctooConfiguration) {
      this.timbuctooConfiguration = timbuctooConfiguration;
    }

    @Override
    public void serverStarted(Server server) {
      // Detect the port Jetty is listening on - works with configured and random ports
      for (final Connector connector : server.getConnectors()) {
        try {
          if ("application".equals(connector.getName())) {
            final ServerSocketChannel channel = (ServerSocketChannel) connector
              .getTransport();
            final InetSocketAddress socket = (InetSocketAddress) channel
              .getLocalAddress();

            timbuctooConfiguration.getUriHelper().notifyOfPort(socket.getPort());
          }
        } catch (Exception e) {
          LOG.error("No base url provided, and unable to get generate one myself", e);
        }
      }
    }
  }
}
