package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Predicate;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static nl.knaw.huygens.timbuctoo.model.Entity.DB_ID_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_REV_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.IS_LATEST;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EdgeSearchResultBuilder {
  private Map<String, Object> searchProperties;
  private List<Edge> edges;
  private List<String> withoutProperties;

  private EdgeSearchResultBuilder() {
    searchProperties = Maps.newHashMap();
    withoutProperties = Lists.newArrayList();
    edges = Lists.newArrayList();
  }

  public static EdgeSearchResultBuilder anEdgeSearchResult() {
    return new EdgeSearchResultBuilder();
  }

  public static EdgeSearchResultBuilder anEmptyEdgeSearchResult() {
    return new EdgeSearchResultBuilder();
  }

  public EdgeSearchResultBuilder containsEdge(Edge edge) {
    return addEdge(edge);
  }

  public EdgeSearchResultBuilder andEdge(Edge edge) {
    return addEdge(edge);
  }

  private EdgeSearchResultBuilder addEdge(Edge edge) {
    edges.add(edge);
    return this;
  }

  public EdgeSearchResultBuilder forProperty(String propertyName, Object value) {
    searchProperties.put(propertyName, value);
    return this;
  }

  public EdgeSearchResultBuilder forId(Object value) {
    return this.forProperty(DB_ID_PROP_NAME, value);

  }

  public EdgeSearchResultBuilder forType(Class<? extends Entity> type) {
    return this.forProperty(ELEMENT_TYPES, TypeNames.getInternalName(type));
  }

  public EdgeSearchResultBuilder forRevision(int value) {
    this.forProperty(DB_REV_PROP_NAME, value);
    return this;
  }

  public QueryVerifier foundInDatabase(Graph db) {
    GraphQuery graphQueryMock = mock(GraphQuery.class);
    when(db.query()).thenReturn(graphQueryMock);

    for (Entry<String, Object> searchProperty : searchProperties.entrySet()) {
      String key = searchProperty.getKey();
      Object value = searchProperty.getValue();
      if (ELEMENT_TYPES.equals(key)) {
        when(graphQueryMock.has(argThat(is(key)), any(Predicate.class), argThat(is(value)))).thenReturn(graphQueryMock);
      } else {
        when(graphQueryMock.has(key, value)).thenReturn(graphQueryMock);
      }

    }

    for (String withoutProperty : withoutProperties) {
      when(graphQueryMock.hasNot(withoutProperty)).thenReturn(graphQueryMock);
    }

    foundByGraphQuery(graphQueryMock);

    return new QueryVerifier(graphQueryMock);
  }

  public EdgeSearchResultBuilder withoutProperty(String propertyName) {
    withoutProperties.add(propertyName);

    return this;
  }

  public void foundByGraphQuery(GraphQuery graphQuery) {
    when(graphQuery.edges()).thenReturn(edges);
  }

  public EdgeSearchResultBuilder forLatest() {
    return this.forProperty(IS_LATEST, true);
  }

  public class QueryVerifier {
    private final GraphQuery graphQueryMock;

    public QueryVerifier(GraphQuery graphQueryMock) {
      this.graphQueryMock = graphQueryMock;
    }

    public void verify() {
      for (Entry<String, Object> searchProperty : searchProperties.entrySet()) {
        String key = searchProperty.getKey();
        Object value = searchProperty.getValue();
        if (ELEMENT_TYPES.equals(key)) {
          Mockito.verify(graphQueryMock).has(argThat(is(key)), any(Predicate.class), argThat(is(value)));
        } else {
          Mockito.verify(graphQueryMock).has(key, value);
        }
      }

      for (String property : withoutProperties) {
        when(graphQueryMock.hasNot(property)).thenReturn(graphQueryMock);
      }
    }
  }
}
