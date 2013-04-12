package nl.knaw.huygens.repository.variation.model.projecta;

import com.fasterxml.jackson.annotation.JsonProperty;

import nl.knaw.huygens.repository.variation.model.TestBaseDoc;

/**
 * Another extension of the basic test doc.
 */
public class OtherDoc extends TestBaseDoc {
  public String otherThing;
  private String defaultVRE;

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
