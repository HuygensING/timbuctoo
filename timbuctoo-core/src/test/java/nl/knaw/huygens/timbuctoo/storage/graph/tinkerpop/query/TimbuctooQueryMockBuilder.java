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

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.TinkerPopGraphQueryBuilder;

import com.tinkerpop.blueprints.GraphQuery;

public class TimbuctooQueryMockBuilder {
  private GraphQuery graphQuery;
  private TinkerPopGraphQueryBuilder queryBuilder;
  private boolean searchLatestOnly;

  private TimbuctooQueryMockBuilder() {

  }

  public static TimbuctooQueryMockBuilder aQuery() {
    return new TimbuctooQueryMockBuilder();
  }

  public TimbuctooQuery build() {
    TimbuctooQuery query = mock(TimbuctooQuery.class);
    when(query.hasNotNullProperty(anyString(), anyObject())).thenReturn(query);
    when(query.searchLatestOnly(anyBoolean())).thenReturn(query);
    when(query.createGraphQuery(queryBuilder)).thenReturn(graphQuery);
    when(query.searchLatestOnly()).thenReturn(searchLatestOnly);
    return query;
  }

  public TimbuctooQueryMockBuilder createsGraphQueryForDB(TinkerPopGraphQueryBuilder queryBuilder, GraphQuery graphQuery) {
    this.queryBuilder = queryBuilder;
    this.graphQuery = graphQuery;
    return this;
  }

  public TimbuctooQueryMockBuilder searchesLatestOnly(boolean searchLatestOnly) {
    this.searchLatestOnly = searchLatestOnly;
    return this;
  }

}
