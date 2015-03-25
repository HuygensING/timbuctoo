package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.DomainEntityMatcher.likeDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.Neo4JLegacyStorageWrapper.RELATIONSHIP_ID_INDEX;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipIndexMockBuilder.aRelationshipIndexForName;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipTypeMatcher.likeRelationshipType;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.anEmptySearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubADomainEntityBuilder.aDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubARelationBuilder.aRelation;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.TestSystemEntityWrapperBuilder.aSystemEntity;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.TestSystemEntityWrapperMatcher.likeTestSystemEntityWrapper;
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

import com.google.common.collect.Lists;

public class Neo4JStorageTest {

  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final String PRIMITIVE_DOMAIN_ENTITY_NAME = TypeNames.getInternalName(PRIMITIVE_DOMAIN_ENTITY_TYPE);

  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Label DOMAIN_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(DOMAIN_ENTITY_TYPE));
  private static final String ID = "id";
  private static final int FIRST_REVISION = 1;
  private static final int SECOND_REVISION = 2;
  private static final String PID = "pid";
  private static final Change CHANGE = Change.newInternalInstance();

  private static final Class<Relationship> RELATIONSHIP_TYPE = Relationship.class;
  private static final String RELATION_TYPE_ID = "typeId";
  private static final String RELATION_TARGET_ID = "targetId";
  private static final String RELATION_SOURCE_ID = "sourceId";
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final Class<RelationType> RELATIONTYPE_TYPE = RelationType.class;
  private static final String RELATION_TYPE_NAME = TypeNames.getInternalName(RELATIONTYPE_TYPE);

  private Neo4JStorage instance;
  private PropertyContainerConverterFactory propertyContainerConverterFactoryMock;
  private Transaction transactionMock = mock(Transaction.class);
  private GraphDatabaseService dbMock;
  private NodeDuplicator nodeDuplicatorMock;
  private RelationshipDuplicator relationshipDuplicatorMock;
  private IdGenerator idGeneratorMock;
  private Neo4JLowLevelAPI neo4JLowLevelAPIMock;

  @Before
  public void setup() throws Exception {
    neo4JLowLevelAPIMock = mock(Neo4JLowLevelAPI.class);
    nodeDuplicatorMock = mock(NodeDuplicator.class);
    relationshipDuplicatorMock = mock(RelationshipDuplicator.class);
    idGeneratorMock = mock(IdGenerator.class);
    setupEntityConverterFactory();
    setupDBTransaction();
    TypeRegistry typeRegistry = TypeRegistry.getInstance().init("timbuctoo.model test.model");
    instance = new Neo4JStorage(dbMock, propertyContainerConverterFactoryMock, nodeDuplicatorMock, relationshipDuplicatorMock, idGeneratorMock, typeRegistry, neo4JLowLevelAPIMock);
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
  public void addSystemEntitySavesTheSystemAsNodeAndReturnsItsId() throws Exception {
    Node nodeMock = aNode().createdBy(dbMock);
    idGeneratorMockCreatesIDFor(SYSTEM_ENTITY_TYPE, ID);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(SYSTEM_ENTITY_TYPE);
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

  @Test(expected = StorageException.class)
  public void addSystemEntityRollsBackTheTransactionAndThrowsStorageExceptionObjectrapperThrowsAConversionException() throws Exception {
    Node nodeMock = aNode().createdBy(dbMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(SYSTEM_ENTITY_TYPE);

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
    Node nodeMock = aNode().build();
    latestNodeFoundFor(SYSTEM_ENTITY_TYPE, ID, nodeMock);

    TestSystemEntityWrapper systemEntity = aSystemEntity().build();
    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(SYSTEM_ENTITY_TYPE);
    when(systemEntityConverterMock.convertToEntity(nodeMock)).thenReturn(systemEntity);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(equalTo(systemEntity)));

    InOrder inOrder = inOrder(dbMock, systemEntityConverterMock, transactionMock);
    inOrder.verify(systemEntityConverterMock).convertToEntity(nodeMock);
    inOrder.verify(transactionMock).success();
  }

  @Test
  public void getEntityReturnsNullIfNoItemIsFound() throws Exception {
    // setup
    noLatestNodeFoundFor(SYSTEM_ENTITY_TYPE, ID);

    // action
    TestSystemEntityWrapper actualEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(actualEntity, is(nullValue()));

    verify(transactionMock).success();
    verifyZeroInteractions(propertyContainerConverterFactoryMock);
  }

  @Test(expected = ConversionException.class)
  public void getEntityThrowsStorageExceptionWhenEntityWrapperThrowsAConversionException() throws Exception {
    // setup
    Node nodeMock = aNode().build();
    latestNodeFoundFor(SYSTEM_ENTITY_TYPE, ID, nodeMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(SYSTEM_ENTITY_TYPE);
    when(systemEntityConverterMock.convertToEntity(nodeMock)).thenThrow(new ConversionException());

    try {
      // action
      instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      verify(systemEntityConverterMock).convertToEntity(nodeMock);
      verify(transactionMock).failure();
    }
  }

  @Test(expected = StorageException.class)
  public void getEntityThrowsStorageExceptionWhenNodeConverterThrowsAnInstantiationException() throws Exception {
    // setup
    Node nodeMock = aNode().build();
    latestNodeFoundFor(SYSTEM_ENTITY_TYPE, ID, nodeMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(SYSTEM_ENTITY_TYPE);
    doThrow(InstantiationException.class).when(systemEntityConverterMock).convertToEntity(nodeMock);

    try {
      // action
      instance.getEntity(SYSTEM_ENTITY_TYPE, ID);
    } finally {
      // verify
      verify(systemEntityConverterMock).convertToEntity(nodeMock);
      verify(transactionMock).failure();
    }
  }

  private void noLatestNodeFoundFor(Class<? extends Entity> type, String id) {
    when(neo4JLowLevelAPIMock.getLatestNodeById(type, id)).thenReturn(null);
  }

  private void latestNodeFoundFor(Class<? extends Entity> type, String id, Node foundNode) {
    when(neo4JLowLevelAPIMock.getLatestNodeById(type, id)).thenReturn(foundNode);
  }

  @Test
  public void updateDomainEntityRetrievesTheNodeAndUpdatesItsValues() throws Exception {
    // setup
    Node nodeMock = aNode().withRevision(FIRST_REVISION).build();
    latestNodeFoundFor(DOMAIN_ENTITY_TYPE, ID, nodeMock);

    NodeConverter<SubADomainEntity> domainEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);;

    Change oldModified = CHANGE;
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(FIRST_REVISION)//
        .withAPid()//
        .withModified(oldModified)//
        .build();

    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, CHANGE);

    // verify
    InOrder inOrder = inOrder(dbMock, domainEntityConverterMock, transactionMock);
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
  }

  @Test(expected = UpdateException.class)
  public void updateDomainEntityThrowsAnUpdateExceptionWhenTheEntityCannotBeFound() throws Exception {
    // setup
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    Change oldModified = CHANGE;
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(FIRST_REVISION)//
        .withAPid()//
        .withModified(oldModified)//
        .build();

    try {
      // action
      instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, CHANGE);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test(expected = UpdateException.class)
  public void updateDomainEntityThrowsAnUpdateExceptionWhenRevOfTheNodeIsHigherThanThatOfTheEntity() throws Exception {
    // setup
    Node nodeWithHigherRef = aNode().withRevision(SECOND_REVISION).build();
    latestNodeFoundFor(DOMAIN_ENTITY_TYPE, ID, nodeWithHigherRef);

    Change oldModified = CHANGE;
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(FIRST_REVISION)//
        .withAPid()//
        .withModified(oldModified)//
        .build();

    try {
      // action
      instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, CHANGE);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test(expected = UpdateException.class)
  public void updateDomainEntityThrowsAnUpdateExceptionWhenRevOfTheNodeIsLowerThanThatOfTheEntity() throws Exception {
    // setup
    Node nodeWithLowerRev = aNode().withRevision(SECOND_REVISION).build();
    latestNodeFoundFor(DOMAIN_ENTITY_TYPE, ID, nodeWithLowerRev);

    Change oldModified = CHANGE;
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(FIRST_REVISION)//
        .withAPid()//
        .withModified(oldModified)//
        .build();

    try {
      // action
      instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, domainEntity, CHANGE);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test
  public void updateSystemEntityRetrievesTheEntityAndUpdatesTheData() throws Exception {
    // setup
    Node nodeMock = aNode().withRevision(FIRST_REVISION).build();
    latestNodeFoundFor(SYSTEM_ENTITY_TYPE, ID, nodeMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(SYSTEM_ENTITY_TYPE);

    Change oldModified = new Change();
    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(FIRST_REVISION)//
        .withModified(oldModified)//
        .build();

    instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);

    // verify
    InOrder inOrder = inOrder(systemEntityConverterMock, transactionMock);
    inOrder.verify(systemEntityConverterMock).updatePropertyContainer(argThat(equalTo(nodeMock)), //
        argThat(likeTestSystemEntityWrapper() //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)));
    inOrder.verify(systemEntityConverterMock).updateModifiedAndRev(argThat(equalTo(nodeMock)), //
        argThat(likeTestSystemEntityWrapper() //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)));
    inOrder.verify(transactionMock).success();
  }

  @Test(expected = UpdateException.class)
  public void updateSystemEntityThrowsAnUpdateExceptionIfTheNodeIsNewerThanTheEntityWithTheUpdatedInformation() throws Exception {
    // setup
    Node nodeWithNewerRevision = aNode().withRevision(SECOND_REVISION).build();
    latestNodeFoundFor(SYSTEM_ENTITY_TYPE, ID, nodeWithNewerRevision);

    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(FIRST_REVISION)//
        .build();

    try {
      // action
      instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test(expected = UpdateException.class)
  public void updateSystemEntityThrowsAnUpdateExceptionIfTheNodeIsOlderThanTheEntityWithTheUpdatedInformation() throws Exception {
    // setup
    Node nodeWithLowerRev = aNode().withRevision(FIRST_REVISION).build();
    latestNodeFoundFor(SYSTEM_ENTITY_TYPE, ID, nodeWithLowerRev);

    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(SECOND_REVISION).build();

    try {
      // action
      instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test(expected = UpdateException.class)
  public void updateSystemEntityThrowsAnUpdateExceptionIfTheNodeCannotBeFound() throws Exception {
    // setup
    noLatestNodeFoundFor(SYSTEM_ENTITY_TYPE, ID);

    TestSystemEntityWrapper systemEntity = aSystemEntity() //
        .withId(ID)//
        .withRev(FIRST_REVISION).build();

    try {
      // action
      instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, systemEntity);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test(expected = ConversionException.class)
  public void updateSystemEntityThrowsAConversionExceptionWhenTheEntityConverterThrowsOne() throws Exception {
    // setup
    Node nodeMock = aNode().withRevision(FIRST_REVISION).build();
    latestNodeFoundFor(SYSTEM_ENTITY_TYPE, ID, nodeMock);

    NodeConverter<TestSystemEntityWrapper> systemEntityConverterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(SYSTEM_ENTITY_TYPE);

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
      verify(transactionMock).failure();
    }
  }

  @Test
  public void deleteDomainEntityFirstRemovesTheNodesRelationShipsAndThenTheNodeItselfTheDatabase() throws Exception {
    // setup
    Relationship relMock1 = aRelationship().build();
    Relationship relMock2 = aRelationship().build();
    Node nodeMock = aNode().withOutgoingRelationShip(relMock1).andOutgoingRelationship(relMock2).build();

    nodesFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, nodeMock);

    // action
    instance.deleteDomainEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, CHANGE);

    // verify
    InOrder inOrder = inOrder(dbMock, nodeMock, relMock1, relMock2, transactionMock);
    inOrder.verify(dbMock).beginTx();
    verifyNodeAndItsRelationAreDelete(nodeMock, relMock1, relMock2, inOrder);
    inOrder.verify(transactionMock).success();

  }

  private void nodesFoundFor(Class<? extends Entity> type, String id, Node... foundNodes) {
    when(neo4JLowLevelAPIMock.getNodesWithId(type, id)).thenReturn(Lists.newArrayList(foundNodes));
  }

  private void noNodesFoundFor(Class<? extends Entity> type, String id) {
    List<Node> nodes = Lists.newArrayList();
    when(neo4JLowLevelAPIMock.getNodesWithId(type, id)).thenReturn(nodes);
  }

  @Test
  public void deleteDomainEntityRemovesAllTheFoundNodes() throws Exception {
    // setup
    Relationship relMock1 = aRelationship().build();
    Relationship relMock2 = aRelationship().build();
    Node nodeMock = aNode().withOutgoingRelationShip(relMock1).andOutgoingRelationship(relMock2).build();

    Relationship relMock3 = aRelationship().build();
    Relationship relMock4 = aRelationship().build();
    Node nodeMock2 = aNode().withOutgoingRelationShip(relMock3).andOutgoingRelationship(relMock4).build();

    Relationship relMock5 = aRelationship().build();
    Relationship relMock6 = aRelationship().build();
    Node nodeMock3 = aNode().withOutgoingRelationShip(relMock5).andOutgoingRelationship(relMock6).build();

    nodesFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, nodeMock, nodeMock2, nodeMock3);

    // action
    instance.deleteDomainEntity(Neo4JLegacyStorageWrapperTest.PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, CHANGE);

    // verify
    InOrder inOrder = inOrder(dbMock, nodeMock, relMock1, relMock2, nodeMock2, relMock3, relMock4, nodeMock3, relMock5, relMock6, transactionMock);
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
    noNodesFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID);
    try {
      // action
      instance.deleteDomainEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, CHANGE);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteThrowsAnIllegalArgumentExceptionWhenTheEntityIsNotAPrimitiveDomainEntity() throws Exception {

    try {
      // action
      instance.deleteDomainEntity(DOMAIN_ENTITY_TYPE, ID, CHANGE);
    } finally {
      // verify
      verifyZeroInteractions(dbMock);
    }
  }

  @Test
  public void deleteSystemEntityFirstRemovesTheNodesRelationShipsAndThenTheNodeItselfTheDatabase() throws Exception {
    // setup
    Relationship relMock1 = aRelationship().build();
    Relationship relMock2 = aRelationship().build();
    Node nodeMock = aNode().withOutgoingRelationShip(relMock1).andOutgoingRelationship(relMock2).build();

    nodesFoundFor(SYSTEM_ENTITY_TYPE, ID, nodeMock);

    // action
    int numDeleted = instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(numDeleted, is(equalTo(1)));
    InOrder inOrder = inOrder(dbMock, nodeMock, relMock1, relMock2, transactionMock);
    inOrder.verify(dbMock).beginTx();
    verifyNodeAndItsRelationAreDelete(nodeMock, relMock1, relMock2, inOrder);
    inOrder.verify(transactionMock).success();

  }

  @Test
  public void deleteSystemEntityReturns0WhenTheEntityCannotBeFound() throws Exception {
    // setup
    noNodesFoundFor(SYSTEM_ENTITY_TYPE, ID);

    // action
    int numDeleted = instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);
    // verify
    assertThat(numDeleted, is(equalTo(0)));
    verify(transactionMock).success();
  }

  @Test
  public void getDomainEntityRevisionReturnsTheDomainEntityWithTheRequestedRevision() throws Exception {
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).withAPID().build();
    nodeWithRevisionFound(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION, nodeWithSameRevision);

    NodeConverter<SubADomainEntity> converter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(nodeWithSameRevision)).thenReturn(aDomainEntity().withAPid().build());

    // action
    SubADomainEntity entity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(entity, is(instanceOf(SubADomainEntity.class)));
    verify(converter).convertToEntity(nodeWithSameRevision);
    verify(transactionMock).success();
  }

  private void nodeWithRevisionFound(Class<SubADomainEntity> type, String id, int revision, Node node) {
    when(neo4JLowLevelAPIMock.getNodeWithRevision(type, id, revision)).thenReturn(node);
  }

  private void noNodeWithRevisionFound(Class<SubADomainEntity> type, String id, int revision) {
    when(neo4JLowLevelAPIMock.getNodeWithRevision(type, id, revision)).thenReturn(null);
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheFoundEntityHasNoPID() throws Exception {
    Node nodeWithoutPID = aNode().withRevision(FIRST_REVISION).build();
    nodeWithRevisionFound(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION, nodeWithoutPID);

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(nodeWithoutPID)).thenReturn(aDomainEntity().build());

    // action
    SubADomainEntity actualEntity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(actualEntity, is(nullValue()));
    verify(transactionMock).success();
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheEntityCannotBeFound() throws Exception {
    // setup
    noNodeWithRevisionFound(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

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
    nodeWithRevisionFound(DOMAIN_ENTITY_TYPE, ID, SECOND_REVISION, nodeWithDifferentRevision);

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
    nodeWithRevisionFound(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION, nodeWithSameRevision);

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
    nodeWithRevisionFound(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION, nodeWithSameRevision);

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
    latestNodeFoundFor(DOMAIN_ENTITY_TYPE, ID, nodeWithLatestRevision);

    NodeConverter<SubADomainEntity> converterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converterMock.convertToEntity(nodeWithLatestRevision)).thenReturn(aDomainEntity().withId(ID).build());

    // action
    instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);

    // verify
    verify(converterMock).addValuesToPropertyContainer( //
        argThat(equalTo(nodeWithLatestRevision)), //
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE).withId(ID).withPID(PID)));

  }

  @Test
  public void setDomainEntityPIDAddsAPIDToTheNodeAndDuplicatesTheNode() throws InstantiationException, IllegalAccessException, Exception {
    // setup
    Node node = aNode().build();
    latestNodeFoundFor(DOMAIN_ENTITY_TYPE, ID, node);

    NodeConverter<SubADomainEntity> converterMock = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converterMock.convertToEntity(node)).thenReturn(aDomainEntity().withId(ID).build());

    // action
    instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);

    // verify
    verify(nodeDuplicatorMock).saveDuplicate(node);
    verify(transactionMock).success();
  }

  @Test(expected = IllegalStateException.class)
  public void setDomainEntityPIDThrowsAnIllegalStateExceptionWhenTheEntityAlreadyHasAPID() throws Exception {
    // setup
    Node aNodeWithAPID = aNode().withAPID().build();
    latestNodeFoundFor(DOMAIN_ENTITY_TYPE, ID, aNodeWithAPID);

    SubADomainEntity entityWithPID = aDomainEntity().withAPid().build();

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(aNodeWithAPID)).thenReturn(entityWithPID);

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test(expected = ConversionException.class)
  public void setDomainEntityPIDThrowsAConversionExceptionWhenTheNodeCannotBeConverted() throws Exception {
    // setup
    Node aNode = aNode().build();
    latestNodeFoundFor(DOMAIN_ENTITY_TYPE, ID, aNode);

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(aNode)).thenThrow(new ConversionException());

    try {
      // action
      instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test(expected = ConversionException.class)
  public void setDomainEntityPIDThrowsAConversionsExceptionWhenTheUpdatedEntityCannotBeCovnverted() throws Exception {
    // setup
    Node aNode = aNode().build();
    latestNodeFoundFor(DOMAIN_ENTITY_TYPE, ID, aNode);

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
    noLatestNodeFoundFor(DOMAIN_ENTITY_TYPE, ID);

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
    latestNodeFoundFor(DOMAIN_ENTITY_TYPE, ID, aNode);

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
  public void addRelationAddsARelationshipToTheSourceAndReturnsTheId() throws Exception {
    // setup
    String name = "regularTypeName";

    Node sourceNodeMock = aNode().build();
    latestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID, sourceNodeMock);

    Node targetNodeMock = aNode().build();
    latestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_TARGET_ID, targetNodeMock);

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
    latestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID, sourceNodeMock);

    Node targetNodeMock = aNode().build();
    latestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_TARGET_ID, targetNodeMock);

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
    latestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID, sourceNodeMock);

    Node targetNodeMock = aNode().build();
    latestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_TARGET_ID, targetNodeMock);

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
    latestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID, sourceNodeMock);

    Node targetNodeMock = aNode().build();
    latestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_TARGET_ID, targetNodeMock);

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
    noLatestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID);

    SubARelation relation = new SubARelation();
    relation.setSourceId(RELATION_SOURCE_ID);
    relation.setSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME);

    try {
      // action
      instance.addRelation(RELATION_TYPE, relation, new Change());
    } finally {
      // verify
      verifyTransactionFailed();
    }
  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAStorageExceptionWhenTheTargetCannotBeFound() throws Exception {
    // setup
    latestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID, aNode().build());
    noLatestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_TARGET_ID);

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
      verifyTransactionFailed();
    }
  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAStorageExceptionWhenRelationTypeCannotBeFound() throws Exception {
    // setup
    latestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID, aNode().build());
    latestNodeFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_TARGET_ID, aNode().build());
    noLatestNodeFoundFor(RELATIONTYPE_TYPE, RELATION_TYPE_ID);

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
      verifyTransactionFailed();
    }
  }

  private NodeConverter<RelationType> relationTypeWithRegularNameExists(String name) throws Exception {
    Node relationTypeNodeMock = aNode().build();
    latestNodeFoundFor(RELATIONTYPE_TYPE, RELATION_TYPE_ID, relationTypeNodeMock);

    NodeConverter<RelationType> relationTypeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(RELATIONTYPE_TYPE);
    RelationType relationType = new RelationType();
    relationType.setRegularName(name);
    when(relationTypeConverter.convertToEntity(relationTypeNodeMock)).thenReturn(relationType);

    return relationTypeConverter;
  }

  @Test
  public void getRelationReturnsTheRelationThatBelongsToTheId() throws Exception {
    // setup
    Relationship relationshipMock = aRelationship().build();
    latestRelationshipFoundForId(ID, relationshipMock);
    SubARelation relation = aRelation().build();

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(relationConverterMock.convertToEntity(relationshipMock)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getRelation(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verifyTransactionSucceeded();
  }

  private void latestRelationshipFoundForId(String string, Relationship foundRelation) {
    when(neo4JLowLevelAPIMock.getLatestRelationship(string)).thenReturn(foundRelation);
  }

  private void noLatestRelationshipFoundForId(String string) {
    when(neo4JLowLevelAPIMock.getLatestRelationship(string)).thenReturn(null);
  }

  @Test
  public void getRelationReturnsNullIfTheRelationIsNotFound() throws Exception {
    // setup
    noLatestRelationshipFoundForId(ID);

    // action
    SubARelation actualRelation = instance.getRelation(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(nullValue()));

    verifyTransactionSucceeded();
  }

  @Test(expected = ConversionException.class)
  public void getRelationThrowsAConversionExceptionWhenTheRelationConverterDoes() throws Exception {
    // setup
    Relationship relationshipMock = aRelationship().build();
    latestRelationshipFoundForId(ID, relationshipMock);
    SubARelation relation = aRelation().build();

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    doThrow(ConversionException.class).when(relationConverterMock).convertToEntity(relationshipMock);

    // action
    SubARelation actualRelation = instance.getRelation(RELATION_TYPE, ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verifyTransactionFailed();
  }

  @Test(expected = StorageException.class)
  public void getRelationThrowsStorageExceptionWhenRelationshipConverterThrowsAnInstantiationException() throws Exception {
    // setup
    Relationship relationshipMock = aRelationship().build();
    latestRelationshipFoundForId(ID, relationshipMock);

    RelationshipConverter<SubARelation> relationConverterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    doThrow(InstantiationException.class).when(relationConverterMock).convertToEntity(relationshipMock);

    try {
      // action
      instance.getRelation(RELATION_TYPE, ID);
    } finally {
      // verify
      verifyTransactionFailed();
    }
  }

  @Test
  public void getRelationRevisionReturnsTheRelationForTheRequestedRevision() throws Exception {
    Relationship relationshipWithPID = aRelationship()//
        .withRevision(FIRST_REVISION)//
        .withAPID()//
        .build();

    relationshipWithRevisionFound(RELATION_TYPE, ID, FIRST_REVISION, relationshipWithPID);

    SubARelation relation = aRelation().withAPID().build();
    RelationshipConverter<SubARelation> converterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    when(converterMock.convertToEntity(relationshipWithPID)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));

    verifyTransactionSucceeded();
  }

  private void relationshipWithRevisionFound(Class<SubARelation> type, String id, int revision, Relationship foundRelationship) {
    when(neo4JLowLevelAPIMock.getRelationshipWithRevision(type, id, revision)).thenReturn(foundRelationship);
  }

  private void noRelationshipWithRevisionFound(Class<SubARelation> type, String id, int revision) {
    when(neo4JLowLevelAPIMock.getRelationshipWithRevision(type, id, revision)).thenReturn(null);
  }

  @Test
  public void getRelationRevisionReturnsNullIfTheFoundRelationshipHasNoPID() throws Exception {
    Relationship relationshipWithoutPID = aRelationship().withRevision(FIRST_REVISION).build();
    relationshipWithRevisionFound(RELATION_TYPE, ID, FIRST_REVISION, relationshipWithoutPID);

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
    // setup
    noRelationshipWithRevisionFound(RELATION_TYPE, ID, FIRST_REVISION);

    // action
    SubARelation relation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

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
    relationshipWithRevisionFound(RELATION_TYPE, ID, FIRST_REVISION, relationshipWithPID);

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
    relationshipWithRevisionFound(RELATION_TYPE, ID, FIRST_REVISION, relationshipWithPID);

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

  @Test
  public void updateRelationRetrievesTheRelationAndUpdateItsValuesAndAdministrativeValues() throws Exception {
    // setup
    Relationship relationship = aRelationship().withRevision(FIRST_REVISION).build();
    latestRelationshipFoundForId(ID, relationship);

    Change oldModified = CHANGE;
    SubARelation relation = aRelation()//
        .withId(ID) //
        .withRevision(FIRST_REVISION) //
        .withModified(oldModified) //
        .build();

    RelationshipConverter<SubARelation> converterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);

    // action
    instance.updateRelation(RELATION_TYPE, relation, CHANGE);

    // verify
    verify(converterMock).updatePropertyContainer( //
        argThat(equalTo(relationship)), //
        argThat(likeDomainEntity(RELATION_TYPE) //
            .withId(ID) //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)));
    verify(converterMock).updateModifiedAndRev( //
        argThat(equalTo(relationship)), //
        argThat(likeDomainEntity(RELATION_TYPE) //
            .withId(ID) //
            .withAModifiedValueNotEqualTo(oldModified) //
            .withRevision(SECOND_REVISION)));
    verifyTransactionSucceeded();
  }

  @Test
  public void updateRelationRemovesThePIDOfTheRelationBeforeTheUpdate() throws Exception {
    // setup
    Relationship relationship = aRelationship().withRevision(FIRST_REVISION).build();
    latestRelationshipFoundForId(ID, relationship);

    Change oldModified = CHANGE;
    SubARelation relation = aRelation()//
        .withId(ID) //
        .withRevision(FIRST_REVISION) //
        .withModified(oldModified) //
        .withAPID() //
        .build();

    RelationshipConverter<SubARelation> converterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);

    // action
    instance.updateRelation(RELATION_TYPE, relation, CHANGE);

    // verify
    verify(converterMock).updatePropertyContainer( //
        argThat(equalTo(relationship)), //
        argThat(likeDomainEntity(RELATION_TYPE).withoutAPID()));
    verify(converterMock).updateModifiedAndRev( //
        argThat(equalTo(relationship)), //
        argThat(likeDomainEntity(RELATION_TYPE).withoutAPID()));
  }

  @Test(expected = UpdateException.class)
  public void updateRelationThrowsAnUpdateExceptionWhenTheRelationshipToUpdateCannotBeFound() throws Exception {
    // setup
    noLatestRelationshipFoundForId(ID);

    SubARelation relation = aRelation()//
        .withId(ID)//
        .withRevision(FIRST_REVISION)//
        .build();
    try {
      // action
      instance.updateRelation(RELATION_TYPE, relation, CHANGE);
    } finally {
      // verify
      verifyTransactionFailed();
    }
  }

  @Test(expected = UpdateException.class)
  public void updateRelationThrowsAnUpdateExceptionWhenRevOfTheRelationshipIsHigherThanThatOfTheEntity() throws Exception {
    // setup
    Relationship relationshipWithHigherRev = aRelationship().withRevision(SECOND_REVISION).build();
    latestRelationshipFoundForId(ID, relationshipWithHigherRev);

    SubARelation relation = aRelation()//
        .withId(ID)//
        .withRevision(FIRST_REVISION) //
        .build();

    try {
      // action
      instance.updateRelation(RELATION_TYPE, relation, CHANGE);
    } finally {
      // verify
      verify(dbMock).beginTx();

      verifyTransactionFailed();
    }
  }

  @Test(expected = UpdateException.class)
  public void updateRelationThrowsAnUpdateExceptionWhenRevOfTheRelationshipIsLowerThanThatOfTheEntity() throws Exception {
    // setup
    Relationship relationshipWithLowerRev = aRelationship().withRevision(FIRST_REVISION).build();
    latestRelationshipFoundForId(ID, relationshipWithLowerRev);

    SubARelation relation = aRelation()//
        .withId(ID)//
        .withRevision(SECOND_REVISION) //
        .build();

    try {
      // action
      instance.updateRelation(RELATION_TYPE, relation, CHANGE);
    } finally {
      // verify
      verify(dbMock).beginTx();

      verifyTransactionFailed();
    }
  }

  @Test(expected = ConversionException.class)
  public void updateRelationThrowsAConversionExceptionWhenTheRelationshipConverterThrowsOne() throws Exception {
    // setup
    Relationship relationship = aRelationship().withRevision(FIRST_REVISION).build();
    latestRelationshipFoundForId(ID, relationship);

    Change oldModified = CHANGE;
    SubARelation relation = aRelation()//
        .withId(ID) //
        .withRevision(FIRST_REVISION) //
        .build();

    RelationshipConverter<SubARelation> converterMock = propertyContainerConverterFactoryHasRelationshipConverterFor(RELATION_TYPE);
    doThrow(ConversionException.class).when(converterMock).updatePropertyContainer(relationship, relation);

    try {
      // action
      instance.updateRelation(RELATION_TYPE, relation, oldModified);
    } finally {
      // verify
      verify(dbMock).beginTx();
      verify(converterMock).updatePropertyContainer(relationship, relation);
      verifyTransactionFailed();
    }
  }

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

}
