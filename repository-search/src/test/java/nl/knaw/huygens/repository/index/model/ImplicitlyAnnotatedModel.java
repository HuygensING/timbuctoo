package nl.knaw.huygens.repository.index.model;

import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.model.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImplicitlyAnnotatedModel extends Entity {

  @Override
  public String getId() {
    return "";
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  @IndexAnnotation
  public String getString() {
    return "";
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
