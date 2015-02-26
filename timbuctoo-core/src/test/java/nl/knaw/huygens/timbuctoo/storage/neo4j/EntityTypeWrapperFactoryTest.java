package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

public class EntityWrapperFactoryTest {

  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private EntityTypeWrapperFactory instance;
  @SuppressWarnings("rawtypes")
  private EntityTypeWrapper entityWrapperMock;
  private AbstractFieldWrapper fieldWrapperMock;
  private FieldWrapperFactory fieldWrapperFactoryMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    entityWrapperMock = mock(EntityTypeWrapper.class);

    fieldWrapperMock = mock(AbstractFieldWrapper.class);
    fieldWrapperFactoryMock = mock(FieldWrapperFactory.class);

    when(fieldWrapperFactoryMock.wrap(any(Class.class), any(Field.class))).thenReturn(fieldWrapperMock);

    instance = new EntityTypeWrapperFactory(fieldWrapperFactoryMock) {
      @Override
      protected <T extends Entity> EntityTypeWrapper<T> createEntityWrapper(Class<T> type) {
        return entityWrapperMock;
      }
    };
  }

  private int countNumberOfFields() {
    int numberOfFields = TYPE.getDeclaredFields().length;
    numberOfFields += SystemEntity.class.getDeclaredFields().length;
    numberOfFields += Entity.class.getDeclaredFields().length;
    return numberOfFields;
  }

  @Test
  public void createFromTypeAddsAFieldWrapperForEachField() throws Exception {
    // setup
    int numberOfFields = countNumberOfFields();

    // action
    EntityTypeWrapper<TestSystemEntityWrapper> entityWrapper = instance.createFromType(TYPE);

    // verify
    verify(entityWrapper, times(numberOfFields)).addFieldWrapper(fieldWrapperMock);
  }

}
