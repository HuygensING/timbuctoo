package nl.knaw.huygens.repository.index.model;

import nl.knaw.huygens.repository.facet.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.facet.annotations.IndexAnnotations;

public class ModelWithOverriddenIndexAnnotations extends ExplicitlyAnnotatedModelWithIndexAnnotations {

  @Override
  @IndexAnnotations({ @IndexAnnotation(fieldName = "test"), @IndexAnnotation(fieldName = "test2"), @IndexAnnotation(fieldName = "test3") })
  public String getString() {
    return super.getString();
  }

}
