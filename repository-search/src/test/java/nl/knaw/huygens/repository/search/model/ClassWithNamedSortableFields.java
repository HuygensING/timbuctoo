package nl.knaw.huygens.repository.search.model;

import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClassWithNamedSortableFields extends Document {

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
