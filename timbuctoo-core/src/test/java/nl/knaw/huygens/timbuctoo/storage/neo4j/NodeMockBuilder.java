package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.DomainEntity.PID;
import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.helpers.collection.IteratorUtil.asIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class NodeMockBuilder {
  private final List<Label> labels;
  private List<Relationship> outGoingRelationships;
  private List<Relationship> incommingRelationships;

  private final Map<String, Object> properties;

  private NodeMockBuilder() {
    labels = Lists.newArrayList();
    incommingRelationships = Lists.newArrayList();
    outGoingRelationships = Lists.newArrayList();
    properties = Maps.newHashMap();
  }

  public static NodeMockBuilder aNode() {
    return new NodeMockBuilder();
  }

  public NodeMockBuilder withLabel(Label label) {
    labels.add(label);
    return this;
  }

  public NodeMockBuilder withId(String id) {
    addProperty(ID_PROPERTY_NAME, id);
    return this;
  }

  public NodeMockBuilder withAPID() {
    this.addProperty(PID, "pid");
    return this;
  }

  public NodeMockBuilder withRevision(int revision) {
    this.addProperty(REVISION_PROPERTY_NAME, revision);
    return this;
  }

  private void addProperty(String propertyName, Object value) {
    this.properties.put(propertyName, value);
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

  public Node build() {
    Node node = mock(Node.class);

    addPropertiesToNode(node, properties);

    when(node.getLabels()).thenReturn(asIterable(getLabels().iterator()));
    when(node.getRelationships(Direction.OUTGOING)).thenReturn(asIterable(getOutgoingRelationships().iterator()));
    when(node.getRelationships(Direction.INCOMING)).thenReturn(asIterable(getIncommingRelationships().iterator()));
    when(node.getRelationships()).thenReturn(getAllRelationships());

    return node;
  }

  private void addPropertiesToNode(Node node, Map<String, Object> properties) {
    when(node.getPropertyKeys()).thenReturn(asIterable(properties.keySet().iterator()));
    for (Entry<String, Object> entry : properties.entrySet()) {
      String key = entry.getKey();

      when(node.getProperty(key)).thenReturn(entry.getValue());
      when(node.hasProperty(key)).thenReturn(true);
    }

  }

  public Node createdBy(GraphDatabaseService dbMock) {
    Node node = this.build();
    when(dbMock.createNode()).thenReturn(node);
    return node;
  }

}
