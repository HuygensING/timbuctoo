package nl.knaw.huygens.repository.search;

import java.lang.reflect.Method;
import java.util.Set;

import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.annotations.IndexAnnotations;
import nl.knaw.huygens.repository.model.Document;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

public class FullTextSearchFieldFinder {
  private static final String FULL_TEXT_SEARCH_PREFIX = "facet_t_";
  private static final Class<IndexAnnotations> INDEX_ANNOTATIONS_CLASS = IndexAnnotations.class;
  private static final Class<IndexAnnotation> INDEX_ANNOTATION_CLASS = IndexAnnotation.class;

  @SuppressWarnings("unchecked")
  public Set<String> findFullTextSearchFields(Class<? extends Document> type) {
    Set<String> fieldList = Sets.newHashSet();

    Method[] methods = type.getMethods();

    for (Method method : methods) {
      if (method.isAnnotationPresent(INDEX_ANNOTATION_CLASS)) {
        IndexAnnotation indexAnnotation = method.getAnnotation(INDEX_ANNOTATION_CLASS);
        addFullTextSearchField(fieldList, indexAnnotation);

      } else if (method.isAnnotationPresent(INDEX_ANNOTATIONS_CLASS)) {
        IndexAnnotations indexAnnotations = method.getAnnotation(INDEX_ANNOTATIONS_CLASS);
        IndexAnnotation[] values = indexAnnotations.value();
        for (IndexAnnotation indexAnnotation : values) {
          addFullTextSearchField(fieldList, indexAnnotation);
        }
      }
    }

    Class<?> superclass = type.getSuperclass();
    if (!Document.class.equals(superclass)) {
      fieldList.addAll(findFullTextSearchFields((Class<? extends Document>) type.getSuperclass()));
    }

    return fieldList;
  }

  private void addFullTextSearchField(Set<String> fullTextSearchFields, IndexAnnotation indexAnnotation) {
    String fieldName = indexAnnotation.fieldName();
    if (StringUtils.startsWith(fieldName, FULL_TEXT_SEARCH_PREFIX)) {
      fullTextSearchFields.add(fieldName);
    }
  }

}
