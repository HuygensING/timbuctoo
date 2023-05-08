package nl.knaw.huygens.util;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import nl.knaw.huygens.timbuctoo.server.TimbuctooV4;
import nl.knaw.huygens.timbuctoo.util.EvilEnvironmentVariableHacker;

public class DropwizardMaker {
  static {
    EvilEnvironmentVariableHacker.setEnv(
      "http://localhost",
      "9200",
      "elastic",
      "changeme",
      "http://127.0.0.1:0",
      "src/spec/resources/specrunstate",
      "src/spec/resources/specrunstate",
      "8089",
      "8088"
    );
  }

  public static DropwizardAppExtension<TimbuctooConfiguration> makeTimbuctoo() {
    return new DropwizardAppExtension<>(
      TimbuctooV4.class,
      "example_config.yaml"
    );
  }
}
