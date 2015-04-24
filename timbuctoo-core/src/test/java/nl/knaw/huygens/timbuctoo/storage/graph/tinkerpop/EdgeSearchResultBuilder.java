package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Predicate;

public class EdgeSearchResultBuilder {
  private Map<String, Object> searchProperties;
  private List<Edge> edges;

  private EdgeSearchResultBuilder() {
    searchProperties = Maps.newHashMap();
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
    return this.forProperty(ID_PROPERTY_NAME, value);

  }

  public EdgeSearchResultBuilder forType(Class<? extends Entity> type) {
    return this.forProperty(ELEMENT_TYPES, TypeNames.getInternalName(type));
  }

  public EdgeSearchResultBuilder forRevision(int value) {
    this.forProperty(REVISION_PROPERTY_NAME, value);
    return this;
  }

  public void foundInDatabase(Graph db) {
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

    when(graphQueryMock.edges()).thenReturn(edges);

  }
}
