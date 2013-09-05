package nl.knaw.huygens.repository.search.model;

import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClassWithUnNamedSortableFields extends Document {

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
