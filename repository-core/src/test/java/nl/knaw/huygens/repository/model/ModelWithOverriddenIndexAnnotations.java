package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.facet.IndexAnnotations;

public class ModelWithOverriddenIndexAnnotations extends ExplicitlyAnnotatedModelWithIndexAnnotations {

  @Override
  @IndexAnnotations({ @IndexAnnotation(fieldName = "test"), @IndexAnnotation(fieldName = "test2"), @IndexAnnotation(fieldName = "test3") })
  public String getString() {
    return super.getString();
  }

}
