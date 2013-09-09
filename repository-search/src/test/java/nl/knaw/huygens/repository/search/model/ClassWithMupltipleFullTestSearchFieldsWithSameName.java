package nl.knaw.huygens.repository.search.model;

import nl.knaw.huygens.repository.facet.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClassWithMupltipleFullTestSearchFieldsWithSameName extends Document {

  private String simpleIndexField;

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @IndexAnnotation(fieldName = "facet_t_simple", isFaceted = true, title = "Simple")
  public String getSimpleIndexField() {
    return simpleIndexField;
  }

  public void setSimpleIndexField(String simpleIndexField) {
    this.simpleIndexField = simpleIndexField;
  }

  @IndexAnnotation(fieldName = "facet_t_simple", isFaceted = true, title = "Simple1")
  public String getSimpleIndexField1() {
    return simpleIndexField;
  }

  public void setSimpleIndexField1(String simpleIndexField) {
    this.simpleIndexField = simpleIndexField;
  }

}
