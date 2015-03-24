package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.DomainEntityMatcher.likeDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.Neo4JLegacyStorageWrapper.RELATIONSHIP_ID_INDEX;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipIndexMockBuilder.aRelationshipIndexForName;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubARelationBuilder.aRelation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;

import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.graphdb.Relationship;

import test.model.projecta.SubARelation;

public class Neo4JLegacyStorageWrapperRelationTest extends Neo4JLegacyStorageWrapperTest {
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;

  private <T extends Relation> RelationshipConverter<T> propertyContainerConverterFactoryHasRelationshipConverterFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    RelationshipConverter<T> relationshipConverter = mock(RelationshipConverter.class);
    when(propertyContainerConverterFactoryMock.createForRelation(type)).thenReturn(relationshipConverter);

    return relationshipConverter;
  }

  @Test
  public void addDomainEntityForRelationDelegatesToNeo4JStorageAddRelation() throws Exception {
    // setup
    SubARelation relation = aRelation().build();
    when(neo4JStorageMock.addRelation(RELATION_TYPE, relation, CHANGE)).thenReturn(ID);

    // action
    String id = instance.addDomainEntity(RELATION_TYPE, relation, CHANGE);

    // verify
    assertThat(id, is(equalTo(ID)));
    verify(neo4JStorageMock).addRelation(RELATION_TYPE, relation, CHANGE);
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    SubARelation relation = aRelation().build();
    when(neo4JStorageMock.addRelation(RELATION_TYPE, relation, CHANGE)).thenThrow(new StorageException());

    // action
    instance.addDomainEntity(RELATION_TYPE, relation, CHANGE);
  }

  @Test
  public void getEntityForRelationDelegatesToNeo4JStorageGetRelation() throws Exception {
    // setup
    SubARelation relation = aRelation().build();
    when(neo4JStorageMock.getRelation(RELATION_TYPE, ID)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));
  }

  @Test(expected = StorageException.class)
  public void getEntityForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(neo4JStorageMock.getRelation(RELATION_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getEntity(RELATION_TYPE, ID);
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
  public void setPIDForRelationDelegatesToNeo4JStorageSetRelationPID() throws Exception {
    // action
    instance.setPID(RELATION_TYPE, ID, PID);

    // verify
    verify(neo4JStorageMock).setRelationPID(RELATION_TYPE, ID, PID);
  }

  @Test(expected = StorageException.class)
  public void setPIDForRelationThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    doThrow(StorageException.class).when(neo4JStorageMock).setRelationPID(RELATION_TYPE, ID, PID);

    // action
    instance.setPID(RELATION_TYPE, ID, PID);
  }

  private void verifyTransactionFailed() {
    verify(transactionMock).failure();
  }

  @Test
  public void getRevisionForRelationDelegatesTheCallToNeo4JStorageGetRelationRevision() throws Exception {
    // setup
    when(neo4JStorageMock.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION)).thenReturn(aRelation().build());

    // action
    SubARelation relation = instance.getRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(notNullValue()));

    verify(neo4JStorageMock).getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);
  }

  @Test(expected = StorageException.class)
  public void getRevisionThrowsAStorageExceptionWhenNeo4JStorageGetRelationRevisionDoes() throws Exception {
    // setup
    when(neo4JStorageMock.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION)).thenThrow(new StorageException());

    // action
    instance.getRevision(RELATION_TYPE, ID, FIRST_REVISION);
  }

}
