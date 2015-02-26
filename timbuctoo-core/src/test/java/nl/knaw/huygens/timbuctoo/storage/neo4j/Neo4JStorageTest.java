package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.BaseDomainEntityMatcher.likeBaseDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.TestSystemEntityWrapperMatcher.likeTestSystemEntityWrapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
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
import test.model.projecta.SubADomainEntity;

public class Neo4JStorageTest {

  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final int FIRST_REVISION = 1;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final Label SYSTEM_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(SYSTEM_ENTITY_TYPE));
  private Node nodeMock;
  private SubADomainEntity domainEntity;
  private TestSystemEntityWrapper systemEntity;
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
    domainEntity = new SubADomainEntity();
    systemEntity = new TestSystemEntityWrapper();
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
    when(entityWrapperFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(entityWrapperMock);
  }

  @Test
  public void addDomainEntitySavesTheProjectVersionAndThePrimitiveAndReturnsTheId() throws Exception {
    // setup
    dbMockCreatesNode(nodeMock);
    dbMockCreatesTransaction(transactionMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    EntityTypeWrapper<SubADomainEntity> domainEntityTypeWrapperMock = entityTypeWrapperFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    EntityTypeWrapper<? super SubADomainEntity> primitiveDomainEntityTypeWrapperMock = entityTypeWrapperFactoryCreatesAnEntityWrapperTypeForSuperType(DOMAIN_ENTITY_TYPE);

    // action
    String actualId = instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());

    // verify
    verify(dbMock).beginTx();
    verify(dbMock).createNode();
    verify(domainEntityTypeWrapperMock).addValuesToNode( //
        argThat(equalTo(nodeMock)), // 
        argThat(likeBaseDomainEntity(DOMAIN_ENTITY_TYPE) //
            .withId(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    verify(primitiveDomainEntityTypeWrapperMock).addValuesToNode( //
        argThat(equalTo(nodeMock)), //
        argThat(likeBaseDomainEntity(DOMAIN_ENTITY_TYPE) //
            .withId(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    verify(transactionMock).success();
    verifyNoMoreInteractions(domainEntityTypeWrapperMock, primitiveDomainEntityTypeWrapperMock);
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityRollsBackTheTransactionAndThrowsAStorageExceptionWhenTheDomainEntityTypeWrapperThrowsAConversionException() throws Exception {
    // setup
    dbMockCreatesNode(nodeMock);
    dbMockCreatesTransaction(transactionMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    EntityTypeWrapper<SubADomainEntity> domainEntityTypeWrapperMock = entityTypeWrapperFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(domainEntityTypeWrapperMock).addValuesToNode(nodeMock, domainEntity);
    EntityTypeWrapper<? super SubADomainEntity> primitiveDomainEntityTypeWrapperMock = entityTypeWrapperFactoryCreatesAnEntityWrapperTypeForSuperType(DOMAIN_ENTITY_TYPE);

    try {
      // action
      instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(domainEntityTypeWrapperMock).addValuesToNode( //
          argThat(equalTo(nodeMock)), // 
          argThat(likeBaseDomainEntity(DOMAIN_ENTITY_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verify(transactionMock).failure();
      verifyNoMoreInteractions(domainEntityTypeWrapperMock);
      verifyZeroInteractions(primitiveDomainEntityTypeWrapperMock);
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityRollsBackTheTransactionAndThrowsAStorageExceptionWhenThePrimitiveDomainEntityTypeWrapperThrowsAConversionException() throws Exception {
    // setup
    dbMockCreatesNode(nodeMock);
    dbMockCreatesTransaction(transactionMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    EntityTypeWrapper<SubADomainEntity> domainEntityTypeWrapperMock = entityTypeWrapperFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    EntityTypeWrapper<? super SubADomainEntity> primitiveDomainEntityTypeWrapperMock = entityTypeWrapperFactoryCreatesAnEntityWrapperTypeForSuperType(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(primitiveDomainEntityTypeWrapperMock).addValuesToNode(nodeMock, domainEntity);

    try {
      // action
      instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(domainEntityTypeWrapperMock).addValuesToNode( //
          argThat(equalTo(nodeMock)), // 
          argThat(likeBaseDomainEntity(DOMAIN_ENTITY_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verify(primitiveDomainEntityTypeWrapperMock).addValuesToNode( //
          argThat(equalTo(nodeMock)), //
          argThat(likeBaseDomainEntity(DOMAIN_ENTITY_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verify(transactionMock).failure();
      verifyNoMoreInteractions(domainEntityTypeWrapperMock, primitiveDomainEntityTypeWrapperMock);
    }
  }

  private <T extends DomainEntity> EntityTypeWrapper<? super T> entityTypeWrapperFactoryCreatesAnEntityWrapperTypeForSuperType(Class<T> type) {
    @SuppressWarnings("unchecked")
    EntityTypeWrapper<? super T> entityWrapper = mock(EntityTypeWrapper.class);
    doReturn(entityWrapper).when(entityWrapperFactoryMock).createForPrimitive(type);
    return entityWrapper;
  }

  private <T extends Entity> EntityTypeWrapper<T> entityTypeWrapperFactoryCreatesAnEntityWrapperTypeFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    EntityTypeWrapper<T> entityWrapper = mock(EntityTypeWrapper.class);
    when(entityWrapperFactoryMock.createForType(type)).thenReturn(entityWrapper);
    return entityWrapper;
  }

  @Test
  public void addSystemEntitySavesTheSystemAsNodeAndReturnsItsId() throws Exception {
    dbMockCreatesTransaction(transactionMock);
    dbMockCreatesNode(nodeMock);
    idGeneratorMockCreatesIDFor(SYSTEM_ENTITY_TYPE, ID);

    // action
    String actualId = instance.addSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);

    // verify
    InOrder inOrder = inOrder(dbMock, transactionMock, entityWrapperMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).createNode();
    inOrder.verify(entityWrapperMock).addValuesToNode(//
        argThat(equalTo(nodeMock)), // 
        argThat(likeTestSystemEntityWrapper() //
            .withId(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    inOrder.verify(transactionMock).success();
    verifyNoMoreInteractions(entityWrapperMock);
  }

  private void idGeneratorMockCreatesIDFor(Class<? extends Entity> type, String id) {
    when(idGeneratorMock.nextIdFor(type)).thenReturn(id);
  }

  private void dbMockCreatesNode(Node node) {
    when(dbMock.createNode()).thenReturn(node);
  }

  private void dbMockCreatesTransaction(Transaction transaction) {
    when(dbMock.beginTx()).thenReturn(transaction);
  }

  @Test(expected = StorageException.class)
  public void addSystemEntityRollsBackTheTransactionAndThrowsStorageExceptionObjectrapperThrowsAConversionException() throws Exception {
    dbMockCreatesTransaction(transactionMock);
    dbMockCreatesNode(nodeMock);

    when(entityWrapperFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(entityWrapperMock);
    doThrow(ConversionException.class).when(entityWrapperMock).addValuesToNode(nodeMock, systemEntity);

    try {
      // action
      instance.addSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(entityWrapperMock).addValuesToNode(nodeMock, systemEntity);
      verifyNoMoreInteractions(entityWrapperMock);
      verify(transactionMock).failure();
    }
  }

  @Test
  public void getEntityReturnsTheItemWhenFound() throws Exception {

    oneNodeIsFound(nodeMock);
    when(entityWrapperFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(entityWrapperMock);
    when(entityInstantiatorMock.createInstanceOf(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(systemEntity)));

    InOrder inOrder = inOrder(dbMock, entityWrapperFactoryMock, entityWrapperMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(entityWrapperFactoryMock).createForType(SYSTEM_ENTITY_TYPE);
    inOrder.verify(entityWrapperMock).addValuesToEntity(systemEntity, nodeMock);
    verifyNoMoreInteractions(dbMock, entityWrapperFactoryMock, entityWrapperMock);
  }

  @Test
  // This is what mongo did by findOne
  public void getEntityReturnsTheFirstIfMoreThanOneItemIsFound() throws Exception {
    // setup
    Node otherFoundNode = mock(Node.class);
    multipleNodesAreFound(nodeMock, otherFoundNode);
    when(entityWrapperFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(entityWrapperMock);
    when(entityInstantiatorMock.createInstanceOf(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(systemEntity)));

    InOrder inOrder = inOrder(dbMock, entityWrapperFactoryMock, entityWrapperMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(entityWrapperFactoryMock).createForType(SYSTEM_ENTITY_TYPE);
    inOrder.verify(entityWrapperMock).addValuesToEntity(systemEntity, nodeMock);
    verifyNoMoreInteractions(dbMock, entityWrapperFactoryMock, entityWrapperMock);
  }

  @Test
  public void getEntityReturnsNullIfNoItemIsFound() throws Exception {
    // setup
    nodeNodeIsFound();

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(nullValue()));

    verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
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
    when(dbMock.findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID)).thenReturn(foundNodes);
  }

  @Test(expected = StorageException.class)
  public void getEntityThrowsStorageExceptionWhenEntityWrapperThrowsAConversionException() throws Exception {
    // setup
    oneNodeIsFound(nodeMock);
    when(entityWrapperFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(entityWrapperMock);
    when(entityInstantiatorMock.createInstanceOf(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntity);
    doThrow(ConversionException.class).when(entityWrapperMock).addValuesToEntity(systemEntity, nodeMock);

    try {
      // action
      instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, entityWrapperFactoryMock, entityWrapperMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      inOrder.verify(entityWrapperFactoryMock).createForType(SYSTEM_ENTITY_TYPE);
      inOrder.verify(entityWrapperMock).addValuesToEntity(systemEntity, nodeMock);
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
    when(entityWrapperFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(entityWrapperMock);
    doThrow(exceptionToThrow).when(entityInstantiatorMock).createInstanceOf(SYSTEM_ENTITY_TYPE);

    try {
      // action
      instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, entityWrapperFactoryMock, entityWrapperMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      verifyNoMoreInteractions(dbMock);
      verifyZeroInteractions(entityWrapperFactoryMock, entityWrapperMock);
    }
  }
}
