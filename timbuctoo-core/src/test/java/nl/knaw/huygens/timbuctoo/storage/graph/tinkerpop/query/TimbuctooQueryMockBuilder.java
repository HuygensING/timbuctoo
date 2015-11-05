package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

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
