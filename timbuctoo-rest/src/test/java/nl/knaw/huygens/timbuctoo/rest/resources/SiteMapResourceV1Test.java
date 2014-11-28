package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.config.Paths;

public class SiteMapResourceV1Test extends SiteMapResourceTest {
  @Override
  protected String getAPIVersion() {
    return Paths.V1_PATH;
  }
}
