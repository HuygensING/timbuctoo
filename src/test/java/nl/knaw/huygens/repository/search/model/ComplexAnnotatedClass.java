package nl.knaw.huygens.repository.search.model;

import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.annotations.IndexAnnotations;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ComplexAnnotatedClass extends Document {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @IndexAnnotations({ @IndexAnnotation(fieldName = "facet_t_complex1", accessors = "getDisplayName", isFaceted = true, title = "Complex1"),
      @IndexAnnotation(fieldName = "facet_t_complex2", accessors = "getSimpleIndexField", isFaceted = true, title = "Complex2") })
  public SimpleAnnotatedClass getSimpleAnnotatedClass() {

    return null;
  }

}
