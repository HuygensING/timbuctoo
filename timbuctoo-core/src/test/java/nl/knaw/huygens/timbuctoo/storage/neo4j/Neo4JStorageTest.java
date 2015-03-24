package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.DomainEntityMatcher.likeDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.Neo4JLegacyStorageWrapper.RELATIONSHIP_ID_INDEX;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipIndexMockBuilder.aRelationshipIndexForName;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.aSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.anEmptySearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubADomainEntityBuilder.aDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubARelationBuilder.aRelation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.RelationshipIndex;

import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

public class Neo4JStorageTest {

  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Label DOMAIN_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(DOMAIN_ENTITY_TYPE));
  private static final int FIRST_REVISION = 1;
  private static final String ID = "id";
  private static final int SECOND_REVISION = 2;
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final String PID = "pid";
  private Neo4JStorage instance;
  private PropertyContainerConverterFactory propertyContainerConverterFactoryMock;
  private Transaction transactionMock = mock(Transaction.class);
  private GraphDatabaseService dbMock;
  private NodeDuplicator nodeDuplicatorMock;
  private RelationshipDuplicator relationshipDuplicatorMock;

  @Before
  public void setup() throws Exception {
    nodeDuplicatorMock = mock(NodeDuplicator.class);
    relationshipDuplicatorMock = mock(RelationshipDuplicator.class);
    setupEntityConverterFactory();
    setupDBTransaction();
    instance = new Neo4JStorage(dbMock, propertyContainerConverterFactoryMock, nodeDuplicatorMock, relationshipDuplicatorMock);
  }

  private void setupDBTransaction() {
    transactionMock = mock(Transaction.class);
    dbMock = mock(GraphDatabaseService.class);
    when(dbMock.beginTx()).thenReturn(transactionMock);
  }

  @Test
  public void getDomainEntityRevisionReturnsTheDomainEntityWithTheRequestedRevision() throws Exception {
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(nodeWithSameRevision)).thenReturn(aDomainEntity().withAPid().build());

    // action
    SubADomainEntity entity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(entity, is(instanceOf(SubADomainEntity.class)));
    verify(converter).convertToEntity(nodeWithSameRevision);
    verify(transactionMock).success();
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheFoundEntityHasNoPID() throws Exception {
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(nodeWithSameRevision)).thenReturn(aDomainEntity().build());

    // action
    SubADomainEntity actualEntity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(actualEntity, is(nullValue()));
    verify(transactionMock).success();
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheEntityCannotBeFound() throws Exception {
    // setup
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    // action
    SubADomainEntity entity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(entity, is(nullValue()));
    verify(transactionMock).success();
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheRevisionCannotBeFound() throws Exception {
    // setup
    Node nodeWithDifferentRevision = aNode().withRevision(SECOND_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithDifferentRevision).foundInDB(dbMock);

    // action
    SubADomainEntity entity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(entity, is(nullValue()));
    verify(transactionMock).success();
  }

  @Test(expected = StorageException.class)
  public void getDomainEntityRevisionThrowsAStorageExceptionIfTheEntityCannotBeInstantiated() throws Exception {
    // setup
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(nodeWithSameRevision)).thenThrow(new InstantiationException());

    try {
      // action
      instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test(expected = ConversionException.class)
  public void getDomainEntityRevisionThrowsAConversionExceptionIfTheEntityCannotBeConverted() throws Exception {
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(nodeWithSameRevision)).thenThrow(new ConversionException());

    try {
      // action
      instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verify(converter).convertToEntity(nodeWithSameRevision);
      verify(transactionMock).failure();
    }
  }

  private void setupEntityConverterFactory() throws Exception {
    propertyContainerConverterFactoryMock = mock(PropertyContainerConverterFactory.class);
  }

  private <T extends Entity> NodeConverter<T> propertyContainerConverterFactoryHasANodeConverterTypeFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    NodeConverter<T> nodeConverter = mock(NodeConverter.class);
    when(propertyContainerConverterFactoryMock.createForType(argThat(equalTo(type)))).thenReturn(nodeConverter);
    return nodeConverter;
  }

  @Test
  public void setDomainEntityPIDAddsAPIDToTheLatestNodeIfMultipleAreFound() throws InstantiationException, IllegalAccessException, Exception {
    // setup
    Node nodeWithLatestRevision = aNode().withRevision(SECOND_REVISION).build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(aNode().withRevision(FIRST_REVISION).build()).withNode(nodeWithLatestRevision)//
        .foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converterMock.convertToEntity(nodeWithLatestRevision)).thenReturn(aDomainEntity().withId(ID).build());

    // action
    instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);

    verify(converterMock).addValuesToPropertyContainer( //
        argThat(equalTo(nodeWithLatestRevision)), //
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE).withId(ID).withPID(PID)));

  }

  @Test
  public void setDomainEntityPIDAddsAPIDToTheNodeAndDuplicatesTheNode() throws InstantiationException, IllegalAccessException, Exception {
    // setup
    Node node = aNode().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(node)//
        .foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converterMock.convertToEntity(node)).thenReturn(aDomainEntity().withId(ID).build());

    // action
    instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);

    InOrder inOrder = inOrder(converterMock, transactionMock, nodeDuplicatorMock);
    inOrder.verify(converterMock).addValuesToPropertyContainer( //
        argThat(equalTo(node)), //
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE).withId(ID).withPID(PID)));
    inOrder.verify(nodeDuplicatorMock).saveDuplicate(node);
    inOrder.verify(transactionMock).success();
  }

  @Test(expected = IllegalStateException.class)
  public void setDomainEntityPIDThrowsAnIllegalStateExceptionWhenTheEntityAlreadyHasAPID() throws Exception {
    // setup
    Node aNodeWithAPID = aNode().withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(aNodeWithAPID)//
        .foundInDB(dbMock);

    SubADomainEntity entityWithPID = aDomainEntity().withAPid().build();

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(aNodeWithAPID)).thenReturn(entityWithPID);

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(nodeConverter).convertToEntity(aNodeWithAPID);
      verify(transactionMock).failure();
    }
  }

  @Test(expected = ConversionException.class)
  public void setDomainEntityPIDThrowsAConversionExceptionWhenTheNodeCannotBeConverted() throws Exception {
    // setup
    Node aNodeWithAPID = aNode().withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(aNodeWithAPID)//
        .foundInDB(dbMock);

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(aNodeWithAPID)).thenThrow(new ConversionException());

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(nodeConverter).convertToEntity(aNodeWithAPID);
      verify(transactionMock).failure();
    }
  }

  @Test(expected = ConversionException.class)
  public void setDomainEntityPIDThrowsAConversionsExceptionWhenTheUpdatedEntityCannotBeCovnverted() throws Exception {
    // setup
    Node aNode = aNode().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(aNode)//
        .foundInDB(dbMock);

    SubADomainEntity entity = aDomainEntity().build();

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(aNode)).thenReturn(entity);
    doThrow(ConversionException.class).when(nodeConverter).addValuesToPropertyContainer(aNode, entity);

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(nodeConverter).addValuesToPropertyContainer(aNode, entity);
      verify(transactionMock).failure();
    }
  }

  @Test(expected = StorageException.class)
  public void setDomainEntityPIDThrowsAStorageExceptionWhenTheEntityDoesNotExist() throws Exception {
    // setup
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(transactionMock).failure();
    }

  }

  @Test(expected = StorageException.class)
  public void setDomainEntityPIDThrowsAStorageExceptionWhenTheEntityCannotBeInstatiated() throws Exception {

    // setup
    Node aNode = aNode().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(aNode)//
        .foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converterMock.convertToEntity(aNode)).thenThrow(new InstantiationException());

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  /* *****************************************************************************
   * Relation
   * *****************************************************************************/

  @Test
  public void getRelationRevisionReturnsTheRelationForTheRequestedRevision() throws Exception {
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
    SubARelation relation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(instanceOf(RELATION_TYPE)));

    InOrder inOrder = inOrder(indexMock, converterMock, transactionMock);
    inOrder.verify(indexMock).get(ID_PROPERTY_NAME, ID);
    inOrder.verify(converterMock).convertToEntity(relationshipWithPID);
    inOrder.verify(transactionMock).success();
  }

  @Test
  public void getRelationRevisionReturnsNullIfTheFoundRelationshipHasNoPID() throws Exception {
    Relationship relationshipWithoutPID = aRelationship().withRevision(FIRST_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID)//
        .relationship(relationshipWithoutPID)//
        .foundInDB(dbMock);

    SubARelation entityWithoutPID = aRelation().build();
    RelationshipConverter<SubARelation> relationshipConverter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(relationshipConverter.convertToEntity(relationshipWithoutPID)).thenReturn(entityWithoutPID);

    // action
    SubARelation relation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(nullValue()));
    verifyTransactionSucceeded();
  }

  @Test
  public void getRelationRevisionReturnsNullIfTheRelationshipDoesNotExist() throws Exception {
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsNothingForId(ID).foundInDB(dbMock);

    // action
    SubARelation relation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(nullValue()));
    verifyTransactionSucceeded();
  }

  @Test
  public void getRelationRevisionReturnsNullIfTheRevisionDoesNotExist() throws Exception {
    Relationship relationshipWithDifferentRevision = aRelationship().withAPID().withRevision(FIRST_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID)//
        .relationship(relationshipWithDifferentRevision)//
        .foundInDB(dbMock);

    // action
    SubARelation relation = instance.getRelationRevision(RELATION_TYPE, ID, SECOND_REVISION);

    // verify
    assertThat(relation, is(nullValue()));
    verifyTransactionSucceeded();
  }

  private void verifyTransactionSucceeded() {
    verify(transactionMock).success();
  }

  @Test(expected = StorageException.class)
  public void getRelationRevisionThrowsAStorageExceptionIfTheRelationCannotBeInstantiated() throws Exception {
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
      instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verifyTransactionFailed();
    }
  }

  private void verifyTransactionFailed() {
    verify(transactionMock).failure();
  }

  @Test(expected = ConversionException.class)
  public void getRelationRevisionThrowsAStorageExceptionIfTheRelationCannotBeConverted() throws Exception {
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
      instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verify(converter).convertToEntity(relationshipWithPID);
      verifyTransactionFailed();
    }
  }

  private <T extends Relation> RelationshipConverter<T> propertyContainerConverterFactoryHasRelationshipConverterFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    RelationshipConverter<T> relationshipConverter = mock(RelationshipConverter.class);
    when(propertyContainerConverterFactoryMock.createForRelation(type)).thenReturn(relationshipConverter);

    return relationshipConverter;
  }

  @Test
  public void setRelationPIDSetsThePIDOfTheRelationAndDuplicatesIt() throws Exception {
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
      instance.setRelationPID(RELATION_TYPE, ID, PID);
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
  public void setRelationPIDSetsToTheLatest() throws Exception {
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
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).addValuesToPropertyContainer(latestRelationship, entity);
      verifyTransactionSucceeded();
    }
  }

  @Test(expected = IllegalStateException.class)
  public void setRelationPIDThrowsAnIllegalStateExceptionIfTheRelationAlreadyHasAPID() throws Exception {
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
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).convertToEntity(relationship);
      verifyTransactionFailed();
    }
  }

  @Test(expected = ConversionException.class)
  public void setRelationPIDThrowsAConversionExceptionIfTheRelationshipCannotBeConverted() throws Exception {
    // setup
    Relationship relationship = aRelationship().build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenThrow(new ConversionException());

    try {
      // action
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).convertToEntity(relationship);
      verifyTransactionFailed();
    }
  }

  @Test(expected = ConversionException.class)
  public void setRelationPIDThrowsAConversionsExceptionWhenTheUpdatedEntityCannotBeConvertedToARelationship() throws Exception {
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
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).addValuesToPropertyContainer(//
          argThat(equalTo(relationship)), //
          argThat(likeDomainEntity(RELATION_TYPE)//
              .withPID(PID)));
    }
  }

  @Test(expected = StorageException.class)
  public void setRelationPIDThrowsAStorageExceptionIfTheRelationCannotBeInstatiated() throws Exception {
    // setup
    Relationship relationship = aRelationship().build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenThrow(new InstantiationException());

    try {
      // action
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verifyTransactionFailed();
    }
  }

  @Test(expected = NoSuchEntityException.class)
  public void setRelationPIDThrowsANoSuchEntityExceptionIfTheRelationshipCannotBeFound() throws Exception {
    // setup
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsNothingForId(ID).foundInDB(dbMock);

    try {
      // action
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      verifyTransactionFailed();
    }

  }

}
