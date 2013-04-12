package nl.knaw.huygens.repository.variation.model.projectb;

import com.fasterxml.jackson.annotation.JsonProperty;

import nl.knaw.huygens.repository.variation.model.TestBaseDoc;

public class TestDoc extends TestBaseDoc {
  private String defaultVRE;

  public TestDoc() {
    super();
  }

  public String blah;

  @Override
  @JsonProperty("!defaultVRE")
  public String getCurrentVariation() {
    return defaultVRE;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public void setCurrentVariation(String defaultVRE) {
    this.defaultVRE = defaultVRE;
  }
}
