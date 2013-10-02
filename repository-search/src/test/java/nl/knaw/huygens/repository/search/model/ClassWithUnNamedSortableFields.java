package nl.knaw.huygens.repository.search.model;

import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClassWithUnNamedSortableFields extends Entity {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @IndexAnnotation(isSortable = true)
  public String getTest() {
    return null;
  }

  @IndexAnnotation(isSortable = true)
  public String getTest1() {
    return null;
  }

}
