package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Vertex;
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
    return this.forProperty(DB_ID_PROP_NAME, value);

  }

  public VertexSearchResultBuilder forType(Class<? extends Entity> type) {
    return this.forProperty(ELEMENT_TYPES, TypeNames.getInternalName(type));
  }

  public VertexSearchResultBuilder forRevision(int value) {
    this.forProperty(DB_REV_PROP_NAME, value);
    return this;
  }

  public VertexSearchResultBuilder forLatest() {
    return this.forProperty(IS_LATEST, true);
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

    for (String property : withoutProperties) {
      when(graphQueryMock.hasNot(property)).thenReturn(graphQueryMock);
    }

    when(graphQueryMock.vertices()).thenReturn(vertices);

    return new QueryVerifier(graphQueryMock);
  }


  public VertexSearchResultBuilder withoutProperty(String propertyName) {

    withoutProperties.add(propertyName);

    return this;
  }

  public void foundByGraphQuery(GraphQuery query) {
    when(query.vertices()).thenReturn(vertices);
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
