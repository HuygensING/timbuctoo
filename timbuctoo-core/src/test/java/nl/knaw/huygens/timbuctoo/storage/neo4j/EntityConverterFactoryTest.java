package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
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
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;

public class EntityConverterFactoryTest {

  private static final Class<Node> NODE_TYPE = Node.class;
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private EntityConverterFactory instance;
  @SuppressWarnings("rawtypes")
  private NoOpEntityConverter noOpEntityConverterMock;
  @SuppressWarnings("rawtypes")
  private RegularEntityConverter regularEntityConverterMock;
  private AbstractFieldConverter fieldConverterMock;
  private FieldConverterFactory fieldConverterFactoryMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    regularEntityConverterMock = mock(RegularEntityConverter.class);
    noOpEntityConverterMock = mock(NoOpEntityConverter.class);

    fieldConverterMock = mock(AbstractFieldConverter.class);
    fieldConverterFactoryMock = mock(FieldConverterFactory.class);

    when(fieldConverterFactoryMock.wrap(any(Class.class), any(Field.class))).thenReturn(fieldConverterMock);

    instance = new EntityConverterFactory(fieldConverterFactoryMock) {
      @Override
      protected <T extends Entity, U extends Node> EntityConverter<T, U> createEntityConverter(Class<T> type, Class<U> nodeType) {
        return regularEntityConverterMock;
      }

      @Override
      protected <T extends Entity, U extends PropertyContainer> EntityConverter<T, U> createNoOpEntityConverter(Class<T> type, Class<U> nodeType) {
        return noOpEntityConverterMock;
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
    EntityConverter<TestSystemEntityWrapper, Node> entityConverter = instance.createForTypeAndPropertyContainer(SYSTEM_ENTITY_TYPE, NODE_TYPE);

    // verify
    verify(fieldConverterFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(SYSTEM_ENTITY_TYPE)), any(Field.class));
    verify(entityConverter, times(numberOfFields)).addFieldConverter(fieldConverterMock);
  }

  @Test
  public void createEntityForTypeCreatesANoOpEntityConverterIfPropertyConverterIsUsed() {
    // action
    EntityConverter<TestSystemEntityWrapper, PropertyContainer> entityConverter = instance.createForTypeAndPropertyContainer(SYSTEM_ENTITY_TYPE, PropertyContainer.class);

    // verify
    assertThat(entityConverter, instanceOf(NoOpEntityConverter.class));
  }

  @Test
  public void createForPrimitiveRetrievesThePrimitiveDomainEntityAndCreatesAFieldWrapperForIt() {
    // setup
    int numberOfFields = getNumberOfFields(PRIMITIVE_DOMAIN_ENTITY_TYPE);
    numberOfFields += getNumberOfFields(DomainEntity.class);
    numberOfFields += getNumberOfFields(Entity.class);

    // action
    EntityConverter<? super SubADomainEntity, Node> wrapper = instance.createForPrimitive(DOMAIN_ENTITY_TYPE, NODE_TYPE);

    // verify
    verify(fieldConverterFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(PRIMITIVE_DOMAIN_ENTITY_TYPE)), any(Field.class));
    verify(wrapper, times(numberOfFields)).addFieldConverter(fieldConverterMock);
  }

  private int getNumberOfFields(Class<? extends Entity> type) {
    return type.getDeclaredFields().length;
  }
}
