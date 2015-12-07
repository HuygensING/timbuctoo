package nl.knaw.huygens.timbuctoo;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class TimbuctooApplication extends Application<TimbuctooConfiguration> {
  public static void main(String[] args) throws Exception {
    new TimbuctooApplication().run(args);
  }
  @Override
  public void run(TimbuctooConfiguration configuration, Environment environment) throws Exception {

  }
}
