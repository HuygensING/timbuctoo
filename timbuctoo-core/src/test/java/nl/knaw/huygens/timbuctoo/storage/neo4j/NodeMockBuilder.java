package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.DomainEntity.PID;
import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.helpers.collection.IteratorUtil.asIterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class NodeMockBuilder {
  private final List<Label> labels;
  private Map<RelationshipType, List<Relationship>> outGoingRelationships;
  private Map<RelationshipType, List<Relationship>> incommingRelationships;

  private final Map<String, Object> properties;

  private NodeMockBuilder() {
    labels = Lists.newArrayList();
    incommingRelationships = Maps.newHashMap();
    outGoingRelationships = Maps.newHashMap();
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

  private Collection<Relationship> getIncommingRelationships() {
    return getRelationships(incommingRelationships);
  }

  private Collection<Relationship> getOutgoingRelationships() {
    return getRelationships(outGoingRelationships);
  }

  private Collection<Relationship> getRelationships(Map<RelationshipType, List<Relationship>> relationshipMap) {
    Collection<List<Relationship>> values = relationshipMap.values();
    List<Relationship> relationships = Lists.newArrayList();
    for (List<Relationship> valuesPart : values) {
      relationships.addAll(valuesPart);
    }
    return relationships;
  }

  private List<Relationship> getAllRelationships() {
    ArrayList<Relationship> allRelationships = Lists.newArrayList();
    allRelationships.addAll(getIncommingRelationships());
    allRelationships.addAll(getOutgoingRelationships());
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
    addRelationship(relationship, outGoingRelationships);
  }

  public NodeMockBuilder withIncomingRelationShip(Relationship relationship) {
    addIncommingRelationship(relationship);
    return this;
  }

  public NodeMockBuilder andInComingRelationShip(Relationship relationship) {
    addIncommingRelationship(relationship);
    return this;
  }

  private void addIncommingRelationship(Relationship relationship) {
    addRelationship(relationship, incommingRelationships);

  }

  private void addRelationship(Relationship relationship, Map<RelationshipType, List<Relationship>> relationshipMap) {
    RelationshipType relationshipType = relationship.getType();
    List<Relationship> relationshipsOfType = relationshipMap.get(relationshipType);
    if (relationshipsOfType == null) {
      relationshipsOfType = Lists.newArrayList();
      relationshipMap.put(relationshipType, relationshipsOfType);
    }

    relationshipsOfType.add(relationship);
  }

  public Node build() {
    Node node = mock(Node.class);

    addPropertiesToNode(node, properties);

    when(node.getLabels()).thenReturn(getLabels());
    addRelationships(node);

    return node;
  }

  private void addRelationships(Node node) {
    when(node.getRelationships(Direction.OUTGOING)).thenReturn(asIterable(getOutgoingRelationships().iterator()));
    when(node.getRelationships(Direction.INCOMING)).thenReturn(asIterable(getIncommingRelationships().iterator()));
    when(node.getRelationships()).thenReturn(getAllRelationships());

    hasRelationship(node, Direction.OUTGOING, outGoingRelationships);
    hasRelationship(node, Direction.INCOMING, incommingRelationships);
  }

  private void hasRelationship(Node node, Direction direction, Map<RelationshipType, List<Relationship>> relationshipMap) {
    for (RelationshipType relationshipType : relationshipMap.keySet()) {
      when(node.hasRelationship(direction, relationshipType)).thenReturn(true);
    }
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
