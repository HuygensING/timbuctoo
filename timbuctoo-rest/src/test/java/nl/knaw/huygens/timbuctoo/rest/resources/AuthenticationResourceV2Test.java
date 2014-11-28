package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.config.Paths;

public class AuthenticationResourceV2Test extends AuthenticationResourceTest {
  @Override
  protected String getAPIVersion() {
    return Paths.V2_PATH;
  }
}
