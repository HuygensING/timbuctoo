package nl.knaw.huygens.repository.search;

import java.util.Set;

import nl.knaw.huygens.repository.facet.IndexAnnotation;

import org.apache.commons.lang.StringUtils;

class SortableFieldFinder extends AbstractFieldFinder {

  @Override
  protected void addField(Set<String> fields, IndexAnnotation indexAnnotation) {
    if (indexAnnotation.isSortable() && StringUtils.isNotBlank(indexAnnotation.fieldName())) {
      fields.add(indexAnnotation.fieldName());
    }
  }

}
