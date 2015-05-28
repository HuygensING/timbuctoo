package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import static nl.knaw.huygens.timbuctoo.storage.RelationMatcher.likeRelation;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeMockBuilder.anEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;

import org.junit.Before;
import org.junit.Test;

import test.model.BaseDomainEntity;
import test.model.projecta.ProjectAPerson;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

public class ExtendableEdgeConverterTest {

  private static final String NON_EXISTING_FIELD_NAME = "nonExistingFieldName";
  private static final Class<Relation> PRIMITIVE_TYPE = Relation.class;
  private static final Class<SubARelation> DOMAIN_ENTITY_TYPE = SubARelation.class;
  private static final String PROPERTY1_NAME = "property1Name";
  private static final String PROPERTY2_NAME = "property2Name";
  private static final String FIELD1_NAME = "field1Name";
  private static final String FIELD2_NAME = "field2Name";
  private PropertyConverter propertyConverter1;
  private PropertyConverter propertyConverter2;
  private PropertyConverter modifiedConverterMock;
  private PropertyConverter revConverterMock;
  private ExtendableEdgeConverter<SubARelation> instance;
  private Edge edgeMock;
  private SubARelation entity;
  private List<PropertyConverter> propertyConverters;
  private EntityInstantiator entityInstantiatorMock;
  private TypeRegistry typeRegistry;

  @Before
  public void setup() throws Exception {
    typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init(getPackageName(SubADomainEntity.class) + " " + getPackageName(BaseDomainEntity.class) + " " + getPackageName(Person.class));

    propertyConverter1 = createPropertyConverter(PROPERTY1_NAME, FIELD1_NAME, FieldType.REGULAR);
    propertyConverter2 = createPropertyConverter(PROPERTY2_NAME, FIELD2_NAME, FieldType.REGULAR);
    modifiedConverterMock = createPropertyConverter(Entity.MODIFIED_PROPERTY_NAME, Entity.MODIFIED_PROPERTY_NAME, FieldType.ADMINISTRATIVE);
    revConverterMock = createPropertyConverter(Entity.REVISION_PROPERTY_NAME, Entity.REVISION_PROPERTY_NAME, FieldType.ADMINISTRATIVE);
    propertyConverters = Lists.newArrayList(propertyConverter1, propertyConverter2, modifiedConverterMock, revConverterMock);

    entityInstantiatorMock = mock(EntityInstantiator.class);

    instance = createInstance(DOMAIN_ENTITY_TYPE, propertyConverters, entityInstantiatorMock);

    edgeMock = mock(Edge.class);
    entity = new SubARelation();
  }

  private String getPackageName(Class<? extends DomainEntity> type) {
    return type.getPackage().getName();
  }

  private <T extends Relation> ExtendableEdgeConverter<T> createInstance(Class<T> type, List<PropertyConverter> propertyConverters, EntityInstantiator entityInstantiator) {
    return new ExtendableEdgeConverter<T>(type, propertyConverters, entityInstantiator, typeRegistry);
  }

  private PropertyConverter createPropertyConverter(String propertyName, String fieldName, FieldType fieldType) {
    PropertyConverter propertyConverter = mock(PropertyConverter.class);
    when(propertyConverter.propertyName()).thenReturn(propertyName);
    when(propertyConverter.getFieldName()).thenReturn(fieldName);
    when(propertyConverter.getFieldType()).thenReturn(fieldType);
    return propertyConverter;
  }

  @Test
  public void addValuesToElementLetsThePropertyConvertersAddTheirValuesToTheEdge() throws Exception {
    // action
    instance.addValuesToElement(edgeMock, entity);

    // verify
    verify(propertyConverter1).setPropertyOfElement(edgeMock, entity);
    verify(propertyConverter2).setPropertyOfElement(edgeMock, entity);
    verify(modifiedConverterMock).setPropertyOfElement(edgeMock, entity);
    verify(revConverterMock).setPropertyOfElement(edgeMock, entity);

  }

  @Test
  public void addValuesToEdgeAddsTheTypeOfTheEdgeConverter() throws Exception {
    // setup 
    ExtendableEdgeConverter<Relation> instance = createInstance(PRIMITIVE_TYPE, propertyConverters, entityInstantiatorMock);

    // action
    instance.addValuesToElement(edgeMock, entity);

    verifyTypeIsAdded(edgeMock, PRIMITIVE_TYPE);
  }

  private void verifyTypeIsAdded(Element elementMock, Class<? extends Entity> type) throws Exception {
    List<String> types = Lists.newArrayList(TypeNames.getInternalName(type));

    ObjectMapper objectMapper = new ObjectMapper();
    String value = objectMapper.writeValueAsString(types);

    verify(elementMock).setProperty(ELEMENT_TYPES, value);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToElementThrowsAConversionExceptionWhenOneOfThePropertyConvertersDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(propertyConverter1).setPropertyOfElement(edgeMock, entity);

    // action
    instance.addValuesToElement(edgeMock, entity);

  }

  @Test
  public void convertToEntityCreatesAnInstanceOfTheEntityThenLetThePropertyConvertersAddTheValues() throws Exception {
    // setup
    when(entityInstantiatorMock.createInstanceOf(DOMAIN_ENTITY_TYPE)).thenReturn(entity);

    String sourceId = "sourceId";
    String sourceType = TypeNames.getInternalName(BaseDomainEntity.class);
    Vertex source = aVertex()//
        .withId(sourceId)//
        .withType(BaseDomainEntity.class)//
        .withType(SubADomainEntity.class)//
        .build();

    String targetId = "targetId";
    String targetType = TypeNames.getInternalName(Person.class);
    Vertex target = aVertex()//
        .withId(targetId)//
        .withType(Person.class)//
        .withType(ProjectAPerson.class) //
        .build();

    Edge edgeMock = anEdge().withSource(source).withTarget(target).build();

    // action
    SubARelation createdEntity = instance.convertToEntity(edgeMock);

    // verify
    assertThat(createdEntity, likeRelation() //
        .withSourceId(sourceId) //
        .withSourceType(sourceType) //
        .withTargetId(targetId) //
        .withTargetType(targetType));

    verify(propertyConverter1).addValueToEntity(entity, edgeMock);
    verify(propertyConverter2).addValueToEntity(entity, edgeMock);

  }

  @Test(expected = ConversionException.class)
  public void convertToEntityThrowsAConversionExceptionWhenTheEntityCannotBeInstatiated() throws Exception {
    // setup
    when(entityInstantiatorMock.createInstanceOf(DOMAIN_ENTITY_TYPE)).thenThrow(new InstantiationException());

    // action
    instance.convertToEntity(edgeMock);

  }

  @Test(expected = ConversionException.class)
  public void convertToEntityThrowsAConversionExceptionWhenOneOfTheValuesCannotBeConverted() throws Exception {
    // setup
    when(entityInstantiatorMock.createInstanceOf(DOMAIN_ENTITY_TYPE)).thenReturn(entity);
    doThrow(ConversionException.class).when(propertyConverter1).addValueToEntity(entity, edgeMock);

    // action
    instance.convertToEntity(edgeMock);
  }

  @Test
  public void getPropertyNameReturnsTheNameOfTheFoundPropertyConverter() {
    // setup
    when(propertyConverter1.propertyName()).thenReturn(PROPERTY1_NAME);

    // action
    String actualPropertyName = instance.getPropertyName(FIELD1_NAME);

    // verify
    assertThat(actualPropertyName, is(equalTo(PROPERTY1_NAME)));
  }

  @Test(expected = NoSuchFieldException.class)
  public void getPropertyNameThrowsARuntimeExceptionWhenThePropertyIsNotFound() {
    // action
    instance.getPropertyName(NON_EXISTING_FIELD_NAME);
  }

  @Test
  public void removePropertyByFieldNameRemovesThePropertyFromTheEdge() {
    // action
    instance.removePropertyByFieldName(edgeMock, FIELD1_NAME);

    // verify
    verify(propertyConverter1).removeFrom(edgeMock);
  }

  @Test(expected = NoSuchFieldException.class)
  public void removePropertyByFieldNameThrowsANoSuchFieldException() {
    // action
    instance.removePropertyByFieldName(edgeMock, NON_EXISTING_FIELD_NAME);
  }

  @Test
  public void updateModifiedAndRevLetTheFieldConvertersSetTheValuesForRevisionAndModified() throws Exception {
    // action
    instance.updateModifiedAndRev(edgeMock, entity);

    // verify
    verify(modifiedConverterMock).setPropertyOfElement(edgeMock, entity);
    verify(revConverterMock).setPropertyOfElement(edgeMock, entity);
    verify(propertyConverter1, times(0)).setPropertyOfElement(edgeMock, entity);
    verify(propertyConverter2, times(0)).setPropertyOfElement(edgeMock, entity);
  }

  @Test(expected = ConversionException.class)
  public void updateModifiedAndRevThrowsAConversionExceptionIfOneOfTheFieldsCannotBeConverted() throws Exception {
    // setup
    doThrow(ConversionException.class).when(modifiedConverterMock).setPropertyOfElement(edgeMock, entity);

    // action
    instance.updateModifiedAndRev(edgeMock, entity);
  }

  @Test
  public void updateVertexSetsTheValuesOfTheNonAdministrativeFields() throws Exception {

    // action
    instance.updateElement(edgeMock, entity);

    // verify
    verify(propertyConverter1).setPropertyOfElement(edgeMock, entity);
    verify(propertyConverter2).setPropertyOfElement(edgeMock, entity);

    verify(modifiedConverterMock, times(0)).setPropertyOfElement(edgeMock, entity);
    verify(revConverterMock, times(0)).setPropertyOfElement(edgeMock, entity);
  }

  @Test(expected = ConversionException.class)
  public void updateVertexThrowsAConversionExceptionWhenAFieldConverterThrowsOne() throws Exception {
    // setup
    doThrow(ConversionException.class).when(propertyConverter1).setPropertyOfElement(edgeMock, entity);

    // action
    instance.updateElement(edgeMock, entity);
  }
}
