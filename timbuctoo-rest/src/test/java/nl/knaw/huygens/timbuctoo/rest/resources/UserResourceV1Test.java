package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.config.Paths;

public class UserResourceV1Test extends UserResourceTest {
  @Override
  protected String getAPIVersion() {
    return Paths.V1_PATH;
  }
}
