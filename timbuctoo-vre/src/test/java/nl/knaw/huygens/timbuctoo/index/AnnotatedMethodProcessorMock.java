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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;

public class AnnotatedMethodProcessorMock implements AnnotatedMethodProcessor {

  private int numberOfIndexAnnotatios;
  private Map<Method, List<IndexAnnotation>> methods;

  public AnnotatedMethodProcessorMock() {
    methods = new HashMap<Method, List<IndexAnnotation>>();
  }

  @Override
  public void process(Method m, IndexAnnotation annotation) {
    numberOfIndexAnnotatios++;

    if (methods.containsKey(m)) {
      methods.get(m).add(annotation);
    } else {
      List<IndexAnnotation> annotations = new ArrayList<IndexAnnotation>();
      annotations.add(annotation);
      methods.put(m, annotations);
    }
  }

  public int getNumberOfIndexAnnotations() {
    return numberOfIndexAnnotatios;
  }

  public List<IndexAnnotation> getIndexAnnotationsForMethod(Method m) {
    return methods.get(m);
  }

}
