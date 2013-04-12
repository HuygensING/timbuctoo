package nl.knaw.huygens.repository.index;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.indexdata.IndexAnnotations;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExplicitlyAnnotatedModelWithIndexAnnotations extends Document {

  @Override
  @IndexAnnotation(fieldName = "id")
  public String getId() {
    return "";
  }

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @IndexAnnotations(value = { @IndexAnnotation(fieldName = "test"), @IndexAnnotation(fieldName = "test2") })
  public String getString() {
    return "";
  }

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String defaultVRE) {
    // TODO Auto-generated method stub

  }

}
