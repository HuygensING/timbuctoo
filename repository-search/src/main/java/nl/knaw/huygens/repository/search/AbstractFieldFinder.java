package nl.knaw.huygens.repository.search;

import java.lang.reflect.Method;
import java.util.Set;

import nl.knaw.huygens.repository.facet.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.facet.annotations.IndexAnnotations;
import nl.knaw.huygens.repository.model.Document;

import com.google.common.collect.Sets;

public abstract class AbstractFieldFinder {

  protected static final Class<IndexAnnotations> INDEX_ANNOTATIONS_CLASS = IndexAnnotations.class;
  protected static final Class<IndexAnnotation> INDEX_ANNOTATION_CLASS = IndexAnnotation.class;

  public AbstractFieldFinder() {
    super();
  }

  protected abstract void addField(Set<String> fields, IndexAnnotation indexAnnotation);

  @SuppressWarnings("unchecked")
  public Set<String> findFields(Class<? extends Document> type) {
    Set<String> fieldList = Sets.newHashSet();

    Method[] methods = type.getMethods();

    for (Method method : methods) {
      if (method.isAnnotationPresent(INDEX_ANNOTATION_CLASS)) {
        IndexAnnotation indexAnnotation = method.getAnnotation(INDEX_ANNOTATION_CLASS);
        addField(fieldList, indexAnnotation);

      } else if (method.isAnnotationPresent(INDEX_ANNOTATIONS_CLASS)) {
        IndexAnnotations indexAnnotations = method.getAnnotation(INDEX_ANNOTATIONS_CLASS);
        IndexAnnotation[] values = indexAnnotations.value();
        for (IndexAnnotation indexAnnotation : values) {
          addField(fieldList, indexAnnotation);
        }
      }
    }

    Class<?> superclass = type.getSuperclass();
    if (!Document.class.equals(superclass)) {
      fieldList.addAll(findFields((Class<? extends Document>) type.getSuperclass()));
    }

    return fieldList;
  }

}