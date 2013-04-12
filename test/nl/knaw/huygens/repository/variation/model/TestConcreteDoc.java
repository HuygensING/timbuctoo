package nl.knaw.huygens.repository.variation.model;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestConcreteDoc extends Document {
  public String name;
  private String defaultVRE;

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

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
