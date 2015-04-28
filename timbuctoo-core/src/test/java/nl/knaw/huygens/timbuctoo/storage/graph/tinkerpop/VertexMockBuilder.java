package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class VertexMockBuilder {
  private Map<String, List<Edge>> incomingEdges;
  private Map<String, List<Edge>> outgoingEdges;
  private Map<String, Object> properties;
  private List<String> types;

  private VertexMockBuilder() {
    outgoingEdges = Maps.newHashMap();
    incomingEdges = Maps.newHashMap();
    properties = Maps.newHashMap();
    types = Lists.newArrayList();
  }

  public static VertexMockBuilder aVertex() {
    return new VertexMockBuilder();
  }

  public VertexMockBuilder withIncomingEdgeWithLabel(String label, Edge edge) {
    addEdge(edge, label, incomingEdges);
    return this;
  }

  public VertexMockBuilder withIncomingEdgeWithLabel(SystemRelationType label) {
    addEdge(mock(Edge.class), label.name(), incomingEdges);
    return this;
  }

  private void addEdge(Edge edge, String label, Map<String, List<Edge>> edgeCollection) {
    List<Edge> edges = edgeCollection.get(label);
    if (edges == null) {
      edges = Lists.newArrayList();
      edgeCollection.put(label, edges);
    }

    edges.add(edge);
  }

  public VertexMockBuilder withOutgoingEdgeWithLabel(String label, Edge edge) {
    addEdge(edge, label, outgoingEdges);
    return this;
  }

  public Vertex build() {
    Vertex vertex = mock(Vertex.class);

    addOutGoingEdges(vertex);
    addIncomingEdges(vertex);
    addProperties(vertex);

    when(vertex.getProperty(ELEMENT_TYPES)).thenReturn(types.toArray(new String[types.size()]));

    return vertex;
  }

  private void addProperties(Vertex vertex) {
    when(vertex.getPropertyKeys()).thenReturn(properties.keySet());

    for (Entry<String, Object> property : properties.entrySet()) {
      when(vertex.getProperty(property.getKey())).thenReturn(property.getValue());
    }
  }

  private void addIncomingEdges(Vertex vertex) {
    when(vertex.getEdges(IN)).thenReturn(getAllEdges(incomingEdges));

    for (Entry<String, List<Edge>> entry : incomingEdges.entrySet()) {
      when(vertex.getEdges(IN, entry.getKey())).thenReturn(entry.getValue());
    }
  }

  private void addOutGoingEdges(Vertex vertex) {
    when(vertex.getEdges(OUT)).thenReturn(getAllEdges(outgoingEdges));

    for (Entry<String, List<Edge>> entry : outgoingEdges.entrySet()) {
      when(vertex.getEdges(OUT, entry.getKey())).thenReturn(entry.getValue());
    }
  }

  private Collection<Edge> getAllEdges(Map<String, List<Edge>> edgeMap) {
    List<Edge> allEdges = Lists.newArrayList();

    for (List<Edge> edges : edgeMap.values()) {
      allEdges.addAll(edges);
    }

    return allEdges;
  }

  public VertexMockBuilder withRev(int value) {
    addProperty(REVISION_PROPERTY_NAME, value);
    return this;
  }

  private void addProperty(String name, Object value) {
    properties.put(name, value);
  }

  public VertexMockBuilder withId(String id) {
    addProperty(ID_PROPERTY_NAME, id);
    return this;
  }

  public VertexMockBuilder withType(Class<? extends Entity> type) {
    types.add(TypeNames.getInternalName(type));
    return this;
  }

}
