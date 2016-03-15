package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Predicate;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.AbstractGraphQueryBuilder;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;

import java.util.List;
import java.util.Map.Entry;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.IS_LATEST;

public class TinkerPopGraphQueryBuilder extends AbstractGraphQueryBuilder<GraphQuery> {
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

    if(searchLatestOnly){
      query.has(IS_LATEST, true);
    }

    return query;
  }
}
