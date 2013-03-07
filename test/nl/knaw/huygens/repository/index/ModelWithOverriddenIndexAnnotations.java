package nl.knaw.huygens.repository.index;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.indexdata.IndexAnnotations;

public class ModelWithOverriddenIndexAnnotations extends ExplicitlyAnnotatedModelWithIndexAnnotations {
  @Override
  @IndexAnnotations({ @IndexAnnotation(fieldName = "test"), @IndexAnnotation(fieldName = "test2"), @IndexAnnotation(fieldName = "test3") })
  public String getString() {
    // TODO Auto-generated method stub
    return super.getString();
  }
}
