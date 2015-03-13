package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IteratorUtil;

import com.google.common.collect.Lists;

public class NodeMockBuilder {
  private final List<Label> labels;
  private Node node;
  private String id;

  private NodeMockBuilder(Node node) {
    this.node = node;
    labels = Lists.newArrayList();

  }

  public static NodeMockBuilder aNode() {
    return new NodeMockBuilder(null);
  }

  public static NodeMockBuilder node(Node node) {
    return new NodeMockBuilder(node);
  }

  public NodeMockBuilder withLabel(Label label) {
    labels.add(label);
    return this;
  }

  public NodeMockBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public Node build() {
    if (node == null) {
      node = mock(Node.class);
    }
    when(node.getProperty(Entity.ID_PROPERTY_NAME)).thenReturn(id);
    when(node.getLabels()).thenReturn(IteratorUtil.asIterable(labels.iterator()));

    return node;
  }

  public List<Label> getLabels() {
    return labels;
  }

  public String getId() {
    return id;
  }
}
