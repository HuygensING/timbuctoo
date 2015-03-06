package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.RelationMatcher.likeRelation;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.FieldConverterMockBuilder.newFieldConverter;
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

public class RelationshipConverterTest {

  private static final String OTHER_FIELD_CONVERTER = "otherFieldConverter";
  private static final String FIELD_CONVERTER = "fieldConverter";
  private static final String FIELD_TO_IGNORE2 = "fieldToIgnore2";
  private static final String FIELD_TO_IGNORE1 = "fieldToIgnore1";
  private static final ArrayList<String> FIELD_TO_IGNORE = Lists.newArrayList(FIELD_TO_IGNORE1, FIELD_TO_IGNORE2);
  private FieldConverter fieldConverterMockToIgnore1;
  private FieldConverter fieldConverterMockToIgnore2;
  private FieldConverter fieldConverterMock;
  private FieldConverter someOtherFieldConverterMock;
  private Relationship relationshipMock;
  private Relation relation;
  private RelationshipConverter<Relation, Relationship> instance;

  @Before
  public void setUp() {
    fieldConverterMockToIgnore1 = newFieldConverter().withName(FIELD_TO_IGNORE1).build();
    fieldConverterMockToIgnore2 = newFieldConverter().withName(FIELD_TO_IGNORE2).build();
    fieldConverterMock = newFieldConverter().withName(FIELD_CONVERTER).withType(FieldType.ADMINISTRATIVE).build();
    someOtherFieldConverterMock = newFieldConverter().withName(OTHER_FIELD_CONVERTER).withType(FieldType.REGULAR).build();

    instance = new RelationshipConverter<Relation, Relationship>(FIELD_TO_IGNORE);
    instance.addFieldConverter(someOtherFieldConverterMock);
    instance.addFieldConverter(fieldConverterMock);
    instance.addFieldConverter(fieldConverterMockToIgnore2);
    instance.addFieldConverter(fieldConverterMockToIgnore1);

    relationshipMock = mock(Relationship.class);
    relation = new Relation();
  }

  @Test
  public void addValuesToPropertyContainerCallsAllTheFieldConvertersThatAreNotOnTheIgnoreList() throws Exception {
    // action
    instance.addValuesToPropertyContainer(relationshipMock, relation);

    // verify
    verify(fieldConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(someOtherFieldConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(fieldConverterMockToIgnore1, never()).setPropertyContainerProperty(relationshipMock, relation);
    verify(fieldConverterMockToIgnore2, never()).setPropertyContainerProperty(relationshipMock, relation);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToPropertyContainerThrowsAConversionExceptionIfOneOfTheFieldMappersDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(fieldConverterMock).setPropertyContainerProperty(relationshipMock, relation);

    try {
      // action
      instance.addValuesToPropertyContainer(relationshipMock, relation);
    } finally {
      // verify
      verify(fieldConverterMock).setPropertyContainerProperty(relationshipMock, relation);
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

    verify(fieldConverterMock).addValueToEntity(relation, relationshipMock);
    verify(someOtherFieldConverterMock).addValueToEntity(relation, relationshipMock);
    verify(fieldConverterMockToIgnore1, never()).addValueToEntity(relation, relationshipMock);
    verify(fieldConverterMockToIgnore2, never()).addValueToEntity(relation, relationshipMock);
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
    doThrow(ConversionException.class).when(fieldConverterMock).addValueToEntity(relation, relationshipMock);

    try {
      // action
      instance.addValuesToEntity(relation, relationshipMock);
    } finally {
      // verify
      verify(fieldConverterMock).addValueToEntity(relation, relationshipMock);;
    }
  }
}
