package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public class TinkerPopQuery implements TimbuctooQuery {

  private Map<String, Object> hasProperties;

  public TinkerPopQuery() {
    this(Maps.<String, Object> newHashMap());
  }

  public TinkerPopQuery(Map<String, Object> hasProperties) {
    this.hasProperties = hasProperties;
  }

  /**
   * Uses the property only when the value is not null.
   * @param name the name of the property
   * @param value the value of the property
   * @return the current instance
   */
  @Override
  public TinkerPopQuery hasNotNullProperty(String name, Object value) {
    if (value != null) {
      hasProperties.put(name, value);
    }
    return this;
  }

  @Override
  public GraphQuery createGraphQuery(Graph db) {
    GraphQuery query = db.query();

    for (Entry<String, Object> entry : hasProperties.entrySet()) {
      query.has(entry.getKey(), entry.getValue());
    }

    return query;

  }

  @Override
  public TimbuctooQuery hasType(Class<? extends Entity> any) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
