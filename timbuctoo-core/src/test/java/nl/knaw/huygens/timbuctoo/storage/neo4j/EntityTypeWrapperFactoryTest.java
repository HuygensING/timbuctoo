package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.junit.Before;
import org.junit.Test;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;

public class EntityTypeWrapperFactoryTest {

  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
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

  @Test
  public void createForTypeAddsAFieldWrapperForEachField() throws Exception {
    // setup
    int numberOfFields = getNumberOfFields(SYSTEM_ENTITY_TYPE);
    numberOfFields += getNumberOfFields(SystemEntity.class);
    numberOfFields += getNumberOfFields(Entity.class);

    // action
    EntityTypeWrapper<TestSystemEntityWrapper> entityWrapper = instance.createForType(SYSTEM_ENTITY_TYPE);

    // verify
    verify(fieldWrapperFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(SYSTEM_ENTITY_TYPE)), any(Field.class));
    verify(entityWrapper, times(numberOfFields)).addFieldWrapper(fieldWrapperMock);
  }

  @Test
  public void createForPrimitiveRetrievesThePrimitiveDomainEntityAndCreatesAFieldWrapperForIt() {
    // setup
    int numberOfFields = getNumberOfFields(PRIMITIVE_DOMAIN_ENTITY_TYPE);
    numberOfFields += getNumberOfFields(DomainEntity.class);
    numberOfFields += getNumberOfFields(Entity.class);

    // action
    EntityTypeWrapper<? super SubADomainEntity> wrapper = instance.createForPrimitive(DOMAIN_ENTITY_TYPE);

    // verify
    verify(fieldWrapperFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(PRIMITIVE_DOMAIN_ENTITY_TYPE)), any(Field.class));
    verify(wrapper, times(numberOfFields)).addFieldWrapper(fieldWrapperMock);
  }

  private int getNumberOfFields(Class<? extends Entity> type) {
    return type.getDeclaredFields().length;
  }
}
