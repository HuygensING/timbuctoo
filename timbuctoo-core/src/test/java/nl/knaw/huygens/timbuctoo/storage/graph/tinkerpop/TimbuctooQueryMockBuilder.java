package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public class TimbuctooQueryMockBuilder {
  private Graph db;
  private GraphQuery graphQuery;

  private TimbuctooQueryMockBuilder() {

  }

  public static TimbuctooQueryMockBuilder aQuery() {
    return new TimbuctooQueryMockBuilder();
  }

  public TimbuctooQuery build() {
    TimbuctooQuery query = mock(TimbuctooQuery.class);
    when(query.hasNotNullProperty(anyString(), anyObject())).thenReturn(query);
    when(query.createGraphQuery(db)).thenReturn(graphQuery);
    return query;
  }

  public TimbuctooQueryMockBuilder createsGraphQueryForDB(Graph db, GraphQuery graphQuery) {
    this.db = db;
    this.graphQuery = graphQuery;
    return this;
  }

}
