package nl.knaw.huygens.timbuctoo.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VertexBuilder {

  private final HashMap<String, Object> properties;
  private final HashMap<String, List<Vertex>> outGoingRelationMap;
  private List<String> types;
  private String id;
  private final ObjectMapper objectMapper;
  private boolean isLatest;

  private VertexBuilder() {
    types = Lists.newArrayList();
    objectMapper = new ObjectMapper();
    properties = Maps.newHashMap();
    outGoingRelationMap = Maps.newHashMap();

  }

  public static VertexBuilder vertex() {
    return new VertexBuilder();
  }

  public Vertex build(Graph graph) {
    try {
      Vertex vertex = graph.addVertex();
      vertex.property("types", objectMapper.writeValueAsString(Lists.newArrayList(types)));
      vertex.property("tim_id", id);
      vertex.property("isLatest", isLatest);

      for (Map.Entry<String, Object> entry : properties.entrySet()) {
        vertex.property(entry.getKey(), entry.getValue());
      }

      for (Map.Entry<String, List<Vertex>> entry : outGoingRelationMap.entrySet()) {
        for (Vertex vertex1 : entry.getValue()) {
          vertex.addEdge(entry.getKey(), vertex1);
        }
      }
      return vertex;
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

  public VertexBuilder isLatest(boolean isLatest) {
    this.isLatest = isLatest;
    return this;
  }

  public VertexBuilder withProperty(String name, Object value) {
    try {
      properties.put(name, objectMapper.writeValueAsString(value));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public VertexBuilder withOutgoingRelation(String relationName, Vertex otherVertex) {
    if (!outGoingRelationMap.containsKey(relationName)) {
      outGoingRelationMap.put(relationName, Lists.newArrayList());
    }

    outGoingRelationMap.get(relationName).add(otherVertex);

    return this;
  }
}
