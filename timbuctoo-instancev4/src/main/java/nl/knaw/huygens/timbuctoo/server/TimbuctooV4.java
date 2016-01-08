package nl.knaw.huygens.timbuctoo.server;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.timbuctoo.server.rest.AuthenticationV2_1EndPoint;
import nl.knaw.huygens.timbuctoo.server.rest.JsonBasedAuthenticator;
import nl.knaw.huygens.timbuctoo.server.rest.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.server.rest.Timeout;
import nl.knaw.huygens.timbuctoo.server.rest.UserV2_1Endpoint;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.concurrent.TimeUnit.HOURS;

public class TimbuctooV4 extends Application<TimbuctooConfiguration> {

  public static final String ENCRYPTION_ALGORITHM = "SHA-256";

  public static void main(String[] args) throws Exception {
    new TimbuctooV4().run(args);
  }

  @Override
  public void run(TimbuctooConfiguration configuration, Environment environment) throws Exception {
    Path loginsPath = getLoginsPath();

    JsonBasedAuthenticator authenticator = new JsonBasedAuthenticator(loginsPath, ENCRYPTION_ALGORITHM);
    LoggedInUserStore loggedInUserStore = new LoggedInUserStore(authenticator, null, new Timeout(8, HOURS));
    environment.jersey().register(new AuthenticationV2_1EndPoint(loggedInUserStore));
    environment.jersey().register(new UserV2_1Endpoint(loggedInUserStore));

    // register health checks
    registerHealthCheck(environment, "Encryption algorithm", new EncryptionAlgorithmHealthCheck(ENCRYPTION_ALGORITHM));
    registerHealthCheck(environment, "Local logins", new FileHealthCheck(loginsPath));
  }

  private Path getLoginsPath() {
    String userHome = System.getProperty("user.home");
    return Paths.get(userHome, "repository", "data", "logins.json");
  }

  private void registerHealthCheck(Environment environment, String name, HealthCheck healthCheck) {
    environment.healthChecks().register(name, healthCheck);
  }
}
