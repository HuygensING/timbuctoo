package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.DomainEntityBuilder.aDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.DomainEntityMatcher.likeDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.aSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.anEmptySearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SystemEntityBuilder.aSystemEntity;
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
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
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
import org.neo4j.graphdb.Transaction;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

public class Neo4JStorageTest {

  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  protected static final Class<SubARelation> RELATION_TYPE = SubARelation.class;

  protected static final String PRIMITIVE_DOMAIN_ENTITY_NAME = TypeNames.getInternalName(PRIMITIVE_DOMAIN_ENTITY_TYPE);

  protected static final int FIRST_REVISION = 1;
  protected static final int SECOND_REVISION = 2;
  protected static final int THIRD_REVISION = 3;
  private static final int FOURTH_REVISION = 4;
  private static final Label DOMAIN_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(DOMAIN_ENTITY_TYPE));
  protected static final Label PRIMITIVE_DOMAIN_ENTITY_LABEL = DynamicLabel.label(PRIMITIVE_DOMAIN_ENTITY_NAME);
  private static final Label SYSTEM_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(SYSTEM_ENTITY_TYPE));

  protected static final String ID = "id";

  protected GraphDatabaseService dbMock;
  protected PropertyContainerConverterFactory propertyContainerConverterFactoryMock;
  protected Neo4JStorage instance;
  protected Transaction transactionMock;
  protected EntityInstantiator entityInstantiatorMock;
  protected IdGenerator idGeneratorMock;

  @Before
  public void setUp() throws Exception {
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
    Node nodeMock = aNode().createdBy(dbMock);
    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    NodeConverter<? super SubADomainEntity> compositeConverter = propertyContainerConverterFactoryHasCompositeConverterFor(DOMAIN_ENTITY_TYPE);
    SubADomainEntity domainEntity = aDomainEntity().build();

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
    Node nodeMock = aNode().createdBy(dbMock);

    idGeneratorMockCreatesIDFor(DOMAIN_ENTITY_TYPE, ID);

    SubADomainEntity domainEntity = aDomainEntity().build();
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

  protected <T extends Entity> NodeConverter<T> propertyContainerConverterFactoryHasAnEntityWrapperTypeFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    NodeConverter<T> nodeConverter = mock(NodeConverter.class);
    when(propertyContainerConverterFactoryMock.createForType(argThat(equalTo(type)))).thenReturn(nodeConverter);
    return nodeConverter;
  }

  @Test
  public void addSystemEntitySavesTheSystemAsNodeAndReturnsItsId() throws Exception {
    Node nodeMock = aNode().createdBy(dbMock);
    idGeneratorMockCreatesIDFor(SYSTEM_ENTITY_TYPE, ID);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);
    // action
    String actualId = instance.addSystemEntity(SYSTEM_ENTITY_TYPE, aSystemEntity().build());

    // verify
    InOrder inOrder = inOrder(dbMock, transactionMock, systemEntityConverterMock);
    inOrder.verify(dbMock).beginTx();
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

  @Test(expected = StorageException.class)
  public void addSystemEntityRollsBackTheTransactionAndThrowsStorageExceptionObjectrapperThrowsAConversionException() throws Exception {
    Node nodeMock = aNode().createdBy(dbMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);

    TestSystemEntityWrapper systemEntity = aSystemEntity().build();
    doThrow(ConversionException.class).when(systemEntityConverterMock).addValuesToPropertyContainer(nodeMock, systemEntity);

    try {
      // action
      instance.addSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(systemEntityConverterMock).addValuesToPropertyContainer(nodeMock, systemEntity);
      verifyNoMoreInteractions(systemEntityConverterMock);
      verify(transactionMock).failure();
    }
  }

  @Test
  public void getEntityReturnsTheItemWhenFound() throws Exception {
    Node nodeMock2 = aNode().build();
    aSearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock2) //
        .foundInDB(dbMock);

    TestSystemEntityWrapper systemEntity = aSystemEntity().build();
    when(entityInstantiatorMock.createInstanceOf(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntity);
    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(systemEntity)));

    InOrder inOrder = inOrder(dbMock, propertyContainerConverterFactoryMock, systemEntityConverterMock, transactionMock);
    inOrder.verify(dbMock).beginTx();
    inOrder.verify(dbMock).findNodesByLabelAndProperty(SYSTEM_ENTITY_LABEL, ID_PROPERTY_NAME, ID);
    inOrder.verify(systemEntityConverterMock).addValuesToEntity(systemEntity, nodeMock2);
    inOrder.verify(transactionMock).success();
    verifyNoMoreInteractions(dbMock, systemEntityConverterMock);
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

    NodeConverter<SubADomainEntity> domainEntityConverterMock = propertyContainerConverterFactoryHasAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);
    when(entityInstantiatorMock.createInstanceOf(DOMAIN_ENTITY_TYPE)).thenReturn(domainEntity);

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

  @Test(expected = StorageException.class)
  public void getEntityThrowsStorageExceptionWhenEntityWrapperThrowsAConversionException() throws Exception {
    // setup
    Node nodeMock = aNode().build();
    aSearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID)//
        .withNode(nodeMock)//
        .foundInDB(dbMock);

    TestSystemEntityWrapper systemEntity = aSystemEntity().build();

    when(entityInstantiatorMock.createInstanceOf(SYSTEM_ENTITY_TYPE)).thenReturn(systemEntity);
    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);
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
    Node nodeMock = aNode().build();
    aSearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

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
    Node nodeMock = aNode().withRevision(FIRST_REVISION).build();
    aSearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);

    Change oldModified = new Change();
    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(FIRST_REVISION)//
        .withModified(oldModified)//
        .build();

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
    int newerRevision = 2;
    Node nodeMock = aNode().withRevision(newerRevision).build();
    aSearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(FIRST_REVISION)//
        .build();

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
    Node nodeMock = aNode().withRevision(FIRST_REVISION).build();
    aSearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    int newerRevision = 2;
    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(newerRevision).build();

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
    anEmptySearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(FIRST_REVISION).build();

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
    Node nodeMock = aNode().withRevision(FIRST_REVISION).build();
    aSearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasAnEntityWrapperTypeFor(SYSTEM_ENTITY_TYPE);

    Change oldModified = new Change();
    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(FIRST_REVISION)//
        .withModified(oldModified)//
        .build();

    doThrow(ConversionException.class).when(systemEntityConverterMock).updatePropertyContainer(nodeMock, systemEntity);

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
    Node nodeMock = aNode().withRevision(FIRST_REVISION).build();

    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    NodeConverter<SubADomainEntity> domainEntityConverterMock = propertyContainerConverterFactoryHasAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);;

    Change oldModified = new Change();
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(FIRST_REVISION)//
        .withAPid()//
        .withModified(oldModified)//
        .build();

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
    Node nodeWithThirdRevision = aNode().withRevision(THIRD_REVISION).build();

    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(aNode().withRevision(FIRST_REVISION).build()) //
        .andNode(aNode().withRevision(SECOND_REVISION).build()) //
        .andNode(nodeWithThirdRevision)//
        .foundInDB(dbMock);

    NodeConverter<SubADomainEntity> domainEntityConverterMock = propertyContainerConverterFactoryHasAnEntityWrapperTypeFor(DOMAIN_ENTITY_TYPE);

    Change oldModified = new Change();
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(THIRD_REVISION)//
        .withAPid()//
        .withModified(oldModified)//
        .build();

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
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    Change oldModified = new Change();
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(FIRST_REVISION)//
        .withAPid()//
        .withModified(oldModified)//
        .build();

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
    Node nodeMock = aNode().withRevision(SECOND_REVISION).build();

    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    Change oldModified = new Change();
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(FIRST_REVISION)//
        .withAPid()//
        .withModified(oldModified)//
        .build();

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
    Node nodeMock = aNode().withRevision(SECOND_REVISION).build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

    Change oldModified = new Change();
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(FIRST_REVISION)//
        .withAPid()//
        .withModified(oldModified)//
        .build();

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
    Relationship relMock1 = aRelationship().build();
    Relationship relMock2 = aRelationship().build();
    Node nodeMock2 = aNode().withARelationShip(relMock1).andRelationship(relMock2).build();

    aSearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock2) //
        .foundInDB(dbMock);

    // action
    int numDeleted = instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(numDeleted, is(equalTo(1)));
    InOrder inOrder = inOrder(dbMock, nodeMock2, relMock1, relMock2, transactionMock);
    inOrder.verify(dbMock).beginTx();
    verifyNodeAndItsRelationAreDelete(nodeMock2, relMock1, relMock2, inOrder);
    inOrder.verify(transactionMock).success();

  }

  @Test
  public void deleteSystemEntityReturns0WhenTheEntityCannotBeFound() throws Exception {
    // setup
    anEmptySearchResult().forLabel(SYSTEM_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

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
    Relationship relMock1 = aRelationship().build();
    Relationship relMock2 = aRelationship().build();
    Node nodeMock = aNode().withARelationShip(relMock1).andRelationship(relMock2).build();

    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .foundInDB(dbMock);

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
    Relationship relMock1 = aRelationship().build();
    Relationship relMock2 = aRelationship().build();
    Node nodeMock = aNode().withARelationShip(relMock1).andRelationship(relMock2).build();

    Relationship relMock3 = aRelationship().build();
    Relationship relMock4 = aRelationship().build();
    Node nodeMock2 = aNode().withARelationShip(relMock3).andRelationship(relMock4).build();

    Relationship relMock5 = aRelationship().build();
    Relationship relMock6 = aRelationship().build();
    Node nodeMock3 = aNode().withARelationShip(relMock5).andRelationship(relMock6).build();

    aSearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(ID) //
        .withNode(nodeMock) //
        .andNode(nodeMock2) //
        .andNode(nodeMock3) //
        .foundInDB(dbMock);

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
    anEmptySearchResult().forLabel(PRIMITIVE_DOMAIN_ENTITY_LABEL).andId(ID).foundInDB(dbMock);
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
