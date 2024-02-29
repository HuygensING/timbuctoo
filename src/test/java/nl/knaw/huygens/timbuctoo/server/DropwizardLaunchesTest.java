package nl.knaw.huygens.timbuctoo.server;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DropwizardLaunchesTest {
  public static final DropwizardAppExtension<TimbuctooConfiguration> APP = new DropwizardAppExtension<>(
    Timbuctoo.class,
    resourceFilePath("test_config.yaml"),
    ConfigOverride.config("securityConfiguration.accessFactory.authorizationsPath", resourceFilePath("testrunstate") + "/datasets"),
    ConfigOverride.config("securityConfiguration.accessFactory.permissionConfig", resourceFilePath("testrunstate/permissionConfig.json")),
    ConfigOverride.config("securityConfiguration.accessFactory.usersFilePath", resourceFilePath("testrunstate/users.json")),
    ConfigOverride.config("databases.databaseLocation", resourceFilePath("testrunstate") + "/datasets"),
    ConfigOverride.config("dataSet.dataStorage.rootDir", resourceFilePath("testrunstate") + "/datasets")
  );

  @Test
  public void dropwizardLaunches() throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(String.format("http://localhost:%d/healthcheck", APP.getAdminPort()));

    Response response = target.request().get();

    assertThat(response.getStatus()).isEqualTo(200);
  }
}
