package nl.knaw.huygens.repository.search;

import java.util.Set;

import nl.knaw.huygens.repository.facet.IndexAnnotation;

import org.apache.commons.lang.StringUtils;

class FullTextSearchFieldFinder extends AbstractFieldFinder {
  private static final String FULL_TEXT_SEARCH_PREFIX = "facet_t_";

  @Override
  protected void addField(Set<String> fields, IndexAnnotation indexAnnotation) {
    String fieldName = indexAnnotation.fieldName();
    if (StringUtils.startsWith(fieldName, FULL_TEXT_SEARCH_PREFIX)) {
      fields.add(fieldName);
    }
  }

}
