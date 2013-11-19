package nl.knaw.huygens.timbuctoo.index.model;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ExplicitlyAnnotatedModelWithIndexAnnotations extends DomainEntity {

  @Override
  @IndexAnnotation(fieldName = "id")
  public String getId() {
    return "";
  }

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

  @IndexAnnotations(value = { @IndexAnnotation(fieldName = "test"), @IndexAnnotation(fieldName = "test2") })
  public String getString() {
    return "";
  }

}
