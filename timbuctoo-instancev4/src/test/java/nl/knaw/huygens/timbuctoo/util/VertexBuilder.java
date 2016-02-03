package nl.knaw.huygens.timbuctoo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VertexBuilder {

  private final HashMap<String, Object> properties;
  private final HashMap<String, List<String>> outGoingRelationMap;
  private List<String> types;
  private String id;
  private final ObjectMapper objectMapper;
  private boolean isLatest;
  private final HashMap<String, List<String>> incomingRelationMap;

  VertexBuilder() {
    objectMapper = new ObjectMapper();
    properties = Maps.newHashMap();
    outGoingRelationMap = Maps.newHashMap();
    incomingRelationMap = Maps.newHashMap();
  }

  public Vertex build(Vertex vertex) {
    try {
      if (types != null) {
        vertex.property("types", objectMapper.writeValueAsString(Lists.newArrayList(types)));
      }
      if (id != null) {
        vertex.property("tim_id", id);
      }
      vertex.property("isLatest", isLatest);

      for (Map.Entry<String, Object> entry : properties.entrySet()) {
        vertex.property(entry.getKey(), entry.getValue());
      }

      return vertex;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public void setRelations(Vertex self, Map<String, Vertex> others) {
    for (Map.Entry<String, List<String>> entry : outGoingRelationMap.entrySet()) {
      for (String vertexLookup : entry.getValue()) {
        Vertex other = others.get(vertexLookup);
        self.addEdge(entry.getKey(), other);
      }
    }
    for (Map.Entry<String, List<String>> entry : incomingRelationMap.entrySet()) {
      for (String vertexLookup : entry.getValue()) {
        Vertex other = others.get(vertexLookup);
        other.addEdge(entry.getKey(), self);
      }
    }
  }

  public VertexBuilder withType(String type) {
    if (types == null) {
      types = Lists.newArrayList();
    }
    this.types.add(type);
    return this;
  }

  public VertexBuilder withTimId(String id) {
    this.id = id;
    return this;
  }

  public VertexBuilder isLatest(boolean isLatest) {
    this.isLatest = isLatest;
    return this;
  }

  public VertexBuilder withProperty(String name, boolean value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, byte value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, short value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, int value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, long value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, float value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, double value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, char value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, String value) {
    properties.put(name, value);
    return this;
  }


  public VertexBuilder withProperty(String name, boolean[] value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, byte[] value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, short[] value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, int[] value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, long[] value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, float[] value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, double[] value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, char[] value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withProperty(String name, String[] value) {
    properties.put(name, value);
    return this;
  }

  public VertexBuilder withOutgoingRelation(String relationName, String otherVertex) {
    if (!outGoingRelationMap.containsKey(relationName)) {
      outGoingRelationMap.put(relationName, Lists.newArrayList());
    }

    outGoingRelationMap.get(relationName).add(otherVertex);

    return this;
  }


  public VertexBuilder withIncomingRelation(String relationName, String otherVertex) {
    if (!incomingRelationMap.containsKey(relationName)) {
      incomingRelationMap.put(relationName, Lists.newArrayList());
    }

    incomingRelationMap.get(relationName).add(otherVertex);

    return this;
  }
}
