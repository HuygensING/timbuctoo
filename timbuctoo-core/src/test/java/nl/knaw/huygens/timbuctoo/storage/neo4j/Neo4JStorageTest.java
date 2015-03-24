package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.DomainEntityMatcher.likeDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.Neo4JLegacyStorageWrapper.RELATIONSHIP_ID_INDEX;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipIndexMockBuilder.aRelationshipIndexForName;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipTypeMatcher.likeRelationshipType;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.aSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.anEmptySearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubADomainEntityBuilder.aDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubARelationBuilder.aRelation;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.TestSystemEntityWrapperBuilder.aSystemEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
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
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.RelationshipIndex;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

public class Neo4JStorageTest {

  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final String PRIMITIVE_DOMAIN_ENTITY_NAME = TypeNames.getInternalName(PRIMITIVE_DOMAIN_ENTITY_TYPE);
  private static final Label PRIMITIVE_DOMAIN_ENTITY_LABEL = DynamicLabel.label(PRIMITIVE_DOMAIN_ENTITY_NAME);

  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final Label SYSTEM_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(SYSTEM_ENTITY_TYPE));
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Label DOMAIN_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(DOMAIN_ENTITY_TYPE));
  private static final String ID = "id";
  private static final int FIRST_REVISION = 1;
  private static final int SECOND_REVISION = 2;
  private static final int THIRD_REVISION = 3;
  private static final String PID = "pid";
  private static final Change CHANGE = Change.newInternalInstance();

  private static final Class<Relationship> RELATIONSHIP_TYPE = Relationship.class;
  private static final String RELATION_TYPE_ID = "typeId";
  private static final String RELATION_TARGET_ID = "targetId";
  private static final String RELATION_SOURCE_ID = "sourceId";
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final Class<RelationType> RELATIONTYPE_TYPE = RelationType.class;
  private static final String RELATION_TYPE_NAME = TypeNames.getInternalName(RELATIONTYPE_TYPE);
  private static final Label RELATION_TYPE_LABEL = DynamicLabel.label(RELATION_TYPE_NAME);

  private Neo4JStorage instance;
  private PropertyContainerConverterFactory propertyContainerConverterFactoryMock;
  private Transaction transactionMock = mock(Transaction.class);
  private GraphDatabaseService dbMock;
  private NodeDuplicator nodeDuplicatorMock;
  private RelationshipDuplicator relationshipDuplicatorMock;
  private IdGenerator idGeneratorMock;

  @Before
  public void setup() throws Exception {
    nodeDuplicatorMock = mock(NodeDuplicator.class);
    relationshipDuplicatorMock = mock(RelationshipDuplicator.class);
    idGeneratorMock = mock(IdGenerator.class);
    setupEntityConverterFactory();
    setupDBTransaction();
    TypeRegistry typeRegistry = TypeRegistry.getInstance().init("timbuctoo.model test.model");
    instance = new Neo4JStorage(dbMock, propertyContainerConverterFactoryMock, nodeDuplicatorMock, relationshipDuplicatorMock, idGeneratorMock, typeRegistry);
  }

  private void setupDBTransaction() {
    transactionMock = mock(Transaction.class);
    dbMock = mock(GraphDatabaseService.class);
    when(dbMock.beginTx()).thenReturn(transactionMock);
  }

  private void idGeneratorMockCreatesIDFor(Class<? extends Entity> type, String id) {
    when(idGeneratorMock.nextIdFor(type)).thenReturn(id);
  }

  @Test
  public void addDomainEntitySavesTheProjectVersionAndThePrimitiveAndReturnsTheId() throws Exception {
    // setup
    Node nodeMock = aNode().createdBy(dbMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    NodeConverter<? super SubADomainEntity> compositeConverter = propertyContainerConverterFactoryHasCompositeConverterFor(DOMAIN_ENTITY_TYPE);
    SubADomainEntity domainEntity = aDomainEntity().build();

    // action
    String actualId = instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, CHANGE);

    // verify
    verify(dbMock).beginTx();
    verify(dbMock).createNode();
    verify(compositeConverter).addValuesToPropertyContainer( //
        argThat(equalTo(nodeMock)), // 
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE) //
            .withId(actualId) //
            .withACreatedValue() //
            .withAModifiedValue() //
            .withRevision(FIRST_REVISION)//
            .withoutAPID()));
    verify(transactionMock).success();
  }

  @Test
  public void addDomainEntityRemovesThePIDBeforeSaving() throws Exception {
    // setup
    Node nodeMock = aNode().createdBy(dbMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    NodeConverter<? super SubADomainEntity> compositeConverter = propertyContainerConverterFactoryHasCompositeConverterFor(DOMAIN_ENTITY_TYPE);
    SubADomainEntity domainEntityWithAPID = aDomainEntity().withAPid().build();

    // action
    instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntityWithAPID, CHANGE);

    // verify
    verify(dbMock).beginTx();
    verify(dbMock).createNode();
    verify(compositeConverter).addValuesToPropertyContainer( //
        argThat(equalTo(nodeMock)), // 
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE) //
            .withoutAPID()));
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
    Node nodeMock = aNode().createdBy(dbMock);

    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    SubADomainEntity domainEntity = aDomainEntity().build();
    NodeConverter<? super SubADomainEntity> compositeConverter = propertyContainerConverterFactoryHasCompositeConverterFor(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(compositeConverter).addValuesToPropertyContainer(nodeMock, domainEntity);

    try {
      // action
      instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, CHANGE);
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

  @Test
  public void getEntityReturnsTheLatestIfMoreThanOneItemIsFound() throws Exception {
    // setup
    Node nodeWithThirdRevision = aNode().withRevision(THIRD_REVISION).build();

    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(aNode().withRevision(FIRST_REVISION).build()) //
        .andNode(aNode().withRevision(SECOND_REVISION).build()) //
        .andNode(nodeWithThirdRevision)//
        .foundInDB(dbMock);

    SubADomainEntity domainEntity = aDomainEntity().withId(ID).build();

    NodeConverter<SubADomainEntity> domainEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(domainEntityConverterMock.convertToEntity(nodeWithThirdRevision)).thenReturn(domainEntity);

    // action
    SubADomainEntity actualEntity = instance.getEntity(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(domainEntity)));

    InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, domainEntityConverterMock, transactionMock);
    inOrder.verify(dbMock).findNodesByLabelAndProperty(DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(domainEntityConverterMock).convertToEntity(nodeWithThirdRevision);
    inOrder.verify(transactionMock).success();
  }

  @Test
  public void getEntityReturnsTheItemWhenFound() throws Exception {
    Node nodeMock = aNode().build();
    aSearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    TestSystemEntityWrapper systemEntity = aSystemEntity().build();
    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(SYSTEM_ENTITY_TYPE);
    when(systemEntityConverterMock.convertToEntity(nodeMock)).thenReturn(systemEntity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(systemEntity)));

    InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, systemEntityConverterMock, transactionMock);
    inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(systemEntityConverterMock).convertToEntity(nodeMock);
    inOrder.verify(transactionMock).success();
  }

  @Test
  public void getEntityReturnsNullIfNoItemIsFound() throws Exception {
    // setup
    anEmptySearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(nullValue()));

    verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    verify(transactionMock).success();
    verifyZeroInteractions(propertyContainerConverterFactoryMock);
  }

  @Test(expected = ConversionException.class)
  public void getEntityThrowsStorageExceptionWhenEntityWrapperThrowsAConversionException() throws Exception {
    // setup
    Node nodeMock = aNode().build();
    aSearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID)//
        .withNode(nodeMock)//
        .foundInDB(dbMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(SYSTEM_ENTITY_TYPE);
    when(systemEntityConverterMock.convertToEntity(nodeMock)).thenThrow(new ConversionException());

    try {
      // action
      instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, systemEntityConverterMock, transactionMock);
      inOrder.verify(systemEntityConverterMock).convertToEntity(nodeMock);
      inOrder.verify(transactionMock).failure();
    }
  }

  @Test(expected = StorageException.class)
  public void getEntityThrowsStorageExceptionWhenNodeConverterThrowsAnInstantiationException() throws Exception {
    // setup
    Node nodeMock = aNode().build();
    aSearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(SYSTEM_ENTITY_TYPE);
    doThrow(InstantiationException.class).when(systemEntityConverterMock).convertToEntity(nodeMock);

    try {
      // action
      instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, transactionMock);
      inOrder.verify(transactionMock).failure();
    }
  }

  @Test
  public void getDomainEntityRevisionReturnsTheDomainEntityWithTheRequestedRevision() throws Exception {
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(nodeWithSameRevision)).thenReturn(aDomainEntity().withAPid().build());

    // action
    SubADomainEntity entity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(entity, is(instanceOf(SubADomainEntity.class)));
    verify(converter).convertToEntity(nodeWithSameRevision);
    verify(transactionMock).success();
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheFoundEntityHasNoPID() throws Exception {
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(nodeWithSameRevision)).thenReturn(aDomainEntity().build());

    // action
    SubADomainEntity actualEntity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(actualEntity, is(nullValue()));
    verify(transactionMock).success();
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheEntityCannotBeFound() throws Exception {
    // setup
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    // action
    SubADomainEntity entity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(entity, is(nullValue()));
    verify(transactionMock).success();
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheRevisionCannotBeFound() throws Exception {
    // setup
    Node nodeWithDifferentRevision = aNode().withRevision(SECOND_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithDifferentRevision).foundInDB(dbMock);

    // action
    SubADomainEntity entity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(entity, is(nullValue()));
    verify(transactionMock).success();
  }

  @Test(expected = StorageException.class)
  public void getDomainEntityRevisionThrowsAStorageExceptionIfTheEntityCannotBeInstantiated() throws Exception {
    // setup
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(nodeWithSameRevision)).thenThrow(new InstantiationException());

    try {
      // action
      instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test(expected = ConversionException.class)
  public void getDomainEntityRevisionThrowsAConversionExceptionIfTheEntityCannotBeConverted() throws Exception {
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(nodeWithSameRevision)).thenThrow(new ConversionException());

    try {
      // action
      instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verify(converter).convertToEntity(nodeWithSameRevision);
      verify(transactionMock).failure();
    }
  }

  private void setupEntityConverterFactory() throws Exception {
    propertyContainerConverterFactoryMock = mock(PropertyContainerConverterFactory.class);
  }

  private <T extends Entity> NodeConverter<T> propertyContainerConverterFactoryHasANodeConverterTypeFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    NodeConverter<T> nodeConverter = mock(NodeConverter.class);
    when(propertyContainerConverterFactoryMock.createForType(argThat(equalTo(type)))).thenReturn(nodeConverter);
    return nodeConverter;
  }

  @Test
  public void setDomainEntityPIDAddsAPIDToTheLatestNodeIfMultipleAreFound() throws InstantiationException, IllegalAccessException, Exception {
    // setup
    Node nodeWithLatestRevision = aNode().withRevision(SECOND_REVISION).build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(aNode().withRevision(FIRST_REVISION).build()).withNode(nodeWithLatestRevision)//
        .foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converterMock.convertToEntity(nodeWithLatestRevision)).thenReturn(aDomainEntity().withId(ID).build());

    // action
    instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);

    verify(converterMock).addValuesToPropertyContainer( //
        argThat(equalTo(nodeWithLatestRevision)), //
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE).withId(ID).withPID(PID)));

  }

  @Test
  public void setDomainEntityPIDAddsAPIDToTheNodeAndDuplicatesTheNode() throws InstantiationException, IllegalAccessException, Exception {
    // setup
    Node node = aNode().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(node)//
        .foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converterMock.convertToEntity(node)).thenReturn(aDomainEntity().withId(ID).build());

    // action
    instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);

    InOrder inOrder = inOrder(converterMock, transactionMock, nodeDuplicatorMock);
    inOrder.verify(converterMock).addValuesToPropertyContainer( //
        argThat(equalTo(node)), //
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE).withId(ID).withPID(PID)));
    inOrder.verify(nodeDuplicatorMock).saveDuplicate(node);
    inOrder.verify(transactionMock).success();
  }

  @Test(expected = IllegalStateException.class)
  public void setDomainEntityPIDThrowsAnIllegalStateExceptionWhenTheEntityAlreadyHasAPID() throws Exception {
    // setup
    Node aNodeWithAPID = aNode().withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(aNodeWithAPID)//
        .foundInDB(dbMock);

    SubADomainEntity entityWithPID = aDomainEntity().withAPid().build();

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(aNodeWithAPID)).thenReturn(entityWithPID);

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(nodeConverter).convertToEntity(aNodeWithAPID);
      verify(transactionMock).failure();
    }
  }

  @Test(expected = ConversionException.class)
  public void setDomainEntityPIDThrowsAConversionExceptionWhenTheNodeCannotBeConverted() throws Exception {
    // setup
    Node aNodeWithAPID = aNode().withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(aNodeWithAPID)//
        .foundInDB(dbMock);

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(aNodeWithAPID)).thenThrow(new ConversionException());

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(nodeConverter).convertToEntity(aNodeWithAPID);
      verify(transactionMock).failure();
    }
  }

  @Test(expected = ConversionException.class)
  public void setDomainEntityPIDThrowsAConversionsExceptionWhenTheUpdatedEntityCannotBeCovnverted() throws Exception {
    // setup
    Node aNode = aNode().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(aNode)//
        .foundInDB(dbMock);

    SubADomainEntity entity = aDomainEntity().build();

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(aNode)).thenReturn(entity);
    doThrow(ConversionException.class).when(nodeConverter).addValuesToPropertyContainer(aNode, entity);

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(nodeConverter).addValuesToPropertyContainer(aNode, entity);
      verify(transactionMock).failure();
    }
  }

  @Test(expected = StorageException.class)
  public void setDomainEntityPIDThrowsAStorageExceptionWhenTheEntityDoesNotExist() throws Exception {
    // setup
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(transactionMock).failure();
    }

  }

  @Test(expected = StorageException.class)
  public void setDomainEntityPIDThrowsAStorageExceptionWhenTheEntityCannotBeInstatiated() throws Exception {

    // setup
    Node aNode = aNode().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID)//
        .withNode(aNode)//
        .foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converterMock.convertToEntity(aNode)).thenThrow(new InstantiationException());

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  /* *****************************************************************************
   * Relation
   * *****************************************************************************/

  @Test
  public void getRelationRevisionReturnsTheRelationForTheRequestedRevision() throws Exception {
    Relationship relationshipWithPID = aRelationship()//
        .withRevision(FIRST_REVISION)//
        .withAPID()//
        .build();
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID)//
        .relationship(relationshipWithPID)//
        .foundInDB(dbMock);

    SubARelation entity = aRelation().withAPID().build();
    RelationshipConverter<SubARelation> converterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converterMock.convertToEntity(relationshipWithPID)).thenReturn(entity);

    // action
    SubARelation relation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(instanceOf(RELATION_TYPE)));

    InOrder inOrder = inOrder(indexMock, converterMock, transactionMock);
    inOrder.verify(indexMock).get(ID_PROPERTY_NAME, ID);
    inOrder.verify(converterMock).convertToEntity(relationshipWithPID);
    inOrder.verify(transactionMock).success();
  }

  @Test
  public void getRelationRevisionReturnsNullIfTheFoundRelationshipHasNoPID() throws Exception {
    Relationship relationshipWithoutPID = aRelationship().withRevision(FIRST_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID)//
        .relationship(relationshipWithoutPID)//
        .foundInDB(dbMock);

    SubARelation entityWithoutPID = aRelation().build();
    RelationshipConverter<SubARelation> relationshipConverter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(relationshipConverter.convertToEntity(relationshipWithoutPID)).thenReturn(entityWithoutPID);

    // action
    SubARelation relation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(nullValue()));
    verifyTransactionSucceeded();
  }

  @Test
  public void getRelationRevisionReturnsNullIfTheRelationshipDoesNotExist() throws Exception {
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsNothingForId(ID).foundInDB(dbMock);

    // action
    SubARelation relation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(relation, is(nullValue()));
    verifyTransactionSucceeded();
  }

  @Test
  public void getRelationRevisionReturnsNullIfTheRevisionDoesNotExist() throws Exception {
    Relationship relationshipWithDifferentRevision = aRelationship().withAPID().withRevision(FIRST_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID)//
        .relationship(relationshipWithDifferentRevision)//
        .foundInDB(dbMock);

    // action
    SubARelation relation = instance.getRelationRevision(RELATION_TYPE, ID, SECOND_REVISION);

    // verify
    assertThat(relation, is(nullValue()));
    verifyTransactionSucceeded();
  }

  private void verifyTransactionSucceeded() {
    verify(transactionMock).success();
  }

  @Test(expected = StorageException.class)
  public void getRelationRevisionThrowsAStorageExceptionIfTheRelationCannotBeInstantiated() throws Exception {
    Relationship relationshipWithPID = aRelationship()//
        .withRevision(FIRST_REVISION)//
        .withAPID()//
        .build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID)//
        .relationship(relationshipWithPID)//
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationshipWithPID)).thenThrow(new InstantiationException());

    try {
      // action
      instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verifyTransactionFailed();
    }
  }

  private void verifyTransactionFailed() {
    verify(transactionMock).failure();
  }

  @Test(expected = ConversionException.class)
  public void getRelationRevisionThrowsAStorageExceptionIfTheRelationCannotBeConverted() throws Exception {
    Relationship relationshipWithPID = aRelationship()//
        .withRevision(FIRST_REVISION)//
        .withAPID()//
        .build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID)//
        .relationship(relationshipWithPID)//
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationshipWithPID)).thenThrow(new ConversionException());

    try {
      // action
      instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verify(converter).convertToEntity(relationshipWithPID);
      verifyTransactionFailed();
    }
  }

  //  private <T extends Relation> RelationshipConverter<T> propertyContainerConverterFactoryHasRelationshipConverterFor(Class<T> type) {
  //    @SuppressWarnings("unchecked")
  //    RelationshipConverter<T> relationshipConverter = mock(RelationshipConverter.class);
  //    when(propertyContainerConverterFactoryMock.createForRelation(type)).thenReturn(relationshipConverter);
  //
  //    return relationshipConverter;
  //  }

  @Test
  public void setRelationPIDSetsThePIDOfTheRelationAndDuplicatesIt() throws Exception {
    // setup
    Relationship relationship = aRelationship().withRevision(SECOND_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    SubARelation entity = aRelation().build();

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenReturn(entity);

    try {
      // action
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      InOrder inOrder = inOrder(converter, relationshipDuplicatorMock, transactionMock);
      inOrder.verify(converter).addValuesToPropertyContainer(//
          argThat(equalTo(relationship)), //
          argThat(likeDomainEntity(RELATION_TYPE)//
              .withPID(PID)));
      inOrder.verify(relationshipDuplicatorMock).saveDuplicate(relationship);
      inOrder.verify(transactionMock).success();
    }
  }

  @Test
  public void setRelationPIDSetsToTheLatest() throws Exception {
    // setup
    Relationship latestRelationship = aRelationship().withRevision(SECOND_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(aRelationship().withRevision(FIRST_REVISION).build()) //
        .andRelationship(latestRelationship) //
        .foundInDB(dbMock);

    SubARelation entity = aRelation().build();

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(latestRelationship)).thenReturn(entity);

    try {
      // action
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).addValuesToPropertyContainer(latestRelationship, entity);
      verifyTransactionSucceeded();
    }
  }

  @Test(expected = IllegalStateException.class)
  public void setRelationPIDThrowsAnIllegalStateExceptionIfTheRelationAlreadyHasAPID() throws Exception {
    // setup
    Relationship relationship = aRelationship().withAPID().build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    SubARelation entityWithAPID = aRelation().withAPID().build();

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenReturn(entityWithAPID);

    try {
      // action
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).convertToEntity(relationship);
      verifyTransactionFailed();
    }
  }

  @Test(expected = ConversionException.class)
  public void setRelationPIDThrowsAConversionExceptionIfTheRelationshipCannotBeConverted() throws Exception {
    // setup
    Relationship relationship = aRelationship().build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenThrow(new ConversionException());

    try {
      // action
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).convertToEntity(relationship);
      verifyTransactionFailed();
    }
  }

  @Test(expected = ConversionException.class)
  public void setRelationPIDThrowsAConversionsExceptionWhenTheUpdatedEntityCannotBeConvertedToARelationship() throws Exception {
    // setup
    Relationship relationship = aRelationship().withRevision(SECOND_REVISION).build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    SubARelation entity = aRelation().build();

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenReturn(entity);
    doThrow(ConversionException.class).when(converter).addValuesToPropertyContainer(relationship, entity);

    try {
      // action
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verify(converter).addValuesToPropertyContainer(//
          argThat(equalTo(relationship)), //
          argThat(likeDomainEntity(RELATION_TYPE)//
              .withPID(PID)));
    }
  }

  @Test(expected = StorageException.class)
  public void setRelationPIDThrowsAStorageExceptionIfTheRelationCannotBeInstatiated() throws Exception {
    // setup
    Relationship relationship = aRelationship().build();
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationship) //
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> converter = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(relationship)).thenThrow(new InstantiationException());

    try {
      // action
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      // verify
      verifyTransactionFailed();
    }
  }

  @Test(expected = NoSuchEntityException.class)
  public void setRelationPIDThrowsANoSuchEntityExceptionIfTheRelationshipCannotBeFound() throws Exception {
    // setup
    aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsNothingForId(ID).foundInDB(dbMock);

    try {
      // action
      instance.setRelationPID(RELATION_TYPE, ID, PID);
    } finally {
      verifyTransactionFailed();
    }

  }

  @Test
  public void getRelationReturnsTheRelationThatBelongsToTheId() throws Exception {
    // setup
    Relationship relationshipMock = aRelationship().build();
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX) //
        .containsForId(ID) //
        .relationship(relationshipMock) //
        .foundInDB(dbMock);
    SubARelation relation = new SubARelation();

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(relationConverterMock.convertToEntity(relationshipMock)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getRelation(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).convertToEntity(relationshipMock);
    verifyTransactionSucceeded();
  }

  @Test
  public void getRelationReturnsTheLatestIfMultipleAreFound() throws Exception {
    // setup
    Relationship relationshipFirstRevision = aRelationship().withRevision(FIRST_REVISION).build();
    Relationship relationshipSecondRevision = aRelationship().withRevision(SECOND_REVISION).build();
    Relationship relationshipThirdRevision = aRelationship().withRevision(THIRD_REVISION).build();
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).containsForId(ID) //
        .relationship(relationshipFirstRevision) //
        .andRelationship(relationshipThirdRevision) //
        .andRelationship(relationshipSecondRevision) //
        .foundInDB(dbMock);
    SubARelation relation = new SubARelation();

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(relationConverterMock.convertToEntity(relationshipThirdRevision)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getRelation(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).convertToEntity(relationshipThirdRevision);
    verifyTransactionSucceeded();
  }

  @Test
  public void getRelationReturnsNullIfTheRelationIsNotFound() throws Exception {
    // setup
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsNothingForId(ID)//
        .foundInDB(dbMock);

    // action
    SubARelation actualRelation = instance.getRelation(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(nullValue()));

    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verifyZeroInteractions(propertyContainerConverterFactoryMock);
    verifyTransactionSucceeded();
  }

  @Test(expected = ConversionException.class)
  public void getRelationThrowsAConversionExceptionWhenTheRelationConverterDoes() throws Exception {
    // setup
    Relationship relationshipMock = aRelationship().build();
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID)//
        .relationship(relationshipMock)//
        .foundInDB(dbMock);
    SubARelation relation = new SubARelation();

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    doThrow(ConversionException.class).when(relationConverterMock).convertToEntity(relationshipMock);

    // action
    SubARelation actualRelation = instance.getRelation(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verify(dbMock).beginTx();
    verify(indexMock).get(ID_PROPERTY_NAME, ID);
    verify(relationConverterMock).convertToEntity(relationshipMock);
    verifyTransactionFailed();
  }

  @Test(expected = StorageException.class)
  public void getRelationThrowsStorageExceptionWhenRelationshipConverterThrowsAnInstantiationException() throws Exception {
    // setup
    Relationship relationshipMock = aRelationship().build();
    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX)//
        .containsForId(ID)//
        .relationship(relationshipMock)//
        .foundInDB(dbMock);

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    doThrow(InstantiationException.class).when(relationConverterMock).convertToEntity(relationshipMock);

    try {
      // action
      instance.getRelation(RELATION_TYPE, ID);
    } finally {
      // verify
      verify(indexMock).get(ID_PROPERTY_NAME, ID);
      verify(relationConverterMock).convertToEntity(relationshipMock);
      verifyTransactionFailed();
    }
  }

  @Test
  public void addRelationAddsARelationshipToTheSourceAndReturnsTheId() throws Exception {
    // setup
    String name = "regularTypeName";

    Node sourceNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(sourceNodeMock) //
        .foundInDB(dbMock);

    Node targetNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID) //
        .withNode(targetNodeMock) //
        .foundInDB(dbMock);

    relationTypeWithRegularNameExists(name);

    RelationshipIndex indexMock = aRelationshipIndexForName(RELATIONSHIP_ID_INDEX).foundInDB(dbMock);
    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerFactoryHasCompositeRelationshipConverterFor(RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID) //
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTargetId(RELATION_TARGET_ID) //
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTypeId(RELATION_TYPE_ID) //
        .withTypeType(RELATION_TYPE_NAME).build();

    // action
    String id = instance.addRelation(RELATION_TYPE, relation, new Change());
    // verify
    assertThat(id, is(equalTo(ID)));

    InOrder inOrder = inOrder(dbMock, sourceNodeMock, relationConverterMock, indexMock, transactionMock);

    inOrder.verify(dbMock).beginTx();
    inOrder.verify(sourceNodeMock).createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)));

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
    Node relationTypeNodeMock = aNode().build();
    aSearchResult().forLabel(RELATION_TYPE_LABEL).andId(RELATION_TYPE_ID) //
        .withNode(relationTypeNodeMock) //
        .foundInDB(dbMock);

    NodeConverter<RelationType> relationTypeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(RELATIONTYPE_TYPE);
    RelationType relationType = new RelationType();
    relationType.setRegularName(name);
    when(relationTypeConverter.convertToEntity(relationTypeNodeMock)).thenReturn(relationType);

    return relationTypeConverter;
  }

  private <T extends Relation> RelationshipConverter<T> propertyContainerFactoryHasCompositeRelationshipConverterFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    RelationshipConverter<T> relationshipConverter = mock(RelationshipConverter.class);
    when(propertyContainerConverterFactoryMock.createCompositeForRelation(type)).thenReturn(relationshipConverter);

    return relationshipConverter;
  }

  private <T extends Relation> RelationshipConverter<T> propertyContainerConverterFactoryHasRelationshipConverterFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    RelationshipConverter<T> relationshipConverter = mock(RelationshipConverter.class);
    when(propertyContainerConverterFactoryMock.createForRelation(type)).thenReturn(relationshipConverter);

    return relationshipConverter;
  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAConversionExceptionWhenTheRelationshipConverterDoes() throws Exception {
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID) //
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTargetId(RELATION_TARGET_ID) //
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTypeId(RELATION_TYPE_ID) //
        .withTypeType(RELATION_TYPE_NAME).build();
    String name = "regularTypeName";

    Node sourceNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(sourceNodeMock) //
        .foundInDB(dbMock);
    Node targetNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID) //
        .withNode(targetNodeMock) //
        .foundInDB(dbMock);

    relationTypeWithRegularNameExists(name);

    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerFactoryHasCompositeRelationshipConverterFor(RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);
    doThrow(ConversionException.class).when(relationConverterMock).addValuesToPropertyContainer(relationShipMock, relation);

    try {
      // action
      instance.addRelation(RELATION_TYPE, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(sourceNodeMock).createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)));

      verify(relationConverterMock).addValuesToPropertyContainer( //
          argThat(equalTo(relationShipMock)), //
          argThat(likeDomainEntity(RELATION_TYPE) //
              .withId(ID) //
              .withACreatedValue() //
              .withAModifiedValue() //
              .withRevision(FIRST_REVISION)));
      verifyTransactionFailed();
    }
  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAStorageExceptionWhenTheRelationTypeCannotBeInstantiated() throws Exception {
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID) //
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTargetId(RELATION_TARGET_ID) //
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTypeId(RELATION_TYPE_ID) //
        .withTypeType(RELATION_TYPE_NAME).build();
    String name = "regularTypeName";

    Node sourceNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(sourceNodeMock) //
        .foundInDB(dbMock);
    Node targetNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID) //
        .withNode(targetNodeMock) //
        .foundInDB(dbMock);

    NodeConverter<RelationType> relationTypeConverter = relationTypeWithRegularNameExists(name);

    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);
    when(relationTypeConverter.convertToEntity(any(Node.class))).thenThrow(new InstantiationException());

    try {
      // action
      instance.addRelation(RELATION_TYPE, relation, new Change());
    } finally {
      // verify
      verifyTransactionFailed();
    }
  }

  @Test(expected = ConversionException.class)
  public void addRelationThrowsAConversionExceptionWhenTheRelationCannotBeConverted() throws Exception {
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID) //
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTargetId(RELATION_TARGET_ID) //
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTypeId(RELATION_TYPE_ID) //
        .withTypeType(RELATION_TYPE_NAME).build();
    String name = "regularTypeName";

    Node sourceNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(sourceNodeMock) //
        .foundInDB(dbMock);

    Node targetNodeMock = aNode().build();
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID) //
        .withNode(targetNodeMock) //
        .foundInDB(dbMock);

    NodeConverter<RelationType> relationTypeConverter = relationTypeWithRegularNameExists(name);

    Relationship relationShipMock = mock(RELATIONSHIP_TYPE);

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerFactoryHasCompositeRelationshipConverterFor(RELATION_TYPE);

    when(sourceNodeMock.createRelationshipTo(argThat(equalTo(targetNodeMock)), argThat(likeRelationshipType().withName(name)))).thenReturn(relationShipMock);
    when(idGeneratorMock.nextIdFor(RELATION_TYPE)).thenReturn(ID);
    when(relationTypeConverter.convertToEntity(any(Node.class))).thenThrow(new ConversionException());

    try {
      // action
      instance.addRelation(RELATION_TYPE, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verifyTransactionFailed();
      verifyZeroInteractions(relationConverterMock, sourceNodeMock);
    }
  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAStorageExceptionWhenTheSourceCannotBeFound() throws Exception {
    // setup
    anEmptySearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID).foundInDB(dbMock);

    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);

    try {
      // action
      instance.addRelation(RELATION_TYPE, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
      verifyTransactionFailed();
    }
  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAStorageExceptionWhenTheTargetCannotBeFound() throws Exception {
    // setup
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(aNode().build())//
        .foundInDB(dbMock);

    anEmptySearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID).foundInDB(dbMock);

    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME);
    relation.setTargetId(RELATION_TARGET_ID);

    try {
      // action
      instance.addRelation(RELATION_TYPE, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_TARGET_ID);
      verifyTransactionFailed();
    }
  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAStorageExceptionWhenRelationTypeCannotBeFound() throws Exception {

    // setup
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_SOURCE_ID) //
        .withNode(aNode().build()) //
        .foundInDB(dbMock);
    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(RELATION_TARGET_ID) //
        .withNode(aNode().build()) //
        .foundInDB(dbMock);

    anEmptySearchResult().forLabel(RELATION_TYPE_LABEL).andId(RELATION_TYPE_ID).foundInDB(dbMock);
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID) //
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTargetId(RELATION_TARGET_ID) //
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME) //
        .withTypeId(RELATION_TYPE_ID) //
        .withTypeType(RELATION_TYPE_NAME).build();

    try {
      // action
      instance.addRelation(RELATION_TYPE, relation, new Change());
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_SOURCE_ID);
      verify(dbMock).findNodesByLabelAndProperty(PRIMITIVE_DOMAIN_ENTITY_LABEL, ID_PROPERTY_NAME, RELATION_TARGET_ID);
      verify(dbMock).findNodesByLabelAndProperty(RELATION_TYPE_LABEL, ID_PROPERTY_NAME, RELATION_TYPE_ID);
      verifyTransactionFailed();
    }
  }

}
