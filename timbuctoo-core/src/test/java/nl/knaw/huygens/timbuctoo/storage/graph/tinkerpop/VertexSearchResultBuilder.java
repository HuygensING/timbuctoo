package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_DB_PROPERTY_NAME;
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
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Vertex;

public class VertexSearchResultBuilder {
  private Map<String, Object> searchProperties;
  private List<Vertex> vertices;
  private List<String> withoutProperties;

  private VertexSearchResultBuilder() {
    withoutProperties = Lists.newArrayList();
    searchProperties = Maps.newHashMap();
    vertices = Lists.newArrayList();
  }

  public static VertexSearchResultBuilder aVertexSearchResult() {
    return new VertexSearchResultBuilder();
  }

  public static VertexSearchResultBuilder anEmptyVertexSearchResult() {
    return new VertexSearchResultBuilder();
  }

  public VertexSearchResultBuilder containsVertex(Vertex vertex) {
    return addVertex(vertex);
  }

  public VertexSearchResultBuilder andVertex(Vertex vertex) {
    return addVertex(vertex);
  }

  private VertexSearchResultBuilder addVertex(Vertex vertex) {
    vertices.add(vertex);
    return this;
  }

  public VertexSearchResultBuilder forProperty(String propertyName, Object value) {
    searchProperties.put(propertyName, value);
    return this;
  }

  public VertexSearchResultBuilder forId(Object value) {
    return this.forProperty(ID_DB_PROPERTY_NAME, value);

  }

  public VertexSearchResultBuilder forType(Class<? extends Entity> type) {
    return this.forProperty(ELEMENT_TYPES, TypeNames.getInternalName(type));
  }

  public VertexSearchResultBuilder forRevision(int value) {
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

    for (String property : withoutProperties) {
      when(graphQueryMock.hasNot(property)).thenReturn(graphQueryMock);
    }

    when(graphQueryMock.vertices()).thenReturn(vertices);

  }

  public VertexSearchResultBuilder withoutProperty(String propertyName) {

    withoutProperties.add(propertyName);

    return this;
  }

  public void foundByGraphQuery(GraphQuery query) {
    when(query.vertices()).thenReturn(vertices);
  }
}
