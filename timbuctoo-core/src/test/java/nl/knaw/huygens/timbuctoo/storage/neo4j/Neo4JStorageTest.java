package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import test.model.TestSystemEntity;

public class Neo4JStorageTest {

  private static final Class<TestSystemEntity> TYPE = TestSystemEntity.class;
  private static final Node NODE_MOCK = mock(Node.class);
  private static final TestSystemEntity ENTITY = new TestSystemEntity();
  private static final String ID = "id";
  private GraphDatabaseService dbMock;
  private EntityWrapper entityWrapperMock;
  private EntityWrapperFactory entityWrapperFactoryMock;
  private Neo4JStorage instance;
  private Transaction transactionMock;

  @Before
  public void setUp() {
    dbMock = mock(GraphDatabaseService.class);
    entityWrapperMock = mock(EntityWrapper.class);
    entityWrapperFactoryMock = mock(EntityWrapperFactory.class);
    transactionMock = mock(Transaction.class);

    instance = new Neo4JStorage(dbMock, entityWrapperFactoryMock);
  }

  @Test
  public void addSystemEntitySavesTheSystemAsNodeAndReturnsItsId() throws Exception {
    when(dbMock.beginTx()).thenReturn(transactionMock);
    when(dbMock.createNode()).thenReturn(NODE_MOCK);

    when(entityWrapperFactoryMock.wrapNew(ENTITY)).thenReturn(entityWrapperMock);
    when(entityWrapperMock.getId()).thenReturn(ID);

    // action
    String actualId = instance.addSystemEntity(TYPE, ENTITY);

    // verify
    assertThat(actualId, is(equalTo(ID)));

    InOrder inOrder = inOrder(dbMock, transactionMock, entityWrapperMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).createNode();
    inOrder.verify(entityWrapperMock).addValuesToNode(NODE_MOCK);
    inOrder.verify(entityWrapperMock).addAdministrativeValues(NODE_MOCK);
    inOrder.verify(transactionMock).success();
  }

  @Test(expected = StorageException.class)
  public void addSystemEntityRollsBackTheTransactionAndAndThrowsAStorageExceptionWhenObjectWrapperThrowsAnIllegalArgumentException() throws Exception {

    addSystemEntityRollsBackTheTransactionAndThrowsStorageExceptionObjectrapperThrowsAnException(IllegalArgumentException.class);
  }

  @Test(expected = StorageException.class)
  public void addSystemEntityRollsBackTheTransactionAndAndThrowsAStorageExceptionWhenObjectWrapperThrowsAnIllegalAccessException() throws Exception {

    addSystemEntityRollsBackTheTransactionAndThrowsStorageExceptionObjectrapperThrowsAnException(IllegalAccessException.class);
  }

  private void addSystemEntityRollsBackTheTransactionAndThrowsStorageExceptionObjectrapperThrowsAnException(Class<? extends Exception> exceptionToThrow) throws Exception {
    when(dbMock.beginTx()).thenReturn(transactionMock);
    when(dbMock.createNode()).thenReturn(NODE_MOCK);

    when(entityWrapperFactoryMock.wrapNew(ENTITY)).thenReturn(entityWrapperMock);
    doThrow(exceptionToThrow).when(entityWrapperMock).addValuesToNode(NODE_MOCK);

    try {
      // action
      instance.addSystemEntity(TYPE, ENTITY);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(entityWrapperMock).addValuesToNode(NODE_MOCK);
      verifyNoMoreInteractions(entityWrapperMock);
      verify(transactionMock).failure();
    }
  }
}
