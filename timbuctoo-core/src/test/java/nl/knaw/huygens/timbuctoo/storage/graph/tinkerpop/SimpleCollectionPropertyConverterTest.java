package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.property.SimpleArrayMatcher.isSimpleArrayOfType;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;

public class SimpleCollectionPropertyConverterTest implements PropertyConverterTest {

  private static final Class<Integer> COMPONENT_TYPE = Integer.class;
  private static final int VALUE_4 = 4;
  private static final int VALUE_3 = 3;
  private static final int VALUE_2 = 2;
  private static final int VALUE_1 = 1;
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final Class<TestSystemEntityWrapper> CONTAINING_TYPE = TestSystemEntityWrapper.class;
  private static final String FIELD_NAME = "primitiveCollection";
  private String propertyName;
  private SimpleCollectionPropertyConverter<Integer> instance;
  private Vertex vertexMock;
  private TestSystemEntityWrapper entity;

  @Before
  public void setUp() throws NoSuchFieldException {
    instance = new SimpleCollectionPropertyConverter<>(COMPONENT_TYPE);
    setupInstance(instance);

    propertyName = FIELD_TYPE.propertyName(CONTAINING_TYPE, FIELD_NAME);

    vertexMock = mock(Vertex.class);
    entity = new TestSystemEntityWrapper();
  }

  private void setupInstance(SimpleCollectionPropertyConverter<Integer> simpleCollectionFieldWrapper) throws NoSuchFieldException {
    simpleCollectionFieldWrapper.setField(CONTAINING_TYPE.getDeclaredField(FIELD_NAME));
    simpleCollectionFieldWrapper.setFieldType(FIELD_TYPE);
    simpleCollectionFieldWrapper.setContainingType(CONTAINING_TYPE);
    simpleCollectionFieldWrapper.setName(FIELD_NAME);
  }

  @Test
  @Override
  public void setValueOfVertexSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    // setup
    List<Integer> value = Lists.newArrayList(VALUE_1, VALUE_2, VALUE_3, VALUE_4);
    entity.setPrimitiveCollection(value);

    // action
    instance.setValueOfVertex(vertexMock, entity);

    // verify
    verify(vertexMock).setProperty(argThat(equalTo(propertyName)), //
        argThat(isSimpleArrayOfType(COMPONENT_TYPE) //
            .withValues(VALUE_1, VALUE_2, VALUE_3, VALUE_4)));
  }

  @Test
  @Override
  public void setValueOfVertexDoesNotSetIfTheValueIsNull() throws Exception {
    // setup
    entity.setPrimitiveCollection(null);

    // action
    instance.setValueOfVertex(vertexMock, entity);

    // verify
    verifyZeroInteractions(vertexMock);
  }

  @Test
  public void setValueOfVertexDoesNotSetIfTheValueIsEmpty() throws Exception {
    // setup
    entity.setPrimitiveCollection(Lists.<Integer> newArrayList());

    // action
    instance.setValueOfVertex(vertexMock, entity);

    // verify
    verifyZeroInteractions(vertexMock);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void setValueOfVertexThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception {
    // setup
    SimpleCollectionPropertyConverter<Integer> instance = new SimpleCollectionPropertyConverter<Integer>(COMPONENT_TYPE) {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalAccessException();
      }
    };

    setupInstance(instance);

    // action
    instance.setValueOfVertex(vertexMock, entity);

  }

  @Test(expected = ConversionException.class)
  @Override
  public void setValueOfVertexThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception {
    // setup
    SimpleCollectionPropertyConverter<Integer> instance = new SimpleCollectionPropertyConverter<Integer>(COMPONENT_TYPE) {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalArgumentException();
      }
    };

    setupInstance(instance);

    // action
    instance.setValueOfVertex(vertexMock, entity);
  }

}
