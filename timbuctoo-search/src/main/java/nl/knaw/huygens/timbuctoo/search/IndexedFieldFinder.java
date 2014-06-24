package nl.knaw.huygens.timbuctoo.search;

import java.util.Set;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;

public class IndexedFieldFinder extends AbstractFieldFinder {

  @Override
  protected void addField(Set<String> fields, IndexAnnotation indexAnnotation) {
    fields.add(indexAnnotation.fieldName());
  }

}
