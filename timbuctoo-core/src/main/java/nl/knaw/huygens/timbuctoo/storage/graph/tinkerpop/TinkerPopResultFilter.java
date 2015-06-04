package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.util.List;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.pipes.PipeFunction;

public class TinkerPopResultFilter {

  public TinkerPopResultFilter(List<PipeFunction<Vertex, Object>> pipeFunctions) {
    // TODO Auto-generated constructor stub
  }

  public <T extends Element> Iterable<T> filter(Iterable<T> iterableToFilter) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
