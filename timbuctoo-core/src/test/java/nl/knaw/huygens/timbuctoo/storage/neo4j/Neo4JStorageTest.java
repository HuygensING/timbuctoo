package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import test.model.TestSystemEntity;

public class Neo4JStorageTest {
  @Ignore
  @Test
  public void addSystemEntitySavesTheSystemAsNodeAndReturnsItsId() throws Exception {
    String id = "id";
    TestSystemEntity entity = new TestSystemEntity();
    Node nodeMock = mock(Node.class);
    Class<TestSystemEntity> type = TestSystemEntity.class;

    GraphDatabaseService dbMock = mock(GraphDatabaseService.class);
    EntityWrapper objectWrapperMock = mock(EntityWrapper.class);

    EntityWrapperFactory objectWrapperFactoryMock = mock(EntityWrapperFactory.class);
    when(objectWrapperFactoryMock.wrap(entity)).thenReturn(objectWrapperMock);
    when(objectWrapperMock.addAdministrativeValues(nodeMock)).thenReturn(id);

    Neo4JStorage instance = new Neo4JStorage(dbMock, objectWrapperFactoryMock);

    Transaction transactionMock = mock(Transaction.class);
    when(dbMock.beginTx()).thenReturn(transactionMock);
    when(dbMock.createNode()).thenReturn(nodeMock);

    // action
    String actualId = instance.addSystemEntity(type, entity);

    // verify
    assertThat(actualId, is(equalTo(id)));

    InOrder inOrder = inOrder(dbMock, transactionMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).createNode();
    inOrder.verify(objectWrapperMock).addValuesToNode(nodeMock);
    inOrder.verify(objectWrapperMock).addAdministrativeValues(nodeMock);
    inOrder.verify(transactionMock).success();

  }
}
