package nl.knaw.huygens.timbuctoo.server;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.huygens.timbuctoo.util.EvilEnvironmentVariableHacker;
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
  static {
    EvilEnvironmentVariableHacker.setEnv(
      "http://localhost",
      "9200",
      "elastic",
      "changeme",
      "http://127.0.0.1:0",
      resourceFilePath("testrunstate"),
      resourceFilePath("testrunstate"),
      "0",
      "0"
    );
  }

  public static final DropwizardAppExtension<TimbuctooConfiguration> APP = new DropwizardAppExtension<>(
    TimbuctooV4.class,
    "example_config.yaml",
    ConfigOverride.config(
        "collectionFilters.elasticsearch.@class",
        "nl.knaw.huygens.timbuctoo.server.TestCollectionFilter"
    )
  );

  @Test
  public void dropwizardLaunches() throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(String.format("http://localhost:%d/healthcheck", APP.getAdminPort()));

    Response response = target.request().get();

    assertThat(response.getStatus()).isEqualTo(200);
  }
}
