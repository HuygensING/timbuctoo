package nl.knaw.huygens.repository.index.model;

import nl.knaw.huygens.repository.facet.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImplicitlyAnnotatedModel extends Document {

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
