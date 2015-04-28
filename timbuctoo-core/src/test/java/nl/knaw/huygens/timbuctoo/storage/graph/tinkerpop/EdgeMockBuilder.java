package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.DomainEntity.PID;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class EdgeMockBuilder {
  private int revision;
  private Vertex source;
  private Vertex target;
  private String label;
  private String pid;

  private EdgeMockBuilder() {}

  public static EdgeMockBuilder anEdge() {
    return new EdgeMockBuilder();
  }

  public EdgeMockBuilder withRev(int revision) {
    this.revision = revision;
    return this;
  }

  public Edge build() {
    Edge edge = mock(Edge.class);

    when(edge.getLabel()).thenReturn(label);
    when(edge.getProperty(REVISION_PROPERTY_NAME)).thenReturn(revision);
    when(edge.getProperty(PID)).thenReturn(pid);
    when(edge.getVertex(Direction.OUT)).thenReturn(source);
    when(edge.getVertex(Direction.IN)).thenReturn(target);

    return edge;
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
    this.pid = "pid";
    return this;
  }
}
