package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;

import com.tinkerpop.blueprints.GraphQuery;

public class TimbuctooQueryMockBuilder {
  private GraphQuery graphQuery;
  private TinkerPopGraphQueryBuilder queryBuilder;
  private boolean searchLatestOnly;
  private TinkerPopResultFilterBuilder resultFilterBuilder;
  private TinkerPopResultFilter resultFilter;

  private TimbuctooQueryMockBuilder() {

  }

  public static TimbuctooQueryMockBuilder aQuery() {
    return new TimbuctooQueryMockBuilder();
  }

  public TimbuctooQuery build() {
    TimbuctooQuery query = mock(TimbuctooQuery.class);
    when(query.hasNotNullProperty(anyString(), anyObject())).thenReturn(query);
    when(query.createGraphQuery(queryBuilder)).thenReturn(graphQuery);
    when(query.searchLatestOnly()).thenReturn(searchLatestOnly);
    when(query.createResultFilter(resultFilterBuilder)).thenReturn(resultFilter);
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

  public TimbuctooQueryMockBuilder createsResultFilter(TinkerPopResultFilterBuilder resultFilterBuilder, TinkerPopResultFilter resultFilter) {
    this.resultFilterBuilder = resultFilterBuilder;
    this.resultFilter = resultFilter;

    return this;
  }

}
