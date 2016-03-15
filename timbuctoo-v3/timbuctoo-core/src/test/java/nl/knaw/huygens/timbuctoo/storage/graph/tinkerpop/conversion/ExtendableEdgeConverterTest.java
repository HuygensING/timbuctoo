package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import org.junit.Before;
import org.junit.Test;
import test.model.BaseDomainEntity;
import test.model.projecta.ProjectAPerson;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_MOD_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_REV_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
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

public class ExtendableEdgeConverterTest extends AbstractElementConverterTest {

  private static final String NON_EXISTING_FIELD_NAME = "nonExistingFieldName";
  private static final Class<Relation> BASE_DOMAIN_ENTITY_TYPE = Relation.class;
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
  public static final String SOURCE_ID = "sourceId";
  public static final String SOURCE_TYPE = TypeNames.getInternalName(BaseDomainEntity.class);
  public static final String TARGET_ID = "targetId";
  public static final String TARGET_TYPE = TypeNames.getInternalName(Person.class);

  @Before
  public void setup() throws Exception {
    typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init(getPackageName(SubADomainEntity.class) + " " + getPackageName(BaseDomainEntity.class) + " " + getPackageName(Person.class));

    propertyConverter1 = createPropertyConverter(PROPERTY1_NAME, FIELD1_NAME, FieldType.REGULAR);
    propertyConverter2 = createPropertyConverter(PROPERTY2_NAME, FIELD2_NAME, FieldType.REGULAR);
    modifiedConverterMock = createPropertyConverter(DB_MOD_PROP_NAME, MODIFIED_PROPERTY_NAME, FieldType.ADMINISTRATIVE);
    revConverterMock = createPropertyConverter(DB_REV_PROP_NAME, REVISION_PROPERTY_NAME, FieldType.ADMINISTRATIVE);
    propertyConverters = Lists.newArrayList(propertyConverter1, propertyConverter2, modifiedConverterMock, revConverterMock);

    entityInstantiatorMock = mock(EntityInstantiator.class);

    instance = createInstance(DOMAIN_ENTITY_TYPE, propertyConverters, entityInstantiatorMock);

    setupEdgeMock();
    entity = new SubARelation();
    setupEntityInstantiator();
  }

  private void setupEdgeMock() {
    Vertex source = aVertex()//
      .withId(SOURCE_ID)//
      .withType(BaseDomainEntity.class)//
      .withType(SubADomainEntity.class)//
      .build();

    Vertex target = aVertex()//
      .withId(TARGET_ID)//
      .withType(Person.class)//
      .withType(ProjectAPerson.class) //
      .build();

    edgeMock = anEdge().withSource(source).withTarget(target).build();
  }

  private void setupEntityInstantiator() throws InstantiationException {
    when(entityInstantiatorMock.createInstanceOf(DOMAIN_ENTITY_TYPE)).thenReturn(entity);
  }

  private String getPackageName(Class<? extends DomainEntity> type) {
    return type.getPackage().getName();
  }

  private <T extends Relation> ExtendableEdgeConverter<T> createInstance(Class<T> type, List<PropertyConverter> propertyConverters, EntityInstantiator entityInstantiator) {
    return new ExtendableEdgeConverter<T>(type, propertyConverters, entityInstantiator, typeRegistry);
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
    ExtendableEdgeConverter<Relation> instance = createInstance(BASE_DOMAIN_ENTITY_TYPE, propertyConverters, entityInstantiatorMock);

    // action
    instance.addValuesToElement(edgeMock, entity);

    verifyTypeIsSet(edgeMock, BASE_DOMAIN_ENTITY_TYPE);
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
    // action
    SubARelation createdEntity = instance.convertToEntity(edgeMock);

    // verify
    assertThat(createdEntity, likeRelation() //
      .withSourceId(SOURCE_ID) //
      .withSourceType(SOURCE_TYPE) //
      .withTargetId(TARGET_ID) //
      .withTargetType(TARGET_TYPE));

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
    doThrow(ConversionException.class).when(propertyConverter1).addValueToEntity(entity, edgeMock);

    // action
    instance.convertToEntity(edgeMock);
  }

  @Test
  public void convertToSubTypeCreatesAnInstanceOfTheUsedTypeAndAddsThePropertyValuesOfTheTypeOfTheNodeConverter() throws Exception {
    // setup
    ExtendableEdgeConverter<Relation> instance = createInstance(BASE_DOMAIN_ENTITY_TYPE, propertyConverters, entityInstantiatorMock);

    // action
    SubARelation actualEntity = instance.convertToSubType(DOMAIN_ENTITY_TYPE, edgeMock);

    // verify
    assertThat(actualEntity, likeRelation() //
      .withSourceId(SOURCE_ID) //
      .withSourceType(SOURCE_TYPE) //
      .withTargetId(TARGET_ID) //
      .withTargetType(TARGET_TYPE));

    verify(propertyConverter1).addValueToEntity(entity, edgeMock);
    verify(propertyConverter2).addValueToEntity(entity, edgeMock);
  }


  @Test(expected = ConversionException.class)
  public void convertToSubTypeThrowsAConversionExceptionExceptionWhenTheTypeCannotBeInstantiated() throws Exception {
    // setup

    when(entityInstantiatorMock.createInstanceOf(DOMAIN_ENTITY_TYPE)).thenThrow(new InstantiationException());
    ExtendableEdgeConverter<Relation> instance = createInstance(BASE_DOMAIN_ENTITY_TYPE, propertyConverters, entityInstantiatorMock);

    // action
    instance.convertToSubType(DOMAIN_ENTITY_TYPE, edgeMock);
  }

  @Test(expected = ConversionException.class)
  public void convertToSubTypeThrowsAConverterExceptionWhenOneOfTheFieldsCannotBeConverted() throws Exception {
    // setup
    doThrow(ConversionException.class).when(propertyConverter1).addValueToEntity(entity, edgeMock);

    ExtendableEdgeConverter<Relation> instance = createInstance(BASE_DOMAIN_ENTITY_TYPE, propertyConverters, entityInstantiatorMock);

    // action
    SubARelation actualEntity = instance.convertToSubType(DOMAIN_ENTITY_TYPE, edgeMock);
  }

  @Test
  public void getPropertyNameReturnsTheNameOfTheFoundPropertyConverter() {
    // setup
    when(propertyConverter1.completePropertyName()).thenReturn(PROPERTY1_NAME);

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

  @Test
  public void updateElementAddsEntityTypeIfItIsNotAddedYet() throws Exception {
    // setup
    String initialTypes = getTypesAsString(Lists.newArrayList(getInternalName(BASE_DOMAIN_ENTITY_TYPE)));
    when(edgeMock.getProperty(ELEMENT_TYPES)).thenReturn(initialTypes);

    // action
    instance.addValuesToElement(edgeMock, entity);

    // verify
    verifyTypeIsSet(edgeMock, BASE_DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_TYPE);
  }

  @Test
  public void updateElementDoesNotAddTheElementTypeWhenItIsAlreadyInElementTypes() throws Exception {
    // setup
    String initialTypes = getTypesAsString(Lists.newArrayList(getInternalName(BASE_DOMAIN_ENTITY_TYPE), getInternalName(DOMAIN_ENTITY_TYPE)));
    when(edgeMock.getProperty(ELEMENT_TYPES)).thenReturn(initialTypes);

    // action
    instance.addValuesToElement(edgeMock, entity);

    // verify
    verifyTypeIsSet(edgeMock, BASE_DOMAIN_ENTITY_TYPE, DOMAIN_ENTITY_TYPE);
  }

  @Test(expected = ConversionException.class)
  public void updateVertexThrowsAConversionExceptionWhenAFieldConverterThrowsOne() throws Exception {
    // setup
    doThrow(ConversionException.class).when(propertyConverter1).setPropertyOfElement(edgeMock, entity);

    // action
    instance.updateElement(edgeMock, entity);
  }
}
