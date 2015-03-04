package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.DomainEntityMatcher.likeDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipTypeMatcher.likeRelationshipType;
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

import java.util.Iterator;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.IteratorUtil;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

import com.google.common.collect.Lists;

public class Neo4JStorageTest {

  private static final Class<Node> NODE_TYPE = Node.class;
  private static final String RELATION_TYPE_ID = "typeId";
  private static final String RELATION_TARGET_ID = "targetId";
  private static final String RELATION_SOURCE_ID = "sourceId";
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final String PRIMITIVE_DOMAIN_ENTITY_NAME = TypeNames.getInternalName(PRIMITIVE_DOMAIN_ENTITY_TYPE);
  private static final String RELATION_TYPE_NAME = TypeNames.getInternalName(RelationType.class);
  private static final int FIRST_REVISION = 1;
  private static final int SECOND_REVISION = 2;
  private static final int THIRD_REVISION = 3;
  private static final int FOURTH_REVISION = 4;
  private static final Label DOMAIN_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(DOMAIN_ENTITY_TYPE));
  private static final Label PRIMITIVE_DOMAIN_ENTITY_LABEL = DynamicLabel.label(PRIMITIVE_DOMAIN_ENTITY_NAME);
  private static final Label SYSTEM_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(SYSTEM_ENTITY_TYPE));
  private static final Label RELATION_TYPE_LABEL = DynamicLabel.label(RELATION_TYPE_NAME);

  private Node nodeMock;
  private SubADomainEntity domainEntity;
  private TestSystemEntityWrapper systemEntity;
  private static final String ID = "id";
  private GraphDatabaseService dbMock;
  private EntityConverter<TestSystemEntityWrapper, Node> systemEntityConverterMock;
  private EntityConverterFactory entityConverterFactoryMock;
  private Neo4JStorage instance;
  private Transaction transactionMock;
  private EntityInstantiator entityInstantiatorMock;
  private IdGenerator idGeneratorMock;
  private RelationConverter relationConverterMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    domainEntity = new SubADomainEntity();
    systemEntity = new TestSystemEntityWrapper();
    nodeMock = mock(NODE_TYPE);
    dbMock = mock(GraphDatabaseService.class);
    systemEntityConverterMock = mock(RegularEntityConverter.class);
    setupEntityConverterFactory();

    transactionMock = mock(Transaction.class);
    entityInstantiatorMock = mock(EntityInstantiator.class);
    idGeneratorMock = mock(IdGenerator.class);
    relationConverterMock = mock(RelationConverter.class);

    TypeRegistry typeRegistry = TypeRegistry.getInstance().init("timbuctoo.model test.model");

    instance = new Neo4JStorage(dbMock, entityConverterFactoryMock, entityInstantiatorMock, idGeneratorMock, typeRegistry, relationConverterMock);
  }

  private void setupEntityConverterFactory() throws Exception {
    entityConverterFactoryMock = mock(EntityConverterFactory.class);
    when(entityConverterFactoryMock.createForTypeAndPropertyContainer(SYSTEM_ENTITY_TYPE, NODE_TYPE)).thenReturn(systemEntityConverterMock);
  }

  @Test
  public void addDomainEntitySavesTheProjectVersionAndThePrimitiveAndReturnsTheId() throws Exception {
    // setup
    dbMockCreatesNode(nodeMock);
    dbMockCreatesTransaction(transactionMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    EntityConverter<SubADomainEntity, Node> domainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    EntityConverter<? super SubADomainEntity, Node> primitiveDomainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeForSuperType(DOMAIN_ENTITY_TYPE);

    // action
    String actualId = instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());

    // verify
    verify(dbMock).beginTx();
    verify(dbMock).createNode();
    verify(domainEntityConverterMock).addValuesToPropertyContainer( //
        argThat(equalTo(nodeMock)), // 
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE) //
            .withId(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    verify(primitiveDomainEntityConverterMock).addValuesToPropertyContainer( //
        argThat(equalTo(nodeMock)), //
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE) //
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

    EntityConverter<SubADomainEntity, Node> domainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(domainEntityConverterMock).addValuesToPropertyContainer(nodeMock, domainEntity);
    EntityConverter<? super SubADomainEntity, Node> primitiveDomainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeForSuperType(DOMAIN_ENTITY_TYPE);

    try {
      // action
      instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(domainEntityConverterMock).addValuesToPropertyContainer( //
          argThat(equalTo(nodeMock)), // 
          argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE) //
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

    EntityConverter<SubADomainEntity, Node> domainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    EntityConverter<? super SubADomainEntity, Node> primitiveDomainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeForSuperType(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(primitiveDomainEntityConverterMock).addValuesToPropertyContainer(nodeMock, domainEntity);

    try {
      // action
      instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(domainEntityConverterMock).addValuesToPropertyContainer( //
          argThat(equalTo(nodeMock)), // 
          argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verify(primitiveDomainEntityConverterMock).addValuesToPropertyContainer( //
          argThat(equalTo(nodeMock)), //
          argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verify(transactionMock).failure();
      verifyNoMoreInteractions(domainEntityConverterMock, primitiveDomainEntityConverterMock);
    }
  }

  private <T extends DomainEntity> EntityConverter<? super T, Node> entityConverterFactoryCreatesAnEntityWrapperTypeForSuperType(Class<T> type) {
    @SuppressWarnings("unchecked")
    EntityConverter<? super T, Node> entityWrapper = mock(RegularEntityConverter.class);
    doReturn(entityWrapper).when(entityConverterFactoryMock).createForPrimitive(type, NODE_TYPE);
    return entityWrapper;
  }

  private <T extends Entity> EntityConverter<T, Node> entityConverterFactoryCreatesAnEntityWrapperTypeFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    EntityConverter<T, Node> entityWrapper = mock(RegularEntityConverter.class);
    when(entityConverterFactoryMock.createForTypeAndPropertyContainer(type, NODE_TYPE)).thenReturn(entityWrapper);
    return entityWrapper;
  }

  @Test
  public void addDomainEntityWithRelationAddsARelationShipToTheSourceAndReturnsTheId() throws Exception {
    // setup
    String name = "regularTypeName";

    Node sourceNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_SOURCE_ID, sourceNodeMock);
    Node targetNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_TARGET_ID, targetNodeMock);
    Node relationTypeNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(RELATION_TYPE_LABEL, RELATION_TYPE_ID, relationTypeNodeMock);

    Relationship relationShipMock = mock(Relationship.class);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(relationTypeNodeMock.getProperty(RelationType.REGULAR_NAME)).thenReturn(name);
    when(dbMock.beginTx()).thenReturn(transactionMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);

    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTypeId(RELATION_TYPE_ID);
    relation.setTypeType(RELATION_TYPE_NAME);

    // action
    String id = instance.addDomainEntity(SubARelation.class, relation, new Change());
    // verify
    assertThat(id, is(equalTo(ID)));

    verify(dbMock).beginTx();
    verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
    verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_TARGET_ID);
    verify(dbMock).findNodesByLabelAndProperty(RELATION_TYPE_LABEL, ID_PROPERTY_NAME, RELATION_TYPE_ID);
    verify(sourceNodeMock).createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)));
    // TODO refactor that the properties ProjectARelation and Relation are added. 
    verify(relationConverterMock).addValuesToRelationship( //
        argThat(equalTo(relationShipMock)), //
        argThat(likeDomainEntity(Relation.class) //
            .withId(ID) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    verify(transactionMock).success();
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityWithRelationThrowsAStorageExceptionWhenTheSourceCannotBeFound() throws Exception {
    // setup
    noNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_SOURCE_ID);
    when(dbMock.beginTx()).thenReturn(transactionMock);

    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
      verify(transactionMock).failure();
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityWithRelationThrowsAStorageExceptionWhenTheTargetCannotBeFound() throws Exception {
    // setup
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_SOURCE_ID, mock(NODE_TYPE));
    noNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_TARGET_ID);
    when(dbMock.beginTx()).thenReturn(transactionMock);

    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_TARGET_ID);
      verify(transactionMock).failure();
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityWithRelationThrowsAStorageExceptionWhenRelationTypeCannotBeFound() throws Exception {

    // setup
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_SOURCE_ID, mock(NODE_TYPE));
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_TARGET_ID, mock(NODE_TYPE));
    noNodeIsFound(RELATION_TYPE_LABEL, RELATION_TYPE_ID);
    when(dbMock.beginTx()).thenReturn(transactionMock);

    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTypeId(RELATION_TYPE_ID);
    relation.setTypeType(RELATION_TYPE_NAME);

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_TARGET_ID);
      verify(dbMock).findNodesByLabelAndProperty(RELATION_TYPE_LABEL, ID_PROPERTY_NAME, RELATION_TYPE_ID);
      verify(transactionMock).failure();
    }
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

    when(entityConverterFactoryMock.createForTypeAndPropertyContainer(SYSTEM_ENTITY_TYPE, NODE_TYPE)).thenReturn(systemEntityConverterMock);
    doThrow(ConversionException.class).when(systemEntityConverterMock).addValuesToPropertyContainer(nodeMock, systemEntity);

    try {
      // action
      instance.addSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(systemEntityConverterMock).addValuesToPropertyContainer(nodeMock, systemEntity);
      verifyNoMoreInteractions(systemEntityConverterMock);
      verify(transactionMock).failure();
    }
  }

  @Test
  public void getEntityReturnsTheItemWhenFound() throws Exception {

    oneNodeIsFound(SYSTEM_ENTITY_LABEL, ID, nodeMock);
    when(entityConverterFactoryMock.createForTypeAndPropertyContainer(SYSTEM_ENTITY_TYPE, NODE_TYPE)).thenReturn(systemEntityConverterMock);
    when(entityInstantiatorMock.createInstanceOf(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(systemEntity)));

    InOrder inOrder = inOrder(dbMock, entityConverterFactoryMock, systemEntityConverterMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(entityConverterFactoryMock).createForTypeAndPropertyContainer(SYSTEM_ENTITY_TYPE, NODE_TYPE);
    inOrder.verify(systemEntityConverterMock).addValuesToEntity(systemEntity, nodeMock);
    verifyNoMoreInteractions(dbMock, entityConverterFactoryMock, systemEntityConverterMock);
  }

  @Test
  public void getEntityReturnsTheLatestIfMoreThanOneItemIsFound() throws Exception {
    // setup
    Node nodeWithFirstRevision = createNodeWithRevision(FIRST_REVISION);
    Node nodeWithSecondRevision = createNodeWithRevision(SECOND_REVISION);
    Node nodeWithThirdRevision = createNodeWithRevision(THIRD_REVISION);
    multipleNodesAreFound(DOMAIN_ENTITY_LABEL, ID, nodeWithFirstRevision, nodeWithThirdRevision, nodeWithSecondRevision);

    EntityConverter<SubADomainEntity, Node> domainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    when(entityConverterFactoryMock.createForTypeAndPropertyContainer(DOMAIN_ENTITY_TYPE, NODE_TYPE)).thenReturn(domainEntityConverterMock);
    when(entityInstantiatorMock.createInstanceOf(DOMAIN_ENTITY_TYPE)).thenReturn(domainEntity);

    domainEntity.setId(ID);

    // action
    SubADomainEntity actualEntity = instance.getEntity(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(domainEntity)));

    InOrder inOrder = inOrder(dbMock, entityConverterFactoryMock, domainEntityConverterMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(entityConverterFactoryMock).createForTypeAndPropertyContainer(DOMAIN_ENTITY_TYPE, NODE_TYPE);
    inOrder.verify(domainEntityConverterMock).addValuesToEntity(domainEntity, nodeWithThirdRevision);
    verifyNoMoreInteractions(dbMock, entityConverterFactoryMock, domainEntityConverterMock);
  }

  private Node createNodeWithRevision(int revision) {
    Node otherFoundNode = mock(NODE_TYPE);
    when(otherFoundNode.getProperty(REVISION_PROPERTY_NAME)).thenReturn(revision);
    return otherFoundNode;
  }

  @Test
  public void getEntityReturnsNullIfNoItemIsFound() throws Exception {
    // setup
    noNodeIsFound(SYSTEM_ENTITY_LABEL, ID);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(nullValue()));

    verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    verifyZeroInteractions(entityConverterFactoryMock);
  }

  private void oneNodeIsFound(Label label, String id, Node nodeToBeFound) {
    List<Node> nodes = Lists.newArrayList(nodeToBeFound);

    ResourceIterator<Node> nodeIterator = IteratorUtil.asResourceIterator(nodes.iterator());

    nodesFound(label, nodeIterator, id);
  }

  private void noNodeIsFound(Label label, String id) {
    List<Node> emptyList = Lists.newArrayList();
    ResourceIterator<Node> nodeIterator = IteratorUtil.asResourceIterator(emptyList.iterator());

    nodesFound(label, nodeIterator, id);
  }

  private void multipleNodesAreFound(Label label, String id, Node node1, Node node2, Node node3) {
    List<Node> nodesList = Lists.newArrayList(node1, node2, node3);

    ResourceIterator<Node> nodeIterator = IteratorUtil.asResourceIterator(nodesList.iterator());

    nodesFound(label, nodeIterator, id);
  }

  private void nodesFound(Label label, ResourceIterator<Node> nodeIterator, String id) {
    Iterable<Node> nodes = IteratorUtil.asIterable(nodeIterator);

    ResourceIterable<Node> foundNodes = Iterables.asResourceIterable(nodes);

    when(dbMock.findNodesByLabelAndProperty(label, ID_PROPERTY_NAME, id)).thenReturn(foundNodes);
  }

  @Test(expected = StorageException.class)
  public void getEntityThrowsStorageExceptionWhenEntityWrapperThrowsAConversionException() throws Exception {
    // setup
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, ID, nodeMock);
    when(entityConverterFactoryMock.createForTypeAndPropertyContainer(SYSTEM_ENTITY_TYPE, NODE_TYPE)).thenReturn(systemEntityConverterMock);
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
      inOrder.verify(entityConverterFactoryMock).createForTypeAndPropertyContainer(SYSTEM_ENTITY_TYPE, NODE_TYPE);
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
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, ID, nodeMock);
    when(entityConverterFactoryMock.createForTypeAndPropertyContainer(SYSTEM_ENTITY_TYPE, NODE_TYPE)).thenReturn(systemEntityConverterMock);
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
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, ID, nodeMock);
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(FIRST_REVISION);
    when(entityConverterFactoryMock.createForTypeAndPropertyContainer(SYSTEM_ENTITY_TYPE, NODE_TYPE)).thenReturn(systemEntityConverterMock);

    systemEntity.setId(ID);
    systemEntity.setRev(FIRST_REVISION);
    Change oldModified = new Change();
    systemEntity.setModified(oldModified);

    instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);

    // verify
    InOrder inOrder = inOrder(dbMock, systemEntityConverterMock, transactionMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
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
    dbMockCreatesTransaction(transactionMock);
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, ID, nodeMock);
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
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, ID, nodeMock);
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
    noNodeIsFound(SYSTEM_ENTITY_LABEL, ID);

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
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, ID, nodeMock);
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(FIRST_REVISION);
    when(entityConverterFactoryMock.createForTypeAndPropertyContainer(SYSTEM_ENTITY_TYPE, NODE_TYPE)).thenReturn(systemEntityConverterMock);
    doThrow(ConversionException.class).when(systemEntityConverterMock).updatePropertyContainer(nodeMock, systemEntity);

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
  public void updateDomainEntityRetrievesTheNodeAndUpdatesItsValues() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    oneNodeIsFound(DOMAIN_ENTITY_LABEL, ID, nodeMock);
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(FIRST_REVISION);
    EntityConverter<SubADomainEntity, Node> domainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);;
    when(entityConverterFactoryMock.createForTypeAndPropertyContainer(DOMAIN_ENTITY_TYPE, NODE_TYPE)).thenReturn(domainEntityConverterMock);

    domainEntity.setId(ID);
    domainEntity.setRev(FIRST_REVISION);
    Change oldModified = new Change();
    domainEntity.setModified(oldModified);

    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());

    // verify
    InOrder inOrder = inOrder(dbMock, domainEntityConverterMock, transactionMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(domainEntityConverterMock).updatePropertyContainer(argThat(equalTo(nodeMock)), //
        argThat(likeDomainEntity(SubADomainEntity.class) //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)));
    inOrder.verify(domainEntityConverterMock).updateModifiedAndRev(argThat(equalTo(nodeMock)), //
        argThat(likeDomainEntity(SubADomainEntity.class) //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)));
    inOrder.verify(transactionMock).success();
    verifyNoMoreInteractions(dbMock, domainEntityConverterMock);
  }

  @Test
  public void updateDomainEntityUpdatesTheLatestIfMultipleAreFound() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    Node nodeWithFirstRevision = createNodeWithRevision(FIRST_REVISION);
    Node nodeWithSecondRevision = createNodeWithRevision(SECOND_REVISION);
    Node nodeWithThirdRevision = createNodeWithRevision(THIRD_REVISION);

    multipleNodesAreFound(DOMAIN_ENTITY_LABEL, ID, nodeWithFirstRevision, nodeWithThirdRevision, nodeWithSecondRevision);
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(SECOND_REVISION);
    EntityConverter<SubADomainEntity, Node> domainEntityConverterMock = entityConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    //    when(entityConverterFactoryMock.createForTypeAndPropertyContainer(DOMAIN_ENTITY_TYPE, Node.class)).thenReturn(domainEntityConverterMock);

    domainEntity.setId(ID);
    domainEntity.setRev(THIRD_REVISION);
    Change oldModified = new Change();
    domainEntity.setModified(oldModified);

    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());

    // verify
    InOrder inOrder = inOrder(dbMock, domainEntityConverterMock, transactionMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(domainEntityConverterMock).updatePropertyContainer(argThat(equalTo(nodeWithThirdRevision)), //
        argThat(likeDomainEntity(SubADomainEntity.class) //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(FOURTH_REVISION)));
    inOrder.verify(domainEntityConverterMock).updateModifiedAndRev(argThat(equalTo(nodeWithThirdRevision)), //
        argThat(likeDomainEntity(SubADomainEntity.class) //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(FOURTH_REVISION)));
    inOrder.verify(transactionMock).success();
    verifyNoMoreInteractions(dbMock, domainEntityConverterMock);
  }

  @Test(expected = UpdateException.class)
  public void updateDomainEntityThrowsAnUpdateExceptionWhenTheEntityCannotBeFound() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    noNodeIsFound(DOMAIN_ENTITY_LABEL, ID);

    domainEntity.setId(ID);
    domainEntity.setRev(FIRST_REVISION);
    Change oldModified = new Change();
    domainEntity.setModified(oldModified);

    try {
      // action
      instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, transactionMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      inOrder.verify(transactionMock).failure();
      verifyZeroInteractions(entityConverterFactoryMock);
      verifyNoMoreInteractions(dbMock);
    }
  }

  @Test(expected = UpdateException.class)
  public void updateDomainEntityThrowsAnUpdateExceptionWhenRevOfTheNodeIsHigherThanThatOfTheEntity() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    oneNodeIsFound(DOMAIN_ENTITY_LABEL, ID, nodeMock);
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(SECOND_REVISION);

    domainEntity.setId(ID);
    domainEntity.setRev(FIRST_REVISION);
    Change oldModified = new Change();
    domainEntity.setModified(oldModified);

    try {
      // action
      instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, transactionMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      inOrder.verify(transactionMock).failure();
      verifyZeroInteractions(entityConverterFactoryMock);
      verifyNoMoreInteractions(dbMock);
    }
  }

  @Test(expected = UpdateException.class)
  public void updateDomainEntityThrowsAnUpdateExceptionWhenRevOfTheNodeIsLowerThanThatOfTheEntity() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    oneNodeIsFound(DOMAIN_ENTITY_LABEL, ID, nodeMock);
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(FIRST_REVISION);

    domainEntity.setId(ID);
    domainEntity.setRev(SECOND_REVISION);
    Change oldModified = new Change();
    domainEntity.setModified(oldModified);

    try {
      // action
      instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, transactionMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      inOrder.verify(transactionMock).failure();
      verifyZeroInteractions(entityConverterFactoryMock);
      verifyNoMoreInteractions(dbMock);
    }
  }

  @Test
  public void deleteSystemEntityFirstRemovesTheNodesRelationShipsAndThenTheNodeItselfTheDatabase() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    Relationship relMock1 = mock(Relationship.class);
    Relationship relMock2 = mock(Relationship.class);
    nodeHaseRelationsShips(nodeMock, relMock1, relMock2);
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, ID, nodeMock);

    // action
    int numDeleted = instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(numDeleted, is(equalTo(1)));
    InOrder inOrder = inOrder(dbMock, nodeMock, relMock1, relMock2, transactionMock);
    inOrder.verify(dbMock).beginTx();
    verifyNodeAndItsRelationAreDelete(nodeMock, relMock1, relMock2, inOrder);
    inOrder.verify(transactionMock).success();

  }

  private void nodeHaseRelationsShips(Node node, Relationship relMock1, Relationship relMock2) {
    Iterator<Relationship> relationshipIterator = Lists.newArrayList(relMock1, relMock2).iterator();
    Iterable<Relationship> relationships = IteratorUtil.asIterable(relationshipIterator);

    when(node.getRelationships()).thenReturn(relationships);
  }

  @Test
  public void deleteSystemEntityReturns0WhenTheEntityCannotBeFound() throws Exception {
    // setup
    noNodeIsFound(SYSTEM_ENTITY_LABEL, ID);
    dbMockCreatesTransaction(transactionMock);

    // action
    int numDeleted = instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);
    // verify
    assertThat(numDeleted, is(equalTo(0)));
    verify(dbMock).beginTx();
    verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    verify(transactionMock).success();
  }

  @Test
  public void deleteDomainEntityFirstRemovesTheNodesRelationShipsAndThenTheNodeItselfTheDatabase() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    Relationship relMock1 = mock(Relationship.class);
    Relationship relMock2 = mock(Relationship.class);
    nodeHaseRelationsShips(nodeMock, relMock1, relMock2);
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID, nodeMock);

    // action
    instance.deleteDomainEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, new Change());

    // verify
    InOrder inOrder = inOrder(dbMock, nodeMock, relMock1, relMock2, transactionMock);
    inOrder.verify(dbMock).beginTx();
    verifyNodeAndItsRelationAreDelete(nodeMock, relMock1, relMock2, inOrder);
    inOrder.verify(transactionMock).success();

  }

  @Test
  public void deleteDomainEntityRemovesAllTheFoundNodes() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    Relationship relMock1 = mock(Relationship.class);
    Relationship relMock2 = mock(Relationship.class);
    nodeHaseRelationsShips(nodeMock, relMock1, relMock2);

    Relationship relMock3 = mock(Relationship.class);
    Relationship relMock4 = mock(Relationship.class);
    Node nodeMock2 = mock(NODE_TYPE);
    nodeHaseRelationsShips(nodeMock2, relMock3, relMock4);

    Relationship relMock5 = mock(Relationship.class);
    Relationship relMock6 = mock(Relationship.class);
    Node nodeMock3 = mock(NODE_TYPE);
    nodeHaseRelationsShips(nodeMock3, relMock5, relMock6);

    multipleNodesAreFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID, nodeMock, nodeMock2, nodeMock3);

    // action
    instance.deleteDomainEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, new Change());

    // verify
    InOrder inOrder = inOrder(dbMock, nodeMock, relMock1, relMock2, nodeMock2, relMock3, relMock4, nodeMock3, relMock5, relMock6, transactionMock);
    inOrder.verify(dbMock).beginTx();
    verifyNodeAndItsRelationAreDelete(nodeMock, relMock1, relMock2, inOrder);
    verifyNodeAndItsRelationAreDelete(nodeMock2, relMock3, relMock4, inOrder);
    verifyNodeAndItsRelationAreDelete(nodeMock3, relMock5, relMock6, inOrder);
    inOrder.verify(transactionMock).success();
  }

  private void verifyNodeAndItsRelationAreDelete(Node node, Relationship relMock1, Relationship relMock2, InOrder inOrder) {
    inOrder.verify(node).getRelationships();
    inOrder.verify(relMock1).delete();
    inOrder.verify(relMock2).delete();
    inOrder.verify(node).delete();
  }

  @Test(expected = NoSuchEntityException.class)
  public void deleteDomainEntityThrowsANoSuchEntityExceptionWhenTheEntityCannotBeFound() throws Exception {
    // setup
    noNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID);
    dbMockCreatesTransaction(transactionMock);

    try {
      // action
      instance.deleteDomainEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      verify(transactionMock).failure();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteThrowsAnIllegalArgumentExceptionWhenTheEntityIsNotAPrimitiveDomainEntity() throws Exception {

    try {
      // action
      instance.deleteDomainEntity(DOMAIN_ENTITY_TYPE, ID, new Change());
    } finally {
      // verify
      verifyZeroInteractions(dbMock);
    }
  }

}
