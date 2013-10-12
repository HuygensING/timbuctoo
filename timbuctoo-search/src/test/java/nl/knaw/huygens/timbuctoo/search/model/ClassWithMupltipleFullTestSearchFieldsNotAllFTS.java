package nl.knaw.huygens.timbuctoo.search.model;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClassWithMupltipleFullTestSearchFieldsNotAllFTS extends DomainEntity {

  private String simpleIndexField;

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @IndexAnnotation(fieldName = "dynamic_t_simple", isFaceted = true, title = "Simple")
  public String getSimpleIndexField() {
    return simpleIndexField;
  }

  public void setSimpleIndexField(String simpleIndexField) {
    this.simpleIndexField = simpleIndexField;
  }

  @IndexAnnotation(fieldName = "dynamic_s_simple", isFaceted = true, title = "Simple1")
  public String getSimpleIndexField1() {
    return simpleIndexField;
  }

  public void setSimpleIndexField1(String simpleIndexField) {
    this.simpleIndexField = simpleIndexField;
  }

}
