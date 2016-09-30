package nl.knaw.huygens.timbuctoo.server;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardLaunchesTest {

  @ClassRule
  public static final DropwizardAppRule<TimbuctooConfiguration> RULE = new DropwizardAppRule<>(
      TimbuctooV4.class,
      ResourceHelpers.resourceFilePath("testrunstate/config.yaml"),
      ConfigOverride.config("databasePath", ResourceHelpers.resourceFilePath("testrunstate/database")),
      ConfigOverride.config("authorizationsPath", ResourceHelpers.resourceFilePath("testrunstate/authorizations")),
      ConfigOverride.config("usersFilePath", ResourceHelpers.resourceFilePath("testrunstate/users.json")),
      ConfigOverride.config("loginsFilePath", ResourceHelpers.resourceFilePath("testrunstate/logins.json"))
    );


  @Test
  public void dropwizardLaunches() throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(String.format("http://localhost:%d/healthcheck", RULE.getAdminPort()));

    Response response = target.request().get();

    assertThat(response.getStatus()).isEqualTo(200);
  }
}
