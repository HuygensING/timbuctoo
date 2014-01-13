package nl.knaw.huygens.timbuctoo.index;

/*
 * #%L
 * Timbuctoo search
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.Entity;

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

  public void processClass(AnnotatedMethodProcessor proc, Class<? extends Entity> cls) {
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

    if (firstAnnotedMethod == null && (cls != Entity.class)) {
      firstAnnotedMethod = getFirstIndexAnnotedMethod(overridingMethod, cls);
    }
    return firstAnnotedMethod;
  }

}
