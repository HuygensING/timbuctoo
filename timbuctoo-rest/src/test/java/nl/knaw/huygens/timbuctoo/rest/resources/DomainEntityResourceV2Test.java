package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.config.Paths;

public class DomainEntityResourceV2Test extends DomainEntityResourceTest {
  @Override
  protected String getAPIVersion() {
    return Paths.V2_PATH;
  }
}
