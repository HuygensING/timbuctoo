package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.RelationMatcher.likeRelation;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.PropertyConverterMockBuilder.newPropertyConverter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.neo4j.graphdb.DynamicLabel.label;

import java.util.ArrayList;
import java.util.Iterator;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.IteratorUtil;

import com.google.common.collect.Lists;

public class ExtendableRelationshipConverterTest {

  private static final String OTHER_FIELD_CONVERTER = "otherFieldConverter";
  private static final String FIELD_CONVERTER = "fieldConverter";
  private static final String FIELD_TO_IGNORE2 = "fieldToIgnore2";
  private static final String FIELD_TO_IGNORE1 = "fieldToIgnore1";
  private static final ArrayList<String> FIELD_TO_IGNORE = Lists.newArrayList(FIELD_TO_IGNORE1, FIELD_TO_IGNORE2);
  private PropertyConverter propertyConverterMockToIgnore1;
  private PropertyConverter propertyConverterMockToIgnore2;
  private PropertyConverter propertyConverterMock;
  private PropertyConverter someOtherPropertyConverterMock;
  private Relationship relationshipMock;
  private Relation relation;
  private ExtendableRelationshipConverter<Relation> instance;

  @Before
  public void setUp() {
    propertyConverterMockToIgnore1 = newPropertyConverter().withName(FIELD_TO_IGNORE1).build();
    propertyConverterMockToIgnore2 = newPropertyConverter().withName(FIELD_TO_IGNORE2).build();
    propertyConverterMock = newPropertyConverter().withName(FIELD_CONVERTER).withType(FieldType.ADMINISTRATIVE).build();
    someOtherPropertyConverterMock = newPropertyConverter().withName(OTHER_FIELD_CONVERTER).withType(FieldType.REGULAR).build();

    instance = new ExtendableRelationshipConverter<Relation>(FIELD_TO_IGNORE);
    instance.addPropertyConverter(someOtherPropertyConverterMock);
    instance.addPropertyConverter(propertyConverterMock);
    instance.addPropertyConverter(propertyConverterMockToIgnore2);
    instance.addPropertyConverter(propertyConverterMockToIgnore1);

    relationshipMock = mock(Relationship.class);
    relation = new Relation();
  }

  @Test
  public void addValuesToPropertyContainerCallsAllTheFieldConvertersThatAreNotOnTheIgnoreList() throws Exception {
    // action
    instance.addValuesToPropertyContainer(relationshipMock, relation);

    // verify
    verify(propertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(someOtherPropertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(propertyConverterMockToIgnore1, never()).setPropertyContainerProperty(relationshipMock, relation);
    verify(propertyConverterMockToIgnore2, never()).setPropertyContainerProperty(relationshipMock, relation);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToPropertyContainerThrowsAConversionExceptionIfOneOfTheFieldMappersDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(propertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);

    try {
      // action
      instance.addValuesToPropertyContainer(relationshipMock, relation);
    } finally {
      // verify
      verify(propertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    }
  }

  @Test
  public void addValuesToEntityCallsAllTheFieldConvertersThatAreNotOnTheIgnoredListAndSetsFieldsOfRelationThatArePackedInTheStartAndEndNode() throws Exception {
    // setup
    String sourceId = "sourceId";
    String sourceType = "sourceType";
    Node startNode = mock(Node.class);
    nodeHasIdProperty(startNode, sourceId);
    nodeHasLabels(startNode, sourceType, "otherSourceType");

    String targetId = "targetId";
    String targetType = "targetType";
    Node endNode = mock(Node.class);
    nodeHasLabels(endNode, targetType, "otherTargetType");
    nodeHasIdProperty(endNode, targetId);

    when(relationshipMock.getStartNode()).thenReturn(startNode);
    when(relationshipMock.getEndNode()).thenReturn(endNode);

    // action
    instance.addValuesToEntity(relation, relationshipMock);

    // verify
    assertThat(relation, likeRelation() //
        .withSourceId(sourceId) //
        .withSourceType(sourceType) //
        .withTargetId(targetId) //
        .withTargetType(targetType));

    verify(propertyConverterMock).addValueToEntity(relation, relationshipMock);
    verify(someOtherPropertyConverterMock).addValueToEntity(relation, relationshipMock);
    verify(propertyConverterMockToIgnore1, never()).addValueToEntity(relation, relationshipMock);
    verify(propertyConverterMockToIgnore2, never()).addValueToEntity(relation, relationshipMock);
  }

  private void nodeHasLabels(Node node, String label1, String label2) {
    Iterator<Label> iterator = Lists.newArrayList(label(label2), label(label1)).iterator();
    when(node.getLabels()).thenReturn(IteratorUtil.asIterable(iterator));
  }

  private void nodeHasIdProperty(Node node, String propertyValue) {
    when(node.hasProperty(Entity.ID_PROPERTY_NAME)).thenReturn(true);
    when(node.getProperty(Entity.ID_PROPERTY_NAME)).thenReturn(propertyValue);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToEntityThrowsAConversionExceptionIfOneOfTheFieldMappersDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(propertyConverterMock).addValueToEntity(relation, relationshipMock);

    try {
      // action
      instance.addValuesToEntity(relation, relationshipMock);
    } finally {
      // verify
      verify(propertyConverterMock).addValueToEntity(relation, relationshipMock);;
    }
  }
}
