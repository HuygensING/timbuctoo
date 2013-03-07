package nl.knaw.huygens.repository.index;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;

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
  
  public List<IndexAnnotation> getIndexAnnotationsForMethod(Method m){
    return methods.get(m);
  }

}
