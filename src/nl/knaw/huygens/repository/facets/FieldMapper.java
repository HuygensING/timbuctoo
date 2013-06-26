package nl.knaw.huygens.repository.facets;

import java.lang.reflect.Method;
import java.util.Map;

import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.index.AnnotatedMethodProcessor;
import nl.knaw.huygens.repository.index.Utils;
import nl.knaw.huygens.repository.indexdata.CustomIndexer;
import nl.knaw.huygens.repository.indexdata.CustomIndexer.NoopIndexer;

import com.google.common.collect.Maps;

class FieldMapper implements AnnotatedMethodProcessor {

  private final Map<String, Boolean> rv;

  public FieldMapper() {
    rv = Maps.newHashMap();
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
      fieldName = Utils.getFieldName(m);
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
