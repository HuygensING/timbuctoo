package nl.knaw.huygens.repository.variation.model.projectb;

import nl.knaw.huygens.repository.variation.model.TestInheritsFromTestBaseDoc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestDoc extends TestInheritsFromTestBaseDoc {
  private String defaultVRE;

  public TestDoc() {
    super();
  }

  public String blah;

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    return defaultVRE;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String defaultVRE) {
    this.defaultVRE = defaultVRE;
  }
}
