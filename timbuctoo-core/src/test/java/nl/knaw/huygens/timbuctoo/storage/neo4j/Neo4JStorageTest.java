package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;

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
  private static final int SECOND_REVISION = 2;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final Label SYSTEM_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(SYSTEM_ENTITY_TYPE));
  private static final Label DOMAIN_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(DOMAIN_ENTITY_TYPE));
  private Node nodeMock;
  private SubADomainEntity domainEntity;
  private TestSystemEntityWrapper systemEntity;
  private static final String ID = "id";
  private GraphDatabaseService dbMock;
  private EntityConverter<TestSystemEntityWrapper> systemEntityConverterMock;
  private EntityConverterFactory entityConverterFactoryMock;
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
    systemEntityConverterMock = mock(EntityConverter.class);
    setupEntityConverterFactory();

    transactionMock = mock(Transaction.class);
    entityInstantiatorMock = mock(EntityInstantiator.class);
    idGeneratorMock = mock(IdGenerator.class);

    instance = new Neo4JStorage(dbMock, entityConverterFactoryMock, entityInstantiatorMock, idGeneratorMock);
  }

  private void setupEntityConverterFactory() throws Exception {
    entityConverterFactoryMock = mock(EntityConverterFactory.class);
    when(entityConverterFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntityConverterMock);
  }

  @Test
  public void addDomainEntitySavesTheProjectVersionAndThePrimitiveAndReturnsTheId() throws Exception {
    // setup
    dbMockCreatesNode(nodeMock);
    dbMockCreatesTransaction(transactionMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    EntityConverter<SubADomainEntity> domainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    EntityConverter<? super SubADomainEntity> primitiveDomainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeForSuperType(DOMAIN_ENTITY_TYPE);

    // action
    String actualId = instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());

    // verify
    verify(dbMock).beginTx();
    verify(dbMock).createNode();
    verify(domainEntityConverterMock).addValuesToNode( //
        argThat(equalTo(nodeMock)), // 
        argThat(likeBaseDomainEntity(DOMAIN_ENTITY_TYPE) //
            .withId(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    verify(primitiveDomainEntityConverterMock).addValuesToNode( //
        argThat(equalTo(nodeMock)), //
        argThat(likeBaseDomainEntity(DOMAIN_ENTITY_TYPE) //
            .withId(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    verify(transactionMock).success();
    verifyNoMoreInteractions(domainEntityConverterMock, primitiveDomainEntityConverterMock);
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityRollsBackTheTransactionAndThrowsAStorageExceptionWhenTheDomainEntityConverterThrowsAConversionException() throws Exception {
    // setup
    dbMockCreatesNode(nodeMock);
    dbMockCreatesTransaction(transactionMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    EntityConverter<SubADomainEntity> domainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(domainEntityConverterMock).addValuesToNode(nodeMock, domainEntity);
    EntityConverter<? super SubADomainEntity> primitiveDomainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeForSuperType(DOMAIN_ENTITY_TYPE);

    try {
      // action
      instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(domainEntityConverterMock).addValuesToNode( //
          argThat(equalTo(nodeMock)), // 
          argThat(likeBaseDomainEntity(DOMAIN_ENTITY_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verify(transactionMock).failure();
      verifyNoMoreInteractions(domainEntityConverterMock);
      verifyZeroInteractions(primitiveDomainEntityConverterMock);
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityRollsBackTheTransactionAndThrowsAStorageExceptionWhenThePrimitiveDomainEntityConverterThrowsAConversionException() throws Exception {
    // setup
    dbMockCreatesNode(nodeMock);
    dbMockCreatesTransaction(transactionMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    EntityConverter<SubADomainEntity> domainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    EntityConverter<? super SubADomainEntity> primitiveDomainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeForSuperType(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(primitiveDomainEntityConverterMock).addValuesToNode(nodeMock, domainEntity);

    try {
      // action
      instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(domainEntityConverterMock).addValuesToNode( //
          argThat(equalTo(nodeMock)), // 
          argThat(likeBaseDomainEntity(DOMAIN_ENTITY_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verify(primitiveDomainEntityConverterMock).addValuesToNode( //
          argThat(equalTo(nodeMock)), //
          argThat(likeBaseDomainEntity(DOMAIN_ENTITY_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verify(transactionMock).failure();
      verifyNoMoreInteractions(domainEntityConverterMock, primitiveDomainEntityConverterMock);
    }
  }

  private <T extends DomainEntity> EntityConverter<? super T> entityConverterFactoryCreatesAnEntityWrapperTypeForSuperType(Class<T> type) {
    @SuppressWarnings("unchecked")
    EntityConverter<? super T> entityWrapper = mock(EntityConverter.class);
    doReturn(entityWrapper).when(entityConverterFactoryMock).createForPrimitive(type);
    return entityWrapper;
  }

  private <T extends Entity> EntityConverter<T> entityConverterFactoryCreatesAnEntityWrapperTypeFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    EntityConverter<T> entityWrapper = mock(EntityConverter.class);
    when(entityConverterFactoryMock.createForType(type)).thenReturn(entityWrapper);
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
    InOrder inOrder = inOrder(dbMock, transactionMock, systemEntityConverterMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).createNode();
    inOrder.verify(systemEntityConverterMock).addValuesToNode(//
        argThat(equalTo(nodeMock)), // 
        argThat(likeTestSystemEntityWrapper() //
            .withId(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    inOrder.verify(transactionMock).success();
    verifyNoMoreInteractions(systemEntityConverterMock);
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

    when(entityConverterFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntityConverterMock);
    doThrow(ConversionException.class).when(systemEntityConverterMock).addValuesToNode(nodeMock, systemEntity);

    try {
      // action
      instance.addSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(systemEntityConverterMock).addValuesToNode(nodeMock, systemEntity);
      verifyNoMoreInteractions(systemEntityConverterMock);
      verify(transactionMock).failure();
    }
  }

  @Test
  public void getEntityReturnsTheItemWhenFound() throws Exception {

    oneNodeIsFound(SYSTEM_ENTITY_LABEL, nodeMock);
    when(entityConverterFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntityConverterMock);
    when(entityInstantiatorMock.createInstanceOf(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(systemEntity)));

    InOrder inOrder = inOrder(dbMock, entityConverterFactoryMock, systemEntityConverterMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(entityConverterFactoryMock).createForType(SYSTEM_ENTITY_TYPE);
    inOrder.verify(systemEntityConverterMock).addValuesToEntity(systemEntity, nodeMock);
    verifyNoMoreInteractions(dbMock, entityConverterFactoryMock, systemEntityConverterMock);
  }

  @Test
  public void getEntityReturnsTheLatestIfMoreThanOneItemIsFound() throws Exception {
    // setup
    Node nodeWithFirstRevision = createNodeWithRevision(FIRST_REVISION);
    Node nodeWithSecondRevision = createNodeWithRevision(SECOND_REVISION);
    Node nodeWithThirdRevision = createNodeWithRevision(3);
    multipleNodesAreFound(DOMAIN_ENTITY_LABEL, nodeWithFirstRevision, nodeWithThirdRevision, nodeWithSecondRevision);

    EntityConverter<SubADomainEntity> domainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    when(entityConverterFactoryMock.createForType(DOMAIN_ENTITY_TYPE)).thenReturn(domainEntityConverterMock);
    when(entityInstantiatorMock.createInstanceOf(DOMAIN_ENTITY_TYPE)).thenReturn(domainEntity);

    domainEntity.setId(ID);

    // action
    SubADomainEntity actualEntity = instance.getEntity(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(domainEntity)));

    InOrder inOrder = inOrder(dbMock, entityConverterFactoryMock, domainEntityConverterMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(entityConverterFactoryMock).createForType(DOMAIN_ENTITY_TYPE);
    inOrder.verify(domainEntityConverterMock).addValuesToEntity(domainEntity, nodeWithThirdRevision);
    verifyNoMoreInteractions(dbMock, entityConverterFactoryMock, domainEntityConverterMock);
  }

  private Node createNodeWithRevision(int revision) {
    Node otherFoundNode = mock(Node.class);
    when(otherFoundNode.getProperty(REVISION_PROPERTY_NAME)).thenReturn(revision);
    return otherFoundNode;
  }

  @Test
  public void getEntityReturnsNullIfNoItemIsFound() throws Exception {
    // setup
    noNodeIsFound(SYSTEM_ENTITY_LABEL);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(nullValue()));

    verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    verifyZeroInteractions(entityConverterFactoryMock);
  }

  private void oneNodeIsFound(Label label, Node nodeToBeFound) {
    @SuppressWarnings("unchecked")
    ResourceIterator<Node> nodeIterator = mock(ResourceIterator.class);
    when(nodeIterator.hasNext()).thenReturn(true, false);
    when(nodeIterator.next()).thenReturn(nodeToBeFound);

    nodesFound(label, nodeIterator);
  }

  private void noNodeIsFound(Label label) {
    @SuppressWarnings("unchecked")
    ResourceIterator<Node> nodeIterator = mock(ResourceIterator.class);
    when(nodeIterator.hasNext()).thenReturn(false);

    nodesFound(label, nodeIterator);
  }

  private void multipleNodesAreFound(Label label, Node node1, Node node2, Node node3) {
    @SuppressWarnings("unchecked")
    ResourceIterator<Node> nodeIterator = mock(ResourceIterator.class);
    when(nodeIterator.hasNext()).thenReturn(true, true, true, false);
    when(nodeIterator.next()).thenReturn(node1, node2, node3);

    nodesFound(label, nodeIterator);
  }

  private void nodesFound(Label label, ResourceIterator<Node> nodeIterator) {
    @SuppressWarnings("unchecked")
    ResourceIterable<Node> foundNodes = mock(ResourceIterable.class);
    when(foundNodes.iterator()).thenReturn(nodeIterator);
    when(dbMock.findNodesByLabelAndProperty(label, ID_PROPERTY_NAME, ID)).thenReturn(foundNodes);
  }

  @Test(expected = StorageException.class)
  public void getEntityThrowsStorageExceptionWhenEntityWrapperThrowsAConversionException() throws Exception {
    // setup
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, nodeMock);
    when(entityConverterFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntityConverterMock);
    when(entityInstantiatorMock.createInstanceOf(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntity);
    doThrow(ConversionException.class).when(systemEntityConverterMock).addValuesToEntity(systemEntity, nodeMock);

    try {
      // action
      instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, entityConverterFactoryMock, systemEntityConverterMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      inOrder.verify(entityConverterFactoryMock).createForType(SYSTEM_ENTITY_TYPE);
      inOrder.verify(systemEntityConverterMock).addValuesToEntity(systemEntity, nodeMock);
      verifyNoMoreInteractions(dbMock, entityConverterFactoryMock, systemEntityConverterMock);
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
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, nodeMock);
    when(entityConverterFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntityConverterMock);
    doThrow(exceptionToThrow).when(entityInstantiatorMock).createInstanceOf(SYSTEM_ENTITY_TYPE);

    try {
      // action
      instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, entityConverterFactoryMock, systemEntityConverterMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      verifyNoMoreInteractions(dbMock);
      verifyZeroInteractions(entityConverterFactoryMock, systemEntityConverterMock);
    }
  }

  @Test
  public void updateSystemEntityRetrievesTheEntityAndUpdatesTheData() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, nodeMock);
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(FIRST_REVISION);
    when(entityConverterFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntityConverterMock);

    systemEntity.setId(ID);
    systemEntity.setRev(FIRST_REVISION);
    Change oldModified = new Change();
    systemEntity.setModified(oldModified);

    instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);

    // verify
    InOrder inOrder = inOrder(dbMock, systemEntityConverterMock, transactionMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(systemEntityConverterMock).updateNode(argThat(equalTo(nodeMock)), //
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
    dbMockCreatesTransaction(transactionMock);
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, nodeMock);
    int newerRevision = 2;
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(newerRevision);

    systemEntity.setRev(FIRST_REVISION);
    systemEntity.setId(ID);
    try {
      // action
      instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      verify(transactionMock).failure();
      verifyZeroInteractions(entityConverterFactoryMock);
    }
  }

  @Test(expected = UpdateException.class)
  public void updateSystemEntityThrowsAnUpdateExceptionIfTheNodeIsOlderThanTheEntityWithTheUpdatedInformation() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, nodeMock);
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(FIRST_REVISION);

    int newerRevision = 2;
    systemEntity.setRev(newerRevision);
    systemEntity.setId(ID);
    try {
      // action
      instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      verify(transactionMock).failure();
      verifyZeroInteractions(entityConverterFactoryMock);
    }
  }

  @Test(expected = UpdateException.class)
  public void updateSystemEntityThrowsAnUpdateExceptionIfTheNodeCannotBeFound() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    noNodeIsFound(SYSTEM_ENTITY_LABEL);

    systemEntity.setRev(FIRST_REVISION);
    systemEntity.setId(ID);
    try {
      // action
      instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      verify(transactionMock).failure();
      verifyZeroInteractions(entityConverterFactoryMock);
    }
  }

  @Test(expected = ConversionException.class)
  public void updateSystemEntityThrowsAConversionExceptionWhenTheEntityCovnerterThrowsOne() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, nodeMock);
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(FIRST_REVISION);
    when(entityConverterFactoryMock.createForType(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntityConverterMock);
    doThrow(ConversionException.class).when(systemEntityConverterMock).updateNode(nodeMock, systemEntity);

    systemEntity.setRev(FIRST_REVISION);
    systemEntity.setId(ID);
    Change oldModified = new Change();
    systemEntity.setModified(oldModified);

    try {
      // action
      instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      verify(systemEntityConverterMock).updateNode(argThat(equalTo(nodeMock)), //
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
}
