package nl.knaw.huygens.util;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import nl.knaw.huygens.timbuctoo.server.TimbuctooV4;
import nl.knaw.huygens.timbuctoo.util.EvilEnvironmentVariableHacker;

public class DropwizardMaker {
  static {
    EvilEnvironmentVariableHacker.setEnv(ImmutableMap.of(
      "timbuctoo_dataPath", "src/spec/resources/specrunstate",
      "timbuctoo_port", "8089",
      "timbuctoo_adminPort", "8088"
    ));
  }

  public static DropwizardAppRule<TimbuctooConfiguration> makeTimbuctoo() {
    return new DropwizardAppRule<>(
      TimbuctooV4.class,
      "example_config.yaml"
    );
  }
}
