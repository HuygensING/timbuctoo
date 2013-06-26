package nl.knaw.huygens.repository.index;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.annotations.IndexAnnotations;
import nl.knaw.huygens.repository.model.Document;

public class ModelIterator {

  /**
   * Utility method to deal with either one or multiple annotations per
   * element...
   */
  private void processMethod(AnnotatedMethodProcessor proc, Method method, Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof IndexAnnotation) {
        proc.process(method, (IndexAnnotation) annotation);
      } else if (annotation instanceof IndexAnnotations) {
        // Recursion alert! (Java's own fault for not allowing multiple
        // identical annotations on a single element, of course)
        processMethod(proc, method, ((IndexAnnotations) annotation).value());
      }
    }
  }

  public void processClass(AnnotatedMethodProcessor proc, Class<? extends Document> cls) {
    for (Method method : cls.getMethods()) {
      if (hasIndexAnnotations(method)) {
        processMethod(proc, method, method.getAnnotations());
      } else if (!hasIndexAnnotations(method)) {
        method = getFirstIndexAnnotedMethod(method, cls);
        if (method != null) {
          processMethod(proc, method, method.getAnnotations());
        }
      }
    }
  }

  private boolean hasIndexAnnotations(Method m) {
    return (m.getAnnotation(IndexAnnotation.class) != null || m.getAnnotation(IndexAnnotations.class) != null);
  }

  private Method getFirstIndexAnnotedMethod(Method overridingMethod, Class<?> subClass) {
    Class<?> cls = subClass.getSuperclass();
    Method firstAnnotedMethod = null;
    try {
      Method method = cls.getMethod(overridingMethod.getName());
      if (hasIndexAnnotations(method)) {
        firstAnnotedMethod = method;
      }
    } catch (NoSuchMethodException e) {} catch (SecurityException e) {}

    if (firstAnnotedMethod == null && (cls != Document.class)) {
      firstAnnotedMethod = getFirstIndexAnnotedMethod(overridingMethod, cls);
    }
    return firstAnnotedMethod;
  }

}
