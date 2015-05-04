package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_ID;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_ID;
import static nl.knaw.huygens.timbuctoo.storage.graph.DomainEntityMatcher.likeDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.graph.SubADomainEntityBuilder.aDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.graph.SubARelationBuilder.aRelation;
import static nl.knaw.huygens.timbuctoo.storage.graph.TestSystemEntityWrapperBuilder.aSystemEntity;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeMockBuilder.anEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TinkerpopStorageTest {

  private static final String FIELD_NAME = "fieldName";
  private static final String PROPERTY_NAME = "propertyName";
  private static final String PROPERTY_VALUE = "propertyValue";
  private static final String A_LABEL = "aLabel";
  private static final Class<Relation> PRIMITIVE_RELATION_TYPE = Relation.class;
  private static final String PID = "pid";
  private static final String REGULAR_RELATION_NAME = "regularTypeName";
  private static final int FIRST_REVISION = 1;
  private static final int SECOND_REVISION = 2;
  private static final int THIRD_REVISION = 3;

  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Change CHANGE = new Change();
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final String ID = "id";

  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final String RELATION_TYPE_ID = "typeId";
  private static final String RELATION_TARGET_ID = "targetId";
  private static final String RELATION_SOURCE_ID = "sourceId";
  private static final Class<RelationType> RELATIONTYPE_TYPE = RelationType.class;
  private static final String RELATION_TYPE_NAME = TypeNames.getInternalName(RELATIONTYPE_TYPE);
  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final String PRIMITIVE_DOMAIN_ENTITY_NAME = TypeNames.getInternalName(PRIMITIVE_DOMAIN_ENTITY_TYPE);

  private Graph dbMock;
  private TinkerpopStorage instance;
  private ElementConverterFactory elementConverterFactoryMock;
  private Vertex createdVertex;
  private TinkerpopLowLevelAPI lowLevelAPIMock;
  private StorageIteratorFactory storageIteratorFactoryMock;

  @Before
  public void setup() throws Exception {
    dbMock = mock(Graph.class);
    lowLevelAPIMock = mock(TinkerpopLowLevelAPI.class);
    elementConverterFactoryMock = mock(ElementConverterFactory.class);
    TypeRegistry typeRegistry = TypeRegistry.getInstance().init("timbuctoo.model test.model test.model.projecta");
    storageIteratorFactoryMock = mock(StorageIteratorFactory.class);
    instance = new TinkerpopStorage(dbMock, elementConverterFactoryMock, lowLevelAPIMock, typeRegistry, storageIteratorFactoryMock);

    createdVertex = mock(Vertex.class);
    when(dbMock.addVertex(null)).thenReturn(createdVertex);
  }

  @Test
  public void addDomainEntitySavesTheProjectVersionAndThePrimitive() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();

    VertexConverter<SubADomainEntity> converter = compositeVertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);

    // action
    instance.addDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);

    // verify
    verify(converter).addValuesToElement(createdVertex, entity);
  }

  @Test(expected = ConversionException.class)
  public void addDomainEntityRollsBackTheTransactionAndThrowsAConversionExceptionWhenTheDomainEntityConverterThrowsAConversionException() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();

    VertexConverter<SubADomainEntity> converter = compositeVertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(converter).addValuesToElement(createdVertex, entity);

    // action
    instance.addDomainEntity(DOMAIN_ENTITY_TYPE, entity, CHANGE);
  }

  @Test
  public void addSystemEntitySavesTheSystemAsVertex() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    VertexConverter<TestSystemEntityWrapper> vertexConverter = vertexConverterCreatedFor(SYSTEM_ENTITY_TYPE);

    // action
    instance.addSystemEntity(SYSTEM_ENTITY_TYPE, entity);

    // verify
    verify(dbMock).addVertex(null);
    verify(vertexConverter).addValuesToElement(createdVertex, entity);
  }

  @Test(expected = StorageException.class)
  public void addSystemEntityRollsBackTheTransactionAndThrowsStorageExceptionVertexConverterThrowsAConversionException() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    VertexConverter<TestSystemEntityWrapper> vertexConverter = vertexConverterCreatedFor(SYSTEM_ENTITY_TYPE);
    doThrow(ConversionException.class).when(vertexConverter).addValuesToElement(createdVertex, entity);

    try {
      // action
      instance.addSystemEntity(SYSTEM_ENTITY_TYPE, entity);
    } finally {

      // verify
      verify(dbMock).removeVertex(createdVertex);
    }
  }

  @Test
  public void countEntitiesRequestsAnIteratorWithTheLatestEntitiesOfTheLowLevelAPI() {
    // setup
    List<Vertex> foundVertices = Lists.newArrayList(aVertex().build(), aVertex().build());
    when(lowLevelAPIMock.getLatestVerticesOf(SYSTEM_ENTITY_TYPE)).thenReturn(foundVertices.iterator());

    // action
    long numberOfEntities = instance.countEntities(SYSTEM_ENTITY_TYPE);

    // verify
    assertThat(numberOfEntities, is((long) foundVertices.size()));
  }

  @Test
  public void countEntitiesCountsThePrimitiveDomainEntities() {
    // setup
    List<Vertex> foundVertices = Lists.newArrayList(aVertex().build(), aVertex().build());
    when(lowLevelAPIMock.getLatestVerticesOf(PRIMITIVE_DOMAIN_ENTITY_TYPE)).thenReturn(foundVertices.iterator());

    // action
    instance.countEntities(DOMAIN_ENTITY_TYPE);

    // verify
    verify(lowLevelAPIMock).getLatestVerticesOf(PRIMITIVE_DOMAIN_ENTITY_TYPE);
  }

  @Test
  public void deleteDomainEntityFirstRemovesTheVerticesWithItsEdgesFromTheDatabase() throws Exception {
    // setup
    Edge incomingEdge1 = anEdge().withLabel(A_LABEL).build();
    Edge outgoingEdge1 = anEdge().withLabel(A_LABEL).build();
    Vertex vertex1 = aVertex().withIncomingEdge(incomingEdge1).withOutgoingEdge(outgoingEdge1).build();
    Edge incomingEdge2 = anEdge().withLabel(A_LABEL).build();
    Edge outgoingEdge2 = anEdge().withLabel(A_LABEL).build();
    Vertex vertex2 = aVertex().withIncomingEdge(incomingEdge2).withOutgoingEdge(outgoingEdge2).build();

    verticesFoundWithTypeAndId(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, vertex1, vertex2);

    // action
    instance.deleteDomainEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID);

    // verify
    verify(dbMock).removeEdge(incomingEdge1);
    verify(dbMock).removeEdge(outgoingEdge1);
    verify(dbMock).removeVertex(vertex1);
    verify(dbMock).removeEdge(incomingEdge2);
    verify(dbMock).removeEdge(outgoingEdge2);
    verify(dbMock).removeVertex(vertex2);

  }

  @Test(expected = NoSuchEntityException.class)
  public void deleteDomainEntityThrowsANoSuchEntityExceptionWhenTheEntityCannotBeFound() throws Exception {
    // setup
    noVerticesWithTypeAndIdFound(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID);

    // action
    instance.deleteDomainEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteThrowsAnIllegalArgumentExceptionWhenTheEntityIsNotAPrimitiveDomainEntity() throws Exception {
    // action
    instance.deleteDomainEntity(DOMAIN_ENTITY_TYPE, ID);
  }

  @Test
  public void deleteSystemEntityFirstRemovesTheVerticesWithItsEdgesFromTheDatabase() throws Exception {
    // setup
    Edge incomingEdge1 = anEdge().withLabel(A_LABEL).build();
    Edge outgoingEdge1 = anEdge().withLabel(A_LABEL).build();
    Vertex vertex1 = aVertex().withIncomingEdge(incomingEdge1).withOutgoingEdge(outgoingEdge1).build();
    Edge incomingEdge2 = anEdge().withLabel(A_LABEL).build();
    Edge outgoingEdge2 = anEdge().withLabel(A_LABEL).build();
    Vertex vertex2 = aVertex().withIncomingEdge(incomingEdge2).withOutgoingEdge(outgoingEdge2).build();

    verticesFoundWithTypeAndId(SYSTEM_ENTITY_TYPE, ID, vertex1, vertex2);

    // action
    int numberOfDeletedEntities = instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(numberOfDeletedEntities, is(2));
    verify(dbMock).removeEdge(incomingEdge1);
    verify(dbMock).removeEdge(outgoingEdge1);
    verify(dbMock).removeVertex(vertex1);
    verify(dbMock).removeEdge(incomingEdge2);
    verify(dbMock).removeEdge(outgoingEdge2);
    verify(dbMock).removeVertex(vertex2);

  }

  private void verticesFoundWithTypeAndId(Class<? extends Entity> type, String id, Vertex... vertices) {
    List<Vertex> vertexList = Lists.newArrayList(vertices);

    when(lowLevelAPIMock.getVerticesWithId(type, id)).thenReturn(vertexList.iterator());
  }

  @Test
  public void deleteSystemEntityReturns0WhenTheEntityCannotBeFound() throws Exception {
    noVerticesWithTypeAndIdFound(SYSTEM_ENTITY_TYPE, ID);

    instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, ID);
  }

  private void noVerticesWithTypeAndIdFound(Class<? extends Entity> type, String id) {
    List<Vertex> entitiesFound = Lists.newArrayList();
    when(lowLevelAPIMock.getVerticesWithId(type, id)).thenReturn(entitiesFound.iterator());
  }

  @Test
  public void entityExistsTriesToRetrieveTheVertextWithTheIdAndReturnsTrueIfTheVertextExists() {
    // setup
    latestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID, aVertex().build());

    // action
    boolean entityExists = instance.entityExists(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(entityExists, is(equalTo(true)));
  }

  @Test
  public void entityExistsTriesToRetrieveTheVertextWithTheIdAndReturnsFalseIfTheVertextDoesNotExist() {
    // setup
    noLatestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID);

    // action
    boolean entityExists = instance.entityExists(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(entityExists, is(equalTo(false)));
  }

  @Test
  public void findEntityByPropertyConvertsTheFirstNodeFoundWithProperty() throws Exception {
    // setup
    Vertex foundVertex = aVertex().build();
    Iterator<Vertex> vertexIterator = Lists.<Vertex> newArrayList(foundVertex).iterator();
    when(lowLevelAPIMock.findVerticesByProperty(DOMAIN_ENTITY_TYPE, PROPERTY_NAME, PROPERTY_VALUE)).thenReturn(vertexIterator);

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    when(converter.getPropertyName(FIELD_NAME)).thenReturn(PROPERTY_NAME);
    SubADomainEntity entity = aDomainEntity().build();
    when(converter.convertToEntity(foundVertex)).thenReturn(entity);

    // action
    SubADomainEntity actualEntity = instance.findEntityByProperty(DOMAIN_ENTITY_TYPE, FIELD_NAME, PROPERTY_VALUE);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));
  }

  @Test
  public void findEntityByPropertyReturnsNullIfNoVertexIsFound() throws Exception {
    // setup
    noEntitiesFoundByPropertyWithValue(DOMAIN_ENTITY_TYPE, PROPERTY_NAME, PROPERTY_VALUE);

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    when(converter.getPropertyName(FIELD_NAME)).thenReturn(PROPERTY_NAME);

    // action
    SubADomainEntity entity = instance.findEntityByProperty(DOMAIN_ENTITY_TYPE, FIELD_NAME, PROPERTY_VALUE);

    // verify
    assertThat(entity, is(nullValue()));

  }

  private void noEntitiesFoundByPropertyWithValue(Class<? extends Entity> type, String name, String value) {
    Iterator<Vertex> vertexIterator = Lists.<Vertex> newArrayList().iterator();
    when(lowLevelAPIMock.findVerticesByProperty(type, name, value)).thenReturn(vertexIterator);
  }

  @Test(expected = ConversionException.class)
  public void findEntityByPropertyThrowsAConversionExceptionWhenTheNodeCannotBeConverted() throws Exception {
    // setup
    Vertex foundVertex = aVertex().build();
    Iterator<Vertex> vertexIterator = Lists.<Vertex> newArrayList(foundVertex).iterator();
    when(lowLevelAPIMock.findVerticesByProperty(DOMAIN_ENTITY_TYPE, PROPERTY_NAME, PROPERTY_VALUE)).thenReturn(vertexIterator);

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    when(converter.getPropertyName(FIELD_NAME)).thenReturn(PROPERTY_NAME);
    when(converter.convertToEntity(foundVertex)).thenThrow(new ConversionException());

    // action
    instance.findEntityByProperty(DOMAIN_ENTITY_TYPE, FIELD_NAME, PROPERTY_VALUE);

  }

  @Test
  public void getAllVariationsReturnsAllVariationsOfANode() throws Exception {
    // setup
    Vertex vertex = aVertex()//
        .withType(PRIMITIVE_DOMAIN_ENTITY_TYPE)//
        .withType(DOMAIN_ENTITY_TYPE)//
        .build();
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, vertex);

    VertexConverter<BaseDomainEntity> primitiveConverter = vertexConverterCreatedFor(PRIMITIVE_DOMAIN_ENTITY_TYPE);
    BaseDomainEntity baseDomainEntity = new BaseDomainEntity();
    when(primitiveConverter.convertToEntity(vertex)).thenReturn(baseDomainEntity);
    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    SubADomainEntity domainEntity = aDomainEntity().build();
    when(converter.convertToEntity(vertex)).thenReturn(domainEntity);

    // action
    List<BaseDomainEntity> variations = instance.getAllVariations(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(variations, containsInAnyOrder(baseDomainEntity, domainEntity));
  }

  @Test
  public void getAllVariationsReturnsAnEmptyListWhenNoVariationsCouldBeFound() throws StorageException {
    // setup
    noLatestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID);

    // action
    List<BaseDomainEntity> allVariations = instance.getAllVariations(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(allVariations, is(emptyCollectionOf(PRIMITIVE_DOMAIN_ENTITY_TYPE)));
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void getAllVariationsThrowsAnIllegalArgumentExceptionWhenTheTypeIsNotAPrimitive() throws Exception {
    // setup
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Nonprimitive type");
    thrown.expectMessage("" + DOMAIN_ENTITY_TYPE);

    // action
    instance.getAllVariations(DOMAIN_ENTITY_TYPE, ID);
  }

  @Test(expected = ConversionException.class)
  public void getAllVariationsThrowsAConversionExceptionWhenTheNodeCouldNotBeConverted() throws Exception {
    // setup
    Vertex vertex = aVertex()//
        .withType(PRIMITIVE_DOMAIN_ENTITY_TYPE)//
        .withType(DOMAIN_ENTITY_TYPE)//
        .build();
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, vertex);

    VertexConverter<BaseDomainEntity> primitiveConverter = vertexConverterCreatedFor(PRIMITIVE_DOMAIN_ENTITY_TYPE);
    when(primitiveConverter.convertToEntity(vertex)).thenThrow(new ConversionException());
    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(vertex)).thenReturn(aDomainEntity().build());

    // action
    instance.getAllVariations(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID);

  }

  @Test
  public void getDomainEntityRevisionReturnsTheDomainEntityWithTheRequestedRevision() throws Exception {
    // setup
    Vertex vertex = vertexWithRevisionFound(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);
    VertexConverter<SubADomainEntity> vertexConverter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    SubADomainEntity entity = aDomainEntity().withAPid().build();
    when(vertexConverter.convertToEntity(vertex)).thenReturn(entity);

    // instance
    SubADomainEntity revision = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(revision, is(sameInstance(entity)));
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheFoundEntityHasNoPID() throws Exception {
    // setup
    Vertex vertex = vertexWithRevisionFound(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);
    VertexConverter<SubADomainEntity> vertexConverter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    SubADomainEntity entityWithoutAPID = aDomainEntity().build();
    when(vertexConverter.convertToEntity(vertex)).thenReturn(entityWithoutAPID);

    // instance
    SubADomainEntity revision = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(revision, is(nullValue()));
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheRevisionCannotBeFound() throws Exception {
    // setup
    noVertexWithRevisionFound(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // instance
    SubADomainEntity foundRevision = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(foundRevision, is(nullValue()));

  }

  @Test(expected = ConversionException.class)
  public void getDomainEntityRevisionThrowsAConversionExceptionIfTheEntityCannotBeConverted() throws Exception {
    // setup
    Vertex vertex = vertexWithRevisionFound(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);
    VertexConverter<SubADomainEntity> vertexConverter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    when(vertexConverter.convertToEntity(vertex)).thenThrow(new ConversionException());

    // instance
    instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);
  }

  private void noVertexWithRevisionFound(Class<SubADomainEntity> type, String id, int rev) {
    when(lowLevelAPIMock.getVertexWithRevision(type, id, rev)).thenReturn(null);
  }

  private Vertex vertexWithRevisionFound(Class<SubADomainEntity> type, String id, int rev) {
    Vertex vertex = aVertex().build();
    when(lowLevelAPIMock.getVertexWithRevision(type, id, rev)).thenReturn(vertex);
    return vertex;
  }

  @Test
  public void getEntityReturnsTheItemWhenFound() throws Exception {
    // setup
    Vertex vertex = aVertex().build();
    latestVertexFoundFor(SYSTEM_ENTITY_TYPE, ID, vertex);

    VertexConverter<TestSystemEntityWrapper> vertexConverter = vertexConverterCreatedFor(SYSTEM_ENTITY_TYPE);
    TestSystemEntityWrapper entity = new TestSystemEntityWrapper();
    when(vertexConverter.convertToEntity(vertex)).thenReturn(entity);

    // action
    TestSystemEntityWrapper foundEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(foundEntity, is(sameInstance(entity)));
  }

  @Test
  public void getEntityReturnsNullIfNoItemIsFound() throws Exception {
    // setup
    noLatestVertexFoundFor(SYSTEM_ENTITY_TYPE, ID);

    // action
    TestSystemEntityWrapper entity = instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(entity, is(nullValue()));
  }

  private void noLatestVertexFoundFor(Class<? extends Entity> type, String id) {
    when(lowLevelAPIMock.getLatestVertexById(type, id)).thenReturn(null);
  }

  @Test(expected = ConversionException.class)
  public void getEntityThrowsConversionExceptionIfTheFoundVertexCannotBeConvertedToTheEntity() throws Exception {
    // setup
    Vertex vertex = aVertex().build();
    latestVertexFoundFor(SYSTEM_ENTITY_TYPE, ID, vertex);

    VertexConverter<TestSystemEntityWrapper> vertexConverter = vertexConverterCreatedFor(SYSTEM_ENTITY_TYPE);
    when(vertexConverter.convertToEntity(vertex)).thenThrow(new ConversionException());

    // action
    instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

  }

  @Test
  public void getEntitiesRetrieveWrapsAllTheVerticesOfACertainTypeInAStorageIterator() throws Exception {
    // setup
    Iterator<Vertex> iterator = Lists.<Vertex> newArrayList().iterator();
    when(lowLevelAPIMock.getLatestVerticesOf(SYSTEM_ENTITY_TYPE)).thenReturn(iterator);

    @SuppressWarnings("unchecked")
    StorageIterator<TestSystemEntityWrapper> storageIterator = mock(StorageIterator.class);
    when(storageIteratorFactoryMock.create(SYSTEM_ENTITY_TYPE, iterator)).thenReturn(storageIterator);

    // action
    StorageIterator<TestSystemEntityWrapper> actualStorageIterator = instance.getEntities(SYSTEM_ENTITY_TYPE);

    // verify
    assertThat(actualStorageIterator, is(sameInstance(storageIterator)));
  }

  @Test(expected = StorageException.class)
  public void getEntitiesThrowsAStorageExceptionWhenTheIteratorCannotBeCreated() throws Exception {
    // setup
    Iterator<Vertex> iterator = Lists.<Vertex> newArrayList().iterator();
    when(lowLevelAPIMock.getLatestVerticesOf(SYSTEM_ENTITY_TYPE)).thenReturn(iterator);

    when(storageIteratorFactoryMock.create(SYSTEM_ENTITY_TYPE, iterator)).thenThrow(new StorageException());

    // action
    instance.getEntities(SYSTEM_ENTITY_TYPE);

  }

  @Test
  public void updateEntityRetrievesTheEntityAndUpdatesTheData() throws Exception {
    // setup
    Vertex vertex = aVertex().withRev(FIRST_REVISION).build();
    latestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID, vertex);
    SubADomainEntity entity = aDomainEntity().withId(ID).withRev(SECOND_REVISION).build();

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);

    // action
    instance.updateEntity(DOMAIN_ENTITY_TYPE, entity);

    // verify
    verify(converter).updateModifiedAndRev(vertex, entity);
    verify(converter).updateElement(vertex, entity);
  }

  @Test(expected = ConversionException.class)
  public void updateEntityThrowsAConversionExceptionWhenTheVertexConverterThrowsOne() throws Exception {
    // setup
    Vertex vertex = aVertex().withRev(FIRST_REVISION).build();
    latestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID, vertex);
    SubADomainEntity entity = aDomainEntity().withId(ID).withRev(SECOND_REVISION).build();

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(converter).updateElement(vertex, entity);

    // action
    instance.updateEntity(DOMAIN_ENTITY_TYPE, entity);

  }

  @Test(expected = NoSuchEntityException.class)
  public void updateEntityThrowsANoSuchEntityExceptionIfTheVertexCannotBeFound() throws Exception {
    // setup
    noLatestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID);

    // action
    instance.updateEntity(DOMAIN_ENTITY_TYPE, aDomainEntity().withId(ID).build());
  }

  @Test(expected = UpdateException.class)
  public void updateEntityThrowsAnUpdateExceptionIfTheVertexHasAHigherRevThanTheEntity() throws Exception {
    testUpdateEntityRevisionExceptions(SECOND_REVISION, FIRST_REVISION);
  }

  @Test(expected = UpdateException.class)
  public void updateEntityThrowsAnUpdateExceptionWhenRevOfTheVertexIsEqualToThatOfTheEntity() throws Exception {
    testUpdateEntityRevisionExceptions(FIRST_REVISION, FIRST_REVISION);
  }

  @Test(expected = UpdateException.class)
  public void updateEntityThrowsAnUpdateExceptionWhenRevOfTheVertexIsMoreThanOneLowerThanThatOfTheEntity() throws Exception {
    testUpdateEntityRevisionExceptions(FIRST_REVISION, THIRD_REVISION);
  }

  private void testUpdateEntityRevisionExceptions(int nodeRev, int entityRev) throws Exception {
    // setup
    Vertex vertex = aVertex().withRev(nodeRev).build();
    latestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID, vertex);

    Change oldModified = CHANGE;
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(entityRev)//
        .withAPid()//
        .withModified(oldModified)//
        .build();

    instance.updateEntity(DOMAIN_ENTITY_TYPE, domainEntity);
  }

  @Test
  public void addVariantAddsANewVariantToTheExistingNodeOfTheBaseType() throws Exception {
    // setup
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(SECOND_REVISION)//
        .withAPid()//
        .build();

    Vertex vertex = aVertex().withRev(FIRST_REVISION).build();
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, vertex);

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);

    // action
    instance.addVariant(DOMAIN_ENTITY_TYPE, domainEntity);

    // verify
    verify(converter).updateModifiedAndRev(vertex, domainEntity);
    verify(converter).addValuesToElement(vertex, domainEntity);

  }

  @Test(expected = NoSuchEntityException.class)
  public void addVariantThrowsANoSuchEntityExceptionWhenTheEntityDoesNotExist() throws Exception {
    // setup
    noLatestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID);

    // action
    instance.addVariant(DOMAIN_ENTITY_TYPE, aDomainEntity().build());

  }

  @Test(expected = UpdateException.class)
  public void addVariantThrowsAnUpdateExceptionWhenRevisionIsHigherMoreThanOneTheTheRevisionOfTheNode() throws Exception {
    addVariantThrowsUpdateExceptionForRevisionMismatch(FIRST_REVISION, THIRD_REVISION);
  }

  @Test(expected = UpdateException.class)
  public void addVariantThrowsAnUpdateExceptionWhenRevisionIsEqualToTheRevisionOfTheNode() throws Exception {
    addVariantThrowsUpdateExceptionForRevisionMismatch(THIRD_REVISION, THIRD_REVISION);
  }

  @Test(expected = UpdateException.class)
  public void addVariantThrowsAnUpdateExceptionWhenRevisionIsLowerThanTheRevisionOfTheNode() throws Exception {
    addVariantThrowsUpdateExceptionForRevisionMismatch(THIRD_REVISION, SECOND_REVISION);
  }

  private void addVariantThrowsUpdateExceptionForRevisionMismatch(int vertexRevision, int entityRevision) throws Exception {
    // setup
    Vertex vertex = aVertex().withRev(vertexRevision).build();
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, vertex);

    Change oldModified = CHANGE;
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(entityRevision)//
        .withAPid()//
        .withModified(oldModified)//
        .build();

    instance.addVariant(DOMAIN_ENTITY_TYPE, domainEntity);
  }

  @Test(expected = UpdateException.class)
  public void addVariantThrowsAnUpdateExceptionWhenTheEntityAlreadyContainsTheVariant() throws Exception {
    // setup
    latestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID, aVertex().build());

    // action
    instance.addVariant(DOMAIN_ENTITY_TYPE, aDomainEntity().withId(ID).build());
  }

  @Test(expected = ConversionException.class)
  public void addVariantThrowsAConversionExceptionWhenTheModifiedOrRevCannotBeUpdated() throws Exception {
    // setup
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(SECOND_REVISION)//
        .withAPid()//
        .build();

    Vertex vertex = aVertex().withRev(FIRST_REVISION).build();
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, vertex);

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    doThrow(new ConversionException()).when(converter).updateModifiedAndRev(vertex, domainEntity);

    // action
    instance.addVariant(DOMAIN_ENTITY_TYPE, domainEntity);
  }

  @Test(expected = ConversionException.class)
  public void addVariantThrowsAConversionExceptionWhenTheVertexCannotBeUpdated() throws Exception {
    // setup
    SubADomainEntity domainEntity = aDomainEntity() //
        .withId(ID) //
        .withRev(SECOND_REVISION)//
        .withAPid()//
        .build();

    Vertex vertex = aVertex().withRev(FIRST_REVISION).build();
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, ID, vertex);

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    doThrow(new ConversionException()).when(converter).addValuesToElement(vertex, domainEntity);

    // action
    instance.addVariant(DOMAIN_ENTITY_TYPE, domainEntity);
  }

  @Test
  public void setDomainEntityPIDAddsAPIDToTheNodeAndDuplicatesTheNode() throws Exception {
    // setup
    Vertex foundVertex = aVertex().build();
    latestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID, foundVertex);

    SubADomainEntity entityWithoutPID = aDomainEntity().build();

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(foundVertex)).thenReturn(entityWithoutPID);

    // action
    instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);

    // verify
    verify(converter).addValuesToElement( //
        argThat(is(foundVertex)), //
        argThat(likeDomainEntity(DOMAIN_ENTITY_TYPE).withPID(PID)));
    verify(lowLevelAPIMock).duplicate(foundVertex);
  }

  @Test(expected = IllegalStateException.class)
  public void setDomainEntityPIDThrowsAnIllegalStateExceptionWhenTheEntityAlreadyHasAPID() throws Exception {
    // setup
    Vertex foundVertex = aVertex().build();
    latestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID, foundVertex);

    SubADomainEntity entityWithPID = aDomainEntity().withAPid().build();

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(foundVertex)).thenReturn(entityWithPID);

    // action
    instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);

  }

  @Test(expected = ConversionException.class)
  public void setDomainEntityPIDThrowsAConversionExceptionWhenTheVertexCannotBeConverted() throws Exception {
    // setup
    Vertex foundVertex = aVertex().build();
    latestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID, foundVertex);

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(foundVertex)).thenThrow(new ConversionException());

    // action
    instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);

  }

  @Test(expected = ConversionException.class)
  public void setDomainEntityPIDThrowsAConversionsExceptionWhenTheUpdatedEntityCannotBeCovnverted() throws Exception {
    // setup
    Vertex foundVertex = aVertex().build();
    latestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID, foundVertex);

    SubADomainEntity entityWithoutPID = aDomainEntity().build();

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(foundVertex)).thenReturn(entityWithoutPID);
    doThrow(ConversionException.class).when(converter).addValuesToElement(foundVertex, entityWithoutPID);

    // action
    instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
  }

  @Test(expected = NoSuchEntityException.class)
  public void setDomainEntityPIDThrowsANoSuchEntityExceptionWhenTheEntityDoesNotExist() throws Exception {
    // setup
    noLatestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID);

    // action
    instance.setDomainEntityPID(DOMAIN_ENTITY_TYPE, ID, PID);
  }

  private void latestVertexFoundFor(Class<? extends Entity> type, String id, Vertex vertex) {
    when(lowLevelAPIMock.getLatestVertexById(type, id)).thenReturn(vertex);
  }

  private <T extends Entity> VertexConverter<T> vertexConverterCreatedFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    VertexConverter<T> vertexConverter = mock(VertexConverter.class);
    when(elementConverterFactoryMock.forType(type)).thenReturn(vertexConverter);
    return vertexConverter;
  }

  private <T extends DomainEntity> VertexConverter<T> compositeVertexConverterCreatedFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    VertexConverter<T> vertexConverter = mock(VertexConverter.class);
    when(elementConverterFactoryMock.compositeForType(type)).thenReturn(vertexConverter);
    return vertexConverter;
  }

  /* ********************************************************************
   * Relation
   * ********************************************************************/

  @Test
  public void addRelationAddsARelationshipToTheSource() throws Exception {
    // setup
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID)//
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME)//
        .withTargetId(RELATION_TARGET_ID)//
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME)//
        .withTypeId(RELATION_TYPE_ID)//
        .withTypeType(RELATION_TYPE_NAME)//
        .build();

    Vertex sourceVertex = aVertex().build();
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID, sourceVertex);
    Vertex targetVertex = aVertex().build();
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_TARGET_ID, targetVertex);

    relationTypeWithRegularNameExists(REGULAR_RELATION_NAME, RELATION_TYPE_ID);

    Edge edge = anEdge().build();
    when(sourceVertex.addEdge(REGULAR_RELATION_NAME, targetVertex)).thenReturn(edge);

    EdgeConverter<SubARelation> converter = createCompositeEdgeConverterFor(RELATION_TYPE);

    // action
    instance.addRelation(RELATION_TYPE, relation, new Change());

    // verify
    converter.addValuesToElement(edge, relation);
  }

  @Test(expected = ConversionException.class)
  public void addRelationThrowsAConversionExceptionWhenRelationCannotBeConverted() throws Exception {
    // setup
    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID)//
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME)//
        .withTargetId(RELATION_TARGET_ID)//
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME)//
        .withTypeId(RELATION_TYPE_ID)//
        .withTypeType(RELATION_TYPE_NAME)//
        .build();

    Vertex sourceVertex = aVertex().build();
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID, sourceVertex);
    Vertex targetVertex = aVertex().build();
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_TARGET_ID, targetVertex);

    relationTypeWithRegularNameExists(REGULAR_RELATION_NAME, RELATION_TYPE_ID);

    Edge edge = anEdge().build();
    when(sourceVertex.addEdge(REGULAR_RELATION_NAME, targetVertex)).thenReturn(edge);

    EdgeConverter<SubARelation> edgeConverter = createCompositeEdgeConverterFor(RELATION_TYPE);
    doThrow(ConversionException.class).when(edgeConverter).addValuesToElement(edge, relation);

    try {
      // action
      instance.addRelation(RELATION_TYPE, relation, new Change());
    } finally {
      dbMock.removeEdge(edge);
    }
  }

  private <T extends Relation> EdgeConverter<T> createCompositeEdgeConverterFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    EdgeConverter<T> edgeConverter = mock(EdgeConverter.class);

    when(elementConverterFactoryMock.compositeForRelation(type)).thenReturn(edgeConverter);

    return edgeConverter;
  }

  private VertexConverter<RelationType> relationTypeWithRegularNameExists(String name, String id) throws Exception {
    Vertex relationTypeNodeMock = aVertex().build();
    latestVertexFoundFor(RELATIONTYPE_TYPE, id, relationTypeNodeMock);

    VertexConverter<RelationType> relationTypeConverter = vertexConverterCreatedFor(RELATIONTYPE_TYPE);
    RelationType relationType = new RelationType();
    relationType.setRegularName(name);
    when(relationTypeConverter.convertToEntity(relationTypeNodeMock)).thenReturn(relationType);

    return relationTypeConverter;
  }

  @Test(expected = ConversionException.class)
  public void addRelationThrowsAConversionExceptionWhenTheRelationTypeCannotBeConverted() throws Exception {
    // setup
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID, aVertex().build());
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_TARGET_ID, aVertex().build());
    Vertex relationTypeVertex = aVertex().build();
    latestVertexFoundFor(RELATIONTYPE_TYPE, RELATION_TYPE_ID, relationTypeVertex);

    VertexConverter<RelationType> relationTypeConverter = vertexConverterCreatedFor(RELATIONTYPE_TYPE);
    when(relationTypeConverter.convertToEntity(relationTypeVertex)).thenThrow(new ConversionException());

    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID)//
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME)//
        .withTargetId(RELATION_TARGET_ID)//
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME)//
        .withTypeId(RELATION_TYPE_ID)//
        .withTypeType(RELATION_TYPE_NAME)//
        .build();

    // action
    instance.addRelation(RELATION_TYPE, relation, new Change());
  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAStorageExceptionWhenTheSourceCannotBeFound() throws Exception {
    // setup
    noLatestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID);

    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID)//
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME)//
        .build();

    // action
    instance.addRelation(RELATION_TYPE, relation, new Change());

  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAStorageExceptionWhenTheTargetCannotBeFound() throws Exception {
    // setup
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID, aVertex().build());
    noLatestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_TARGET_ID);

    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID)//
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME)//
        .withTargetId(RELATION_TARGET_ID)//
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME)//
        .build();

    // action
    instance.addRelation(RELATION_TYPE, relation, new Change());
  }

  @Test(expected = StorageException.class)
  public void addRelationThrowsAStorageExceptionWhenRelationTypeCannotBeFound() throws Exception {
    // setup
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_SOURCE_ID, aVertex().build());
    latestVertexFoundFor(PRIMITIVE_DOMAIN_ENTITY_TYPE, RELATION_TARGET_ID, aVertex().build());
    noLatestVertexFoundFor(RELATION_TYPE, ID);

    SubARelation relation = aRelation()//
        .withSourceId(RELATION_SOURCE_ID)//
        .withSourceType(PRIMITIVE_DOMAIN_ENTITY_NAME)//
        .withTargetId(RELATION_TARGET_ID)//
        .withTargetType(PRIMITIVE_DOMAIN_ENTITY_NAME)//
        .withTypeId(RELATION_TYPE_ID)//
        .withTypeType(RELATION_TYPE_NAME)//
        .build();

    // action
    instance.addRelation(RELATION_TYPE, relation, new Change());
  }

  @Test
  public void findRelationReturnsTheLatestRelation() throws Exception {
    // setup
    Vertex target = aVertex().build();
    Edge edge = anEdge().withLabel(REGULAR_RELATION_NAME).withTarget(target).build();
    Vertex source = aVertex().withOutgoingEdge(edge).build();

    relationTypeWithRegularNameExists(REGULAR_RELATION_NAME, RELATION_TYPE_ID);

    vertexFoundForId(SOURCE_ID, source);
    vertexFoundForId(TARGET_ID, target);

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    SubARelation relation = aRelation().build();
    when(converter.convertToEntity(edge)).thenReturn(relation);

    // action
    SubARelation actualRelation = instance.findRelation(RELATION_TYPE, SOURCE_ID, TARGET_ID, RELATION_TYPE_ID);

    // verify
    assertThat(actualRelation, is(sameInstance(relation)));
  }

  @Test
  public void findRelationReturnsNullIfTheSourceCannotBeFound() throws Exception {
    // setup
    when(lowLevelAPIMock.getLatestVertexById(ID)).thenReturn(null);
    relationTypeWithRegularNameExists(REGULAR_RELATION_NAME, RELATION_TYPE_ID);

    // action
    SubARelation relation = instance.findRelation(RELATION_TYPE, SOURCE_ID, TARGET_ID, RELATION_TYPE_ID);

    // verify
    assertThat(relation, is(nullValue()));
  }

  @Test
  public void findRelationReturnsNullIfTheVertexCannotBeFound() throws Exception {
    // setup
    Vertex notRequestTarget = aVertex().build();
    Edge edge = anEdge().withLabel(REGULAR_RELATION_NAME).withTarget(notRequestTarget).build();
    Vertex source = aVertex().withOutgoingEdge(edge).build();
    Vertex target = aVertex().build();

    relationTypeWithRegularNameExists(REGULAR_RELATION_NAME, RELATION_TYPE_ID);

    vertexFoundForId(SOURCE_ID, source);
    when(lowLevelAPIMock.getLatestVertexById(TARGET_ID)).thenReturn(target);

    // action
    SubARelation relation = instance.findRelation(RELATION_TYPE, SOURCE_ID, TARGET_ID, RELATION_TYPE_ID);

    // verify
    assertThat(relation, is(nullValue()));
  }

  @Test(expected = ConversionException.class)
  public void findRelationThrowsAConversionExceptionWhenTheRelationsCannotBeConverted() throws Exception {
    // setup
    Vertex target = aVertex().build();
    Edge edge = anEdge().withLabel(REGULAR_RELATION_NAME).withTarget(target).build();
    Vertex source = aVertex().withOutgoingEdge(edge).build();

    relationTypeWithRegularNameExists(REGULAR_RELATION_NAME, RELATION_TYPE_ID);

    vertexFoundForId(SOURCE_ID, source);
    vertexFoundForId(TARGET_ID, target);

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(edge)).thenThrow(new ConversionException());

    // action
    instance.findRelation(RELATION_TYPE, SOURCE_ID, TARGET_ID, RELATION_TYPE_ID);

  }

  private void vertexFoundForId(String id, Vertex vertex) {
    when(lowLevelAPIMock.getLatestVertexById(id)).thenReturn(vertex);
  }

  @Test
  public void findRelationByPropertyReturnsTheConvertedFirstFoundEdge() throws Exception {
    // setup
    Edge firstEdge = anEdge().build();
    Edge secondEdge = anEdge().build();
    Iterator<Edge> iterator = Lists.<Edge> newArrayList(firstEdge, secondEdge).iterator();
    when(lowLevelAPIMock.findEdgesByProperty(RELATION_TYPE, PROPERTY_NAME, PROPERTY_VALUE)).thenReturn(iterator);

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.getPropertyName(FIELD_NAME)).thenReturn(PROPERTY_NAME);
    SubARelation foundRelation = aRelation().build();
    when(converter.convertToEntity(firstEdge)).thenReturn(foundRelation);

    // action
    SubARelation actualRelation = instance.findRelationByProperty(RELATION_TYPE, FIELD_NAME, PROPERTY_VALUE);

    // verify
    assertThat(actualRelation, is(sameInstance(foundRelation)));
  }

  @Test
  public void findRelationByPropertyReturnsNullIfNoEdgesCanBeFound() throws Exception {
    // setup
    Iterator<Edge> iterator = Lists.<Edge> newArrayList().iterator();
    when(lowLevelAPIMock.findEdgesByProperty(RELATION_TYPE, PROPERTY_NAME, PROPERTY_VALUE)).thenReturn(iterator);

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.getPropertyName(FIELD_NAME)).thenReturn(PROPERTY_NAME);

    // action
    SubARelation foundRelation = instance.findRelationByProperty(RELATION_TYPE, FIELD_NAME, PROPERTY_VALUE);

    // verify
    assertThat(foundRelation, is(nullValue()));
  }

  @Test(expected = ConversionException.class)
  public void findRelationByPropertyThrowsAConversionExceptionIfTheRelationshipCannotBeConverted() throws Exception {
    // setup
    Edge foundEdge = anEdge().build();
    Iterator<Edge> iterator = Lists.<Edge> newArrayList(foundEdge).iterator();
    when(lowLevelAPIMock.findEdgesByProperty(RELATION_TYPE, PROPERTY_NAME, PROPERTY_VALUE)).thenReturn(iterator);

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.getPropertyName(FIELD_NAME)).thenReturn(PROPERTY_NAME);
    when(converter.convertToEntity(foundEdge)).thenThrow(new ConversionException());

    // action
    instance.findRelationByProperty(RELATION_TYPE, FIELD_NAME, PROPERTY_VALUE);
  }

  @Test
  public void findRelationByCallsGetRelationsByEntityIfThePropertyIsSourceId() throws Exception {
    // setup
    Edge foundEdge = anEdge().build();
    edgesFoundBySource(foundEdge);
    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.getPropertyName(SOURCE_ID)).thenReturn(SOURCE_ID);
    SubARelation foundRelation = aRelation().build();
    when(converter.convertToEntity(foundEdge)).thenReturn(foundRelation);

    // action
    SubARelation actualRelation = instance.findRelationByProperty(RELATION_TYPE, SOURCE_ID, PROPERTY_VALUE);

    // verify
    assertThat(actualRelation, is(sameInstance(actualRelation)));
  }

  private void edgesFoundBySource(Edge... foundEdges) {
    Iterator<Edge> iterator = Lists.<Edge> newArrayList(foundEdges).iterator();
    when(lowLevelAPIMock.findEdgesBySource(RELATION_TYPE, PROPERTY_VALUE)).thenReturn(iterator);
  }

  @Test
  public void findRelationByCallsGetRelationsByEntityIfThePropertyIsTargetId() throws Exception {
    // setup
    Edge foundEdge = anEdge().build();
    edgesFoundByTarget(foundEdge);
    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.getPropertyName(TARGET_ID)).thenReturn(TARGET_ID);
    SubARelation foundRelation = aRelation().build();
    when(converter.convertToEntity(foundEdge)).thenReturn(foundRelation);

    // action
    SubARelation actualRelation = instance.findRelationByProperty(RELATION_TYPE, TARGET_ID, PROPERTY_VALUE);

    // verify
    assertThat(actualRelation, is(sameInstance(actualRelation)));
  }

  private void edgesFoundByTarget(Edge... foundEdges) {
    Iterator<Edge> iterator = Lists.<Edge> newArrayList(foundEdges).iterator();
    when(lowLevelAPIMock.findEdgesByTarget(RELATION_TYPE, PROPERTY_VALUE)).thenReturn(iterator);
  }

  @Test
  public void getRelationReturnsTheRelationThatBelongsToTheId() throws Exception {
    // setup
    Edge edge = latestEdgeFoundWithId(ID, anEdge().build());

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    SubARelation relation = aRelation().build();
    when(converter.convertToEntity(edge)).thenReturn(relation);

    // action
    SubARelation foundRelation = instance.getRelation(RELATION_TYPE, ID);

    // verify
    assertThat(foundRelation, is(sameInstance(relation)));
  }

  @Test
  public void getRelationReturnsNullIfTheRelationIsNotFound() throws Exception {
    // setup
    noLatestEdgeFoundWithId(ID);

    // action
    SubARelation foundRelation = instance.getRelation(RELATION_TYPE, ID);

    // verify
    assertThat(foundRelation, is(nullValue()));
  }

  private void noLatestEdgeFoundWithId(String id) {
    when(lowLevelAPIMock.getLatestEdgeById(RELATION_TYPE, id)).thenReturn(null);
  }

  @Test(expected = ConversionException.class)
  public void getRelationThrowsAConversionExceptionWhenTheEdgeCannotBeConverted() throws Exception {
    // setup
    Edge edge = latestEdgeFoundWithId(ID, anEdge().build());

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(edge)).thenThrow(new ConversionException());

    // action
    instance.getRelation(RELATION_TYPE, ID);
  }

  @Test
  public void getRelationRevisionReturnsTheRelationForTheRequestedRevision() throws Exception {
    // setup
    Edge edge = edgeFoundForIdAndRevision(ID, FIRST_REVISION);

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    SubARelation relationWithAPID = aRelation().withAPID().build();
    when(converter.convertToEntity(edge)).thenReturn(relationWithAPID);

    // action
    SubARelation foundRelation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(foundRelation, is(sameInstance(relationWithAPID)));
  }

  @Test
  public void getRelationRevisionReturnsNullIfTheFoundEdgeHasNoPID() throws Exception {
    // setup
    Edge edge = edgeFoundForIdAndRevision(ID, FIRST_REVISION);

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    SubARelation relationWithoutAPID = aRelation().build();
    when(converter.convertToEntity(edge)).thenReturn(relationWithoutAPID);

    // action
    SubARelation foundRelation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(foundRelation, is(nullValue()));
  }

  @Test
  public void getRelationRevisionReturnsNullIfTheEdgeDoesNotExist() throws Exception {
    // setup
    noEdgeFoundWithIdAndRevision(ID, FIRST_REVISION);

    // action
    SubARelation foundRelation = instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(foundRelation, is(nullValue()));
  }

  @Test(expected = ConversionException.class)
  public void getRelationRevisionThrowsAStorageExceptionIfTheRelationCannotBeConverted() throws Exception {
    // setup
    Edge edge = edgeFoundForIdAndRevision(ID, FIRST_REVISION);

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(edge)).thenThrow(new ConversionException());

    // action
    instance.getRelationRevision(RELATION_TYPE, ID, FIRST_REVISION);
  }

  private void noEdgeFoundWithIdAndRevision(String id, int revision) {
    when(lowLevelAPIMock.getEdgeWithRevision(RELATION_TYPE, id, revision)).thenReturn(null);
  }

  private Edge edgeFoundForIdAndRevision(String id, int revision) {
    Edge edge = anEdge().build();

    when(lowLevelAPIMock.getEdgeWithRevision(RELATION_TYPE, id, revision)).thenReturn(edge);

    return edge;
  }

  private <T extends Relation> EdgeConverter<T> createEdgeConverterFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    EdgeConverter<T> edgeConverter = mock(EdgeConverter.class);

    when(elementConverterFactoryMock.forRelation(type)).thenReturn(edgeConverter);

    return edgeConverter;
  }

  private Edge latestEdgeFoundWithId(String id, Edge edge) {
    when(lowLevelAPIMock.getLatestEdgeById(RELATION_TYPE, id)).thenReturn(edge);

    return edge;
  }

  @Test
  public void getRelationsByEntityIdReturnsAStorageIteratorOfRelationForTheFoundEdges() throws Exception {
    // setup
    Vertex vertex = aVertex().build();
    List<Edge> edges = Lists.newArrayList(anEdge().build(), anEdge().build());
    when(vertex.getEdges(Direction.BOTH)).thenReturn(edges);
    when(lowLevelAPIMock.getLatestVertexById(ID)).thenReturn(vertex);

    @SuppressWarnings("unchecked")
    StorageIterator<SubARelation> storageIterator = mock(StorageIterator.class);

    when(storageIteratorFactoryMock.createForRelation(RELATION_TYPE, edges)).thenReturn(storageIterator);

    // action 
    StorageIterator<SubARelation> actualStorageIterator = instance.getRelationsByEntityId(RELATION_TYPE, ID);

    // verify
    assertThat(actualStorageIterator, is(sameInstance(storageIterator)));
  }

  @Test
  public void getRelationsByEntityIdReturnsAStorageIteratorOfAnEmptyListIfTheEntityIfNotFound() throws Exception {
    // setup
    when(lowLevelAPIMock.getLatestVertexById(ID)).thenReturn(null);

    @SuppressWarnings("unchecked")
    StorageIterator<SubARelation> storageIterator = mock(StorageIterator.class);

    when(storageIteratorFactoryMock.createForRelation(argThat(equalTo(RELATION_TYPE)), argThat(is(emptyCollectionOf(Edge.class))))).thenReturn(storageIterator);

    // action 
    StorageIterator<SubARelation> actualStorageIterator = instance.getRelationsByEntityId(RELATION_TYPE, ID);

    // verify
    assertThat(actualStorageIterator, is(sameInstance(storageIterator)));
    verify(storageIteratorFactoryMock).createForRelation(argThat(equalTo(RELATION_TYPE)), argThat(is(emptyCollectionOf(Edge.class))));
  }

  @Test
  public void relationExistsReturnsTrueIfTheEdgeCanBeFound() {
    latestEdgeFoundWithId(ID, anEdge().build());

    boolean relationExists = instance.relationExists(RELATION_TYPE, ID);

    assertThat(relationExists, is(true));
  }

  @Test
  public void relationExistsReturnsFalseIfTheEdgeCannotBeFound() {
    noLatestEdgeFoundWithId(ID);

    boolean relationExists = instance.relationExists(RELATION_TYPE, ID);

    assertThat(relationExists, is(false));
  }

  @Test
  public void setRelationPIDSetsThePIDOfTheRelationAndDuplicatesIt() throws Exception {
    // setup
    Edge edge = latestEdgeFoundWithId(ID, anEdge().build());
    SubARelation relationWithoutAPID = aRelation().build();

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(edge)).thenReturn(relationWithoutAPID);

    // action
    instance.setRelationPID(RELATION_TYPE, ID, PID);

    // verify
    verify(converter).addValuesToElement( //
        argThat(is(edge)), //
        argThat(likeDomainEntity(RELATION_TYPE).withPID(PID)));
    verify(lowLevelAPIMock).duplicate(edge);
  }

  @Test(expected = IllegalStateException.class)
  public void setRelationPIDThrowsAnIllegalStateExceptionIfTheRelationAlreadyHasAPID() throws Exception {
    // setup
    Edge edge = latestEdgeFoundWithId(ID, anEdge().build());
    SubARelation relationWithAPID = aRelation().withAPID().build();

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(edge)).thenReturn(relationWithAPID);

    // action
    instance.setRelationPID(RELATION_TYPE, ID, PID);
  }

  @Test(expected = ConversionException.class)
  public void setRelationPIDThrowsAConversionExceptionIfTheEdgeCannotBeConverted() throws Exception {
    // setup
    Edge edge = latestEdgeFoundWithId(ID, anEdge().build());

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(edge)).thenThrow(new ConversionException());

    // action
    instance.setRelationPID(RELATION_TYPE, ID, PID);

  }

  @Test(expected = ConversionException.class)
  public void setRelationPIDThrowsAConversionsExceptionWhenTheUpdatedEntityCannotBeConvertedToAnEdge() throws Exception {
    // setup
    Edge edge = latestEdgeFoundWithId(ID, anEdge().build());
    SubARelation relationWithoutAPID = aRelation().build();

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    when(converter.convertToEntity(edge)).thenReturn(relationWithoutAPID);
    doThrow(ConversionException.class).when(converter).addValuesToElement(edge, relationWithoutAPID);

    // action
    instance.setRelationPID(RELATION_TYPE, ID, PID);
  }

  @Test(expected = NoSuchEntityException.class)
  public void setRelationPIDThrowsANoSuchEntityExceptionIfTheRelationshipCannotBeFound() throws Exception {
    noLatestEdgeFoundWithId(ID);

    instance.setRelationPID(RELATION_TYPE, ID, PID);
  }

  @Test
  public void updateRelationRetrievesTheEdgeAndUpdateItsValuesAndAdministrativeValues() throws Exception {
    // setup
    Edge edge = anEdge().withRev(FIRST_REVISION).build();
    latestEdgeFoundWithId(ID, edge);

    SubARelation entity = aRelation().withId(ID).withRevision(SECOND_REVISION).build();

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);

    // action
    instance.updateRelation(RELATION_TYPE, entity, CHANGE);

    // verify
    verify(converter).updateModifiedAndRev(edge, entity);
    verify(converter).updateElement(edge, entity);
  }

  @Test(expected = UpdateException.class)
  public void updateRelationThrowsAnUpdateExceptionWhenTheEdgeToUpdateCannotBeFound() throws Exception {
    // setup
    noLatestEdgeFoundWithId(ID);

    // action
    instance.updateRelation(RELATION_TYPE, aRelation().build(), CHANGE);

  }

  @Test(expected = UpdateException.class)
  public void updateRelationThrowsAnUpdateExceptionWhenRevOfTheEdgeIsHigherThanThatOfTheEntity() throws Exception {
    testUpdateRelationRevisionUpdateException(SECOND_REVISION, FIRST_REVISION);
  }

  @Test(expected = UpdateException.class)
  public void updateRelationThrowsAnUpdateExceptionWhenRevOfTheEdgeMoreThanOneIsLowerThanThatOfTheEntity() throws Exception {
    testUpdateRelationRevisionUpdateException(FIRST_REVISION, THIRD_REVISION);
  }

  @Test(expected = UpdateException.class)
  public void updateRelationThrowsAnUpdateExceptionWhenRevOfTheEdgeIsEqualToThatOfTheEntity() throws Exception {
    testUpdateRelationRevisionUpdateException(FIRST_REVISION, FIRST_REVISION);
  }

  private void testUpdateRelationRevisionUpdateException(int edgeRevision, int entityRevision) throws Exception {
    // setup
    Edge edge = anEdge().withRev(edgeRevision).build();
    latestEdgeFoundWithId(ID, edge);

    SubARelation entity = aRelation().withId(ID).withRevision(entityRevision).build();

    // action
    instance.updateRelation(RELATION_TYPE, entity, CHANGE);
  }

  @Test(expected = ConversionException.class)
  public void updateRelationThrowsAConversionExceptionWhenTheEdgeCannotBeConverted() throws Exception {
    // setup
    Edge edge = anEdge().withRev(FIRST_REVISION).build();
    latestEdgeFoundWithId(ID, edge);

    SubARelation entity = aRelation().withId(ID).withRevision(SECOND_REVISION).build();

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    doThrow(ConversionException.class).when(converter).updateElement(edge, entity);

    // action
    instance.updateRelation(RELATION_TYPE, entity, CHANGE);
  }

  @Test(expected = ConversionException.class)
  public void updateRelationThrowsAConversionExceptionWhenModifiedAndRevCannotBeUpdated() throws Exception {
    // setup
    Edge edge = anEdge().withRev(FIRST_REVISION).build();
    latestEdgeFoundWithId(ID, edge);

    SubARelation entity = aRelation().withId(ID).withRevision(SECOND_REVISION).build();

    EdgeConverter<SubARelation> converter = createEdgeConverterFor(RELATION_TYPE);
    doThrow(ConversionException.class).when(converter).updateModifiedAndRev(edge, entity);

    // action
    instance.updateRelation(RELATION_TYPE, entity, CHANGE);
  }

  @Test
  public void countRelationsCountsTheItemsOfTheIteratorOfTheLowLevelAPI() {
    // setup
    List<Edge> twoEdges = Lists.newArrayList(anEdge().build(), anEdge().build());

    when(lowLevelAPIMock.getLatestEdgesOf(PRIMITIVE_RELATION_TYPE)).thenReturn(twoEdges.iterator());

    // action
    long numberOfRelations = instance.countRelations(RELATION_TYPE);

    // verify
    assertThat(numberOfRelations, is(2l));
  }

  @Test
  public void countRelationsCountsThePrimitiveRelations() {
    // setup
    List<Edge> twoEdges = Lists.newArrayList(anEdge().build(), anEdge().build());

    when(lowLevelAPIMock.getLatestEdgesOf(PRIMITIVE_RELATION_TYPE)).thenReturn(twoEdges.iterator());

    // action
    instance.countRelations(RELATION_TYPE);

    // verify
    verify(lowLevelAPIMock).getLatestEdgesOf(PRIMITIVE_RELATION_TYPE);
  }

  /* ********************************************************************
   * Other methods
   * ********************************************************************/

  @Test
  public void closeDelegatesShutdownToTheGraphAndSetsTheAvailableBooleanToFalse() {
    // action
    instance.close();

    // verify
    verify(dbMock).shutdown();

  }

  @Test
  public void isAvailableReturnsTrueByDefault() {
    // action
    boolean available = instance.isAvailable();

    // verify
    assertThat(available, is(true));
  }

  @Test
  public void isAvailableReturnsFalseWhenCloseIsCalled() {
    // setup
    instance.close();

    // action
    boolean available = instance.isAvailable();

    // verify
    assertThat(available, is(false));
  }
}
