package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.DomainEntityMatcher.likeDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.Neo4JStorage.RELATIONSHIP_ID_INDEX;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipIndexMockBuilder.aRelationshipIndexForName;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipTypeMatcher.likeRelationshipType;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.aSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.anEmptySearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubARelationBuilder.aRelation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;

import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.RelationshipIndex;

import test.model.projecta.SubARelation;

public class Neo4JStorageRelationTest extends Neo4JStorageTest {
  private static final Class<Relationship> RELATIONSHIP_TYPE = Relationship.class;
  private static final String RELATION_TYPE_ID = "typeId";
  private static final String RELATION_TARGET_ID = "targetId";
  private static final String RELATION_SOURCE_ID = "sourceId";
  private static final Class<RelationType> RELATIONTYPE_TYPE = RelationType.class;
  private static final String RELATION_TYPE_NAME = TypeNames.getInternalName(RELATIONTYPE_TYPE);
  private static final Label RELATION_TYPE_LABEL = DynamicLabel.label(RELATION_TYPE_NAME);
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;

  @Test
  public void addDomainEntityForRelationAddsARelationshipToTheSourceAndReturnsTheId() throws Exception {
    // setup
    String name = "regularTypeName";

    Node sourceNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(sourceNodeMock) //
        .foundInDB(dbMock);

    Node targetNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID) //
        .withNode(targetNodeMock) //
        .foundInDB(dbMock);

    relationTypeWithRegularNameExists(name);

    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).foundInDB(dbMock);
    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerFactoryHasCompositeRelationshipConverterFor(Neo4JStorageRelationTest.RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(Neo4JStorageRelationTest.RELATION_TYPE)).thenReturn(ID);
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID) //
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTargetId(RELATION_TARGET_ID) //
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTypeId(RELATION_TYPE_ID) //
        .withTypeType(RELATION_TYPE_NAME).build();

    // action
    String id = instance.addDomainEntity(SubARelation.class, relation, new Change());
    // verify
    assertThat(id, is(equalTo(ID)));

    InOrder inOrder = inOrder(dbMock, sourceNodeMock, relationConverterMock, indexMock, transactionMock);

    inOrder.verify(dbMock).beginTx();
    inOrder.verify(sourceNodeMock).createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)));

    inOrder.verify(relationConverterMock).addValuesToPropertyContainer( //
        argThat(equalTo(relationShipMock)), //
        argThat(likeDomainEntity(RELATION_TYPE) //
            .withId(ID) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));

    inOrder.verify(indexMock).add(relationShipMock, ID_PROPERTY_NAME, id);
    inOrder.verify(transactionMock).success();
  }

  private NodeConverter<RelationType> relationTypeWithRegularNameExists(String name) throws Exception {
    Node relationTypeNodeMock = aNode().build();
    aSearchResult().forLabel(RELATION_TYPE_LABEL).andId(RELATION_TYPE_ID) //
        .withNode(relationTypeNodeMock) //
        .foundInDB(dbMock);

    NodeConverter<RelationType> relationTypeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(RELATIONTYPE_TYPE);
    RelationType relationType = new RelationType();
    relationType.setRegularName(name);
    when(relationTypeConverter.convertToEntity(relationTypeNodeMock)).thenReturn(relationType);

    return relationTypeConverter;
  }

  private <T extends Relation> RelationshipConverter<T> propertyContainerFactoryHasCompositeRelationshipConverterFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    RelationshipConverter<T> relationshipConverter = mock(RelationshipConverter.class);
    when(propertyContainerConverterFactoryMock.createCompositeForRelation(type)).thenReturn(relationshipConverter);

    return relationshipConverter;
  }

  private <T extends Relation> RelationshipConverter<T> propertyContainerConverterFactoryHasRelationshipConverterFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    RelationshipConverter<T> relationshipConverter = mock(RelationshipConverter.class);
    when(propertyContainerConverterFactoryMock.createForRelation(type)).thenReturn(relationshipConverter);

    return relationshipConverter;
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityForRelationThrowsAConversionExceptionWhenTheRelationshipConverterDoes() throws Exception {
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID) //
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTargetId(RELATION_TARGET_ID) //
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTypeId(RELATION_TYPE_ID) //
        .withTypeType(RELATION_TYPE_NAME).build();
    String name = "regularTypeName";

    Node sourceNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(sourceNodeMock) //
        .foundInDB(dbMock);
    Node targetNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID) //
        .withNode(targetNodeMock) //
        .foundInDB(dbMock);

    relationTypeWithRegularNameExists(name);

    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerFactoryHasCompositeRelationshipConverterFor(Neo4JStorageRelationTest.RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(Neo4JStorageRelationTest.RELATION_TYPE)).thenReturn(ID);
    doThrow(ConversionException.class).when(relationConverterMock).addValuesToPropertyContainer(relationShipMock, relation);

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(sourceNodeMock).createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)));

      verify(relationConverterMock).addValuesToPropertyContainer( //
          argThat(equalTo(relationShipMock)), //
          argThat(likeDomainEntity(Neo4JStorageRelationTest.RELATION_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verifyTransactionFailed();
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityForRelationThrowsAStorageExceptionWhenTheRelationTypeCannotBeInstantiated() throws Exception {
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID) //
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTargetId(RELATION_TARGET_ID) //
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTypeId(RELATION_TYPE_ID) //
        .withTypeType(RELATION_TYPE_NAME).build();
    String name = "regularTypeName";

    Node sourceNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(sourceNodeMock) //
        .foundInDB(dbMock);
    Node targetNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID) //
        .withNode(targetNodeMock) //
        .foundInDB(dbMock);

    NodeConverter<RelationType> relationTypeConverter = relationTypeWithRegularNameExists(name);

    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(Neo4JStorageRelationTest.RELATION_TYPE)).thenReturn(ID);
    when(relationTypeConverter.convertToEntity(any(Node.class))).thenThrow(new InstantiationException());

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verifyTransactionFailed();
    }
  }

  @Test(expected = ConversionException.class)
  public void addDomainEntityForRelationThrowsAConversionExceptionWhenTheRelationCannotBeConverted() throws Exception {
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID) //
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTargetId(RELATION_TARGET_ID) //
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTypeId(RELATION_TYPE_ID) //
        .withTypeType(RELATION_TYPE_NAME).build();
    String name = "regularTypeName";

    Node sourceNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(sourceNodeMock) //
        .foundInDB(dbMock);

    Node targetNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID) //
        .withNode(targetNodeMock) //
        .foundInDB(dbMock);

    NodeConverter<RelationType> relationTypeConverter = relationTypeWithRegularNameExists(name);

    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerFactoryHasCompositeRelationshipConverterFor(Neo4JStorageRelationTest.RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(Neo4JStorageRelationTest.RELATION_TYPE)).thenReturn(ID);
    when(relationTypeConverter.convertToEntity(any(Node.class))).thenThrow(new ConversionException());

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verifyTransactionFailed();
      verifyZeroInteractions(relationConverterMock, sourceNodeMock);
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityForRelationThrowsAStorageExceptionWhenTheSourceCannotBeFound() throws Exception {
    // setup
    anEmptySearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID).foundInDB(dbMock);

    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
      verifyTransactionFailed();
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityForRelationThrowsAStorageExceptionWhenTheTargetCannotBeFound() throws Exception {
    // setup
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(aNode().build())//
        .foundInDB(dbMock);

    anEmptySearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID).foundInDB(dbMock);

    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_TARGET_ID);
      verifyTransactionFailed();
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityForRelationThrowsAStorageExceptionWhenRelationTypeCannotBeFound() throws Exception {

    // setup
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(aNode().build()) //
        .foundInDB(dbMock);
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID) //
        .withNode(aNode().build()) //
        .foundInDB(dbMock);

    anEmptySearchResult().forLabel(RELATION_TYPE_LABEL).andId(RELATION_TYPE_ID).foundInDB(dbMock);
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID) //
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTargetId(RELATION_TARGET_ID) //
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTypeId(RELATION_TYPE_ID) //
        .withTypeType(RELATION_TYPE_NAME).build();

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_TARGET_ID);
      verify(dbMock).findNodesByLabelAndProperty(RELATION_TYPE_LABEL, ID_PROPERTY_NAME, RELATION_TYPE_ID);
      verifyTransactionFailed();
    }
  }

  @Test
  public void getEntityForRelationReturnsTheRelationThatBelongsToTheId() throws Exception {
    // setup
    Relationship relationshipMock = aRelationship().build();
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX) //
        .containsForId(ID) //
        .relationship(relationshipMock) //
        .foundInDB(dbMock);
    SubARelation relation = new SubARelation();

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(relationConverterMock.convertToEntity(relationshipMock)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).convertToEntity(relationshipMock);
    verifyTransActionSucceeded();
  }

  @Test
  public void getEntityForRelationReturnsTheLatestIfMultipleAreFound() throws Exception {
    // setup
    Relationship relationshipFirstRevision = aRelationship().withRevision(FIRST_REVISION).build();
    Relationship relationshipSecondRevision = aRelationship().withRevision(SECOND_REVISION).build();
    Relationship relationshipThirdRevision = aRelationship().withRevision(THIRD_REVISION).build();
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationshipFirstRevision) //
        .andRelationship(relationshipThirdRevision) //
        .andRelationship(relationshipSecondRevision) //
        .foundInDB(dbMock);
    SubARelation relation = new SubARelation();

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(Neo4JStorageRelationTest.RELATION_TYPE);
    when(relationConverterMock.convertToEntity(relationshipThirdRevision)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getEntity(Neo4JStorageRelationTest.RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).convertToEntity(relationshipThirdRevision);
    verifyTransActionSucceeded();
  }

  @Test
  public void getEntityForRelationReturnsNullIfTheRelationIsNotFound() throws Exception {
    // setup
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsNothingForId(ID)//
        .foundInDB(dbMock);

    // action
    SubARelation actualRelation = instance.getEntity(Neo4JStorageRelationTest.RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(nullValue()));

    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verifyZeroInteractions(propertyContainerConverterFactoryMock);
    verifyTransActionSucceeded();
  }

  @Test(expected = ConversionException.class)
  public void getEntityForRelationThrowsAConversionExceptionWhenTheRelationConverterDoes() throws Exception {
    // setup
    Relationship relationshipMock = aRelationship().build();
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID)//
        .relationship(relationshipMock)//
        .foundInDB(dbMock);
    SubARelation relation = new SubARelation();

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(Neo4JStorageRelationTest.RELATION_TYPE);
    doThrow(ConversionException.class).when(relationConverterMock).convertToEntity(relationshipMock);

    // action
    SubARelation actualRelation = instance.getEntity(Neo4JStorageRelationTest.RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).convertToEntity(relationshipMock);
    verifyTransactionFailed();
  }

  @Test(expected = StorageException.class)
  public void getEntityForRelationThrowsStorageExceptionWhenRelationshipConverterThrowsAnInstantiationException() throws Exception {
    // setup
    Relationship relationshipMock = aRelationship().build();
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID)//
        .relationship(relationshipMock)//
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(Neo4JStorageRelationTest.RELATION_TYPE);
    doThrow(InstantiationException.class).when(relationConverterMock).convertToEntity(relationshipMock);

    try {
      // action
      instance.getEntity(Neo4JStorageRelationTest.RELATION_TYPE, ID);
    } finally {
      // verify
      verify(indexMock).get(ID_PROPERTY_NAME, ID);
      verify(relationConverterMock).convertToEntity(relationshipMock);
      verifyTransactionFailed();
    }
  }

  @Test
  public void updateDomainEntityRetrievesTheRelationAndUpdateItsValuesAndAdministrativeValues() throws Exception {
    // setup
    Relationship relationship = aRelationship().withRevision(FIRST_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    Change oldModified = CHANGE;
    SubARelation relation = aRelation()//
        .withId(ID) //
        .withRevision(FIRST_REVISION) //
        .withModified(oldModified) //
        .build();

    RelationshipConverter<SubARelation> converterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);

    // action
    instance.updateDomainEntity(RELATION_TYPE, relation, CHANGE);

    // verify
    InOrder inOrder = inOrder(dbMock, transactionMock, converterMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(converterMock).updatePropertyContainer( //
        argThat(equalTo(relationship)), //
        argThat(likeDomainEntity(RELATION_TYPE) //
            .withId(ID) //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)));
    inOrder.verify(converterMock).updateModifiedAndRev( //
        argThat(equalTo(relationship)), //
        argThat(likeDomainEntity(RELATION_TYPE) //
            .withId(ID) //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)));
    inOrder.verify(transactionMock).success();
  }

  @Test
  public void updateDomainEntityRemovesThePIDOfTheRelationBeforeTheUpdate() throws Exception {
    // setup
    Relationship relationship = aRelationship().withRevision(FIRST_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    Change oldModified = CHANGE;
    SubARelation relation = aRelation()//
        .withId(ID) //
        .withRevision(FIRST_REVISION) //
        .withModified(oldModified) //
        .withAPID() //
        .build();

    RelationshipConverter<SubARelation> converterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);

    // action
    instance.updateDomainEntity(RELATION_TYPE, relation, CHANGE);

    // verify
    verify(converterMock).updatePropertyContainer( //
        argThat(equalTo(relationship)), //
        argThat(likeDomainEntity(RELATION_TYPE).withoutAPID()));
    verify(converterMock).updateModifiedAndRev( //
        argThat(equalTo(relationship)), //
        argThat(likeDomainEntity(RELATION_TYPE).withoutAPID()));
  }

  @Test
  public void updateDomainEntityUpdatesTheLatestIfMultipleAreFound() throws Exception {
    // setup
    Relationship relationshipWithHighestRev = aRelationship().withRevision(SECOND_REVISION).build();
    Relationship otherRelationShip = aRelationship().withRevision(FIRST_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationshipWithHighestRev) //
        .andRelationship(otherRelationShip) //
        .foundInDB(dbMock);

    SubARelation relation = aRelation().withId(ID).withRevision(SECOND_REVISION).build();

    RelationshipConverter<SubARelation> converterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);

    // action
    instance.updateDomainEntity(RELATION_TYPE, relation, CHANGE);

    // verify
    verify(converterMock).updatePropertyContainer(relationshipWithHighestRev, relation);
    verify(converterMock).updateModifiedAndRev(relationshipWithHighestRev, relation);
  }

  @Test(expected = UpdateException.class)
  public void updateDomainEntityThrowsAnUpdateExceptionWhenTheRelationshipToUpdateCannotBeFound() throws Exception {
    // setup
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsNothingForId(ID).foundInDB(dbMock);
    SubARelation relation = aRelation()//
        .withId(ID)//
        .build();
    try {
      // action
      instance.updateDomainEntity(RELATION_TYPE, relation, CHANGE);
    } finally {
      // verify
      verify(dbMock).beginTx();

      verifyTransactionFailed();
    }
  }

  @Test(expected = UpdateException.class)
  public void updateDomainEntityThrowsAnUpdateExceptionWhenRevOfTheRelationshipIsHigherThanThatOfTheEntity() throws Exception {
    // setup
    Relationship relationshipWithHigherRev = aRelationship().withRevision(SECOND_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID) //
        .relationship(relationshipWithHigherRev) //
        .foundInDB(dbMock);

    SubARelation relation = aRelation()//
        .withId(ID)//
        .withRevision(FIRST_REVISION) //
        .build();

    try {
      // action
      instance.updateDomainEntity(RELATION_TYPE, relation, CHANGE);
    } finally {
      // verify
      verify(dbMock).beginTx();

      verifyTransactionFailed();
    }
  }

  @Test(expected = UpdateException.class)
  public void updateDomainEntityThrowsAnUpdateExceptionWhenRevOfTheRelationshipIsLowerThanThatOfTheEntity() throws Exception {
    // setup
    Relationship relationshipWithLowerRev = aRelationship().withRevision(FIRST_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID) //
        .relationship(relationshipWithLowerRev) //
        .foundInDB(dbMock);

    SubARelation relation = aRelation()//
        .withId(ID)//
        .withRevision(SECOND_REVISION) //
        .build();

    try {
      // action
      instance.updateDomainEntity(RELATION_TYPE, relation, CHANGE);
    } finally {
      // verify
      verify(dbMock).beginTx();

      verifyTransactionFailed();
    }
  }

  @Test(expected = ConversionException.class)
  public void updateDomainEntityThrowsAConversionExceptionWhenTheRelationshipConverterThrowsOne() throws Exception {
    // setup
    Relationship relationship = aRelationship().withRevision(FIRST_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    Change oldModified = CHANGE;
    SubARelation relation = aRelation()//
        .withId(ID) //
        .withRevision(FIRST_REVISION) //
        .build();

    RelationshipConverter<SubARelation> converterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    doThrow(ConversionException.class).when(converterMock).updatePropertyContainer(relationship, relation);

    try {
      // action
      instance.updateDomainEntity(RELATION_TYPE, relation, oldModified);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(converterMock).updatePropertyContainer(relationship, relation);
      verifyTransactionFailed();
    }
  }

  @Test
  public void setPIDSetsThePIDOfTheRelationAndDuplicatesIt() throws Exception {
    // setup
    Relationship relationship = aRelationship().withRevision(SECOND_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    SubARelation entity = aRelation().build();

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenReturn(entity);

    try {
      // action
      instance.setPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      InOrder inOrder = inOrder(converter, relationshipDuplicatorMock, transactionMock);
      inOrder.verify(converter).addValuesToPropertyContainer(//
          argThat(equalTo(relationship)), //
          argThat(likeDomainEntity(RELATION_TYPE)//
              .withPID(PID)));
      inOrder.verify(relationshipDuplicatorMock).saveDuplicate(relationship);
      inOrder.verify(transactionMock).success();
    }
  }

  @Test
  public void setPIDSetsToTheLatest() throws Exception {
    // setup
    Relationship latestRelationship = aRelationship().withRevision(SECOND_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(aRelationship().withRevision(FIRST_REVISION).build()) //
        .andRelationship(latestRelationship) //
        .foundInDB(dbMock);

    SubARelation entity = aRelation().build();

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(latestRelationship)).thenReturn(entity);

    try {
      // action
      instance.setPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).addValuesToPropertyContainer(latestRelationship, entity);
      verifyTransActionSucceeded();
    }
  }

  @Test(expected = IllegalStateException.class)
  public void setPIDThrowsAnIllegalStateExceptionIfTheRelationAlreadyHasAPID() throws Exception {
    // setup
    Relationship relationship = aRelationship().withAPID().build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    SubARelation entityWithAPID = aRelation().withAPID().build();

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenReturn(entityWithAPID);

    try {
      // action
      instance.setPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).convertToEntity(relationship);
      verifyTransactionFailed();
    }
  }

  @Test(expected = ConversionException.class)
  public void setPIDThrowsAConversionExceptionIfTheRelationshipCannotBeConverted() throws Exception {
    // setup
    Relationship relationship = aRelationship().build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenThrow(new ConversionException());

    try {
      // action
      instance.setPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).convertToEntity(relationship);
      verifyTransactionFailed();
    }
  }

  @Test(expected = ConversionException.class)
  public void setPIDThrowsAConversionsExceptionWhenTheUpdatedEntityCannotBeConvertedToARelationship() throws Exception {
    // setup
    Relationship relationship = aRelationship().withRevision(SECOND_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    SubARelation entity = aRelation().build();

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenReturn(entity);
    doThrow(ConversionException.class).when(converter).addValuesToPropertyContainer(relationship, entity);

    try {
      // action
      instance.setPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).addValuesToPropertyContainer(//
          argThat(equalTo(relationship)), //
          argThat(likeDomainEntity(RELATION_TYPE)//
              .withPID(PID)));
    }
  }

  @Test(expected = StorageException.class)
  public void setPIDThrowsAStorageExceptionIfTheRelationCannotBeInstatiated() throws Exception {
    // setup
    Relationship relationship = aRelationship().build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenThrow(new InstantiationException());

    try {
      // action
      instance.setPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verifyTransactionFailed();
    }
  }

  @Test(expected = NoSuchEntityException.class)
  public void setPIDThrowsANoSuchEntityExceptionIfTheRelationshipCannotBeFound() throws Exception {
    // setup
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsNothingForId(ID).foundInDB(dbMock);

    try {
      // action
      instance.setPID(RELATION_TYPE, ID, PID);
    } finally {
      verifyTransactionFailed();
    }

  }

  @Test
  public void getRevisionReturnsTheRelationForTheRequestedRevision() throws Exception {
    Relationship relationshipWithPID = aRelationship()//
        .withRevision(FIRST_REVISION)//
        .withAPID()//
        .build();
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID)//
        .relationship(relationshipWithPID)//
        .foundInDB(dbMock);

    SubARelation entity = aRelation().withAPID().build();
    RelationshipConverter<SubARelation> converterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converterMock.convertToEntity(relationshipWithPID)).thenReturn(entity);

    // action
    SubARelation relation = instance.getRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(instanceOf(RELATION_TYPE)));

    InOrder inOrder = inOrder(indexMock, converterMock, transactionMock);
    inOrder.verify(indexMock).get(ID_PROPERTY_NAME, ID);
    inOrder.verify(converterMock).convertToEntity(relationshipWithPID);
    inOrder.verify(transactionMock).success();
  }

  @Test
  public void getRevisionReturnsNullIfTheFoundRelationshipHasNoPID() throws Exception {
    Relationship relationshipWithoutPID = aRelationship().withRevision(FIRST_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID)//
        .relationship(relationshipWithoutPID)//
        .foundInDB(dbMock);

    SubARelation entityWithoutPID = aRelation().build();
    RelationshipConverter<SubARelation> relationshipConverter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(relationshipConverter.convertToEntity(relationshipWithoutPID)).thenReturn(entityWithoutPID);

    // action
    SubARelation relation = instance.getRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(nullValue()));
    verifyTransActionSucceeded();
  }

  @Test
  public void getRevisionReturnsNullIfTheRelationshipDoesNotExist() throws Exception {
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsNothingForId(ID).foundInDB(dbMock);

    // action
    SubARelation relation = instance.getRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(nullValue()));
    verifyTransActionSucceeded();
  }

  @Test
  public void getRevisionReturnsNullIfTheRevisionDoesNotExist() throws Exception {
    Relationship relationshipWithDifferentRevision = aRelationship().withAPID().withRevision(FIRST_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID)//
        .relationship(relationshipWithDifferentRevision)//
        .foundInDB(dbMock);

    // action
    SubARelation relation = instance.getRevision(RELATION_TYPE, ID, SECOND_REVISION);

    // verify
    assertThat(relation, is(nullValue()));
    verifyTransActionSucceeded();
  }

  private void verifyTransActionSucceeded() {
    verify(transactionMock).success();
  }

  @Test(expected = StorageException.class)
  public void getRevisionThrowsAStorageExceptionIfTheRelationCannotBeInstantiated() throws Exception {
    Relationship relationshipWithPID = aRelationship()//
        .withRevision(FIRST_REVISION)//
        .withAPID()//
        .build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID)//
        .relationship(relationshipWithPID)//
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationshipWithPID)).thenThrow(new InstantiationException());

    try {
      // action
      instance.getRevision(RELATION_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verifyTransactionFailed();
    }
  }

  private void verifyTransactionFailed() {
    verify(transactionMock).failure();
  }

  @Test(expected = ConversionException.class)
  public void getRevisionThrowsAStorageExceptionIfTheRelationCannotBeConverted() throws Exception {
    Relationship relationshipWithPID = aRelationship()//
        .withRevision(FIRST_REVISION)//
        .withAPID()//
        .build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID)//
        .relationship(relationshipWithPID)//
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationshipWithPID)).thenThrow(new ConversionException());

    try {
      // action
      instance.getRevision(RELATION_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verify(converter).convertToEntity(relationshipWithPID);
      verifyTransactionFailed();
    }
  }
}
