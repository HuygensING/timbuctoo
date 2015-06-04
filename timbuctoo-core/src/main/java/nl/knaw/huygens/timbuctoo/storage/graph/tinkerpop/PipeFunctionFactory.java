package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.pipes.PipeFunction;

public class PipeFunctionFactory {

  public <T> PipeFunction<Vertex, T> forDistinctProperty(String propertyName) {
    return new DistinctPropertyFunction<Vertex, T>(propertyName);
  }

  private static class DistinctPropertyFunction<E extends Element, P> implements PipeFunction<E, P> {

    private String propertyName;

    public DistinctPropertyFunction(String propertyName) {
      this.propertyName = propertyName;
    }

    @Override
    public P compute(E argument) {
      return argument.getProperty(propertyName);
    }
  }

}
