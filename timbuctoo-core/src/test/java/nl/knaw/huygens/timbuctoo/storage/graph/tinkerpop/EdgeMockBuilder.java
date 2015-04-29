package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.DomainEntity.PID;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class EdgeMockBuilder {
  private Vertex source;
  private Vertex target;
  private String label;
  private Map<String, Object> properties;

  private EdgeMockBuilder() {
    properties = Maps.newHashMap();
  }

  public static EdgeMockBuilder anEdge() {
    return new EdgeMockBuilder();
  }

  public EdgeMockBuilder withRev(int revision) {
    return addProperty(REVISION_PROPERTY_NAME, revision);
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

    addProperties(edge);

    return edge;
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
    return this.addProperty(PID, pid);
  }
}
