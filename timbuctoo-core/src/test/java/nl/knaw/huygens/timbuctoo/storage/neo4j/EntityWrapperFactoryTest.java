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

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

public class EntityWrapperFactoryTest {

  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final Change CHANGE = Change.newInternalInstance();
  private static final int REVISION = 1;
  private static final String ID = "id";
  private EntityWrapperFactory instance;
  @SuppressWarnings("rawtypes")
  private EntityWrapper entityWrapperMock;
  private FieldWrapper fieldWrapperMock;
  private FieldWrapperFactory fieldWrapperFactoryMock;
  private IdGenerator idGeneratorMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    entityWrapperMock = mock(EntityWrapper.class);

    fieldWrapperMock = mock(FieldWrapper.class);
    fieldWrapperFactoryMock = mock(FieldWrapperFactory.class);

    when(fieldWrapperFactoryMock.wrap(any(Class.class), any(TYPE), any(Field.class))).thenReturn(fieldWrapperMock);

    idGeneratorMock = mock(IdGenerator.class);
    when(idGeneratorMock.nextIdFor(TYPE)).thenReturn(ID);

    instance = new EntityWrapperFactory(fieldWrapperFactoryMock, idGeneratorMock) {
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
  }

  @Test
  public void createFromInstanceAddsAFieldWrapperForEachFieldInTheEntity() {
    TestSystemEntityWrapper entity = new TestSystemEntityWrapper();
    int numberOfFields = countNumberOfFields();

    // action
    EntityWrapper<TestSystemEntityWrapper> entityWrapper = instance.createFromInstance(TYPE, entity);

    // verify
    assertThat(entityWrapper, is(notNullValue()));

    verify(entityWrapper, times(numberOfFields)).addFieldWrapper(fieldWrapperMock);
    verify(entityWrapper).setEntity(entity);
    verify(entityWrapper).setId(ID);
    verify(entityWrapper).setRev(REVISION);
    verify(entityWrapper).setCreated(CHANGE);
    verify(entityWrapper).setModified(CHANGE);
  }

  private int countNumberOfFields() {
    int numberOfFields = TYPE.getDeclaredFields().length;
    numberOfFields += SystemEntity.class.getDeclaredFields().length;
    numberOfFields += Entity.class.getDeclaredFields().length;
    return numberOfFields;
  }

  @Test
  public void createFromTypeCreatesANewInstanceOfTheTypeAndAddsAFieldWrapperForEachField() throws Exception {
    // setup
    int numberOfFields = countNumberOfFields();

    // action
    EntityWrapper<TestSystemEntityWrapper> entityWrapper = instance.createFromType(TYPE);

    // verify
    verify(entityWrapper, times(numberOfFields)).addFieldWrapper(fieldWrapperMock);
    verify(entityWrapper).setEntity(any(TYPE));
    verify(entityWrapper).setId(ID);
    verify(entityWrapper).setRev(REVISION);
    verify(entityWrapper).setCreated(CHANGE);
    verify(entityWrapper).setModified(CHANGE);

  }

}
