package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.config.Paths;

public class SearchResourceV2Test extends SearchResourceV1Test {

  @Override
  protected String getAPIVersion() {
    return Paths.V2_PATH;
  }
}
