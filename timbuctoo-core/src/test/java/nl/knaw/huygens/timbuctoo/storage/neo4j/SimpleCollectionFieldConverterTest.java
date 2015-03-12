package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.SimpleArrayMatcher.isSimpleArrayOfType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;

import test.model.TestSystemEntityWrapper;

import com.google.common.collect.Lists;

public class SimpleCollectionFieldConverterTest implements FieldConverterTest {

  private static final Class<Integer> COMPONENT_TYPE = Integer.class;
  private static final int VALUE_4 = 4;
  private static final int VALUE_3 = 3;
  private static final int VALUE_2 = 2;
  private static final int VALUE_1 = 1;
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final Class<TestSystemEntityWrapper> CONTAINING_TYPE = TestSystemEntityWrapper.class;
  private static final String FIELD_NAME = "primitiveCollection";
  private String propertyName;
  private SimpleCollectionFieldConverter<Integer> instance;
  private Node nodeMock;
  private TestSystemEntityWrapper entity;

  @Before
  public void setUp() throws NoSuchFieldException {
    instance = new SimpleCollectionFieldConverter<Integer>(COMPONENT_TYPE);
    setupInstance(instance);

    propertyName = FIELD_TYPE.propertyName(CONTAINING_TYPE, FIELD_NAME);

    nodeMock = mock(Node.class);
    entity = new TestSystemEntityWrapper();
  }

  private void setupInstance(SimpleCollectionFieldConverter<Integer> simpleCollectionFieldWrapper) throws NoSuchFieldException {
    simpleCollectionFieldWrapper.setField(CONTAINING_TYPE.getDeclaredField(FIELD_NAME));
    simpleCollectionFieldWrapper.setFieldType(FIELD_TYPE);
    simpleCollectionFieldWrapper.setContainingType(CONTAINING_TYPE);
    simpleCollectionFieldWrapper.setName(FIELD_NAME);
  }

  @Test
  @Override
  public void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    List<Integer> value = Lists.newArrayList(VALUE_1, VALUE_2, VALUE_3, VALUE_4);
    entity.setPrimitiveCollection(value);

    // action
    instance.setPropertyContainerProperty(nodeMock, entity);

    // verify
    verify(nodeMock).setProperty(argThat(equalTo(propertyName)), //
        argThat(isSimpleArrayOfType(COMPONENT_TYPE) //
            .withValues(VALUE_1, VALUE_2, VALUE_3, VALUE_4)));
  }

  @Test
  @Override
  public void addValueToNodeDoesNotSetIfTheValueIsNull() throws Exception {
    List<Integer> nullValue = null;
    entity.setPrimitiveCollection(nullValue);

    // action
    instance.setPropertyContainerProperty(nodeMock, entity);

    // verify
    verifyZeroInteractions(nodeMock);
  }

  @Test
  public void addValueToNodeDoesNotSetIfTheCollectionIsEmpty() throws Exception {
    List<Integer> emptyCollection = Lists.newArrayList();
    entity.setPrimitiveCollection(emptyCollection);

    // action
    instance.setPropertyContainerProperty(nodeMock, entity);

    // verify
    verifyZeroInteractions(nodeMock);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToNodeThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception {
    // setup
    SimpleCollectionFieldConverter<Integer> instance = new SimpleCollectionFieldConverter<Integer>(COMPONENT_TYPE) {
      @Override
      protected Object getFieldValue(Entity entity) throws IllegalArgumentException, IllegalAccessException {
        throw new IllegalAccessException();
      }
    };

    // action
    instance.setPropertyContainerProperty(nodeMock, entity);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToNodeThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception {
    // setup
    SimpleCollectionFieldConverter<Integer> instance = new SimpleCollectionFieldConverter<Integer>(COMPONENT_TYPE) {
      @Override
      protected Object getFieldValue(Entity entity) throws IllegalArgumentException, IllegalAccessException {
        throw new IllegalArgumentException();
      }
    };

    // action
    instance.setPropertyContainerProperty(nodeMock, entity);
  }

  @Override
  @Test(expected = ConversionException.class)
  public void addValueToNodeThrowsAConversionExceptionIfGetFormatedValueThrowsAnIllegalArgumentException() throws Exception {
    // setup
    SimpleCollectionFieldConverter<Integer> instance = new SimpleCollectionFieldConverter<Integer>(COMPONENT_TYPE) {
      @Override
      protected Object getFormattedValue(Object fieldValue) throws IllegalArgumentException {
        throw new IllegalArgumentException();
      }
    };

    setupInstance(instance);
    entity.setPrimitiveCollection(Lists.newArrayList(VALUE_1, VALUE_2, VALUE_3, VALUE_4));

    // action
    instance.setPropertyContainerProperty(nodeMock, entity);
  }

  @Test
  @Override
  public void addValueToEntitySetTheFieldOfTheEntityWithTheValue() throws Exception {
    // setup
    nodeHasValueFor(propertyName, new int[] { VALUE_1, VALUE_2, VALUE_3, VALUE_4 });

    // action
    instance.addValueToEntity(entity, nodeMock);

    // verify
    assertThat(entity.getPrimitiveCollection(), is(contains(VALUE_1, VALUE_2, VALUE_3, VALUE_4)));
    verify(nodeMock, atLeastOnce()).hasProperty(propertyName);
    verify(nodeMock).getProperty(propertyName);
    verifyNoMoreInteractions(nodeMock);
  }

  private void nodeHasValueFor(String propertyName, int[] value) {
    when(nodeMock.hasProperty(propertyName)).thenReturn(true);
    when(nodeMock.getProperty(propertyName)).thenReturn(value);
  }

  @Test
  @Override
  public void addValueToEntityDoesNothingIfThePropertyDoesNotExist() throws Exception {
    // setup
    when(nodeMock.hasProperty(propertyName)).thenReturn(false);

    // action
    instance.addValueToEntity(entity, nodeMock);

    // verify
    assertThat(entity.getPrimitiveCollection(), is(nullValue()));
    verify(nodeMock).hasProperty(propertyName);
    verifyNoMoreInteractions(nodeMock);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnIllegalAccessExceptionIsThrown() throws Exception {
    nodeHasValueFor(propertyName, new int[] { VALUE_1, VALUE_2, VALUE_3, VALUE_4 });

    SimpleCollectionFieldConverter<Integer> instance = new SimpleCollectionFieldConverter<Integer>(COMPONENT_TYPE) {
      @Override
      protected void fillField(Entity entity, PropertyContainer propertyContainer) throws IllegalArgumentException, IllegalAccessException {
        throw new IllegalAccessException();
      }
    };

    setupInstance(instance);

    // action
    instance.addValueToEntity(entity, nodeMock);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnAnIllegalArgumentExceptionIsThrown() throws Exception {
    nodeHasValueFor(propertyName, new int[] { VALUE_1, VALUE_2, VALUE_3, VALUE_4 });

    SimpleCollectionFieldConverter<Integer> instance = new SimpleCollectionFieldConverter<Integer>(COMPONENT_TYPE) {
      @Override
      protected void fillField(Entity entity, PropertyContainer propertyContainer) throws IllegalArgumentException, IllegalAccessException {
        throw new IllegalArgumentException();
      }
    };

    setupInstance(instance);

    // action
    instance.addValueToEntity(entity, nodeMock);
  }

}
