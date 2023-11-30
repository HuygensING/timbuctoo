package nl.knaw.huygens.timbuctoo.server;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.core.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import nl.knaw.huygens.timbuctoo.logging.LoggingFilter;
import nl.knaw.huygens.timbuctoo.remote.rs.ResourceSyncService;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.remote.rs.Discover;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.remote.rs.Import;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.users.Me;
import nl.knaw.huygens.timbuctoo.server.healthchecks.LambdaHealthCheck;
import nl.knaw.huygens.timbuctoo.server.tasks.BackupTask;
import nl.knaw.huygens.timbuctoo.server.tasks.BdbDumpTask;
import nl.knaw.huygens.timbuctoo.server.tasks.MoveDefaultGraphsTask;
import nl.knaw.huygens.timbuctoo.server.tasks.RebuildSchemaTask;
import nl.knaw.huygens.timbuctoo.server.tasks.ReimportDatasetsTask;
import nl.knaw.huygens.timbuctoo.webhook.Webhooks;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.datastores.rssource.RsDocumentBuilder;
import nl.knaw.huygens.timbuctoo.dropwizard.DataSetRepositoryManager;
import nl.knaw.huygens.timbuctoo.dropwizard.contenttypes.CsvWriter;
import nl.knaw.huygens.timbuctoo.dropwizard.contenttypes.GraphVizWriter;
import nl.knaw.huygens.timbuctoo.dropwizard.contenttypes.JsonLdWriter;
import nl.knaw.huygens.timbuctoo.dropwizard.contenttypes.JsonWriter;
import nl.knaw.huygens.timbuctoo.dropwizard.contenttypes.SerializerWriterRegistry;
import nl.knaw.huygens.timbuctoo.dropwizard.endpoints.GetEntity;
import nl.knaw.huygens.timbuctoo.dropwizard.endpoints.GetEntityInGraph;
import nl.knaw.huygens.timbuctoo.dropwizard.endpoints.GraphQl;
import nl.knaw.huygens.timbuctoo.dropwizard.endpoints.RdfUpload;
import nl.knaw.huygens.timbuctoo.dropwizard.endpoints.RsEndpoint;
import nl.knaw.huygens.timbuctoo.dropwizard.endpoints.WellKnown;
import nl.knaw.huygens.timbuctoo.dropwizard.endpoints.auth.AuthCheck;
import nl.knaw.huygens.timbuctoo.dropwizard.healthchecks.CollectionFilterCheck;
import nl.knaw.huygens.timbuctoo.dropwizard.healthchecks.DatabaseAvailabilityCheck;
import nl.knaw.huygens.timbuctoo.dropwizard.tasks.ReloadDataSet;
import nl.knaw.huygens.timbuctoo.dropwizard.tasks.StopBdbDataStore;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.RdfWiringFactory;
import nl.knaw.huygens.timbuctoo.graphql.derivedschema.DerivedSchemaGenerator;
import nl.knaw.huygens.timbuctoo.graphql.rootquery.RootQuery;
import nl.knaw.huygens.timbuctoo.redirectionservice.RedirectionService;
import nl.knaw.huygens.timbuctoo.redirectionservice.RedirectionServiceFactory;
import nl.knaw.huygens.timbuctoo.security.SecurityFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Properties;

public class TimbuctooV4 extends Application<TimbuctooConfiguration> {
  private static final Logger LOG = LoggerFactory.getLogger(TimbuctooV4.class);

  public static void main(String[] args) throws Exception {
    new TimbuctooV4().run(args);
  }

  @Override
  public void initialize(Bootstrap<TimbuctooConfiguration> bootstrap) {
    //bundles
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

    // Support services
    SecurityFactory securityConfig = configuration.getSecurityConfiguration().createNewSecurityFactory();
    securityConfig.getHealthChecks().forEachRemaining(check ->
            register(environment, check.getLeft(), new LambdaHealthCheck(check.getRight())));

    configuration.getCollectionFilters().forEach((key, value) ->
            register(environment, key + "Check", new CollectionFilterCheck(value)));

    environment.lifecycle().addServerLifecycleListener(new BaseUriDeriver(configuration));

    final Webhooks webhooks = configuration.getWebhooks().getWebHook(environment);

    final int numThreads = Math.max(Runtime.getRuntime().availableProcessors() - 2, 2);
    DataSetRepository dataSetRepository = configuration.getDataSetConfiguration().createRepository(
      environment.lifecycle().executorService("dataSet").minThreads(numThreads).maxThreads(numThreads).build(),
      securityConfig.getPermissionFetcher(),
      configuration.getDatabases(),
      configuration.getMetadata(),
      configuration.getRdfIdHelper(),
      (combinedId -> {
        try {
          webhooks.dataSetUpdated(combinedId);
        } catch (IOException e) {
          LOG.error("Webhook call failed", e);
        }
      }),
      configuration.dataSetsArePublicByDefault()
    );

    environment.lifecycle().manage(new DataSetRepositoryManager(dataSetRepository));
    register(environment, "dataStoreAvailabilityCheck", new DatabaseAvailabilityCheck(dataSetRepository));

    RedirectionServiceFactory redirectionServiceFactory = configuration.getRedirectionServiceFactory();
    RedirectionService redirectionService = redirectionServiceFactory != null ?
        redirectionServiceFactory.makeRedirectionService(dataSetRepository) : null;

    HttpClientBuilder apacheHttpClientBuilder = new HttpClientBuilder(environment)
        .using(configuration.getHttpClientConfiguration());
    CloseableHttpClient httpClient = apacheHttpClientBuilder.build("httpclient");

    final ResourceSyncService resourceSyncService = new ResourceSyncService(httpClient, new ResourceSyncContext());

    AuthCheck authCheck = new AuthCheck(
      securityConfig.getUserValidator(),
      securityConfig.getPermissionFetcher(),
      dataSetRepository
    );

    register(environment, new RdfUpload(authCheck));

    SerializerWriterRegistry serializerWriterRegistry = new SerializerWriterRegistry(
      new CsvWriter(),
      new JsonLdWriter(),
      new JsonWriter(),
      new GraphVizWriter()
    );

    final UriHelper uriHelper = configuration.getUriHelper();
    final PaginationArgumentsHelper argHelper = new PaginationArgumentsHelper(configuration.getCollectionFilters());
    final GraphQl graphQlEndpoint = new GraphQl(
      new RootQuery(
        dataSetRepository,
        serializerWriterRegistry,
        configuration.getArchetypesSchema(),
        configuration.getMetadata(),
        (schemaUpdater) -> new RdfWiringFactory(
          dataSetRepository,
          argHelper,
          configuration.getDefaultSummaryProps(),
          uriHelper,
          redirectionService,
          schemaUpdater
        ),
        new DerivedSchemaGenerator(argHelper),
        environment.getObjectMapper(),
        new ResourceSyncFileLoader(httpClient),
        resourceSyncService,
        environment.lifecycle().executorService("GraphQLSchemaUpdate").maxThreads(1).build()
      ),
      serializerWriterRegistry,
      securityConfig.getUserValidator(),
      uriHelper,
      securityConfig.getPermissionFetcher(), dataSetRepository
    );
    register(environment, graphQlEndpoint);

    securityConfig.register(component -> register(environment, component));

    register(environment, new Me(securityConfig.getUserValidator()));
    register(environment, new Discover(resourceSyncService));

    register(environment, new Import(
      new ResourceSyncFileLoader(httpClient),
      authCheck
    ));

    register(environment, new WellKnown());
    RsDocumentBuilder rsDocumentBuilder =
      new RsDocumentBuilder(dataSetRepository, configuration.getUriHelper());
    register(environment, new RsEndpoint(rsDocumentBuilder, securityConfig.getUserValidator()));

    GetEntity getEntity = new GetEntity(dataSetRepository, securityConfig.getUserValidator());
    register(environment, getEntity);

    GetEntityInGraph getEntityInGraph = new GetEntityInGraph(dataSetRepository, securityConfig.getUserValidator());
    register(environment, getEntityInGraph);

    // Admin resources
    environment.admin().addTask(new ReloadDataSet(dataSetRepository));
    environment.admin().addTask(new BdbDumpTask(configuration.getDatabases()));
    environment.admin().addTask(new StopBdbDataStore(configuration.getDatabases()));
    environment.admin().addTask(new BackupTask(dataSetRepository));
    environment.admin().addTask(new MoveDefaultGraphsTask(dataSetRepository));
    environment.admin().addTask(new ReimportDatasetsTask(dataSetRepository));
    environment.admin().addTask(new RebuildSchemaTask(dataSetRepository));

    //Log all http requests
    register(environment, new LoggingFilter(1024, currentVersion));
    //Allow all CORS requests
    register(environment, new PromiscuousCorsFilter());
  }

  private void register(Environment environment, String name, HealthCheck healthCheck) {
    environment.healthChecks().register(name, healthCheck);
  }

  private void register(Environment environment, Object component) {
    environment.jersey().register(component);
  }

  private static class BaseUriDeriver implements ServerLifecycleListener {
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
