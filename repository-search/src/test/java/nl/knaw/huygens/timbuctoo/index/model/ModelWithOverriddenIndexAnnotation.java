package nl.knaw.huygens.timbuctoo.index.model;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelWithOverriddenIndexAnnotation extends Entity {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "description")
  public String getDisplayName() {
    return null;
  }

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    return null;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String defaultVRE) {}

}
