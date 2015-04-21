package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Test;

import test.model.TestSystemEntityWrapper;

public class ElementConverterFactoryTest {

  @Test
  public void forTypeAddsAPropertyConverterForEachField() {
    // setup
    PropertyConverterFactory propertyConverterFactoryMock = mock(PropertyConverterFactory.class);
    when(propertyConverterFactoryMock.createPropertyConverter(argThat(equalTo(TestSystemEntityWrapper.class)), any(Field.class))).thenReturn(mock(PropertyConverter.class));

    ElementConverterFactory instance = new ElementConverterFactory(propertyConverterFactoryMock);

    int numberOfFields = countFieldsOfTypeAndSuperTypes(TestSystemEntityWrapper.class);

    // action
    VertexConverter<TestSystemEntityWrapper> converter = instance.forType(TestSystemEntityWrapper.class);

    // verify
    assertThat(converter, is(instanceOf(ExtendableVertexConverter.class)));
    verify(propertyConverterFactoryMock, times(numberOfFields)).createPropertyConverter(argThat(equalTo(TestSystemEntityWrapper.class)), any(Field.class));
  }

  private int countFieldsOfTypeAndSuperTypes(Class<?> type) {
    int numberOfFields = 0;

    while (Entity.class.isAssignableFrom(type)) {
      numberOfFields += getNumberOfFields(type);
      type = type.getSuperclass();
    }

    return numberOfFields;
  }

  private int getNumberOfFields(Class<?> type) {
    return type.getDeclaredFields().length;
  }
}
