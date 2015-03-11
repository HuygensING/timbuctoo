package nl.knaw.huygens.timbuctoo.storage.neo4j;

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

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

public class PropertyContainerConverterFactoryTest {

  private static final Class<Relation> PRIMITIVE_RELATION_TYPE = Relation.class;
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private PropertyContainerConverterFactory instance;
  @SuppressWarnings("rawtypes")
  private ExtendableNodeConverter nodeConverterMock;
  @SuppressWarnings("rawtypes")
  private ExtendableRelationshipConverter relationshipConverterMock;
  private AbstractFieldConverter fieldConverterMock;
  private FieldConverterFactory fieldConverterFactoryMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    nodeConverterMock = mock(ExtendableNodeConverter.class);
    relationshipConverterMock = mock(ExtendableRelationshipConverter.class);

    fieldConverterMock = mock(AbstractFieldConverter.class);
    fieldConverterFactoryMock = mock(FieldConverterFactory.class);

    when(fieldConverterFactoryMock.wrap(any(Class.class), any(Field.class))).thenReturn(fieldConverterMock);

    instance = new PropertyContainerConverterFactory(fieldConverterFactoryMock) {
      @Override
      protected <T extends Entity> ExtendableNodeConverter<T> createSimpleNodeConverter(Class<T> type) {
        return nodeConverterMock;
      }

      @Override
      protected <T extends Relation> ExtendableRelationshipConverter<T> createSimpleRelationshipConverter(Class<T> type) {
        return relationshipConverterMock;
      }

    };
  }

  @Test
  public void createForTypeAddsAFieldConverterForEachField() throws Exception {
    // setup
    int numberOfFields = getNumberOfFields(SYSTEM_ENTITY_TYPE);
    numberOfFields += getNumberOfFields(SystemEntity.class);
    numberOfFields += getNumberOfFields(Entity.class);

    // action
    PropertyContainerConverter<Node, TestSystemEntityWrapper> propertyContainerConverter = instance.createForType(SYSTEM_ENTITY_TYPE);

    // verify
    assertThat(propertyContainerConverter, instanceOf(ExtendableNodeConverter.class));
    verify(fieldConverterFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(SYSTEM_ENTITY_TYPE)), any(Field.class));
    verify((ExtendableNodeConverter<TestSystemEntityWrapper>) propertyContainerConverter, times(numberOfFields)).addFieldConverter(fieldConverterMock);
  }

  @Test
  public void createForPrimitiveRetrievesThePrimitiveDomainEntityAndCreatesAFieldWrapperForIt() {
    // setup
    int numberOfFields = getNumberOfFields(PRIMITIVE_DOMAIN_ENTITY_TYPE);
    numberOfFields += getNumberOfFields(DomainEntity.class);
    numberOfFields += getNumberOfFields(Entity.class);

    // action
    NodeConverter<? super SubADomainEntity> converter = instance.createForPrimitive(DOMAIN_ENTITY_TYPE);

    // verify
    assertThat(converter, is(instanceOf(ExtendableNodeConverter.class)));
    verify(fieldConverterFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(PRIMITIVE_DOMAIN_ENTITY_TYPE)), any(Field.class));
    verify((ExtendableNodeConverter<? super SubADomainEntity>) converter, times(numberOfFields)).addFieldConverter(fieldConverterMock);
  }

  @Test
  public void createForRelationAddsAFieldConverterForEachFieldOfTheType() {
    int numberOfFields = getNumberOfFields(RELATION_TYPE);
    numberOfFields += getNumberOfFields(PRIMITIVE_RELATION_TYPE);
    numberOfFields += getNumberOfFields(DomainEntity.class);
    numberOfFields += getNumberOfFields(Entity.class);

    // action
    RelationshipConverter<SubARelation> converter = instance.createForRelation(RELATION_TYPE);

    // verify
    assertThat(converter, is(instanceOf(ExtendableRelationshipConverter.class)));
    verify(fieldConverterFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(RELATION_TYPE)), any(Field.class));
    verify((ExtendableRelationshipConverter<SubARelation>) converter, times(numberOfFields)).addFieldConverter(fieldConverterMock);
  }

  @Test
  public void createForPrimitveRelationAddsAFieldConverterForEachFieldOfTheType() {
    int numberOfFields = getNumberOfFields(PRIMITIVE_RELATION_TYPE);
    numberOfFields += getNumberOfFields(DomainEntity.class);
    numberOfFields += getNumberOfFields(Entity.class);

    // action
    RelationshipConverter<? super SubARelation> converter = instance.createForPrimitiveRelation(RELATION_TYPE);

    // verify
    assertThat(converter, is(instanceOf(ExtendableRelationshipConverter.class)));
    verify(fieldConverterFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(PRIMITIVE_RELATION_TYPE)), any(Field.class));
    verify((ExtendableRelationshipConverter<? super SubARelation>) converter, times(numberOfFields)).addFieldConverter(fieldConverterMock);
  }

  private int getNumberOfFields(Class<? extends Entity> type) {
    return type.getDeclaredFields().length;
  }

  @Test
  public void createCompositeForTypeCreatesANodeConverterWithATwoSimpleNodeConvertersAdded() {
    // setup
    int domainEntityNumberOfFields = getNumberOfFields(DOMAIN_ENTITY_TYPE);
    domainEntityNumberOfFields += getNumberOfFields(PRIMITIVE_DOMAIN_ENTITY_TYPE);
    domainEntityNumberOfFields += getNumberOfFields(DomainEntity.class);
    domainEntityNumberOfFields += getNumberOfFields(Entity.class);

    int primitiveDomainEntityNumberOfFields = getNumberOfFields(PRIMITIVE_DOMAIN_ENTITY_TYPE);
    primitiveDomainEntityNumberOfFields += getNumberOfFields(DomainEntity.class);
    primitiveDomainEntityNumberOfFields += getNumberOfFields(Entity.class);

    // action
    NodeConverter<SubADomainEntity> converter = instance.createCompositeForType(DOMAIN_ENTITY_TYPE);

    // verify
    assertThat(converter, is(instanceOf(CompositeNodeConverter.class)));
    assertThat(((CompositeNodeConverter<SubADomainEntity>) converter).getNodeConverters().size(), is(equalTo(2)));

    verify(fieldConverterFactoryMock, times(domainEntityNumberOfFields)).wrap(argThat(equalTo(DOMAIN_ENTITY_TYPE)), any(Field.class));
    verify(fieldConverterFactoryMock, times(primitiveDomainEntityNumberOfFields)).wrap(argThat(equalTo(PRIMITIVE_DOMAIN_ENTITY_TYPE)), any(Field.class));
  }
}
