package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

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
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property.PropertyConverterFactory;

import org.junit.Before;
import org.junit.Test;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;

public class ElementConverterFactoryTest {

  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private PropertyConverterFactory propertyConverterFactoryMock;
  private ElementConverterFactory instance;

  @Before
  public void setup() {
    propertyConverterFactoryMock = mock(PropertyConverterFactory.class);

    instance = new ElementConverterFactory(propertyConverterFactoryMock, mock(EntityInstantiator.class));
  }

  @Test
  public void forTypeAddsAPropertyConverterForEachField() {
    // setup
    hasPropertyConvertersFor(SYSTEM_ENTITY_TYPE);
    int numberOfFields = countFieldsOfTypeAndSuperTypes(SYSTEM_ENTITY_TYPE);

    // action
    VertexConverter<TestSystemEntityWrapper> converter = instance.forType(SYSTEM_ENTITY_TYPE);

    // verify
    assertThat(converter, is(instanceOf(ExtendableVertexConverter.class)));
    verify(propertyConverterFactoryMock, times(numberOfFields)).createPropertyConverter(argThat(equalTo(SYSTEM_ENTITY_TYPE)), any(Field.class));
  }

  @Test
  public void compositeForTypeCreatesACompositeVertextConverterWithATwoExtendableVertexConvertersAdded() {
    // setup
    hasPropertyConvertersFor(DOMAIN_ENTITY_TYPE);
    hasPropertyConvertersFor(PRIMITIVE_DOMAIN_ENTITY_TYPE);

    int domainEntityNumberOfFields = countFieldsOfTypeAndSuperTypes(DOMAIN_ENTITY_TYPE);
    int primitiveDomainEntityNumberOfFields = countFieldsOfTypeAndSuperTypes(PRIMITIVE_DOMAIN_ENTITY_TYPE);

    // instance
    VertexConverter<SubADomainEntity> converter = instance.compositeForType(DOMAIN_ENTITY_TYPE);

    // verify
    assertThat(converter, is(instanceOf(CompositeVertexConverter.class)));
    CompositeVertexConverter<SubADomainEntity> composite = (CompositeVertexConverter<SubADomainEntity>) converter;

    assertThat(composite.getNumberOfDelegates(), is(2));

    verify(propertyConverterFactoryMock, times(domainEntityNumberOfFields)).createPropertyConverter(argThat(equalTo(DOMAIN_ENTITY_TYPE)), any(Field.class));
    verify(propertyConverterFactoryMock, times(primitiveDomainEntityNumberOfFields)).createPropertyConverter(argThat(equalTo(PRIMITIVE_DOMAIN_ENTITY_TYPE)), any(Field.class));
  }

  private void hasPropertyConvertersFor(Class<? extends Entity> type) {
    when(propertyConverterFactoryMock.createPropertyConverter(argThat(equalTo(type)), any(Field.class))).thenReturn(mock(PropertyConverter.class));
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
