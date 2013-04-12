package nl.knaw.huygens.repository.index;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelWithOverriddenIndexAnnotation extends Document {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "description")
  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public String getCurrentVariation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public void setCurrentVariation(String defaultVRE) {
    // TODO Auto-generated method stub

  }

}
