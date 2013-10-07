package nl.knaw.huygens.timbuctoo.search.model;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClassWithNamedSortableFields extends Entity {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @IndexAnnotation(fieldName = "test", isSortable = true)
  public String getTest() {
    return null;
  }

  @IndexAnnotation(fieldName = "blah", isSortable = true)
  public String getTest1() {
    return null;
  }

}
