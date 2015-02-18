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
  private EntityWrapper objectWrapperMock;
  private EntityWrapperFactory objectWrapperFactoryMock;
  private Neo4JStorage instance;
  private Transaction transactionMock;
  private IdGenerator idGeneratorMock;

  @Before
  public void setUp() {
    dbMock = mock(GraphDatabaseService.class);
    objectWrapperMock = mock(EntityWrapper.class);
    objectWrapperFactoryMock = mock(EntityWrapperFactory.class);
    transactionMock = mock(Transaction.class);

    idGeneratorMock = mock(IdGenerator.class);

    instance = new Neo4JStorage(dbMock, objectWrapperFactoryMock, idGeneratorMock);
  }

  @Test
  public void addSystemEntitySavesTheSystemAsNodeAndReturnsItsId() throws Exception {
    when(dbMock.beginTx()).thenReturn(transactionMock);
    when(dbMock.createNode()).thenReturn(NODE_MOCK);

    when(objectWrapperFactoryMock.wrap(ENTITY)).thenReturn(objectWrapperMock);

    when(idGeneratorMock.nextIdFor(TYPE)).thenReturn(ID);

    // action
    String actualId = instance.addSystemEntity(TYPE, ENTITY);

    // verify
    assertThat(actualId, is(equalTo(ID)));

    InOrder inOrder = inOrder(dbMock, transactionMock, objectWrapperMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).createNode();
    inOrder.verify(objectWrapperMock).setId(ID);
    inOrder.verify(objectWrapperMock).addValuesToNode(NODE_MOCK);
    inOrder.verify(objectWrapperMock).addAdministrativeValues(NODE_MOCK);
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

    when(idGeneratorMock.nextIdFor(TYPE)).thenReturn(ID);

    when(objectWrapperFactoryMock.wrap(ENTITY)).thenReturn(objectWrapperMock);
    doThrow(exceptionToThrow).when(objectWrapperMock).addValuesToNode(NODE_MOCK);

    try {
      // action
      instance.addSystemEntity(TYPE, ENTITY);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(objectWrapperMock).setId(ID);
      verify(objectWrapperMock).addValuesToNode(NODE_MOCK);
      verifyNoMoreInteractions(objectWrapperMock);
      verify(transactionMock).failure();
    }
  }
}
