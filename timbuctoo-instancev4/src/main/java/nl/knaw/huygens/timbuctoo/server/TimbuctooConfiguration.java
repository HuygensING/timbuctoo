package nl.knaw.huygens.timbuctoo.server;


import io.dropwizard.Configuration;
import nl.knaw.huygens.timbuctoo.util.Timeout;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

/*
 *TODO: add good default environment for Timbuctoo
 *  - example logins
 *  - example users
 *  - example authorization
 *  - example database
 */
public class TimbuctooConfiguration extends Configuration {
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
