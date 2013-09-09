package nl.knaw.huygens.repository.variation.model;

import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.model.DomainDocument;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TestConcreteDoc extends DomainDocument {

  public String name;

  public TestConcreteDoc() {}

  public TestConcreteDoc(String id) {
    setId(id);
  }

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

}
