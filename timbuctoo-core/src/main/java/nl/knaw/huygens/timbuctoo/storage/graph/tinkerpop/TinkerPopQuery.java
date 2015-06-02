package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public class TinkerPopQuery implements TimbuctooQuery {

  private Map<String, Object> hasProperties;
  private List<Class<? extends Entity>> hasTypes;

  public TinkerPopQuery() {
    this(Lists.<Class<? extends Entity>> newArrayList(), Maps.<String, Object> newHashMap());
  }

  TinkerPopQuery(List<Class<? extends Entity>> hasTypes, Map<String, Object> hasProperties) {
    this.hasTypes = hasTypes;
    this.hasProperties = hasProperties;
  }

  @Override
  public TinkerPopQuery hasNotNullProperty(String name, Object value) {
    if (value != null) {
      hasProperties.put(name, value);
    }
    return this;
  }

  @Override
  public TinkerPopQuery hasType(Class<? extends Entity> type) {
    hasTypes.add(type);
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

}
