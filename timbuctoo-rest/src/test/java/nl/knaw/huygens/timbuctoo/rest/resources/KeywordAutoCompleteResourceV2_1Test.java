package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.timbuctoo.config.Paths;

public class KeywordAutoCompleteResourceV2_1Test extends KeywordAutoCompleteResourceV2Test {
  @Override
  protected String getAPIVersion() {
    return Paths.V2_1_PATH;
  }
}
