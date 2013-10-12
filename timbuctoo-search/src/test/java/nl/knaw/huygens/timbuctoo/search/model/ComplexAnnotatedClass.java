package nl.knaw.huygens.timbuctoo.search.model;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ComplexAnnotatedClass extends DomainEntity {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_t_complex1", accessors = "getDisplayName", isFaceted = true, title = "Complex1"),
      @IndexAnnotation(fieldName = "dynamic_t_complex2", accessors = "getSimpleIndexField", isFaceted = true, title = "Complex2") })
  public SimpleAnnotatedClass getSimpleAnnotatedClass() {

    return null;
  }

}
