package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.pipes.PipeFunction;

class PipeFunctionFactory {

  public <E extends Element, P> PipeFunction<E, P> forDistinctProperty(String propertyName) {
    return new DistinctPropertyFunction<E, P>(propertyName);
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
