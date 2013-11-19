package nl.knaw.huygens.timbuctoo.index.model;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ModelWithOverriddenIndexAnnotation extends DomainEntity {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "description")
  public String getDisplayName() {
    return null;
  }

}
