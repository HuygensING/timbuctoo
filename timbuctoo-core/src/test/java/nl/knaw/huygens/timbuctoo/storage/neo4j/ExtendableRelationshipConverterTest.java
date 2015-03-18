package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.RelationMatcher.likeRelation;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.PropertyConverterMockBuilder.newPropertyConverter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import test.model.BaseDomainEntity;
import test.model.projecta.ProjectAPerson;
import test.model.projecta.SubADomainEntity;
import test.variation.model.projecta.ProjectADomainEntity;

import com.google.common.collect.Lists;

public class ExtendableRelationshipConverterTest {

  private static final String OTHER_FIELD_CONVERTER = "otherFieldConverter";
  private static final String FIELD_CONVERTER = "fieldConverter";
  private static final String FIELD_TO_IGNORE2 = "fieldToIgnore2";
  private static final String FIELD_TO_IGNORE1 = "fieldToIgnore1";
  private static final ArrayList<String> FIELDS_TO_IGNORE = Lists.newArrayList(FIELD_TO_IGNORE1, FIELD_TO_IGNORE2);
  private PropertyConverter propertyConverterMockToIgnore1;
  private PropertyConverter propertyConverterMockToIgnore2;
  private PropertyConverter adminPropertyConverterMock;
  private PropertyConverter regularPropertyConverterMock;
  private Relationship relationshipMock;
  private Relation relation;
  private ExtendableRelationshipConverter<Relation> instance;
  private TypeRegistry typeRegistry;

  @Before
  public void setUp() throws Exception {
    typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init(SubADomainEntity.class.getPackage().getName() + " " + BaseDomainEntity.class.getPackage().getName() + " " + Person.class.getPackage().getName());

    propertyConverterMockToIgnore1 = newPropertyConverter().withName(FIELD_TO_IGNORE1).build();
    propertyConverterMockToIgnore2 = newPropertyConverter().withName(FIELD_TO_IGNORE2).build();
    adminPropertyConverterMock = newPropertyConverter().withName(FIELD_CONVERTER).withType(FieldType.ADMINISTRATIVE).build();
    regularPropertyConverterMock = newPropertyConverter().withName(OTHER_FIELD_CONVERTER).withType(FieldType.REGULAR).build();

    instance = new ExtendableRelationshipConverter<Relation>(typeRegistry, FIELDS_TO_IGNORE);
    instance.addPropertyConverter(regularPropertyConverterMock);
    instance.addPropertyConverter(adminPropertyConverterMock);
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
    verify(adminPropertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(regularPropertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(propertyConverterMockToIgnore1, never()).setPropertyContainerProperty(relationshipMock, relation);
    verify(propertyConverterMockToIgnore2, never()).setPropertyContainerProperty(relationshipMock, relation);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToPropertyContainerThrowsAConversionExceptionIfOneOfTheFieldMappersDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(adminPropertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);

    try {
      // action
      instance.addValuesToPropertyContainer(relationshipMock, relation);
    } finally {
      // verify
      verify(adminPropertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    }
  }

  @Test
  public void addValuesToEntityCallsAllTheFieldConvertersThatAreNotOnTheIgnoredListAndSetsFieldsOfRelationThatArePackedInTheStartAndEndNode() throws Exception {
    // setup
    Label startNodeBaseTypeLabel = getLabelOfType(BaseDomainEntity.class);
    String sourceId = "sourceId";
    String sourceType = startNodeBaseTypeLabel.name();
    Node startNode = aNode().withId(sourceId)//
        .withLabel(getLabelOfType(ProjectADomainEntity.class))//
        .withLabel(startNodeBaseTypeLabel)//
        .build();

    Label endNodeBaseTypeLabel = getLabelOfType(Person.class);
    String targetId = "targetId";
    String targetType = endNodeBaseTypeLabel.name();
    Node endNode = aNode().withId(targetId) //
        .withLabel(getLabelOfType(ProjectAPerson.class)) //
        .withLabel(endNodeBaseTypeLabel)//
        .build();

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

    verify(adminPropertyConverterMock).addValueToEntity(relation, relationshipMock);
    verify(regularPropertyConverterMock).addValueToEntity(relation, relationshipMock);
    verify(propertyConverterMockToIgnore1, never()).addValueToEntity(relation, relationshipMock);
    verify(propertyConverterMockToIgnore2, never()).addValueToEntity(relation, relationshipMock);
  }

  private Label getLabelOfType(Class<? extends DomainEntity> type) {
    return DynamicLabel.label(TypeNames.getInternalName(type));
  }

  @Test(expected = ConversionException.class)
  public void addValuesToEntityThrowsAConversionExceptionIfOneOfTheFieldMappersDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(adminPropertyConverterMock).addValueToEntity(relation, relationshipMock);

    try {
      // action
      instance.addValuesToEntity(relation, relationshipMock);
    } finally {
      // verify
      verify(adminPropertyConverterMock).addValueToEntity(relation, relationshipMock);;
    }
  }

  @Test
  public void updatePropertyContainerUpdatesTheNonAdministrativePropertiesOfTheRelationship() throws Exception {
    // action
    instance.updatePropertyContainer(relationshipMock, relation);

    // verify
    verify(regularPropertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);

    verify(adminPropertyConverterMock, never()).setPropertyContainerProperty(relationshipMock, relation);
    verify(propertyConverterMockToIgnore1, never()).setPropertyContainerProperty(relationshipMock, relation);
    verify(propertyConverterMockToIgnore2, never()).setPropertyContainerProperty(relationshipMock, relation);
  }

  @Test(expected = ConversionException.class)
  public void updatePropertyContainerThrowAConversionExceptionWhenOneOfThePropertyConvertersDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(regularPropertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);

    try {
      // action
      instance.updatePropertyContainer(relationshipMock, relation);
    } finally {
      // verify
      verify(regularPropertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    }
  }

  @Test
  public void updateModifiedAndRevLetTheFieldConvertersSetTheValuesForRevisionAndModified() throws Exception {
    // setup
    PropertyConverter modifiedConverterMock = newPropertyConverter().withName(Entity.MODIFIED_PROPERTY_NAME).withType(FieldType.ADMINISTRATIVE).build();
    PropertyConverter revConverterMock = newPropertyConverter().withName(Entity.REVISION_PROPERTY_NAME).withType(FieldType.ADMINISTRATIVE).build();

    instance.addPropertyConverter(modifiedConverterMock);
    instance.addPropertyConverter(revConverterMock);

    // action
    instance.updateModifiedAndRev(relationshipMock, relation);

    // verify
    verify(modifiedConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(revConverterMock).setPropertyContainerProperty(relationshipMock, relation);

    verify(adminPropertyConverterMock, never()).setPropertyContainerProperty(relationshipMock, relation);
    verify(regularPropertyConverterMock, never()).setPropertyContainerProperty(relationshipMock, relation);
    verify(propertyConverterMockToIgnore1, never()).setPropertyContainerProperty(relationshipMock, relation);
    verify(propertyConverterMockToIgnore2, never()).setPropertyContainerProperty(relationshipMock, relation);
  }
}
