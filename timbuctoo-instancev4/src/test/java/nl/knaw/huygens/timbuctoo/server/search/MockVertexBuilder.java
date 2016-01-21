package nl.knaw.huygens.timbuctoo.server.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.server.search.WwPersonSearchDescription.ID_DB_PROP;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockVertexBuilder {

  private final Vertex vertex;
  private final ObjectMapper objectMapper;
  private final Set<String> keys;
  private final HashMap<String, List<Vertex>> outGoingRelationMap;

  private MockVertexBuilder() {
    objectMapper = new ObjectMapper();
    vertex = mock(Vertex.class);
    keys = Sets.newHashSet();
    outGoingRelationMap = Maps.newHashMap();
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
    return this;
  }

  public MockVertexBuilder withProperty(String key, Object value) {
    try {
      return withProperty(key, objectMapper.writeValueAsString(value));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public MockVertexBuilder withId(String id) {
    return withProperty(ID_DB_PROP, id);
  }

  public Vertex build() {
    when(vertex.keys()).thenReturn(keys);

    when(vertex.vertices(any(Direction.class), anyString())).thenReturn(Lists.<Vertex>newArrayList().iterator());

    for (Map.Entry<String, List<Vertex>> entry : outGoingRelationMap.entrySet()) {
      when(vertex.vertices(Direction.OUT, entry.getKey())).thenReturn(entry.getValue().iterator());
    }
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
