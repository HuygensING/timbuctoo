package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.aSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.anEmptySearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.TestSystemEntityWrapperBuilder.aSystemEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.TestSystemEntityWrapperMatcher.likeTestSystemEntityWrapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;

import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import test.model.TestSystemEntityWrapper;

public class Neo4JStorageSystemEntityTest extends Neo4JStorageTest {
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final Label SYSTEM_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(SYSTEM_ENTITY_TYPE));

  @Test
  public void addSystemEntitySavesTheSystemAsNodeAndReturnsItsId() throws Exception {
    Node nodeMock = aNode().createdBy(dbMock);
    idGeneratorMockCreatesIDFor(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, ID);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE);
    // action
    String actualId = instance.addSystemEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, aSystemEntity().build());

    // verify
    InOrder inOrder = inOrder(dbMock, transactionMock, systemEntityConverterMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(systemEntityConverterMock).addValuesToPropertyContainer(//
        argThat(equalTo(nodeMock)), // 
        argThat(likeTestSystemEntityWrapper() //
            .withId(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    inOrder.verify(transactionMock).success();
    verifyNoMoreInteractions(systemEntityConverterMock);
  }

  @Test(expected = StorageException.class)
  public void addSystemEntityRollsBackTheTransactionAndThrowsStorageExceptionObjectrapperThrowsAConversionException() throws Exception {
    Node nodeMock = aNode().createdBy(dbMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE);

    TestSystemEntityWrapper systemEntity = aSystemEntity().build();
    doThrow(ConversionException.class).when(systemEntityConverterMock).addValuesToPropertyContainer(nodeMock, systemEntity);

    try {
      // action
      instance.addSystemEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(systemEntityConverterMock).addValuesToPropertyContainer(nodeMock, systemEntity);
      verifyNoMoreInteractions(systemEntityConverterMock);
      verify(transactionMock).failure();
    }
  }

  @Test
  public void getEntityReturnsTheItemWhenFound() throws Exception {
    Node nodeMock = aNode().build();
    aSearchResult().forLabel(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    TestSystemEntityWrapper systemEntity = aSystemEntity().build();
    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE);
    when(systemEntityConverterMock.convertToEntity(nodeMock)).thenReturn(systemEntity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(systemEntity)));

    InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, systemEntityConverterMock, transactionMock);
    inOrder.verify(dbMock).findNodesByLabelAndProperty(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(systemEntityConverterMock).convertToEntity(nodeMock);
    inOrder.verify(transactionMock).success();
  }

  @Test
  public void getEntityReturnsNullIfNoItemIsFound() throws Exception {
    // setup
    anEmptySearchResult().forLabel(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(nullValue()));

    verify(dbMock).findNodesByLabelAndProperty(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    verify(transactionMock).success();
    verifyZeroInteractions(propertyContainerConverterFactoryMock);
  }

  @Test(expected = StorageException.class)
  public void getEntityThrowsStorageExceptionWhenEntityWrapperThrowsAConversionException() throws Exception {
    // setup
    Node nodeMock = aNode().build();
    aSearchResult().forLabel(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID)//
        .withNode(nodeMock)//
        .foundInDB(dbMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE);
    doThrow(ConversionException.class).when(systemEntityConverterMock).convertToEntity(nodeMock);

    try {
      // action
      instance.getEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, systemEntityConverterMock, transactionMock);
      inOrder.verify(systemEntityConverterMock).convertToEntity(nodeMock);
      inOrder.verify(transactionMock).failure();
    }
  }

  @Test(expected = StorageException.class)
  public void getEntityThrowsStorageExceptionWhenNodeConverterThrowsAnInstantiationException() throws Exception {
    // setup
    Node nodeMock = aNode().build();
    aSearchResult().forLabel(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE);
    doThrow(InstantiationException.class).when(systemEntityConverterMock).convertToEntity(nodeMock);

    try {
      // action
      instance.getEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, transactionMock);
      inOrder.verify(transactionMock).failure();
    }
  }

  @Test
  public void updateSystemEntityRetrievesTheEntityAndUpdatesTheData() throws Exception {
    // setup
    Node nodeMock = aNode().withRevision(FIRST_REVISION).build();
    aSearchResult().forLabel(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE);

    Change oldModified = new Change();
    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(FIRST_REVISION)//
        .withModified(oldModified)//
        .build();

    instance.updateSystemEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, systemEntity);

    // verify
    InOrder inOrder = inOrder(dbMock, systemEntityConverterMock, transactionMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(systemEntityConverterMock).updatePropertyContainer(argThat(equalTo(nodeMock)), //
        argThat(likeTestSystemEntityWrapper() //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)));
    inOrder.verify(systemEntityConverterMock).updateModifiedAndRev(argThat(equalTo(nodeMock)), //
        argThat(likeTestSystemEntityWrapper() //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)));
    inOrder.verify(transactionMock).success();
    verifyNoMoreInteractions(dbMock, systemEntityConverterMock);
  }

  @Test(expected = UpdateException.class)
  public void updateSystemEntityThrowsAnUpdateExceptionIfTheNodeIsNewerThanTheEntityWithTheUpdatedInformation() throws Exception {
    // setup
    int newerRevision = 2;
    Node nodeMock = aNode().withRevision(newerRevision).build();
    aSearchResult().forLabel(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(FIRST_REVISION)//
        .build();

    try {
      // action
      instance.updateSystemEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      verify(transactionMock).failure();
      verifyZeroInteractions(propertyContainerConverterFactoryMock);
    }
  }

  @Test(expected = UpdateException.class)
  public void updateSystemEntityThrowsAnUpdateExceptionIfTheNodeIsOlderThanTheEntityWithTheUpdatedInformation() throws Exception {
    // setup
    Node nodeMock = aNode().withRevision(FIRST_REVISION).build();
    aSearchResult().forLabel(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    int newerRevision = 2;
    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(newerRevision).build();

    try {
      // action
      instance.updateSystemEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      verify(transactionMock).failure();
      verifyZeroInteractions(propertyContainerConverterFactoryMock);
    }
  }

  @Test(expected = UpdateException.class)
  public void updateSystemEntityThrowsAnUpdateExceptionIfTheNodeCannotBeFound() throws Exception {
    // setup
    anEmptySearchResult().forLabel(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(FIRST_REVISION).build();

    try {
      // action
      instance.updateSystemEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      verify(transactionMock).failure();
      verifyZeroInteractions(propertyContainerConverterFactoryMock);
    }
  }

  @Test(expected = ConversionException.class)
  public void updateSystemEntityThrowsAConversionExceptionWhenTheEntityConverterThrowsOne() throws Exception {
    // setup
    Node nodeMock = aNode().withRevision(FIRST_REVISION).build();
    aSearchResult().forLabel(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE);

    Change oldModified = new Change();
    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(FIRST_REVISION)//
        .withModified(oldModified)//
        .build();

    doThrow(ConversionException.class).when(systemEntityConverterMock).updatePropertyContainer(nodeMock, systemEntity);

    try {
      // action
      instance.updateSystemEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      verify(systemEntityConverterMock).updatePropertyContainer(argThat(equalTo(nodeMock)), //
          argThat(likeTestSystemEntityWrapper() //
              .withAModifiedValueNotEqualTo(oldModified) //
              .withRevision(SECOND_REVISION)));
      verify(systemEntityConverterMock, never()).updateModifiedAndRev(argThat(equalTo(nodeMock)), //
          argThat(likeTestSystemEntityWrapper() //
              .withAModifiedValueNotEqualTo(oldModified) //
              .withRevision(SECOND_REVISION)));
      verify(transactionMock).failure();
    }
  }

  @Test
  public void deleteSystemEntityFirstRemovesTheNodesRelationShipsAndThenTheNodeItselfTheDatabase() throws Exception {
    // setup
    Relationship relMock1 = aRelationship().build();
    Relationship relMock2 = aRelationship().build();
    Node nodeMock2 = aNode().withOutgoingRelationShip(relMock1).andOutgoingRelationship(relMock2).build();

    aSearchResult().forLabel(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock2) //
        .foundInDB(dbMock);

    // action
    int numDeleted = instance.deleteSystemEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(numDeleted, is(equalTo(1)));
    InOrder inOrder = inOrder(dbMock, nodeMock2, relMock1, relMock2, transactionMock);
    inOrder.verify(dbMock).beginTx();
    verifyNodeAndItsRelationAreDelete(nodeMock2, relMock1, relMock2, inOrder);
    inOrder.verify(transactionMock).success();

  }

  @Test
  public void deleteSystemEntityReturns0WhenTheEntityCannotBeFound() throws Exception {
    // setup
    anEmptySearchResult().forLabel(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    // action
    int numDeleted = instance.deleteSystemEntity(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_TYPE, ID);
    // verify
    assertThat(numDeleted, is(equalTo(0)));
    verify(dbMock).beginTx();
    verify(dbMock).findNodesByLabelAndProperty(Neo4JStorageSystemEntityTest.SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    verify(transactionMock).success();
  }
}
