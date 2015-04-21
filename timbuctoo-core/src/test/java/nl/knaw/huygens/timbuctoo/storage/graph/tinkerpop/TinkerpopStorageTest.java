package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.TestSystemEntityWrapperBuilder.aSystemEntity;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TinkerpopStorageTest {

  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private Graph dbMock;
  private TinkerpopStorage instance;
  private ElementConverterFactory elementConverterFactoryMock;
  private Vertex createdVertex;

  @Before
  public void setup() {
    dbMock = mock(Graph.class);
    elementConverterFactoryMock = mock(ElementConverterFactory.class);
    instance = new TinkerpopStorage(dbMock, elementConverterFactoryMock);

    createdVertex = mock(Vertex.class);
    when(dbMock.addVertex(null)).thenReturn(createdVertex);
  }

  /* ************************************************************
   * DomainEntity
   * ************************************************************/

  @Ignore
  @Test
  public void addDomainEntitySavesTheProjectVersionAndThePrimitive() {
    fail("Yet to be implemented");
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

  private <T extends Entity> VertexConverter<T> vertexConverterCreatedFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    VertexConverter<T> vertexConverter = mock(VertexConverter.class);
    when(elementConverterFactoryMock.forType(type)).thenReturn(vertexConverter);
    return vertexConverter;
  }
}
