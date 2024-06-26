package nl.knaw.huygens.timbuctoo.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dropwizard.core.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.logging.common.DefaultLoggingFactory;
import io.dropwizard.logging.common.LoggingFactory;
import io.dropwizard.metrics.common.MetricsFactory;
import io.dropwizard.core.server.DefaultServerFactory;
import io.dropwizard.core.server.ServerFactory;
import nl.knaw.huygens.timbuctoo.webhook.WebhookFactory;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.berkeleydb.BdbPersistentEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.dataset.DataSetConfiguration;
import nl.knaw.huygens.timbuctoo.dataset.dto.Metadata;
import nl.knaw.huygens.timbuctoo.dropwizard.config.SecurityFactoryConfiguration;
import nl.knaw.huygens.timbuctoo.graphql.defaultconfiguration.DefaultSummaryProps;
import nl.knaw.huygens.timbuctoo.redirectionservice.RedirectionServiceFactory;
import nl.knaw.huygens.timbuctoo.util.TimbuctooRdfIdHelper;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@Value.Immutable
@JsonDeserialize(as = ImmutableTimbuctooConfiguration.class)
@JsonSerialize(as = ImmutableTimbuctooConfiguration.class)
public abstract class TimbuctooConfiguration extends Configuration {
  @JsonProperty("rdfUriHelper")
  public abstract TimbuctooRdfIdHelper getRdfIdHelper();

  @Valid
  public abstract SecurityFactoryConfiguration getSecurityConfiguration();

  @Valid
  @Value.Default
  public WebhookFactory getWebhooks() {
    return new WebhookFactory();
  }

  @JsonProperty("httpClient")
  @Valid
  @Value.Default
  public HttpClientConfiguration getHttpClientConfiguration() {
    return new HttpClientConfiguration();
  }

  @Valid
  @JsonProperty("baseUri")
  public abstract UriHelper getUriHelper();

  @Valid
  @Nullable
  @JsonProperty("redirectionService")
  public abstract RedirectionServiceFactory getRedirectionServiceFactory();

  public abstract String getArchetypesSchema();

  public abstract Optional<URI> getUserRedirectUrl();

  @Valid
  public abstract BdbPersistentEnvironmentCreator getDatabases();

  @JsonProperty("dataSet")
  @Valid
  public abstract DataSetConfiguration getDataSetConfiguration();

  @JsonProperty("defaultSummaryProps")
  @Valid
  public abstract DefaultSummaryProps getDefaultSummaryProps();

  @JsonProperty("metadata")
  @Valid
  public abstract Metadata getMetadata();

  @Value.Default
  public boolean dataSetsArePublicByDefault() {
    return false;
  }

  //DROPWIZARD DEFAULT PROPERTIES:
  //Required to make immutables generate json-deserializers for the default properties
  @Override
  @JsonProperty("server")
  @Value.Default
  public ServerFactory getServerFactory() {
    return new DefaultServerFactory();
  }

  @Override
  @JsonProperty("logging")
  @Value.Default
  public LoggingFactory getLoggingFactory() {
    return new DefaultLoggingFactory();
  }

  @Override
  @JsonProperty("metrics")
  @Value.Default
  public MetricsFactory getMetricsFactory() {
    return new MetricsFactory();
  }
}
