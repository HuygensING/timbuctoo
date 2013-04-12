package nl.knaw.huygens.repository.variation.model.projecta;

import nl.knaw.huygens.repository.variation.model.TestBaseDoc;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Another extension of the basic test doc.
 */
public class OtherDoc extends TestBaseDoc {
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
