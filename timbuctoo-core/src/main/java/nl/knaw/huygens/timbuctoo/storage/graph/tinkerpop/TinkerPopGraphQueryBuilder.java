package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;

import java.util.List;
import java.util.Map.Entry;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.AbstractGraphQueryBuilder;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Predicate;

class TinkerPopGraphQueryBuilder extends AbstractGraphQueryBuilder<GraphQuery> {
  static class InCollectionPredicate implements Predicate {

    @Override
    public boolean evaluate(Object first, Object second) {
      List<?> possibleValues = (List<?>) second;

      return possibleValues.contains(first);
    }

  }

  private static final IsOfTypePredicate IS_OF_TYPE = new IsOfTypePredicate();
  private Graph db;

  public TinkerPopGraphQueryBuilder(Class<? extends Entity> type, Graph db) {
    this(type, new PropertyBusinessRules(), db);
  }

  public TinkerPopGraphQueryBuilder(Class<? extends Entity> type, PropertyBusinessRules businessRules, Graph db) {
    super(type, businessRules);
    this.db = db;
    this.hasProperties = Maps.newHashMap();
  }

  @Override
  public GraphQuery build() throws NoSuchFieldException {
    GraphQuery query = db.query();

    for (Entry<String, Object> entry : hasProperties.entrySet()) {
      query.has(getPropertyName(entry.getKey()), entry.getValue());
    }

    for (Entry<String, List<?>> entry : inCollectionProperties.entrySet()) {
      query.has(getPropertyName(entry.getKey()), new InCollectionPredicate(), entry.getValue());
    }

    if (searchByType) {
      query.has(ELEMENT_TYPES, IS_OF_TYPE, TypeNames.getInternalName(type));
    }

    return query;
  }
}