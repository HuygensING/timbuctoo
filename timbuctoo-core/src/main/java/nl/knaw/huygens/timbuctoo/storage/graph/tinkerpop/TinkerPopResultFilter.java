package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ResultFilter;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

public class TinkerPopResultFilter<T extends Element> implements ResultFilter {

  private List<PipeFunction<T, Object>> distinctPropertyFunctions;

  public TinkerPopResultFilter() {}

  public Iterable<T> filter(Iterable<T> iterableToFilter) {
    GremlinPipeline<Iterable<T>, T> pipeline = new GremlinPipeline<Iterable<T>, T>(iterableToFilter);

    for (PipeFunction<T, Object> function : distinctPropertyFunctions) {
      pipeline.dedup(function);
    }

    return pipeline.toList();
  }

  @Override
  public void setDistinctProperties(Set<String> disitinctProperties) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void setType(Class<? extends Entity> type) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }
}
