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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

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

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerFactoryHasCompositeRelationshipConverterFor(RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);

    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTypeId(RELATION_TYPE_ID);
    relation.setTypeType(RELATION_TYPE_NAME);

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

    NodeConverter<RelationType> relationTypeConverter = propertyContainerConverterFactoryHasAnEntityWrapperTypeFor(RELATIONTYPE_TYPE);
    RelationType relationType = new RelationType();
    relationType.setRegularName(name);

    when(entityInstantiatorMock.createInstanceOf(RELATIONTYPE_TYPE)).thenReturn(relationType);
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
    // setup
    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTypeId(RELATION_TYPE_ID);
    relation.setTypeType(RELATION_TYPE_NAME);
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

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerFactoryHasCompositeRelationshipConverterFor(RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);
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
          argThat(likeDomainEntity(RELATION_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verify(transactionMock).failure();
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityForRelationThrowsAStorageExceptionWhenTheEntityInstantiatorThrowsAnIllegalAccessException() throws Exception {
    addDomainEntityForRelationThrowsAStorageExceptionWhenTheEntityInstantiatorThrowsAnException(IllegalAccessException.class);
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityForRelationThrowsAStorageExceptionWhenTheEntityInstantiatorThrowsAnInstantiationException() throws Exception {
    addDomainEntityForRelationThrowsAStorageExceptionWhenTheEntityInstantiatorThrowsAnException(InstantiationException.class);
  }

  private void addDomainEntityForRelationThrowsAStorageExceptionWhenTheEntityInstantiatorThrowsAnException(Class<? extends Exception> exceptionToThrow) throws Exception {
    // setup
    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTypeId(RELATION_TYPE_ID);
    relation.setTypeType(RELATION_TYPE_NAME);
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

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerFactoryHasCompositeRelationshipConverterFor(RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);
    doThrow(exceptionToThrow).when(entityInstantiatorMock).createInstanceOf(RELATIONTYPE_TYPE);

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(entityInstantiatorMock).createInstanceOf(RELATIONTYPE_TYPE);
      verify(transactionMock).failure();
      verifyZeroInteractions(relationConverterMock, sourceNodeMock);
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityForRelationThrowsAConversionExceptionWhenTheRegularNameOfTheRelationTypeCannotBeFound() throws Exception {
    // setup
    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTypeId(RELATION_TYPE_ID);
    relation.setTypeType(RELATION_TYPE_NAME);
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

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerFactoryHasCompositeRelationshipConverterFor(RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);
    doThrow(ConversionException.class).when(relationTypeConverter).addValuesToEntity(any(RelationType.class), any(Node.class));

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(relationTypeConverter).addValuesToEntity(any(RelationType.class), any(Node.class));
      verify(transactionMock).failure();
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
      verify(transactionMock).failure();
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
      verify(transactionMock).failure();
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

    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTypeId(RELATION_TYPE_ID);
    relation.setTypeType(RELATION_TYPE_NAME);

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_TARGET_ID);
      verify(dbMock).findNodesByLabelAndProperty(RELATION_TYPE_LABEL, ID_PROPERTY_NAME, RELATION_TYPE_ID);
      verify(transactionMock).failure();
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
    when(entityInstantiatorMock.createInstanceOf(RELATION_TYPE)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).addValuesToEntity(relation, relationshipMock);
    verify(transactionMock).success();
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

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(entityInstantiatorMock.createInstanceOf(RELATION_TYPE)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).addValuesToEntity(relation, relationshipThirdRevision);
    verify(transactionMock).success();
  }

  @Test
  public void getEntityForRelationReturnsNullIfTheRelationIsNotFound() throws Exception {
    // setup
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsNothingForId(ID)//
        .foundInDB(dbMock);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(nullValue()));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verifyZeroInteractions(propertyContainerConverterFactoryMock, entityInstantiatorMock);
    verify(transactionMock).success();
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

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(entityInstantiatorMock.createInstanceOf(RELATION_TYPE)).thenReturn(relation);

    doThrow(ConversionException.class).when(relationConverterMock).addValuesToEntity(relation, relationshipMock);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).addValuesToEntity(relation, relationshipMock);
    verify(transactionMock).failure();
  }

  @Test(expected = StorageException.class)
  public void getEntityForRelationThrowsStorageExceptionWhenEntityInstantiatorThrowsAnInstantiationException() throws Exception {
    getEntityForRelationThrowsStorageExceptionWhenEntityInstantiatorThrowsAnException(InstantiationException.class);
  }

  @Test(expected = StorageException.class)
  public void getEntityForRelationThrowsStorageExceptionWhenEntityInstantiatorThrowsAnIllegalAccessException() throws Exception {
    getEntityForRelationThrowsStorageExceptionWhenEntityInstantiatorThrowsAnException(IllegalAccessException.class);
  }

  private void getEntityForRelationThrowsStorageExceptionWhenEntityInstantiatorThrowsAnException(Class<? extends Exception> exceptionToThrow) throws Exception {
    // setup
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID)//
        .relationship(aRelationship().build())//
        .foundInDB(dbMock);
    doThrow(exceptionToThrow).when(entityInstantiatorMock).createInstanceOf(RELATION_TYPE);

    try {
      // action
      instance.getEntity(RELATION_TYPE, ID);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).index();
      verify(indexMock).get(ID_PROPERTY_NAME, ID);
      verify(transactionMock).failure();
      verifyNoMoreInteractions(dbMock);
      verifyZeroInteractions(propertyContainerConverterFactoryMock);
    }
  }
}
