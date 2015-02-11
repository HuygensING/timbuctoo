package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.junit.Test;

import test.model.TestSystemEntity;

public class ObjectWrapperFactoryTest {

  @Test
  public void wrapAddsAFieldWrapperForEachFieldInTheEntity() {
    TestSystemEntity entity = new TestSystemEntity();
    int numberOfFields = TestSystemEntity.class.getDeclaredFields().length;
    numberOfFields += SystemEntity.class.getDeclaredFields().length;
    numberOfFields += Entity.class.getDeclaredFields().length;

    FieldWrapper fieldWrapperMock = mock(FieldWrapper.class);
    FieldWrapperFactory fieldWrapperFactoryMock = mock(FieldWrapperFactory.class);

    when(fieldWrapperFactoryMock.wrap(any(Field.class), any(TestSystemEntity.class))).thenReturn(fieldWrapperMock);

    final ObjectWrapper objectWrapperMock = mock(ObjectWrapper.class);

    ObjectWrapperFactory instance = new ObjectWrapperFactory(fieldWrapperFactoryMock) {
      @Override
      protected ObjectWrapper createObjectWrapper() {
        return objectWrapperMock;
      }
    };

    // action
    ObjectWrapper objectWrapper = instance.wrap(entity);

    // verify
    assertThat(objectWrapper, is(notNullValue()));

    verify(fieldWrapperFactoryMock, times(numberOfFields)).wrap(any(Field.class), any(TestSystemEntity.class));
    verify(objectWrapperMock, times(numberOfFields)).addFieldWrapper(fieldWrapperMock);
  }
}
