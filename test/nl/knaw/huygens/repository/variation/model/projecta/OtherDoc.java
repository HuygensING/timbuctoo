package nl.knaw.huygens.repository.variation.model.projecta;

import nl.knaw.huygens.repository.variation.model.TestInheritsFromTestBaseDoc;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Another extension of the basic test doc.
 */
public class OtherDoc extends TestInheritsFromTestBaseDoc {
  public String otherThing;
  private String defaultVRE;

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
