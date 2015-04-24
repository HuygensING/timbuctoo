package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tinkerpop.blueprints.Edge;

public class EdgeMockBuilder {
  private int revision;

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

    when(edge.getProperty(REVISION_PROPERTY_NAME)).thenReturn(revision);

    return edge;
  }
}
