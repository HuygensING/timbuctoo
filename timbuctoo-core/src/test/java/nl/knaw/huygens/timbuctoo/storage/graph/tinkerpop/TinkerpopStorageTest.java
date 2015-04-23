package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.SubADomainEntityBuilder.aDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.graph.TestSystemEntityWrapperBuilder.aSystemEntity;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TinkerpopStorageTest {

  private static final int FIRST_REVISION = 1;
  private static final int SECOND_REVISION = 2;
  private static final int THIRD_REVISION = 3;

  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Change CHANGE = new Change();
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final String ID = "id";
  private Graph dbMock;
  private TinkerpopStorage instance;
  private ElementConverterFactory elementConverterFactoryMock;
  private Vertex createdVertex;
  private TinkerpopLowLevelAPI lowLevelAPIMock;

  @Before
  public void setup() {
    dbMock = mock(Graph.class);
    lowLevelAPIMock = mock(TinkerpopLowLevelAPI.class);
    elementConverterFactoryMock = mock(ElementConverterFactory.class);
    instance = new TinkerpopStorage(dbMock, elementConverterFactoryMock, lowLevelAPIMock);

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
    verify(converter).addValuesToVertex(createdVertex, entity);
  }

  @Test(expected = ConversionException.class)
  public void addDomainEntityRollsBackTheTransactionAndThrowsAConversionExceptionWhenTheDomainEntityConverterThrowsAConversionException() throws Exception {
    // setup
    SubADomainEntity entity = aDomainEntity().build();

    VertexConverter<SubADomainEntity> converter = compositeVertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(converter).addValuesToVertex(createdVertex, entity);

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
    verify(vertexConverter).addValuesToVertex(createdVertex, entity);
  }

  @Test(expected = StorageException.class)
  public void addSystemEntityRollsBackTheTransactionAndThrowsStorageExceptionVertexConverterThrowsAConversionException() throws Exception {
    // setup
    TestSystemEntityWrapper entity = aSystemEntity().build();
    VertexConverter<TestSystemEntityWrapper> vertexConverter = vertexConverterCreatedFor(SYSTEM_ENTITY_TYPE);
    doThrow(ConversionException.class).when(vertexConverter).addValuesToVertex(createdVertex, entity);

    try {
      // action
      instance.addSystemEntity(SYSTEM_ENTITY_TYPE, entity);
    } finally {

      // verify
      verify(dbMock).removeVertex(createdVertex);
    }
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
    verify(converter).updateVertex(vertex, entity);
  }

  @Test(expected = ConversionException.class)
  public void updateEntityThrowsAConversionExceptionWhenTheVertexConverterThrowsOne() throws Exception {
    // setup
    Vertex vertex = aVertex().withRev(FIRST_REVISION).build();
    latestVertexFoundFor(DOMAIN_ENTITY_TYPE, ID, vertex);
    SubADomainEntity entity = aDomainEntity().withId(ID).withRev(SECOND_REVISION).build();

    VertexConverter<SubADomainEntity> converter = vertexConverterCreatedFor(DOMAIN_ENTITY_TYPE);
    doThrow(ConversionException.class).when(converter).updateVertex(vertex, entity);

    // action
    instance.updateEntity(DOMAIN_ENTITY_TYPE, entity);

  }

  @Test(expected = UpdateException.class)
  public void updateEntityThrowsAnUpdateExceptionIfTheVertexCannotBeFound() throws Exception {
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

  public void testUpdateEntityRevisionExceptions(int nodeRev, int entityRev) throws Exception {
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
}
