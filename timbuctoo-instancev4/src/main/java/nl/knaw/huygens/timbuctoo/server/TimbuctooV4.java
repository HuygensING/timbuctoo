package nl.knaw.huygens.timbuctoo.server;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class TimbuctooV4 extends Application<TimbuctooConfiguration> {
  public static void main(String[] args) throws Exception {
    new TimbuctooV4().run(args);
  }

  @Override
  public void run(TimbuctooConfiguration configuration, Environment environment) throws Exception {

  }
}
