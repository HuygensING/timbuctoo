package nl.knaw.huygens.timbuctoo.variation.model;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;

public class ModelWithOverriddenIndexAnnotations extends DomainEntityWithIndexAnnotations {

  @Override
  @IndexAnnotations({ @IndexAnnotation(fieldName = "test"), @IndexAnnotation(fieldName = "test2"), @IndexAnnotation(fieldName = "test3") })
  public String getString() {
    return super.getString();
  }

}
