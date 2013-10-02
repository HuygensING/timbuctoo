package nl.knaw.huygens.repository.variation.model;

import nl.knaw.huygens.repository.annotations.EntityTypeName;
import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

@EntityTypeName("testextrabasedoc")
public class TestExtraBaseDoc extends Entity {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

}
