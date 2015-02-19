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
import nl.knaw.huygens.timbuctoo.model.util.Change;

import org.junit.Test;

import test.model.TestSystemEntityWrapper;

public class EntityWrapperFactoryTest {

  private static final Change CHANGE = Change.newInternalInstance();
  private static final int REVISION = 1;
  private static final String ID = "id";

  @SuppressWarnings("unchecked")
  @Test
  public void wrapAddsAFieldWrapperForEachFieldInTheEntity() {
    TestSystemEntityWrapper entity = new TestSystemEntityWrapper();
    Class<TestSystemEntityWrapper> type = TestSystemEntityWrapper.class;
    int numberOfFields = type.getDeclaredFields().length;
    numberOfFields += SystemEntity.class.getDeclaredFields().length;
    numberOfFields += Entity.class.getDeclaredFields().length;

    FieldWrapper fieldWrapperMock = mock(FieldWrapper.class);
    FieldWrapperFactory fieldWrapperFactoryMock = mock(FieldWrapperFactory.class);

    when(fieldWrapperFactoryMock.wrap(any(Class.class), any(type), any(Field.class))).thenReturn(fieldWrapperMock);

    IdGenerator idGeneratorMock = mock(IdGenerator.class);
    when(idGeneratorMock.nextIdFor(type)).thenReturn(ID);

    @SuppressWarnings("rawtypes")
    final EntityWrapper entityWrapperMock = mock(EntityWrapper.class);

    EntityWrapperFactory instance = new EntityWrapperFactory(fieldWrapperFactoryMock, idGeneratorMock) {
      @Override
      protected <T extends Entity> EntityWrapper<T> createEntityWrapper(Class<T> type) {
        return entityWrapperMock;
      }

      @Override
      protected Change newChange() {
        return CHANGE;
      }

      @Override
      protected int newRevision() {
        return REVISION;
      }

    };

    // action
    EntityWrapper<TestSystemEntityWrapper> objectWrapper = instance.createFromInstance(type, entity);

    // verify
    assertThat(objectWrapper, is(notNullValue()));

    verify(fieldWrapperFactoryMock, times(numberOfFields)).wrap(any(Class.class), any(type), any(Field.class));
    verify(entityWrapperMock, times(numberOfFields)).addFieldWrapper(fieldWrapperMock);
    verify(entityWrapperMock).setEntity(entity);
    verify(entityWrapperMock).setId(ID);
    verify(entityWrapperMock).setRev(REVISION);
    verify(entityWrapperMock).setCreated(CHANGE);
    verify(entityWrapperMock).setModified(CHANGE);
  }
}
