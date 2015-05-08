package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TinkerPopQueryMockBuilder {
  private TinkerPopQueryMockBuilder() {

  }

  public static TinkerPopQueryMockBuilder aQuery() {
    return new TinkerPopQueryMockBuilder();
  }

  public TinkerPopQuery build() {
    TinkerPopQuery query = mock(TinkerPopQuery.class);
    when(query.hasNotNullProperty(anyString(), anyObject())).thenReturn(query);
    return query;
  }

}
