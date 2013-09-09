package nl.knaw.huygens.repository.variation.model;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

@DocumentTypeName("testextrabasedoc")
public class TestExtraBaseDoc extends Document {

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

}
