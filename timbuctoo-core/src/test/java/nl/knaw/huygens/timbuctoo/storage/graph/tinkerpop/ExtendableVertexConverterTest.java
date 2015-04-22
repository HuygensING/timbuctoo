package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexFields.VERTEX_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;

public class ExtendableVertexConverterTest {
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private PropertyConverter propertyConverter1;
  private PropertyConverter propertyConverter2;
  private List<PropertyConverter> propertyConverters;
  private ExtendableVertexConverter<TestSystemEntityWrapper> instance;
  private Vertex vertexMock;
  private TestSystemEntityWrapper entity;
  private EntityInstantiator entityInstantiatorMock;

  @Before
  public void setup() {
    propertyConverter1 = mock(PropertyConverter.class);
    propertyConverter2 = mock(PropertyConverter.class);
    propertyConverters = Lists.newArrayList(propertyConverter1, propertyConverter2);
    entityInstantiatorMock = mock(EntityInstantiator.class);
    instance = new ExtendableVertexConverter<>(TYPE, propertyConverters, entityInstantiatorMock);

    vertexMock = mock(Vertex.class);
    entity = new TestSystemEntityWrapper();
  }

  @Test
  public void addValuesToNodeLetsThePropertyConvertersAddTheirValuesToTheNode() throws Exception {
    // action
    instance.addValuesToVertex(vertexMock, entity);

    // verify
    verifyTypeIsSet(vertexMock);
    verify(propertyConverter1).setValueOfVertex(vertexMock, entity);
    verify(propertyConverter2).setValueOfVertex(vertexMock, entity);

  }

  private void verifyTypeIsSet(Vertex vertexMock) {
    verify(vertexMock).setProperty(VERTEX_TYPE, new String[] { TypeNames.getInternalName(TYPE) });
  }

  @Test(expected = ConversionException.class)
  public void addValuesToNodeFieldMapperThrowsAConversionException() throws Exception {
    // setup
    doThrow(ConversionException.class).when(propertyConverter1).setValueOfVertex(vertexMock, entity);

    try {
      // action
      instance.addValuesToVertex(vertexMock, entity);
    } finally {
      // verify
      verify(propertyConverter1).setValueOfVertex(vertexMock, entity);
      verifyZeroInteractions(propertyConverter2);
    }
  }

  @Test
  public void convertToEntityCreatesAnInstanceOfTheEntityThenLetThePropertyConvertersAddTheValues() throws Exception {
    // setup
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(entity);

    // action
    TestSystemEntityWrapper createdEntity = instance.convertToEntity(vertexMock);

    // verify
    assertThat(createdEntity, is(sameInstance(entity)));

    verify(propertyConverter1).addValueToEntity(entity, vertexMock);
    verify(propertyConverter2).addValueToEntity(entity, vertexMock);

  }

  @Test(expected = ConversionException.class)
  public void convertToEntityThrowsAConversionExceptionWhenTheEntityCannotBeInstatiated() throws Exception {
    // setup
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenThrow(new InstantiationException());

    // action
    instance.convertToEntity(vertexMock);

  }

  @Test(expected = ConversionException.class)
  public void convertToEntityThrowsAConversionExceptionWhenOneOfTheValuesCannotBeConverted() throws Exception {
    // setup
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(entity);
    doThrow(ConversionException.class).when(propertyConverter1).addValueToEntity(entity, vertexMock);

    // action
    instance.convertToEntity(vertexMock);

  }
}
