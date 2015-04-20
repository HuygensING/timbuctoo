package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.RelationshipMockBuilder.aRelationship;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.RelationshipDuplicator;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class RelationshipDuplicatorTest {

  private RelationshipDuplicator instance;
  private RelationshipType relationshipType;
  private Node startNode;
  private Node endNode;

  @Before
  public void setup() {
    instance = new RelationshipDuplicator(mock(GraphDatabaseService.class));
    startNode = aNode().build();
    endNode = aNode().build();
    relationshipType = DynamicRelationshipType.withName("relationshipType");

  }

  @Test
  public void saveDuplicateLetsTheStartNodeCreateANewRelationshipWithTheSameEndNodeAndSameType() {
    Relationship relationship = aRelationship()//
        .withType(relationshipType)//
        .withStartNode(startNode)//
        .withEndNode(endNode)//
        .build();

    instance.saveDuplicate(relationship);

    // verify
    verify(startNode).createRelationshipTo(endNode, relationshipType);
  }

  @Test
  public void saveDuplicateCopiesAllThePropertiesOfTheOriginalRelationshipToTheDuplicate() {
    String propertyName1 = "propertyName1";
    String propertyName2 = "propertyName2";
    String propertyName3 = "propertyName3";

    String propertyValue1 = "propertyValue1";
    String propertyValue2 = "propertyValue2";
    String propertyValue3 = "propertyValue3";

    Relationship relationship = aRelationship()//
        .withType(relationshipType)//
        .withStartNode(startNode)//
        .withEndNode(endNode)//
        .withProperty(propertyName1, propertyValue1)//
        .withProperty(propertyName2, propertyValue2)//
        .withProperty(propertyName3, propertyValue3)//
        .build();

    Relationship duplicate = aRelationship().build();
    when(startNode.createRelationshipTo(endNode, relationshipType)).thenReturn(duplicate);

    // action
    instance.saveDuplicate(relationship);

    // verify
    verify(duplicate).setProperty(propertyName1, propertyValue1);
    verify(duplicate).setProperty(propertyName2, propertyValue2);
    verify(duplicate).setProperty(propertyName3, propertyValue3);
  }
}
