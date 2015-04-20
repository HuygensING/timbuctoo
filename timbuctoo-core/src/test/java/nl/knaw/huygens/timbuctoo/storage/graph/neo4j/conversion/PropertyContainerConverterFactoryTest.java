package nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion;

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

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.NodeConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.PropertyContainerConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.RelationshipConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.CompositeNodeConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.CompositeRelationshipConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.ExtendableNodeConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.ExtendableRelationshipConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.PropertyContainerConverterFactory;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.PropertyConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.property.PropertyConverterFactory;

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
  private PropertyConverter propertyConverterMock;
  private PropertyConverterFactory propertyConverterFactoryMock;
  private TypeRegistry typeRegistryMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    typeRegistryMock = mock(TypeRegistry.class);
    nodeConverterMock = mock(ExtendableNodeConverter.class);
    relationshipConverterMock = mock(ExtendableRelationshipConverter.class);

    propertyConverterMock = mock(PropertyConverter.class);
    propertyConverterFactoryMock = mock(PropertyConverterFactory.class);

    when(propertyConverterFactoryMock.createFor(any(Class.class), any(Field.class))).thenReturn(propertyConverterMock);

    instance = new PropertyContainerConverterFactory(propertyConverterFactoryMock, typeRegistryMock) {
      @Override
      protected <T extends Entity> ExtendableNodeConverter<T> createExtendableNodeConverter(Class<T> type) {
        return nodeConverterMock;
      }

      @Override
      protected <T extends Relation> ExtendableRelationshipConverter<T> createExtendableRelationshipConverter(Class<T> type) {
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
    verify(propertyConverterFactoryMock, times(numberOfFields)).createFor(argThat(equalTo(SYSTEM_ENTITY_TYPE)), any(Field.class));
    verify((ExtendableNodeConverter<TestSystemEntityWrapper>) propertyContainerConverter, times(numberOfFields)).addPropertyConverter(propertyConverterMock);
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
    verify(propertyConverterFactoryMock, times(numberOfFields)).createFor(argThat(equalTo(RELATION_TYPE)), any(Field.class));
    verify((ExtendableRelationshipConverter<SubARelation>) converter, times(numberOfFields)).addPropertyConverter(propertyConverterMock);
  }

  private int getNumberOfFields(Class<? extends Entity> type) {
    return type.getDeclaredFields().length;
  }

  @Test
  public void createCompositeForTypeCreatesANodeConverterWithATwoExtendableNodeConvertersAdded() {
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

    verify(propertyConverterFactoryMock, times(domainEntityNumberOfFields)).createFor(argThat(equalTo(DOMAIN_ENTITY_TYPE)), any(Field.class));
    verify(propertyConverterFactoryMock, times(primitiveDomainEntityNumberOfFields)).createFor(argThat(equalTo(PRIMITIVE_DOMAIN_ENTITY_TYPE)), any(Field.class));
  }

  @Test
  public void createCompositeForRelationCreatesARelationshipConverterWithToExtendableRelationConverters() {
    // setup
    int domainEntityNumberOfFields = getNumberOfFields(RELATION_TYPE);
    domainEntityNumberOfFields += getNumberOfFields(PRIMITIVE_RELATION_TYPE);
    domainEntityNumberOfFields += getNumberOfFields(DomainEntity.class);
    domainEntityNumberOfFields += getNumberOfFields(Entity.class);

    int primitiveDomainEntityNumberOfFields = getNumberOfFields(PRIMITIVE_RELATION_TYPE);
    primitiveDomainEntityNumberOfFields += getNumberOfFields(DomainEntity.class);
    primitiveDomainEntityNumberOfFields += getNumberOfFields(Entity.class);

    // action
    RelationshipConverter<SubARelation> converter = instance.createCompositeForRelation(RELATION_TYPE);

    // verify
    assertThat(converter, is(instanceOf(CompositeRelationshipConverter.class)));
    assertThat(((CompositeRelationshipConverter<SubARelation>) converter).getNodeConverters().size(), is(equalTo(2)));

    verify(propertyConverterFactoryMock, times(domainEntityNumberOfFields)).createFor(argThat(equalTo(RELATION_TYPE)), any(Field.class));
    verify(propertyConverterFactoryMock, times(primitiveDomainEntityNumberOfFields)).createFor(argThat(equalTo(PRIMITIVE_RELATION_TYPE)), any(Field.class));
  }
}
