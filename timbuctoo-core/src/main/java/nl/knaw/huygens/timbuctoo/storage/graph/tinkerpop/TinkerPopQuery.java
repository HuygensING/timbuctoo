package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;

import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public class TinkerPopQuery implements TimbuctooQuery {

  private Map<String, Object> hasProperties;
  private Class<? extends Entity> type;
  private IsOfTypePredicate isOfType = new IsOfTypePredicate();

  public TinkerPopQuery() {
    this(Maps.<String, Object> newHashMap());
  }

  TinkerPopQuery(Map<String, Object> hasProperties) {
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
    this.type = type;
    return this;
  }

  @Override
  public GraphQuery createGraphQuery(Graph db) {
    GraphQuery query = db.query();

    for (Entry<String, Object> entry : hasProperties.entrySet()) {
      query.has(entry.getKey(), entry.getValue());
    }

    if (type != null) {
      query.has(ELEMENT_TYPES, isOfType, TypeNames.getInternalName(type));
    }

    return query;

  }

}
