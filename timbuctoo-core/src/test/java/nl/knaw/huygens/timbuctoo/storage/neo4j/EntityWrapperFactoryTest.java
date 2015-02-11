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

import test.model.TestSystemEntityWrapper;

public class EntityWrapperFactoryTest {

  @Test
  public void wrapAddsAFieldWrapperForEachFieldInTheEntity() {
    TestSystemEntityWrapper entity = new TestSystemEntityWrapper();
    int numberOfFields = TestSystemEntityWrapper.class.getDeclaredFields().length;
    numberOfFields += SystemEntity.class.getDeclaredFields().length;
    numberOfFields += Entity.class.getDeclaredFields().length;

    FieldWrapper fieldWrapperMock = mock(FieldWrapper.class);
    FieldWrapperFactory fieldWrapperFactoryMock = mock(FieldWrapperFactory.class);

    when(fieldWrapperFactoryMock.wrap(any(Field.class), any(TestSystemEntityWrapper.class))).thenReturn(fieldWrapperMock);

    final EntityWrapper objectWrapperMock = mock(EntityWrapper.class);

    EntityWrapperFactory instance = new EntityWrapperFactory(fieldWrapperFactoryMock) {
      @Override
      protected EntityWrapper createObjectWrapper() {
        return objectWrapperMock;
      }
    };

    // action
    EntityWrapper objectWrapper = instance.wrap(entity);

    // verify
    assertThat(objectWrapper, is(notNullValue()));

    verify(fieldWrapperFactoryMock, times(numberOfFields)).wrap(any(Field.class), any(TestSystemEntityWrapper.class));
    verify(objectWrapperMock, times(numberOfFields)).addFieldWrapper(fieldWrapperMock);
  }
}
