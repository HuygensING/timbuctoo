package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.tinkerpop.blueprints.Direction.BOTH;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_ID_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_REV_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.IS_LATEST;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

  public VertexMockBuilder withOutgoingEdgeWithLabel(SystemRelationType label) {
    addEdge(mock(Edge.class), label.name(), outgoingEdges);
    return this;
  }

  public Vertex build() {
    Vertex vertex = mock(Vertex.class);

    addOutGoingEdges(vertex);
    addIncomingEdges(vertex);
    addEdges(vertex);
    addProperties(vertex);
    addTypes(vertex);

    return vertex;
  }

  private void addTypes(Vertex vertex) {
    ObjectMapper objectMapper = new ObjectMapper();
    String value;
    try {
      value = objectMapper.writeValueAsString(types);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    when(vertex.getProperty(ELEMENT_TYPES)).thenReturn(value);
  }

  private void addProperties(Vertex vertex) {
    when(vertex.getPropertyKeys()).thenReturn(properties.keySet());

    for (Entry<String, Object> property : properties.entrySet()) {
      when(vertex.getProperty(property.getKey())).thenReturn(property.getValue());
    }
  }

  private void addEdges(Vertex vertex) {
    List<Edge> allEdges = Lists.newArrayList();
    allEdges.addAll(getAllEdges(incomingEdges));
    allEdges.addAll(getAllEdges(outgoingEdges));

    when(vertex.getEdges(BOTH)).thenReturn(allEdges);
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
    addProperty(DB_REV_PROP_NAME, value);
    return this;
  }

  public VertexMockBuilder isLatest() {
    addProperty(IS_LATEST, true);
    return this;
  }

  private void addProperty(String name, Object value) {
    properties.put(name, value);
  }

  public VertexMockBuilder withId(String id) {
    addProperty(DB_ID_PROP_NAME, id);
    return this;
  }

  public VertexMockBuilder withType(Class<? extends Entity> type) {
    types.add(TypeNames.getInternalName(type));
    return this;
  }

  public VertexMockBuilder withIncomingEdge(Edge edge) {
    addEdge(edge, edge.getLabel(), incomingEdges);
    return this;
  }

  public VertexMockBuilder withOutgoingEdge(Edge edge) {
    addEdge(edge, edge.getLabel(), outgoingEdges);
    return this;
  }


}
