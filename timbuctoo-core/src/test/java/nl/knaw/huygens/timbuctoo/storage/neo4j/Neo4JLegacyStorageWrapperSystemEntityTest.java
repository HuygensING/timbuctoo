package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.aSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.anEmptySearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.TestSystemEntityWrapperBuilder.aSystemEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import test.model.TestSystemEntityWrapper;

public class Neo4JLegacyStorageWrapperSystemEntityTest extends Neo4JLegacyStorageWrapperTest {
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final Label SYSTEM_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(SYSTEM_ENTITY_TYPE));

  @Test
  public void addSystemEntityDelegatesToNeo4JStorage() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    when(neo4JStorageMock.addSystemEntity(SYSTEM_ENTITY_TYPE, entity)).thenReturn(ID);

    // action
    String actualId = instance.addSystemEntity(SYSTEM_ENTITY_TYPE, entity);

    // verify
    assertThat(actualId, is(equalTo(ID)));
    verify(neo4JStorageMock).addSystemEntity(SYSTEM_ENTITY_TYPE, entity);
  }

  @Test(expected = StorageException.class)
  public void addSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    TestSystemEntityWrapper entity = aSystemEntity().build();
    when(neo4JStorageMock.addSystemEntity(SYSTEM_ENTITY_TYPE, entity)).thenThrow(new StorageException());

    // action
    instance.addSystemEntity(SYSTEM_ENTITY_TYPE, entity);
  }

  @Test
  public void getEntityDelegatesToNeo4JStorageGetEntity() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    when(neo4JStorageMock.getEntity(SYSTEM_ENTITY_TYPE, ID)).thenReturn(entity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test(expected = StorageException.class)
  public void getEntityDelegatesThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    when(neo4JStorageMock.getEntity(SYSTEM_ENTITY_TYPE, ID)).thenThrow(new StorageException());

    // action
    instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
  }

  @Test
  public void updateSystemEntityDelegatesToNeo4JStorage() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();

    // action
    instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, entity);

    // verify
    verify(neo4JStorageMock).updateSystemEntity(SYSTEM_ENTITY_TYPE, entity);
  }

  @Test(expected = StorageException.class)
  public void updateSystemEntityThrowsAStorageExceptionWhenTheDelegateDoes() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    doThrow(StorageException.class).when(neo4JStorageMock).updateSystemEntity(SYSTEM_ENTITY_TYPE, entity);

    // action
    instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, entity);
  }

  @Test
  public void deleteSystemEntityFirstRemovesTheNodesRelationShipsAndThenTheNodeItselfTheDatabase() throws Exception {
    // setup
    Relationship relMock1 = aRelationship().build();
    Relationship relMock2 = aRelationship().build();
    Node nodeMock2 = aNode().withOutgoingRelationShip(relMock1).andOutgoingRelationship(relMock2).build();

    aSearchResult().forLabel(Neo4JLegacyStorageWrapperSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock2) //
        .foundInDB(dbMock);

    // action
    int numDeleted = instance.deleteSystemEntity(Neo4JLegacyStorageWrapperSystemEntityTest.SYSTEM_ENTITY_TYPE, ID);

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
    anEmptySearchResult().forLabel(Neo4JLegacyStorageWrapperSystemEntityTest.SYSTEM_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    // action
    int numDeleted = instance.deleteSystemEntity(Neo4JLegacyStorageWrapperSystemEntityTest.SYSTEM_ENTITY_TYPE, ID);
    // verify
    assertThat(numDeleted, is(equalTo(0)));
    verify(dbMock).beginTx();
    verify(dbMock).findNodesByLabelAndProperty(Neo4JLegacyStorageWrapperSystemEntityTest.SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    verify(transactionMock).success();
  }
}
