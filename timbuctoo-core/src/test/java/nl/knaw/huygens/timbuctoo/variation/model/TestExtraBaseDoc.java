package nl.knaw.huygens.timbuctoo.variation.model;

import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

@EntityTypeName("testextrabasedoc")
public class TestExtraBaseDoc extends SystemEntity {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

}
