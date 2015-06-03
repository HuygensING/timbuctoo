package nl.knaw.huygens.timbuctoo.storage.graph;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public class TimbuctooQuery {

  private Map<String, Object> hasProperties;
  private Class<? extends Entity> type;
  private PropertyBusinessRules businessRules;

  public TimbuctooQuery(Class<? extends Entity> type, PropertyBusinessRules businessRules) {
    this(type, businessRules, Maps.<String, Object> newHashMap());

  }

  TimbuctooQuery(Class<? extends Entity> type, PropertyBusinessRules businessRules, Map<String, Object> hasProperties) {
    this.hasProperties = hasProperties;
    this.type = type;
    this.businessRules = businessRules;
  }

  public TimbuctooQuery hasNotNullProperty(String name, Object value) {
    if (value != null) {
      hasProperties.put(name, value);
    }
    return this;
  }

  public TimbuctooQuery hasType(Class<? extends Entity> type) {
    this.type = type;
    return this;
  }

  public GraphQuery createGraphQuery(Graph db) {
    TinkerPopGraphQueryBuilder queryBuilder = new TinkerPopGraphQueryBuilder(type, businessRules, db);

    queryBuilder.setHasProperties(hasProperties);

    if (type != null) {
      queryBuilder.setType(type);
    }

    return queryBuilder.build();

  }

}
