package nl.knaw.huygens.timbuctoo.index.model;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ExplicitlyAnnotatedModel extends DomainEntity {

  @IndexAnnotation
  public String getString() {
    return "";
  }

  @Override
  @IndexAnnotation(fieldName = "id")
  public String getId() {
    return "";
  }

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return "";
  }

}
