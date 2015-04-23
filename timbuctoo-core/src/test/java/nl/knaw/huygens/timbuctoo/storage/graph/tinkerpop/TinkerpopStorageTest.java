package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.SubADomainEntityBuilder.aDomainEntity;
import static nl.knaw.huygens.timbuctoo.storage.graph.TestSystemEntityWrapperBuilder.aSystemEntity;
import static org.hamcrest.MatcherAssert.assertThat;
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
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TinkerpopStorageTest {

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

  /* ************************************************************
   * DomainEntity
   * ************************************************************/

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

  /* ***********************************************************
   * SystemEntity
   * ***********************************************************/

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
  public void getEntityReturnsTheItemWhenFound() throws Exception {
    // setup
    Vertex foundVertex = latestVertexFoundFor();

    VertexConverter<TestSystemEntityWrapper> vertexConverter = vertexConverterCreatedFor(SYSTEM_ENTITY_TYPE);
    TestSystemEntityWrapper entity = new TestSystemEntityWrapper();
    when(vertexConverter.convertToEntity(foundVertex)).thenReturn(entity);

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
    Vertex foundVertex = latestVertexFoundFor();

    VertexConverter<TestSystemEntityWrapper> vertexConverter = vertexConverterCreatedFor(SYSTEM_ENTITY_TYPE);
    when(vertexConverter.convertToEntity(foundVertex)).thenThrow(new ConversionException());

    // action
    instance.getEntity(SYSTEM_ENTITY_TYPE, ID);

  }

  private Vertex latestVertexFoundFor() {
    Vertex foundVertex = mock(Vertex.class);
    when(lowLevelAPIMock.getLatestVertexById(SYSTEM_ENTITY_TYPE, ID)).thenReturn(foundVertex);
    return foundVertex;
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
