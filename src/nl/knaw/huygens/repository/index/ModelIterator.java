package nl.knaw.huygens.repository.index;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.indexdata.IndexAnnotations;

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
}
