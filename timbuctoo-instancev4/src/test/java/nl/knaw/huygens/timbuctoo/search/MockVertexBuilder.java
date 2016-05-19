package nl.knaw.huygens.timbuctoo.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.search.SearchDescription.ID_DB_PROP;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockVertexBuilder {

  private final Vertex vertex;
  private final ObjectMapper objectMapper;
  private final Set<String> keys;
  private final Map<String, List<Vertex>> outGoingRelationMap;

  private MockVertexBuilder() {
    objectMapper = new ObjectMapper();
    vertex = mock(Vertex.class);
    keys = Sets.newHashSet();
    outGoingRelationMap = Maps.newHashMap();
    when(vertex.property(anyString())).thenReturn(mock(VertexProperty.class));
  }

  public static MockVertexBuilder vertex() {
    return new MockVertexBuilder();
  }

  public static MockVertexBuilder vertexWithId(String id) {
    return vertex().withId(id);
  }

  public MockVertexBuilder withProperty(String key, String value) {
    keys.add(key);
    when(vertex.value(key)).thenReturn(value);

    VertexProperty property = createVertexProperty(value);
    when(vertex.property(key)).thenReturn(property);

    return this;
  }

  public MockVertexBuilder withProperty(String key, Object value) {
    try {
      return withProperty(key, objectMapper.writeValueAsString(value));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private VertexProperty createVertexProperty(String value) {
    VertexProperty property = mock(VertexProperty.class);
    when(property.isPresent()).thenReturn(true);
    when(property.value()).thenReturn(value);
    return property;
  }


  public MockVertexBuilder withId(String id) {
    return withProperty(ID_DB_PROP, id);
  }

  public Vertex build() {
    when(vertex.keys()).thenReturn(keys);

    when(vertex.vertices(any(Direction.class), anyString())).thenReturn(Lists.<Vertex>newArrayList().iterator());

    // Problem: with thenReturn iterator() is invoked at creation time,
    // fixed by using thenAnswer() because iterator() is now invoked on vertices
    // Problem2: vertices accepts a vararg, to support the vararg the vertices of all the relations should be combined
    when(vertex.vertices(argThat(is(Direction.OUT)), anyVararg())).thenAnswer(invocationOnMock -> {
      List<Object> vertices = Lists.newArrayList();
      boolean isFirst = true;

      for (Object argument : invocationOnMock.getArguments()) {
        if (isFirst) {
          isFirst = false;
          continue;
        }
        if (outGoingRelationMap.containsKey(argument)) {
          vertices.addAll(outGoingRelationMap.get(argument));
        }
      }

      return vertices.iterator();
    });

    return vertex;
  }

  public MockVertexBuilder withOutgoingRelation(String relationName, Vertex otherVertex) {
    if (!outGoingRelationMap.containsKey(relationName)) {
      outGoingRelationMap.put(relationName, Lists.newArrayList());
    }

    outGoingRelationMap.get(relationName).add(otherVertex);

    return this;
  }
}
