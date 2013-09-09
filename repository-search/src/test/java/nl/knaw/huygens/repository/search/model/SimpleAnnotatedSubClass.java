package nl.knaw.huygens.repository.search.model;

import nl.knaw.huygens.repository.facet.annotations.IndexAnnotation;

public class SimpleAnnotatedSubClass extends SimpleAnnotatedClass {
  private String simpleProperty;

  @IndexAnnotation(fieldName = "facet_s_prop", isFaceted = true, title = "Property")
  public String getSimpleProperty() {
    return simpleProperty;
  }

  public void setSimpleProperty(String simpleProperty) {
    this.simpleProperty = simpleProperty;
  }
}
