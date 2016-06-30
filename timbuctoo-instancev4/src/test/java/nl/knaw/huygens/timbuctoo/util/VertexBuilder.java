package nl.knaw.huygens.timbuctoo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static nl.knaw.huygens.timbuctoo.util.RelationData.RelationDataBuilder.makeRelationData;

public class VertexBuilder {

  private final HashMap<String, Object> properties;
  private final HashMap<String, List<RelationData>> outGoingRelationMap;
  private String type;
  private List<String> vres;
  private String id;
  private final ObjectMapper objectMapper;
  private boolean isLatest;
  private final HashMap<String, List<RelationData>> incomingRelationMap;
  private List<String> labels;

  VertexBuilder() {
    objectMapper = new ObjectMapper();
    properties = Maps.newHashMap();
    outGoingRelationMap = Maps.newHashMap();
    incomingRelationMap = Maps.newHashMap();
    labels = Lists.newArrayList();
  }

  public Vertex build(Vertex vertex) {
    try {
      if (vres == null) {
        vres = Lists.newArrayList("");
      }
      if (type == null) {
        type = "<type>";
      }
      vertex.property("types", objectMapper.writeValueAsString(
        Lists.newArrayList(vres.stream().map(vre -> vre + type).iterator()))
      );
      if (id != null) {
        vertex.property("tim_id", id);
      }
      vertex.property("isLatest", isLatest);

      for (Map.Entry<String, Object> entry : properties.entrySet()) {
        vertex.property(entry.getKey(), entry.getValue());
      }

      for (String label : labels) {
        ((Neo4jVertex) vertex).addLabel(label);
      }

      return vertex;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public void setRelations(Vertex self, Map<String, Vertex> others) {
    for (Map.Entry<String, List<RelationData>> entry : outGoingRelationMap.entrySet()) {
      for (RelationData data : entry.getValue()) {
        Vertex other = others.get(data.getOtherKey());
        if (other == null) {
          throw new RuntimeException(
            data.getOtherKey() +
              " is not available as a named vertex. (Available vertices are: " +
              String.join(", ", others.keySet())
          );
        }
        Edge edge = self.addEdge(entry.getKey(), other);
        data.setProperties(edge, vres);
      }
    }
    for (Map.Entry<String, List<RelationData>> entry : incomingRelationMap.entrySet()) {
      for (RelationData data : entry.getValue()) {
        Vertex other = others.get(data.getOtherKey());
        if (other == null) {
          throw new RuntimeException(
            data.getOtherKey() +
              " is not available as a named vertex. (Available vertices are: " +
              String.join(", ", others.keySet())
          );
        }
        Edge edge = other.addEdge(entry.getKey(), self);
        data.setProperties(edge, vres);
      }
    }
  }

  public VertexBuilder withType(String type) {
    this.type = type;
    return this;
  }

  public VertexBuilder withVre(String vre) {
    if (vres == null) {
      vres = Lists.newArrayList();
    }
    this.vres.add(vre);
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

    outGoingRelationMap.get(relationName).add(new RelationData(otherVertex));

    return this;
  }


  public VertexBuilder withOutgoingRelation(String relationName, String otherVertex,
                                            Function<RelationData.RelationDataBuilder,
                                              RelationData.RelationDataBuilder> relationBuilder) {
    if (!outGoingRelationMap.containsKey(relationName)) {
      outGoingRelationMap.put(relationName, Lists.newArrayList());
    }

    outGoingRelationMap.get(relationName).add(relationBuilder.apply(makeRelationData(otherVertex)).build());

    return this;
  }

  public VertexBuilder withIncomingRelation(String relationName, String otherVertex) {
    if (!incomingRelationMap.containsKey(relationName)) {
      incomingRelationMap.put(relationName, Lists.newArrayList());
    }

    incomingRelationMap.get(relationName).add(new RelationData(otherVertex));

    return this;
  }


  public VertexBuilder withIncomingRelation(String relationName, String otherVertex,
                                            Function<RelationData.RelationDataBuilder,
                                              RelationData.RelationDataBuilder> relationBuilder) {
    if (!incomingRelationMap.containsKey(relationName)) {
      incomingRelationMap.put(relationName, Lists.newArrayList());
    }

    incomingRelationMap.get(relationName).add(relationBuilder.apply(makeRelationData(otherVertex)).build());

    return this;
  }

  public VertexBuilder withLabel(String label) {
    labels.add(label);

    return this;
  }

  public List<String> getLabels() {
    return labels;
  }
}
