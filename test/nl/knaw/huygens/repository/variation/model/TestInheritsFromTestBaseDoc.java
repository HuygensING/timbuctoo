package nl.knaw.huygens.repository.variation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestInheritsFromTestBaseDoc extends TestBaseDoc {

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String currentVariation) {
    // TODO Auto-generated method stub

  }

}
