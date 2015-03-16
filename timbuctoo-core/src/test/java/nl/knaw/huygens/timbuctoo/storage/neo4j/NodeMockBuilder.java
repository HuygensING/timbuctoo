package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.helpers.collection.IteratorUtil.asIterable;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;

public class NodeMockBuilder {
  private final List<Label> labels;
  private List<Relationship> outGoingRelationships;
  private List<String> propertyKeys;
  private String id;
  private int revision = 0;
  private List<Relationship> incommingRelationships;

  private NodeMockBuilder() {
    labels = Lists.newArrayList();
    incommingRelationships = Lists.newArrayList();
    outGoingRelationships = Lists.newArrayList();
    propertyKeys = Lists.newArrayList();
  }

  public static NodeMockBuilder aNode() {
    return new NodeMockBuilder();
  }

  public NodeMockBuilder withLabel(Label label) {
    labels.add(label);
    return this;
  }

  public NodeMockBuilder withId(String id) {
    propertyKeys.add(ID_PROPERTY_NAME);
    this.id = id;
    return this;
  }

  public Node build() {
    Node node = mock(Node.class);
    when(node.getProperty(ID_PROPERTY_NAME)).thenReturn(id);
    when(node.getProperty(REVISION_PROPERTY_NAME)).thenReturn(revision);
    when(node.getLabels()).thenReturn(asIterable(getLabels().iterator()));
    when(node.getRelationships(Direction.OUTGOING)).thenReturn(asIterable(getOutgoingRelationships().iterator()));
    when(node.getRelationships(Direction.INCOMING)).thenReturn(asIterable(getIncommingRelationships().iterator()));
    when(node.getRelationships()).thenReturn(getAllRelationships());
    when(node.getPropertyKeys()).thenReturn(asIterable(propertyKeys.iterator()));

    return node;
  }

  private List<Relationship> getIncommingRelationships() {
    return incommingRelationships;
  }

  private List<Relationship> getOutgoingRelationships() {
    return outGoingRelationships;
  }

  private List<Relationship> getAllRelationships() {
    ArrayList<Relationship> allRelationships = Lists.newArrayList();
    allRelationships.addAll(incommingRelationships);
    allRelationships.addAll(outGoingRelationships);
    return allRelationships;
  }

  private List<Label> getLabels() {
    return labels;
  }

  public NodeMockBuilder withOutgoingRelationShip(Relationship relationship) {
    addOutGoingRelationship(relationship);
    return this;
  }

  public NodeMockBuilder andOutgoingRelationship(Relationship relationship) {
    addOutGoingRelationship(relationship);
    return this;
  }

  private void addOutGoingRelationship(Relationship relationship) {
    getOutgoingRelationships().add(relationship);
  }

  public NodeMockBuilder withIncommingRelationShip(Relationship relationship) {
    addIncommingRelationship(relationship);
    return this;
  }

  public NodeMockBuilder andInCommingRelationShip(Relationship relationship) {
    addIncommingRelationship(relationship);
    return this;
  }

  private void addIncommingRelationship(Relationship relationship) {

    getIncommingRelationships().add(relationship);
  }

  public NodeMockBuilder withRevision(int revision) {
    propertyKeys.add(REVISION_PROPERTY_NAME);
    this.revision = revision;
    return this;
  }

  public Node createdBy(GraphDatabaseService dbMock) {
    Node node = this.build();
    when(dbMock.createNode()).thenReturn(node);
    return node;
  }

}
