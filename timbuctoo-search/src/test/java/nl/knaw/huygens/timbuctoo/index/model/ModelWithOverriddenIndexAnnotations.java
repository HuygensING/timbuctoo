package nl.knaw.huygens.timbuctoo.index.model;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;

public class ModelWithOverriddenIndexAnnotations extends ExplicitlyAnnotatedModelWithIndexAnnotations {

  @Override
  @IndexAnnotations({ @IndexAnnotation(fieldName = "test"), @IndexAnnotation(fieldName = "test2"), @IndexAnnotation(fieldName = "test3") })
  public String getString() {
    return super.getString();
  }

}
