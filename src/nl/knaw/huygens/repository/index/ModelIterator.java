package nl.knaw.huygens.repository.index;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.Lists;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.indexdata.IndexAnnotations;
import nl.knaw.huygens.repository.model.Document;

public class ModelIterator {

  /**
   * Utility method to deal with either one or multiple annotations per element...
   */
  private void processMethod(AnnotatedMethodProcessor proc, Method m, List<? extends Annotation> annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof IndexAnnotation) {
        proc.process(m, (IndexAnnotation) annotation);
      } else if (annotation instanceof IndexAnnotations) {
        // Recursion alert! (Java's own fault for not allowing multiple identical annotations
        // on a single element, of course)
        processMethod(proc, m, Lists.newArrayList(((IndexAnnotations) annotation).value()));
      }
    }
  }

  public void processClass(AnnotatedMethodProcessor proc, Class<? extends Document> cls) {
    Method[] methods = cls.getMethods();
    for (Method m : methods) {
      List<Annotation> annotations = Lists.newArrayList(m.getAnnotations());
      processMethod(proc, m, annotations);
    }
  }
}
