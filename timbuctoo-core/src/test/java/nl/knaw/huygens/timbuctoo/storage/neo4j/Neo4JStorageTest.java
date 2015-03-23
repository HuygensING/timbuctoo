package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
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
  private Neo4JStorage instance;
  private PropertyContainerConverterFactory propertyContainerConverterFactoryMock;
  private Transaction transactionMock = mock(Transaction.class);
  private GraphDatabaseService dbMock;

  @Before
  public void setup() throws Exception {
    setupEntityConverterFactory();
    setupDBTransaction();
    instance = new Neo4JStorage(dbMock, propertyContainerConverterFactoryMock);
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
    verifyTransActionSucceeded();
  }

  @Test
  public void getRelationRevisionReturnsNullIfTheRelationshipDoesNotExist() throws Exception {
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsNothingForId(ID).foundInDB(dbMock);

    // action
    SubARelation relation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(nullValue()));
    verifyTransActionSucceeded();
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
    verifyTransActionSucceeded();
  }

  private void verifyTransActionSucceeded() {
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

}
