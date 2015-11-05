package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Relation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static nl.knaw.huygens.timbuctoo.model.DomainEntity.DB_PID_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_ID_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_REV_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EdgeMockBuilder {
  private Vertex source;
  private Vertex target;
  private String label;
  private Map<String, Object> properties;
  private List<String> types;

  private EdgeMockBuilder() {
    properties = Maps.newHashMap();
    types = Lists.newArrayList();
  }

  public static EdgeMockBuilder anEdge() {
    return new EdgeMockBuilder();
  }

  public EdgeMockBuilder withRev(int revision) {
    return addProperty(DB_REV_PROP_NAME, revision);
  }

  private EdgeMockBuilder addProperty(String name, Object value) {
    properties.put(name, value);
    return this;
  }

  public Edge build() {
    Edge edge = mock(Edge.class);
    when(edge.getLabel()).thenReturn(label);
    when(edge.getVertex(Direction.OUT)).thenReturn(source);
    when(edge.getVertex(Direction.IN)).thenReturn(target);

    addTypes(edge);
    addProperties(edge);

    return edge;
  }

  private void addTypes(Edge edge) {
    ObjectMapper objectMapper = new ObjectMapper();
    String value;
    try {
      value = objectMapper.writeValueAsString(types);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    when(edge.getProperty(ELEMENT_TYPES)).thenReturn(value);
  }

  private void addProperties(Edge edge) {
    when(edge.getPropertyKeys()).thenReturn(properties.keySet());
    for (Entry<String, Object> entry : properties.entrySet()) {
      when(edge.getProperty(entry.getKey())).thenReturn(entry.getValue());
    }
  }

  public EdgeMockBuilder withSource(Vertex source) {
    this.source = source;
    return this;
  }

  public EdgeMockBuilder withTarget(Vertex target) {
    this.target = target;
    return this;
  }

  public EdgeMockBuilder withLabel(String label) {
    this.label = label;
    return this;
  }

  public EdgeMockBuilder withAPID() {
    return withPID("pid");
  }

  public EdgeMockBuilder withPID(String pid) {
    return this.addProperty(DB_PID_PROP_NAME, pid);
  }

  public EdgeMockBuilder withID(String id) {
    return this.addProperty(DB_ID_PROP_NAME, id);
  }

  public EdgeMockBuilder withType(Class<? extends Relation> type) {

    types.add(TypeNames.getInternalName(type));

    return this;
  }

  public EdgeMockBuilder withTypeId(String relationTypeId) {
    return addProperty(Relation.DB_TYPE_ID_PROP_NAME, relationTypeId);
  }
}
