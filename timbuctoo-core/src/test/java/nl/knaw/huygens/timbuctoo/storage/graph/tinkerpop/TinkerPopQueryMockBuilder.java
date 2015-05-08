package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public class TinkerPopQueryMockBuilder {
  private Graph db;
  private GraphQuery graphQuery;

  private TinkerPopQueryMockBuilder() {

  }

  public static TinkerPopQueryMockBuilder aQuery() {
    return new TinkerPopQueryMockBuilder();
  }

  public TinkerPopQuery build() {
    TinkerPopQuery query = mock(TinkerPopQuery.class);
    when(query.hasNotNullProperty(anyString(), anyObject())).thenReturn(query);
    when(query.createGraphQuery(db)).thenReturn(graphQuery);
    return query;
  }

  public TinkerPopQueryMockBuilder createsGraphQueryForDB(Graph db, GraphQuery graphQuery) {
    this.db = db;
    this.graphQuery = graphQuery;
    return this;
  }

}
