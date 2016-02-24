package nl.knaw.huygens.timbuctoo.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.dropwizard.activemq.ActiveMQConfig;
import com.kjetland.dropwizard.activemq.ActiveMQConfigHolder;
import io.dropwizard.Configuration;
import nl.knaw.huygens.timbuctoo.crud.HandleManagerFactory;
import nl.knaw.huygens.timbuctoo.util.Timeout;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

/*
 *TODO: add good default environment for Timbuctoo
 *  - example logins
 *  - example users
 *  - example authorization
 *  - example database
 */
public class TimbuctooConfiguration extends Configuration implements ActiveMQConfigHolder, SearchConfig {
  @NotEmpty
  private String loginsFilePath;
  @NotEmpty
  private String usersFilePath;
  @NotEmpty
  private String databasePath;
  @NotNull
  private TimeoutFactory autoLogoutTimeout;
  @NotNull
  private TimeoutFactory searchResultAvailabilityTimeout;
  @NotNull
  private String baseUri;

  @JsonProperty
  @NotNull
  @Valid
  private ActiveMQConfig activeMq;
  @JsonProperty
  private HandleManagerFactory persistenceManagerFactory = new HandleManagerFactory();

  public HandleManagerFactory getPersistenceManagerFactory() {
    return persistenceManagerFactory;
  }

  public Timeout getAutoLogoutTimeout() {
    return autoLogoutTimeout.createTimeout();
  }

  public String getLoginsFilePath() {
    return loginsFilePath;
  }

  public String getUsersFilePath() {
    return usersFilePath;
  }

  public String getDatabasePath() {
    return databasePath;
  }

  // ActiveMQConfigHolder implementation
  @Override
  public ActiveMQConfig getActiveMQ() {
    return activeMq;
  }

  // SearchConfig implementation
  @Override
  public String getBaseUri() {
    return baseUri;
  }

  @Override
  public Timeout getSearchResultAvailabilityTimeout() {
    return searchResultAvailabilityTimeout.createTimeout();
  }

  // A class to configure timeouts without compromising the Timeout class.
  private class TimeoutFactory {
    private long duration;
    private TimeUnit timeUnit;

    public TimeoutFactory() {
    }

    public void setDuration(long duration) {
      this.duration = duration;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
      this.timeUnit = timeUnit;
    }

    public Timeout createTimeout() {
      return new Timeout(duration, timeUnit);
    }
  }
}
