package nl.knaw.huygens.timbuctoo.server.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

public class VertexBuilder {

  private List<String> types;
  private String id;
  private final ObjectMapper objectMapper;

  private VertexBuilder() {
    types = Lists.newArrayList();
    objectMapper = new ObjectMapper();
  }

  public static VertexBuilder vertex() {
    return new VertexBuilder();
  }

  public void build(Graph graph) {
    try {
      Vertex vertex = graph.addVertex();
      vertex.property("types", objectMapper.writeValueAsString(Lists.newArrayList(types)));
      vertex.property("tim_id", id);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public VertexBuilder withType(String type) {
    this.types.add(type);
    return this;
  }

  public VertexBuilder withId(String id) {
    this.id = id;
    return this;
  }
}
