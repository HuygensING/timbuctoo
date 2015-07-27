package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.config.Paths;

public class AutocompleteResourceV2_1Test extends AutocompleteResourceV2Test{
  @Override
  public String getAPIVersion() {
    return Paths.V2_1_PATH;
  }
}
