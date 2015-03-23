package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion;

import static nl.knaw.huygens.timbuctoo.storage.RelationMatcher.likeRelation;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyConverterMockBuilder.newPropertyConverter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.neo4j.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.EntityInstantiator;

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

public class ExtendableRelationshipConverterTest {

  private static final Class<Relation> TYPE = Relation.class;
  private static final String OTHER_FIELD_CONVERTER = "otherFieldConverter";
  private static final String FIELD_CONVERTER = "fieldConverter";
  private PropertyConverter adminPropertyConverterMock;
  private PropertyConverter regularPropertyConverterMock;
  private Relationship relationshipMock;
  private Relation relation;
  private ExtendableRelationshipConverter<Relation> instance;
  private TypeRegistry typeRegistry;
  private EntityInstantiator entityInstantiatorMock;

  @Before
  public void setUp() throws Exception {
    entityInstantiatorMock = mock(EntityInstantiator.class);

    typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init(getPackageName(SubADomainEntity.class) + " " + getPackageName(BaseDomainEntity.class) + " " + getPackageName(Person.class));

    adminPropertyConverterMock = newPropertyConverter().withName(FIELD_CONVERTER).withType(FieldType.ADMINISTRATIVE).build();
    regularPropertyConverterMock = newPropertyConverter().withName(OTHER_FIELD_CONVERTER).withType(FieldType.REGULAR).build();

    instance = new ExtendableRelationshipConverter<Relation>(TYPE, typeRegistry, entityInstantiatorMock);
    instance.addPropertyConverter(regularPropertyConverterMock);
    instance.addPropertyConverter(adminPropertyConverterMock);

    relationshipMock = mock(Relationship.class);
    relation = new Relation();
  }

  private String getPackageName(Class<? extends DomainEntity> type) {
    return type.getPackage().getName();
  }

  @Test
  public void addValuesToPropertyContainerCallsAllTheFieldConverters() throws Exception {
    // action
    instance.addValuesToPropertyContainer(relationshipMock, relation);

    // verify
    verify(adminPropertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(regularPropertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);

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
  public void addValuesToEntityCallsAllThePropertyConvertersSetsFieldsOfRelationThatArePackedInTheStartAndEndNode() throws Exception {
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

  @Test(expected = CorruptNodeException.class)
  public void addValuesToEntityThrowsACorruptNodeExceptionWhenTheSourceNodeHasNoLabelWithThePrimitiveName() throws Exception {
    // setup
    String sourceId = "sourceId";
    Node startNodeWithoutLabel = aNode().withId(sourceId)//
        .withLabel(getLabelOfType(ProjectADomainEntity.class))//
        .build();

    Label endNodeBaseTypeLabel = getLabelOfType(Person.class);
    String targetId = "targetId";
    Node endNode = aNode().withId(targetId) //
        .withLabel(getLabelOfType(ProjectAPerson.class)) //
        .withLabel(endNodeBaseTypeLabel)//
        .build();

    when(relationshipMock.getStartNode()).thenReturn(startNodeWithoutLabel);
    when(relationshipMock.getEndNode()).thenReturn(endNode);

    // action
    instance.addValuesToEntity(relation, relationshipMock);

  }

  @Test(expected = CorruptNodeException.class)
  public void addValuesToEntityThrowsACorruptNodeExceptionWhenTheTargetNodeHasNoLabelWithThePrimitiveName() throws Exception {
    // setup
    Label startNodeBaseTypeLabel = getLabelOfType(BaseDomainEntity.class);
    String sourceId = "sourceId";
    Node startNode = aNode().withId(sourceId)//
        .withLabel(getLabelOfType(ProjectADomainEntity.class))//
        .withLabel(startNodeBaseTypeLabel)//
        .build();

    String targetId = "targetId";
    Node endNodeWithoutLabel = aNode().withId(targetId) //
        .withLabel(getLabelOfType(ProjectAPerson.class)) //
        .build();

    when(relationshipMock.getStartNode()).thenReturn(startNode);
    when(relationshipMock.getEndNode()).thenReturn(endNodeWithoutLabel);

    // action
    instance.addValuesToEntity(relation, relationshipMock);

  }

  @Test
  public void updatePropertyContainerUpdatesTheNonAdministrativePropertiesOfTheRelationship() throws Exception {
    // action
    instance.updatePropertyContainer(relationshipMock, relation);

    // verify
    verify(regularPropertyConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(adminPropertyConverterMock, never()).setPropertyContainerProperty(relationshipMock, relation);
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
  }

  @Test
  public void convertToEntityCreatesANewInstanceOfTheTypeAndLetThePropertyConvertersAddTheirValuesToIt() throws Exception {
    // setup
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(relation);

    // setup
    Label startNodeBaseTypeLabel = getLabelOfType(BaseDomainEntity.class);
    String sourceId = "sourceId";
    Node startNode = aNode().withId(sourceId)//
        .withLabel(getLabelOfType(ProjectADomainEntity.class))//
        .withLabel(startNodeBaseTypeLabel)//
        .build();

    Label endNodeBaseTypeLabel = getLabelOfType(Person.class);
    String targetId = "targetId";
    Node endNode = aNode().withId(targetId) //
        .withLabel(getLabelOfType(ProjectAPerson.class)) //
        .withLabel(endNodeBaseTypeLabel)//
        .build();

    when(relationshipMock.getStartNode()).thenReturn(startNode);
    when(relationshipMock.getEndNode()).thenReturn(endNode);

    // action
    Relation actualRelation = instance.convertToEntity(relationshipMock);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(adminPropertyConverterMock).addValueToEntity(relation, relationshipMock);
    verify(regularPropertyConverterMock).addValueToEntity(relation, relationshipMock);
  }

  @Test(expected = InstantiationException.class)
  public void convertToEntityThrowsAnInstantionExceptionWhenTheEntityCannotBeInstatiated() throws Exception {
    // setup
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenThrow(new InstantiationException());

    // action
    instance.convertToEntity(relationshipMock);

  }

  @Test(expected = ConversionException.class)
  public void convertToEntityThrowsAConversionExceptionWhenOneOfTheValuesCannotBeConverted() throws Exception {
    // setup
    doThrow(ConversionException.class).when(adminPropertyConverterMock).addValueToEntity(relation, relationshipMock);
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(relation);

    // action
    instance.convertToEntity(relationshipMock);
  }
}
