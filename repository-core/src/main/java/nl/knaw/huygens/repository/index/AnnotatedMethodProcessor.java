package nl.knaw.huygens.repository.index;

import java.lang.reflect.Method;

import nl.knaw.huygens.repository.facet.annotations.IndexAnnotation;

/**
 * Function pointers for Java! We want to loop over the same annotations
 * and not do the same boring stuff all the time. So, we abstract out the actual
 * dealing with the looped items from the looping:
 */
public interface AnnotatedMethodProcessor {
  public void process(Method m, IndexAnnotation annotation);
}