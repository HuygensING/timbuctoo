package nl.knaw.huygens.repository.variation.model;

import nl.knaw.huygens.repository.annotations.DoNotRegister;
import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.model.SystemEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

@DoNotRegister
public class DoNotRegisterTestModel extends SystemEntity {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

}
