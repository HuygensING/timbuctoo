package nl.knaw.huygens.timbuctoo.server.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Set;

import static nl.knaw.huygens.timbuctoo.server.rest.WwPersonSearchDescription.ID_DB_PROP;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockVertexBuilder {

  private final Vertex vertex;
  private final ObjectMapper objectMapper;
  private final Set<String> keys;

  private MockVertexBuilder() {
    objectMapper = new ObjectMapper();
    vertex = mock(Vertex.class);
    keys = Sets.newHashSet();
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

  public static MockVertexBuilder vertex() {
    return new MockVertexBuilder();
  }

  public Vertex build() {
    when(vertex.keys()).thenReturn(keys);
    return vertex;
  }
}
