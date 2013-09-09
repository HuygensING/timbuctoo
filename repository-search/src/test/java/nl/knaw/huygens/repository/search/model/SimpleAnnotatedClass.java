package nl.knaw.huygens.repository.search.model;

import nl.knaw.huygens.repository.facet.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SimpleAnnotatedClass extends Document {

  private String simpleIndexField;

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return "name";
  }

  @IndexAnnotation(fieldName = "facet_s_simple", isFaceted = true, title = "Simple")
  public String getSimpleIndexField() {
    return simpleIndexField;
  }

  public void setSimpleIndexField(String simpleIndexField) {
    this.simpleIndexField = simpleIndexField;
  }

}
