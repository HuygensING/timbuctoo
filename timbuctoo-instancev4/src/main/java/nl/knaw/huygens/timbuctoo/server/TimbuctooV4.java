package nl.knaw.huygens.timbuctoo.server;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.timbuctoo.server.rest.AuthenticationV2_1EndPoint;
import nl.knaw.huygens.timbuctoo.server.rest.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.server.rest.UserV2_1Endpoint;

public class TimbuctooV4 extends Application<TimbuctooConfiguration> {
  public static void main(String[] args) throws Exception {
    new TimbuctooV4().run(args);
  }

  @Override
  public void run(TimbuctooConfiguration configuration, Environment environment) throws Exception {
    LoggedInUserStore loggedInUserStore = new LoggedInUserStore(null);
    environment.jersey().register(new AuthenticationV2_1EndPoint(loggedInUserStore));
    environment.jersey().register(new UserV2_1Endpoint(loggedInUserStore));
  }
}
