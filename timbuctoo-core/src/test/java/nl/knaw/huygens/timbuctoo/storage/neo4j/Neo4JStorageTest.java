package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.DomainEntityMatcher.likeDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.Neo4JStorage.RELATIONSHIP_ID_INDEX;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipTypeMatcher.likeRelationshipType;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.TestSystemEntityWrapperMatcher.likeTestSystemEntityWrapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
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
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.IteratorUtil;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

import com.google.common.collect.Lists;

public class Neo4JStorageTest {

  private static final String PID = "pid";
  private static final Class<Relationship> RELATIONSHIP_TYPE = Relationship.class;
  private static final Class<Node> NODE_TYPE = Node.class;
  private static final String RELATION_TYPE_ID = "typeId";
  private static final String RELATION_TARGET_ID = "targetId";
  private static final String RELATION_SOURCE_ID = "sourceId";
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final Class<RelationType> RELATIONTYPE_TYPE = RelationType.class;
  private static final String PRIMITIVE_DOMAIN_ENTITY_NAME = TypeNames.getInternalName(PRIMITIVE_DOMAIN_ENTITY_TYPE);
  private static final String RELATION_TYPE_NAME = TypeNames.getInternalName(RELATIONTYPE_TYPE);
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
  private PropertyContainerConverterFactory propertyContainerConverterFactoryMock;
  private Neo4JStorage instance;
  private Transaction transactionMock;
  private EntityInstantiator entityInstantiatorMock;
  private IdGenerator idGeneratorMock;

  @Before
  public void setUp() throws Exception {
    domainEntity = new SubADomainEntity();
    systemEntity = new TestSystemEntityWrapper();
    nodeMock = mock(NODE_TYPE);
    setupDBTransaction();
    setupEntityConverterFactory();

    entityInstantiatorMock = mock(EntityInstantiator.class);
    idGeneratorMock = mock(IdGenerator.class);

    TypeRegistry typeRegistry = TypeRegistry.getInstance().init("timbuctoo.model test.model");

    instance = new Neo4JStorage(dbMock, propertyContainerConverterFactoryMock, entityInstantiatorMock, idGeneratorMock, typeRegistry);
  }

  private void setupDBTransaction() {
    transactionMock = mock(Transaction.class);
    dbMock = mock(GraphDatabaseService.class);
    when(dbMock.beginTx()).thenReturn(transactionMock);
  }

  private void setupEntityConverterFactory() throws Exception {
    propertyContainerConverterFactoryMock = mock(PropertyContainerConverterFactory.class);
  }

  @Test
  public void addDomainEntitySavesTheProjectVersionAndThePrimitiveAndReturnsTheId() throws Exception {
    // setup
    dbMockCreatesNode(nodeMock);
    dbMockCreatesTransaction(transactionMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    NodeConverter<? super SubADomainEntity> compositeConverter = propertyContainerConverterFactoryHasCompositeConverterFor(DOMAIN_ENTITY_TYPE);

    // action
    String actualId = instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());

    // verify
    verify(dbMock).beginTx();
    verify(dbMock).createNode();
    verify(compositeConverter).addValuesToPropertyContainer( //
        argThat(equalTo(nodeMock)), // 
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE) //
            .withId(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    verify(transactionMock).success();
  }

  private <T extends DomainEntity> NodeConverter<? super T> propertyContainerConverterFactoryHasCompositeConverterFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    NodeConverter<? super T> converter = mock(NodeConverter.class);
    doReturn(converter).when(propertyContainerConverterFactoryMock).createCompositeForType(type);
    return converter;
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityRollsBackTheTransactionAndThrowsAStorageExceptionWhenTheDomainEntityConverterThrowsAConversionException() throws Exception {
    // setup
    dbMockCreatesNode(nodeMock);
    dbMockCreatesTransaction(transactionMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    NodeConverter<? super SubADomainEntity> compositeConverter = propertyContainerConverterFactoryHasCompositeConverterFor(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(compositeConverter).addValuesToPropertyContainer(nodeMock, domainEntity);

    try {
      // action
      instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).createNode();
      verify(compositeConverter).addValuesToPropertyContainer( //
          argThat(equalTo(nodeMock)), // 
          argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verify(transactionMock).failure();
      verifyNoMoreInteractions(compositeConverter);
    }
  }

  private <T extends Entity> NodeConverter<T> propertyContainerConverterFactoryCreatesAnEntityWrapperTypeFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    NodeConverter<T> nodeConverter = mock(NodeConverter.class);
    when(propertyContainerConverterFactoryMock.createForType(argThat(equalTo(type)))).thenReturn(nodeConverter);
    return nodeConverter;
  }

  @Test
  public void addDomainEntityWithRelationAddsARelationshipToTheSourceAndReturnsTheId() throws Exception {
    // setup
    String name = "regularTypeName";

    Node sourceNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_SOURCE_ID, sourceNodeMock);
    Node targetNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_TARGET_ID, targetNodeMock);
    relationTypeWithRegularNameExists(name);

    RelationshipIndex indexMock = dbHasRelationshipIndexWithName(RELATIONSHIP_ID_INDEX);
    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    RelationshipConverter<SubARelation> subARelationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    RelationshipConverter<? super SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterForPrimitive(RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
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

    InOrder inOrder = inOrder(dbMock, sourceNodeMock, subARelationConverterMock, relationConverterMock, indexMock, transactionMock);

    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
    inOrder.verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_TARGET_ID);
    inOrder.verify(dbMock).findNodesByLabelAndProperty(RELATION_TYPE_LABEL, ID_PROPERTY_NAME, RELATION_TYPE_ID);
    inOrder.verify(sourceNodeMock).createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)));

    inOrder.verify(subARelationConverterMock).addValuesToPropertyContainer( //
        argThat(equalTo(relationShipMock)), //
        argThat(likeDomainEntity(RELATION_TYPE) //
            .withId(ID) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));
    inOrder.verify(relationConverterMock).addValuesToPropertyContainer( //
        argThat(equalTo(relationShipMock)), //
        argThat(likeDomainEntity(RELATION_TYPE) //
            .withId(ID) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)));

    inOrder.verify(indexMock).add(relationShipMock, ID_PROPERTY_NAME, id);
    inOrder.verify(transactionMock).success();
  }

  private NodeConverter<RelationType> relationTypeWithRegularNameExists(String name) throws Exception {
    Node relationTypeNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(RELATION_TYPE_LABEL, RELATION_TYPE_ID, relationTypeNodeMock);
    NodeConverter<RelationType> relationTypeConverter = propertyContainerConverterFactoryCreatesAnEntityWrapperTypeFor(RELATIONTYPE_TYPE);
    RelationType relationType = new RelationType();
    relationType.setRegularName(name);

    when(entityInstantiatorMock.createInstanceOf(RELATIONTYPE_TYPE)).thenReturn(relationType);
    return relationTypeConverter;
  }

  private <T extends Relation> RelationshipConverter<T> propertyContainerConverterFactoryHasRelationshipConverterFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    RelationshipConverter<T> relationshipConverter = mock(RelationshipConverter.class);
    when(propertyContainerConverterFactoryMock.createForRelation(type)).thenReturn(relationshipConverter);

    return relationshipConverter;
  }

  private <T extends Relation> RelationshipConverter<T> propertyContainerConverterFactoryHasRelationshipConverterForPrimitive(Class<T> type) {
    @SuppressWarnings("unchecked")
    RelationshipConverter<T> relationshipConverter = mock(RelationshipConverter.class);
    doReturn(relationshipConverter).when(propertyContainerConverterFactoryMock).createForPrimitiveRelation(type);

    return relationshipConverter;
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityWithRelationThrowsAConversionExceptionWhenOneOfTheEntityConvertersDoes() throws Exception {
    // setup
    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTypeId(RELATION_TYPE_ID);
    relation.setTypeType(RELATION_TYPE_NAME);
    String name = "regularTypeName";

    Node sourceNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_SOURCE_ID, sourceNodeMock);
    Node targetNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_TARGET_ID, targetNodeMock);

    relationTypeWithRegularNameExists(name);

    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    RelationshipConverter<SubARelation> subARelationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    RelationshipConverter<? super SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterForPrimitive(RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);
    doThrow(ConversionException.class).when(subARelationConverterMock).addValuesToPropertyContainer(relationShipMock, relation);

    try {
      // action
      instance.addDomainEntity(SubARelation.class, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_TARGET_ID);
      verify(dbMock).findNodesByLabelAndProperty(RELATION_TYPE_LABEL, ID_PROPERTY_NAME, RELATION_TYPE_ID);
      verify(sourceNodeMock).createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)));

      verify(subARelationConverterMock).addValuesToPropertyContainer( //
          argThat(equalTo(relationShipMock)), //
          argThat(likeDomainEntity(RELATION_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verify(transactionMock).failure();
      verifyZeroInteractions(relationConverterMock);
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityWithRelationThrowsAStorageExceptionWhenTheEntityInstantiatorThrowsAnIllegalAccessException() throws Exception {
    addDomainEntityWithRelationThrowsAStorageExceptionWhenTheEntityInstantiatorThrowsAnException(IllegalAccessException.class);
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityWithRelationThrowsAStorageExceptionWhenTheEntityInstantiatorThrowsAnInstantiationException() throws Exception {
    addDomainEntityWithRelationThrowsAStorageExceptionWhenTheEntityInstantiatorThrowsAnException(InstantiationException.class);
  }

  private void addDomainEntityWithRelationThrowsAStorageExceptionWhenTheEntityInstantiatorThrowsAnException(Class<? extends Exception> exceptionToThrow) throws Exception {
    // setup
    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTypeId(RELATION_TYPE_ID);
    relation.setTypeType(RELATION_TYPE_NAME);
    String name = "regularTypeName";

    Node sourceNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_SOURCE_ID, sourceNodeMock);
    Node targetNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_TARGET_ID, targetNodeMock);

    relationTypeWithRegularNameExists(name);

    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    RelationshipConverter<SubARelation> subARelationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    RelationshipConverter<? super SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterForPrimitive(RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);
    doThrow(exceptionToThrow).when(entityInstantiatorMock).createInstanceOf(RELATIONTYPE_TYPE);

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
      verifyZeroInteractions(relationConverterMock, subARelationConverterMock, sourceNodeMock);
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityWithRelationThrowsAConversionExceptionWhenTheRegularNameOfTheRelationTypeCannotBeFound() throws Exception {
    // setup
    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTypeId(RELATION_TYPE_ID);
    relation.setTypeType(RELATION_TYPE_NAME);
    String name = "regularTypeName";

    Node sourceNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_SOURCE_ID, sourceNodeMock);
    Node targetNodeMock = mock(NODE_TYPE);
    oneNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_TARGET_ID, targetNodeMock);

    NodeConverter<RelationType> relationTypeConverter = relationTypeWithRegularNameExists(name);

    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    RelationshipConverter<SubARelation> subARelationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    RelationshipConverter<? super SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterForPrimitive(RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);
    doThrow(ConversionException.class).when(relationTypeConverter).addValuesToEntity(any(RelationType.class), any(Node.class));

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
      verifyZeroInteractions(relationConverterMock, subARelationConverterMock, sourceNodeMock);
    }
  }

  @Test(expected = StorageException.class)
  public void addDomainEntityWithRelationThrowsAStorageExceptionWhenTheSourceCannotBeFound() throws Exception {
    // setup
    noNodeIsFound(PRIMITIVE_DOMAIN_ENTITY_LABEL, RELATION_SOURCE_ID);

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

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryCreatesAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);

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

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryCreatesAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);

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
    when(entityInstantiatorMock.createInstanceOf(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntity);
    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryCreatesAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(systemEntity)));

    InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, systemEntityConverterMock, transactionMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(systemEntityConverterMock).addValuesToEntity(systemEntity, nodeMock);
    inOrder.verify(transactionMock).success();
    verifyNoMoreInteractions(dbMock, systemEntityConverterMock);
  }

  @Test
  public void getEntityReturnsTheLatestIfMoreThanOneItemIsFound() throws Exception {
    // setup
    Node nodeWithFirstRevision = createNodeWithRevision(FIRST_REVISION);
    Node nodeWithSecondRevision = createNodeWithRevision(SECOND_REVISION);
    Node nodeWithThirdRevision = createNodeWithRevision(THIRD_REVISION);
    multipleNodesAreFound(DOMAIN_ENTITY_LABEL, ID, nodeWithFirstRevision, nodeWithThirdRevision, nodeWithSecondRevision);

    NodeConverter<SubADomainEntity> domainEntityConverterMock = propertyContainerConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    when(entityInstantiatorMock.createInstanceOf(DOMAIN_ENTITY_TYPE)).thenReturn(domainEntity);

    domainEntity.setId(ID);

    // action
    SubADomainEntity actualEntity = instance.getEntity(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(domainEntity)));

    InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, domainEntityConverterMock, transactionMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(domainEntityConverterMock).addValuesToEntity(domainEntity, nodeWithThirdRevision);
    inOrder.verify(transactionMock).success();
    verifyNoMoreInteractions(dbMock, domainEntityConverterMock);
  }

  @Test
  public void getEntityForRelationReturnsTheRelationThatBelongsToTheId() throws Exception {
    // setup
    Relationship relationshipMock = mock(Relationship.class);
    RelationshipIndex indexMock = oneRelationshipIsFoundInIndexWithName(RELATIONSHIP_ID_INDEX, relationshipMock);
    SubARelation relation = new SubARelation();

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(entityInstantiatorMock.createInstanceOf(RELATION_TYPE)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).addValuesToEntity(relation, relationshipMock);
    verify(transactionMock).success();
  }

  @Test
  public void getEntityForRelationReturnsTheLatestIfMultipleAreFound() throws Exception {
    // setup
    Relationship relationshipFirstRevision = createRelationshipWithRevision(FIRST_REVISION);
    Relationship relationshipSecondRevision = createRelationshipWithRevision(SECOND_REVISION);
    Relationship relationshipThirdRevision = createRelationshipWithRevision(THIRD_REVISION);
    RelationshipIndex indexMock = multipleRelationshipsAreFoundInIndexWithName(RELATIONSHIP_ID_INDEX, relationshipFirstRevision, relationshipThirdRevision, relationshipSecondRevision);
    SubARelation relation = new SubARelation();

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(entityInstantiatorMock.createInstanceOf(RELATION_TYPE)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).addValuesToEntity(relation, relationshipThirdRevision);
    verify(transactionMock).success();
  }

  private Relationship createRelationshipWithRevision(int revision) {
    Relationship relationship = mock(RELATIONSHIP_TYPE);
    when(relationship.getProperty(REVISION_PROPERTY_NAME)).thenReturn(revision);
    return relationship;
  }

  @Test
  public void getEntityForRelationReturnsNullIfTheRelationIsNotFound() throws Exception {
    // setup
    RelationshipIndex indexMock = noRelationsAreFoundInIndexWithName(RELATIONSHIP_ID_INDEX);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(nullValue()));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verifyZeroInteractions(propertyContainerConverterFactoryMock, entityInstantiatorMock);
    verify(transactionMock).success();
  }

  @Test(expected = ConversionException.class)
  public void getEntityForRelationThrowsAConversionExceptionWhenTheRelationConverterDoes() throws Exception {
    // setup
    Relationship relationshipMock = mock(Relationship.class);
    RelationshipIndex indexMock = oneRelationshipIsFoundInIndexWithName(RELATIONSHIP_ID_INDEX, relationshipMock);
    SubARelation relation = new SubARelation();

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(entityInstantiatorMock.createInstanceOf(RELATION_TYPE)).thenReturn(relation);

    doThrow(ConversionException.class).when(relationConverterMock).addValuesToEntity(relation, relationshipMock);

    // action
    SubARelation actualRelation = instance.getEntity(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).addValuesToEntity(relation, relationshipMock);
    verify(transactionMock).failure();
  }

  @Test(expected = StorageException.class)
  public void getEntityForRelationThrowsStorageExceptionWhenEntityInstantiatorThrowsAnInstantiationException() throws Exception {
    getEntityForRelationThrowsStorageExceptionWhenEntityInstantiatorThrowsAnException(InstantiationException.class);
  }

  @Test(expected = StorageException.class)
  public void getEntityForRelationThrowsStorageExceptionWhenEntityInstantiatorThrowsAnIllegalAccessException() throws Exception {
    getEntityForRelationThrowsStorageExceptionWhenEntityInstantiatorThrowsAnException(IllegalAccessException.class);
  }

  private void getEntityForRelationThrowsStorageExceptionWhenEntityInstantiatorThrowsAnException(Class<? extends Exception> exceptionToThrow) throws Exception {
    // setup
    Relationship relationshipMock = mock(Relationship.class);
    RelationshipIndex indexMock = oneRelationshipIsFoundInIndexWithName(RELATIONSHIP_ID_INDEX, relationshipMock);
    doThrow(exceptionToThrow).when(entityInstantiatorMock).createInstanceOf(RELATION_TYPE);

    try {
      // action
      instance.getEntity(RELATION_TYPE, ID);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).index();
      verify(indexMock).get(ID_PROPERTY_NAME, ID);
      verify(transactionMock).failure();
      verifyNoMoreInteractions(dbMock);
      verifyZeroInteractions(propertyContainerConverterFactoryMock);
    }
  }

  private RelationshipIndex multipleRelationshipsAreFoundInIndexWithName(String relationShipIdIndex, Relationship relationshipFirstRevision, Relationship relationshipThirdRevision,
      Relationship relationshipSecondRevision) {
    RelationshipIndex index = dbHasRelationshipIndexWithName(relationShipIdIndex);
    relationshipsAreFoundInIndex(index, relationshipFirstRevision, relationshipThirdRevision, relationshipSecondRevision);
    return index;
  }

  private RelationshipIndex noRelationsAreFoundInIndexWithName(String indexName) {
    RelationshipIndex indexMock = dbHasRelationshipIndexWithName(indexName);
    noRelationshipisFoundInIndex(indexMock);
    return indexMock;
  }

  private void noRelationshipisFoundInIndex(RelationshipIndex indexMock) {
    List<Relationship> relationships = Lists.newArrayList();
    ResourceIterator<Relationship> relationshipIterator = IteratorUtil.asResourceIterator(relationships.iterator());
    @SuppressWarnings("unchecked")
    IndexHits<Relationship> indexHitsMock = mock(IndexHits.class);
    when(indexHitsMock.iterator()).thenReturn(relationshipIterator);
    when(indexMock.get(ID_PROPERTY_NAME, ID)).thenReturn(indexHitsMock);
  }

  private RelationshipIndex oneRelationshipIsFoundInIndexWithName(String indexName, Relationship relationshipMock) {
    RelationshipIndex indexMock = dbHasRelationshipIndexWithName(indexName);
    relationshipsAreFoundInIndex(indexMock, relationshipMock);
    return indexMock;
  }

  private void relationshipsAreFoundInIndex(RelationshipIndex indexMock, Relationship... relationshipMocks) {
    List<Relationship> relationships = Lists.newArrayList(relationshipMocks);
    ResourceIterator<Relationship> relationshipIterator = IteratorUtil.asResourceIterator(relationships.iterator());
    @SuppressWarnings("unchecked")
    IndexHits<Relationship> indexHitsMock = mock(IndexHits.class);
    when(indexHitsMock.iterator()).thenReturn(relationshipIterator);
    when(indexMock.get(ID_PROPERTY_NAME, ID)).thenReturn(indexHitsMock);
  }

  private RelationshipIndex dbHasRelationshipIndexWithName(String indexName) {
    RelationshipIndex indexMock = mock(RelationshipIndex.class);
    IndexManager indexManagerMock = mock(IndexManager.class);

    when(indexManagerMock.forRelationships(indexName)).thenReturn(indexMock);
    when(dbMock.index()).thenReturn(indexManagerMock);

    return indexMock;
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
    verify(transactionMock).success();
    verifyZeroInteractions(propertyContainerConverterFactoryMock);
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
    when(entityInstantiatorMock.createInstanceOf(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntity);
    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryCreatesAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);
    doThrow(ConversionException.class).when(systemEntityConverterMock).addValuesToEntity(systemEntity, nodeMock);

    try {
      // action
      instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, systemEntityConverterMock, transactionMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      inOrder.verify(systemEntityConverterMock).addValuesToEntity(systemEntity, nodeMock);
      inOrder.verify(transactionMock).failure();
      verifyNoMoreInteractions(dbMock, systemEntityConverterMock);
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
    doThrow(exceptionToThrow).when(entityInstantiatorMock).createInstanceOf(SYSTEM_ENTITY_TYPE);

    try {
      // action
      instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, transactionMock);
      inOrder.verify(dbMock).beginTx();
      inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
      inOrder.verify(transactionMock).failure();
      verifyNoMoreInteractions(dbMock);
    }
  }

  @Test
  public void updateSystemEntityRetrievesTheEntityAndUpdatesTheData() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, ID, nodeMock);
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(FIRST_REVISION);
    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryCreatesAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);

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
      verifyZeroInteractions(propertyContainerConverterFactoryMock);
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
      verifyZeroInteractions(propertyContainerConverterFactoryMock);
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
      verifyZeroInteractions(propertyContainerConverterFactoryMock);
    }
  }

  @Test(expected = ConversionException.class)
  public void updateSystemEntityThrowsAConversionExceptionWhenTheEntityCovnerterThrowsOne() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    oneNodeIsFound(SYSTEM_ENTITY_LABEL, ID, nodeMock);
    when(nodeMock.getProperty(REVISION_PROPERTY_NAME)).thenReturn(FIRST_REVISION);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryCreatesAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);

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
    NodeConverter<SubADomainEntity> domainEntityConverterMock = propertyContainerConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);;

    domainEntity.setId(ID);
    domainEntity.setRev(FIRST_REVISION);
    domainEntity.setPid(PID);
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
            .withRevision(SECOND_REVISION) //
            .withoutAPID()));
    inOrder.verify(domainEntityConverterMock).updateModifiedAndRev(argThat(equalTo(nodeMock)), //
        argThat(likeDomainEntity(SubADomainEntity.class) //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION) //
            .withoutAPID()));
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
    NodeConverter<SubADomainEntity> domainEntityConverterMock = propertyContainerConverterFactoryCreatesAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);

    domainEntity.setId(ID);
    domainEntity.setRev(THIRD_REVISION);
    domainEntity.setPid(PID);
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
            .withRevision(FOURTH_REVISION) //
            .withoutAPID()));
    inOrder.verify(domainEntityConverterMock).updateModifiedAndRev(argThat(equalTo(nodeWithThirdRevision)), //
        argThat(likeDomainEntity(SubADomainEntity.class) //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(FOURTH_REVISION) //
            .withoutAPID()));
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
    domainEntity.setPid(PID);
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
      verifyZeroInteractions(propertyContainerConverterFactoryMock);
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
    domainEntity.setPid(PID);
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
      verifyZeroInteractions(propertyContainerConverterFactoryMock);
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
    domainEntity.setPid(PID);
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
      verifyZeroInteractions(propertyContainerConverterFactoryMock);
      verifyNoMoreInteractions(dbMock);
    }
  }

  @Test
  public void deleteSystemEntityFirstRemovesTheNodesRelationShipsAndThenTheNodeItselfTheDatabase() throws Exception {
    // setup
    dbMockCreatesTransaction(transactionMock);
    Relationship relMock1 = mock(RELATIONSHIP_TYPE);
    Relationship relMock2 = mock(RELATIONSHIP_TYPE);
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
    Relationship relMock1 = mock(RELATIONSHIP_TYPE);
    Relationship relMock2 = mock(RELATIONSHIP_TYPE);
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
    Relationship relMock1 = mock(RELATIONSHIP_TYPE);
    Relationship relMock2 = mock(RELATIONSHIP_TYPE);
    nodeHaseRelationsShips(nodeMock, relMock1, relMock2);

    Relationship relMock3 = mock(RELATIONSHIP_TYPE);
    Relationship relMock4 = mock(RELATIONSHIP_TYPE);
    Node nodeMock2 = mock(NODE_TYPE);
    nodeHaseRelationsShips(nodeMock2, relMock3, relMock4);

    Relationship relMock5 = mock(RELATIONSHIP_TYPE);
    Relationship relMock6 = mock(RELATIONSHIP_TYPE);
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
