package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.helpers.collection.IteratorUtil.asIterable;

import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;

public class NodeMockBuilder {
  private final List<Label> labels;
  private String id;
  private List<Relationship> relationships;
  private int revision = 0;

  private NodeMockBuilder() {
    labels = Lists.newArrayList();
    relationships = Lists.newArrayList();

  }

  public static NodeMockBuilder aNode() {
    return new NodeMockBuilder();
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
    Node node = mock(Node.class);
    when(node.getProperty(ID_PROPERTY_NAME)).thenReturn(id);
    when(node.getProperty(REVISION_PROPERTY_NAME)).thenReturn(revision);
    when(node.getLabels()).thenReturn(asIterable(labels.iterator()));
    when(node.getRelationships()).thenReturn(asIterable(relationships.iterator()));

    return node;
  }

  public List<Label> getLabels() {
    return labels;
  }

  public String getId() {
    return id;
  }

  public NodeMockBuilder withARelationShip(Relationship relationship) {
    addRelationship(relationship);
    return this;
  }

  public NodeMockBuilder andRelationship(Relationship relationship) {
    addRelationship(relationship);
    return this;

  }

  public NodeMockBuilder withRevision(int revision) {
    this.revision = revision;
    return this;
  }

  private void addRelationship(Relationship relationship) {
    relationships.add(relationship);
  }

  public Node createdBy(GraphDatabaseService dbMock) {
    Node node = this.build();
    when(dbMock.createNode()).thenReturn(node);
    return node;
  }

}
