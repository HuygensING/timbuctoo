package nl.knaw.huygens.repository.index;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.indexdata.IndexAnnotations;

import org.apache.commons.lang.StringUtils;


public class ModelIterator {
  public void processMethods(AnnotatedMethodProcessor proc, Method[] methods) {
    for (Method m : methods) {
      processMethod(proc, m, m.getAnnotations());
    }
  }

  /**
   * Utility method to deal with either one or multiple annotations per element...
   */
  private void processMethod(AnnotatedMethodProcessor proc, Method m, Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof IndexAnnotation) {
        proc.process(m, (IndexAnnotation) annotation);
      } else if (annotation instanceof IndexAnnotations) {
        // Recursion alert! (Java's own fault for not allowing multiple identical annotations
        // on a single element, of course)
        processMethod(proc, m, ((IndexAnnotations) annotation).value());
      }
    }
  }

  
  // FIXME getFieldName should probably go somewhere else...
  /**
   * Determines the index field name from the method name (only used if the annotation doesn't specify a fieldname).
   */
  public String getFieldName(Method m) {
    String name = m.getName();
    String type = m.getReturnType().getSimpleName();
    String rv = name.startsWith("get") ? name.substring(3) : name; // eliminate 'get' part
    String[] parts = StringUtils.splitByCharacterTypeCamelCase(rv);
    type = type.replaceAll("\\[\\]", "");
    if (type.equals("boolean")) {
      type = "b";
    } else if (type.equals("int") || type.equals("long")) {
      type = "i";
    } else {
      type = "s";
    }
    return "facet_" + type + "_" + StringUtils.join(parts, "_").toLowerCase();
  }
}
