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

public class EntityConverterFactoryTest {

  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private EntityConverterFactory instance;
  @SuppressWarnings("rawtypes")
  private EntityConverter entityWrapperMock;
  private AbstractFieldConverter fieldWrapperMock;
  private FieldConverterFactory fieldWrapperFactoryMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    entityWrapperMock = mock(EntityConverter.class);

    fieldWrapperMock = mock(AbstractFieldConverter.class);
    fieldWrapperFactoryMock = mock(FieldConverterFactory.class);

    when(fieldWrapperFactoryMock.wrap(any(Class.class), any(Field.class))).thenReturn(fieldWrapperMock);

    instance = new EntityConverterFactory(fieldWrapperFactoryMock) {
      @Override
      protected <T extends Entity> EntityConverter<T> createEntityConverter(Class<T> type) {
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
    EntityConverter<TestSystemEntityWrapper> entityWrapper = instance.createForType(SYSTEM_ENTITY_TYPE);

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
    EntityConverter<? super SubADomainEntity> wrapper = instance.createForPrimitive(DOMAIN_ENTITY_TYPE);

    // verify
    verify(fieldWrapperFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(PRIMITIVE_DOMAIN_ENTITY_TYPE)), any(Field.class));
    verify(wrapper, times(numberOfFields)).addFieldWrapper(fieldWrapperMock);
  }

  private int getNumberOfFields(Class<? extends Entity> type) {
    return type.getDeclaredFields().length;
  }
}
