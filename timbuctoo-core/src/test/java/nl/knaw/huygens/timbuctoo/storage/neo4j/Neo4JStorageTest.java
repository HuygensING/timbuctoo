package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.TestSystemEntityWrapperMatcher.likeTestSystemEntityWrapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.argThat;
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

import test.model.TestSystemEntityWrapper;

public class Neo4JStorageTest {

  private static final int FIRST_REVISION = 1;
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final Label LABEL = DynamicLabel.label(TypeNames.getInternalName(TYPE));
  private Node nodeMock;
  private TestSystemEntityWrapper entity;
  private static final String ID = "id";
  private GraphDatabaseService dbMock;
  private EntityTypeWrapper<TestSystemEntityWrapper> entityWrapperMock;
  private EntityTypeWrapperFactory entityWrapperFactoryMock;
  private Neo4JStorage instance;
  private Transaction transactionMock;
  private EntityInstantiator entityInstantiatorMock;
  private IdGenerator idGeneratorMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    entity = new TestSystemEntityWrapper();
    nodeMock = mock(Node.class);
    dbMock = mock(GraphDatabaseService.class);
    entityWrapperMock = mock(EntityTypeWrapper.class);
    setupEntityWrapperFactory();

    transactionMock = mock(Transaction.class);
    entityInstantiatorMock = mock(EntityInstantiator.class);
    idGeneratorMock = mock(IdGenerator.class);

    instance = new Neo4JStorage(dbMock, entityWrapperFactoryMock, entityInstantiatorMock, idGeneratorMock);
  }

  private void setupEntityWrapperFactory() throws Exception {
    entityWrapperFactoryMock = mock(EntityTypeWrapperFactory.class);
    when(entityWrapperFactoryMock.createFromType(TYPE)).thenReturn(entityWrapperMock);
  }

  @Test
  public void addSystemEntitySavesTheSystemAsNodeAndReturnsItsId() throws Exception {
    when(dbMock.beginTx()).thenReturn(transactionMock);
    when(dbMock.createNode()).thenReturn(nodeMock);
    when(idGeneratorMock.nextIdFor(TYPE)).thenReturn(ID);

    // action
    String actualId = instance.addSystemEntity(TYPE, entity);

    // verify

    InOrder inOrder = inOrder(dbMock, transactionMock, entityWrapperMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).createNode();
    inOrder.verify(entityWrapperMock).addValuesToNode(//
        argThat(equalTo(nodeMock)), // 
        argThat(likeTestSystemEntityWrapper() //
            .with(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    inOrder.verify(transactionMock).success();
    verifyNoMoreInteractions(entityWrapperMock);
  }

  @Test(expected = StorageException.class)
  public void addSystemEntityRollsBackTheTransactionAndThrowsStorageExceptionObjectrapperThrowsAConversionException() throws Exception {
    when(dbMock.beginTx()).thenReturn(transactionMock);
    when(dbMock.createNode()).thenReturn(nodeMock);

    when(entityWrapperFactoryMock.createFromType(TYPE)).thenReturn(entityWrapperMock);
    doThrow(ConversionException.class).when(entityWrapperMock).addValuesToNode(nodeMock, entity);

    try {
      // action
      instance.addSystemEntity(TYPE, entity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(entityWrapperMock).addValuesToNode(nodeMock, entity);
      verifyNoMoreInteractions(entityWrapperMock);
      verify(transactionMock).failure();
    }
  }

  @Test
  public void getEntityReturnsTheItemWhenFound() throws Exception {

    oneNodeIsFound(nodeMock);
    when(entityWrapperFactoryMock.createFromType(TYPE)).thenReturn(entityWrapperMock);
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(entity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(entity)));

    InOrder inOrder = inOrder(dbMock, entityWrapperFactoryMock, entityWrapperMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(entityWrapperFactoryMock).createFromType(TYPE);
    inOrder.verify(entityWrapperMock).addValuesToEntity(entity, nodeMock);
    verifyNoMoreInteractions(dbMock, entityWrapperFactoryMock, entityWrapperMock);
  }

  @Test
  // This is what mongo did by findOne
  public void getEntityReturnsTheFirstIfMoreThanOneItemIsFound() throws Exception {
    // setup
    Node otherFoundNode = mock(Node.class);
    multipleNodesAreFound(nodeMock, otherFoundNode);
    when(entityWrapperFactoryMock.createFromType(TYPE)).thenReturn(entityWrapperMock);
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(entity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(entity)));

    InOrder inOrder = inOrder(dbMock, entityWrapperFactoryMock, entityWrapperMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(entityWrapperFactoryMock).createFromType(TYPE);
    inOrder.verify(entityWrapperMock).addValuesToEntity(entity, nodeMock);
    verifyNoMoreInteractions(dbMock, entityWrapperFactoryMock, entityWrapperMock);
  }

  @Test
  public void getEntityReturnsNullIfNoItemIsFound() throws Exception {
    // setup
    nodeNodeIsFound();

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(TYPE, ID);

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
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(entity);
    doThrow(ConversionException.class).when(entityWrapperMock).addValuesToEntity(entity, nodeMock);

    try {
      // action
      instance.getEntity(TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, entityWrapperFactoryMock, entityWrapperMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(LABEL, ID_PROPERTY_NAME, ID);
      inOrder.verify(entityWrapperFactoryMock).createFromType(TYPE);
      inOrder.verify(entityWrapperMock).addValuesToEntity(entity, nodeMock);
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
