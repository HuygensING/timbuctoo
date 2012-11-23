package nl.knaw.huygens.repository.facets;

import java.lang.reflect.Method;
import java.util.Map;

import nl.knaw.huygens.repository.index.AnnotatedMethodProcessor;
import nl.knaw.huygens.repository.index.ModelIterator;
import nl.knaw.huygens.repository.indexdata.CustomIndexer;
import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.indexdata.CustomIndexer.NoopIndexer;

import com.google.common.collect.Maps;

class FieldMapper implements AnnotatedMethodProcessor {
  private Map<String, Boolean> rv = Maps.newHashMap();
  private ModelIterator iterator;

  public FieldMapper(ModelIterator iterator) {
    this.iterator = iterator;
  }

  public void process(Method m, IndexAnnotation annotation) {
    String fieldName;
    boolean isFaceted = annotation.isFaceted();
    if (!isFaceted) {
      return;
    }
    if (!annotation.customIndexer().equals(NoopIndexer.class)) {
      try {
        CustomIndexer ci = annotation.customIndexer().getConstructor().newInstance();
        fieldName = ci.getFieldFilter();
      } catch (Exception e) {
        fieldName = "";
        e.printStackTrace();
      }
    } else {
      fieldName = annotation.fieldName();
    }

    if (fieldName.length() == 0) {
      fieldName = iterator.getFieldName(m);
    }
    if (!rv.containsKey(fieldName)) {
      boolean isComplex = annotation.isComplex();
      rv.put(fieldName, isComplex);
    }
  }

  public Map<String, Boolean> getResult() {
    return rv;
  }
}