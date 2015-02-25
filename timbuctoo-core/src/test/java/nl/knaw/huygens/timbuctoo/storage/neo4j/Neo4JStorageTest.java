package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import test.model.TestSystemEntity;

public class Neo4JStorageTest {

  private static final Class<TestSystemEntity> TYPE = TestSystemEntity.class;
  private static final Label LABEL = DynamicLabel.label(TypeNames.getInternalName(TYPE));
  private Node nodeMock;
  private static final TestSystemEntity ENTITY = new TestSystemEntity();
  private static final String ID = "id";
  private GraphDatabaseService dbMock;
  private EntityTypeWrapper<TestSystemEntity> entityWrapperMock;
  private EntityTypeWrapperFactory entityWrapperFactoryMock;
  private Neo4JStorage instance;
  private Transaction transactionMock;
  private EntityInstantiator entityInstantiatorMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    nodeMock = mock(Node.class);
    dbMock = mock(GraphDatabaseService.class);
    entityWrapperMock = mock(EntityTypeWrapper.class);
    setupEntityWrapperFactory();

    transactionMock = mock(Transaction.class);
    entityInstantiatorMock = mock(EntityInstantiator.class);

    instance = new Neo4JStorage(dbMock, entityWrapperFactoryMock, entityInstantiatorMock);
  }

  private void setupEntityWrapperFactory() throws Exception {
    entityWrapperFactoryMock = mock(EntityTypeWrapperFactory.class);
    when(entityWrapperFactoryMock.createFromType(TYPE)).thenReturn(entityWrapperMock);
  }

  @Test
  public void addSystemEntitySavesTheSystemAsNodeAndReturnsItsId() throws Exception {
    when(dbMock.beginTx()).thenReturn(transactionMock);
    when(dbMock.createNode()).thenReturn(nodeMock);
    when(entityWrapperMock.getId()).thenReturn(ID);

    // action
    String actualId = instance.addSystemEntity(TYPE, ENTITY);

    // verify
    assertThat(actualId, is(equalTo(ID)));

    InOrder inOrder = inOrder(dbMock, transactionMock, entityWrapperMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).createNode();
    inOrder.verify(entityWrapperMock).addValuesToNode(nodeMock, ENTITY);
    inOrder.verify(entityWrapperMock).addAdministrativeValues(nodeMock);
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
    when(dbMock.createNode()).thenReturn(nodeMock);

    when(entityWrapperFactoryMock.createFromType(TYPE)).thenReturn(entityWrapperMock);
    doThrow(exceptionToThrow).when(entityWrapperMock).addValuesToNode(nodeMock, ENTITY);

    try {
      // action
      instance.addSystemEntity(TYPE, ENTITY);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(entityWrapperMock).addValuesToNode(nodeMock, ENTITY);
      verifyNoMoreInteractions(entityWrapperMock);
      verify(transactionMock).failure();
    }
  }

  @Test
  public void getEntityReturnsTheItemWhenFound() throws Exception {

    oneNodeIsFound(nodeMock);
    when(entityWrapperFactoryMock.createFromType(TYPE)).thenReturn(entityWrapperMock);
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(ENTITY);

    // action
    TestSystemEntity actualEntity = instance.getEntity(TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(ENTITY)));

    InOrder inOrder = inOrder(dbMock, entityWrapperFactoryMock, entityWrapperMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(entityWrapperFactoryMock).createFromType(TYPE);
    inOrder.verify(entityWrapperMock).addValuesToEntity(ENTITY, nodeMock);
    verifyNoMoreInteractions(dbMock, entityWrapperFactoryMock, entityWrapperMock);
  }

  @Test
  // This is what mongo did by findOne
  public void getEntityReturnsTheFirstIfMoreThanOneItemIsFound() throws Exception {
    // setup
    Node otherFoundNode = mock(Node.class);
    multipleNodesAreFound(nodeMock, otherFoundNode);
    when(entityWrapperFactoryMock.createFromType(TYPE)).thenReturn(entityWrapperMock);
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(ENTITY);

    // action
    TestSystemEntity actualEntity = instance.getEntity(TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(ENTITY)));

    InOrder inOrder = inOrder(dbMock, entityWrapperFactoryMock, entityWrapperMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(entityWrapperFactoryMock).createFromType(TYPE);
    inOrder.verify(entityWrapperMock).addValuesToEntity(ENTITY, nodeMock);
    verifyNoMoreInteractions(dbMock, entityWrapperFactoryMock, entityWrapperMock);
  }

  @Test
  public void getEntityReturnsNullIfNoItemIsFound() throws Exception {
    // setup
    nodeNodeIsFound();

    // action
    TestSystemEntity actualEntity = instance.getEntity(TYPE, ID);

    // verify
    assertThat(actualEntity, is(nullValue()));

    verify(dbMock).findNodesByLabelAndProperty(LABEL, ID_PROPERTY_NAME, ID);
    verifyZeroInteractions(entityWrapperFactoryMock);
  }

  private void oneNodeIsFound(Node nodeToBeFound) {
    @SuppressWarnings("unchecked")
    ResourceIterator<Node> nodeIterator = mock(ResourceIterator.class);
    when(nodeIterator.hasNext()).thenReturn(true, false);
    when(nodeIterator.next()).thenReturn(nodeToBeFound);

    nodesFound(nodeIterator);
  }

  private void nodeNodeIsFound() {
    @SuppressWarnings("unchecked")
    ResourceIterator<Node> nodeIterator = mock(ResourceIterator.class);
    when(nodeIterator.hasNext()).thenReturn(false);

    nodesFound(nodeIterator);
  }

  private void multipleNodesAreFound(Node nodeToBeFound, Node otherNode) {
    @SuppressWarnings("unchecked")
    ResourceIterator<Node> nodeIterator = mock(ResourceIterator.class);
    when(nodeIterator.hasNext()).thenReturn(true, true, false);
    when(nodeIterator.next()).thenReturn(nodeToBeFound, otherNode);

    nodesFound(nodeIterator);
  }

  private void nodesFound(ResourceIterator<Node> nodeIterator) {
    @SuppressWarnings("unchecked")
    ResourceIterable<Node> foundNodes = mock(ResourceIterable.class);
    when(foundNodes.iterator()).thenReturn(nodeIterator);
    when(dbMock.findNodesByLabelAndProperty(LABEL, ID_PROPERTY_NAME, ID)).thenReturn(foundNodes);
  }

  @Test(expected = StorageException.class)
  public void getEntityThrowsStorageExceptionWhenEntityWrapperThrowsAConversionException() throws Exception {
    // setup
    oneNodeIsFound(nodeMock);
    when(entityWrapperFactoryMock.createFromType(TYPE)).thenReturn(entityWrapperMock);
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(ENTITY);
    doThrow(ConversionException.class).when(entityWrapperMock).addValuesToEntity(ENTITY, nodeMock);

    try {
      // action
      instance.getEntity(TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, entityWrapperFactoryMock, entityWrapperMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(LABEL, ID_PROPERTY_NAME, ID);
      inOrder.verify(entityWrapperFactoryMock).createFromType(TYPE);
      inOrder.verify(entityWrapperMock).addValuesToEntity(ENTITY, nodeMock);
      verifyNoMoreInteractions(dbMock, entityWrapperFactoryMock, entityWrapperMock);
    }
  }

  @Test(expected = StorageException.class)
  public void getEntityThrowsStorageExceptionWhenEntityInstantiatorThrowsAnInstantiationException() throws Exception {
    getEntityThrowsStorageExceptionWhenEntityInstantiatorThrowsAnException(InstantiationException.class);
  }

  @Test(expected = StorageException.class)
  public void getEntityThrowsStorageExceptionWhenEntityInstantiatorThrowsAnIllegalAccessException() throws Exception {
    getEntityThrowsStorageExceptionWhenEntityInstantiatorThrowsAnException(IllegalAccessException.class);
  }

  private void getEntityThrowsStorageExceptionWhenEntityInstantiatorThrowsAnException(Class<? extends Exception> exceptionToThrow) throws Exception {
    // setup
    oneNodeIsFound(nodeMock);
    when(entityWrapperFactoryMock.createFromType(TYPE)).thenReturn(entityWrapperMock);
    doThrow(exceptionToThrow).when(entityInstantiatorMock).createInstanceOf(TYPE);

    try {
      // action
      instance.getEntity(TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, entityWrapperFactoryMock, entityWrapperMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(LABEL, ID_PROPERTY_NAME, ID);
      verifyNoMoreInteractions(dbMock);
      verifyZeroInteractions(entityWrapperFactoryMock, entityWrapperMock);
    }
  }
}
