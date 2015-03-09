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
  private static final Class<Node> NODE_TYPE = Node.class;
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<BaseDomainEntity> PRIMITIVE_DOMAIN_ENTITY_TYPE = BaseDomainEntity.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private PropertyContainerConverterFactory instance;
  @SuppressWarnings("rawtypes")
  private NodeConverter nodeConverterMock;
  @SuppressWarnings("rawtypes")
  private RelationshipConverter relationshipConverterMock;
  private AbstractFieldConverter fieldConverterMock;
  private FieldConverterFactory fieldConverterFactoryMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    nodeConverterMock = mock(NodeConverter.class);
    relationshipConverterMock = mock(RelationshipConverter.class);

    fieldConverterMock = mock(AbstractFieldConverter.class);
    fieldConverterFactoryMock = mock(FieldConverterFactory.class);

    when(fieldConverterFactoryMock.wrap(any(Class.class), any(Field.class))).thenReturn(fieldConverterMock);

    instance = new PropertyContainerConverterFactory(fieldConverterFactoryMock) {
      @Override
      protected <T extends Entity> NodeConverter<T> createNodeConverter(Class<T> type) {
        return nodeConverterMock;
      }

      @Override
      protected <T extends Relation> RelationshipConverter<T> createRelationshipConverter(Class<T> type) {
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
    PropertyContainerConverter<Node, TestSystemEntityWrapper> propertyContainerConverter = instance.createForTypeAndPropertyContainer(NODE_TYPE, SYSTEM_ENTITY_TYPE);

    // verify
    verify(fieldConverterFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(SYSTEM_ENTITY_TYPE)), any(Field.class));
    verify(propertyContainerConverter, times(numberOfFields)).addFieldConverter(fieldConverterMock);
  }

  @Test
  public void createEntityForTypeCreatesANodeConverter() {
    // action
    PropertyContainerConverter<Node, TestSystemEntityWrapper> propertyContainerConverter = instance.createForTypeAndPropertyContainer(NODE_TYPE, SYSTEM_ENTITY_TYPE);

    // verify
    assertThat(propertyContainerConverter, instanceOf(NodeConverter.class));
  }

  @Test
  public void createForPrimitiveRetrievesThePrimitiveDomainEntityAndCreatesAFieldWrapperForIt() {
    // setup
    int numberOfFields = getNumberOfFields(PRIMITIVE_DOMAIN_ENTITY_TYPE);
    numberOfFields += getNumberOfFields(DomainEntity.class);
    numberOfFields += getNumberOfFields(Entity.class);

    // action
    PropertyContainerConverter<Node, ? super SubADomainEntity> converter = instance.createForPrimitive(NODE_TYPE, DOMAIN_ENTITY_TYPE);

    // verify
    verify(fieldConverterFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(PRIMITIVE_DOMAIN_ENTITY_TYPE)), any(Field.class));
    verify(converter, times(numberOfFields)).addFieldConverter(fieldConverterMock);
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
    verify(fieldConverterFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(RELATION_TYPE)), any(Field.class));
    verify(converter, times(numberOfFields)).addFieldConverter(fieldConverterMock);
  }

  @Test
  public void createForPrimitveRelationAddsAFieldConverterForEachFieldOfTheType() {
    int numberOfFields = getNumberOfFields(PRIMITIVE_RELATION_TYPE);
    numberOfFields += getNumberOfFields(DomainEntity.class);
    numberOfFields += getNumberOfFields(Entity.class);

    // action
    RelationshipConverter<? super SubARelation> converter = instance.createForPrimitiveRelation(RELATION_TYPE);

    // verify
    verify(fieldConverterFactoryMock, times(numberOfFields)).wrap(argThat(equalTo(PRIMITIVE_RELATION_TYPE)), any(Field.class));
    verify(converter, times(numberOfFields)).addFieldConverter(fieldConverterMock);
  }

  private int getNumberOfFields(Class<? extends Entity> type) {
    return type.getDeclaredFields().length;
  }
}
