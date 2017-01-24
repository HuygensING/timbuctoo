package nl.knaw.huygens.timbuctoo.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.dropwizard.activemq.ActiveMQConfig;
import com.kjetland.dropwizard.activemq.ActiveMQConfigHolder;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopConfig;
import nl.knaw.huygens.timbuctoo.handle.PersistenceManagerFactory;
import nl.knaw.huygens.timbuctoo.security.SecurityFactory;
import nl.knaw.huygens.timbuctoo.util.Timeout;
import nl.knaw.huygens.timbuctoo.util.TimeoutFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *TODO: add good default environment for Timbuctoo
 *  - example logins
 *  - example users
 *  - example authorization
 *  - example database
 */
public class TimbuctooConfiguration extends Configuration implements ActiveMQConfigHolder, SearchConfig {
  @JsonProperty
  @Deprecated
  private String loginsFilePath;
  @JsonProperty
  @Deprecated
  private String usersFilePath;
  @JsonProperty
  @Deprecated
  private Path authorizationsPath;

  @JsonProperty
  private SecurityFactory securityConfiguration;

  @JsonProperty
  @Deprecated
  private String databasePath;
  @JsonProperty
  private TinkerPopConfig databaseConfiguration;
  @Deprecated
  @JsonProperty
  private TimeoutFactory autoLogoutTimeout;
  @NotNull
  private TimeoutFactory searchResultAvailabilityTimeout;

  @NotNull
  private UriHelper uriHelper;

  private String timbuctooSearchUrl;

  @JsonProperty
  @NotNull
  @Valid
  private ActiveMQConfig activeMq;
  @JsonProperty
  @NotNull
  private PersistenceManagerFactory persistenceManager;

  @JsonProperty
  private boolean allowGremlinEndpoint = true;

  @JsonProperty
  @Deprecated
  //left in to make the config not break on older timbuctoo instances
  private int executeDatabaseInvariantCheckAt = 24;

  @Valid
  @NotNull
  private HttpClientConfiguration httpClientConfiguration = new HttpClientConfiguration();

  public PersistenceManagerFactory getPersistenceManagerFactory() {
    return persistenceManager;
  }

  @Deprecated
  public Timeout getAutoLogoutTimeout() {
    return autoLogoutTimeout.createTimeout();
  }

  @Deprecated
  public String getLoginsFilePath() {
    return loginsFilePath;
  }

  @Deprecated
  public String getUsersFilePath() {
    return usersFilePath;
  }

  @Deprecated
  public String getDatabasePath() {
    return databasePath;
  }

  public TinkerPopConfig getDatabaseConfiguration() {
    return databaseConfiguration;
  }

  // ActiveMQConfigHolder implementation
  @Override
  public ActiveMQConfig getActiveMQ() {
    return activeMq;
  }

  public Optional<String> getLocalAmqJmxPath(String queueName) {
    if (activeMq != null) {
      if (activeMq.brokerUrl != null) {
        //this only generates a metrics path when the amq brokerurl is a simple vm-local url
        //A path for remote connections makes no sense because then this JVM can't get at the JMX data directly anyway.
        //A path for the advanced url format might make sense, but I don't understand that format or its use.
        Matcher matcher = Pattern.compile("^vm://([^?]*)").matcher(activeMq.brokerUrl);
        if (matcher.find()) {
          String brokerName = matcher.group(1);
          return Optional.of(String.format(
            //That's a pretty querystring! Did you knwo that you can make your own using https://github.com/cjmx/cjmx?
            "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Queue,destinationName=%s",
            brokerName,
            queueName
          ));
        }
      }
    }
    return Optional.empty();
  }

  public UriHelper getUriHelper() {
    return uriHelper;
  }

  @JsonProperty
  public void setBaseUri(String baseUri) {
    if (this.uriHelper == null) {
      this.uriHelper = new UriHelper(URI.create(baseUri));
    } else {
      this.uriHelper.setBaseUri(URI.create(baseUri));
    }
  }

  // SearchConfig implementation
  @Override
  public Timeout getSearchResultAvailabilityTimeout() {
    return searchResultAvailabilityTimeout.createTimeout();
  }

  @Valid
  @Deprecated
  private FederatedAuthConfiguration federatedAuthentication;

  @JsonProperty("federatedAuthentication")
  public FederatedAuthConfiguration getFederatedAuthentication() {
    return federatedAuthentication;
  }

  @JsonProperty("federatedAuthentication")
  public void setFederatedAuthentication(FederatedAuthConfiguration federatedAuthentication) {
    this.federatedAuthentication = federatedAuthentication;
  }

  @Deprecated
  public Path getAuthorizationsPath() {
    return authorizationsPath;
  }

  public boolean isAllowGremlinEndpoint() {
    return allowGremlinEndpoint;
  }

  public String getTimbuctooSearchUrl() {
    return timbuctooSearchUrl;
  }

  @JsonProperty("httpClient")
  public HttpClientConfiguration getHttpClientConfiguration() {
    return httpClientConfiguration;
  }

  @JsonProperty("httpClient")
  public void setHttpClientConfiguration(HttpClientConfiguration httpClientConfiguration) {
    this.httpClientConfiguration = httpClientConfiguration;
  }

  public SecurityFactory getSecurityConfiguration() {
    return securityConfiguration;
  }


}
