package nl.knaw.huygens.timbuctoo.tools.util.metadata;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import static nl.knaw.huygens.timbuctoo.tools.util.metadata.TypeFacadeBuilder.aTypeFacade;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;
import nl.knaw.huygens.timbuctoo.tools.util.metadata.TypeFacade.FieldType;

import org.junit.Test;

public class TypeFacadeTest {

  private void testGetFieldTypeForFieldOfClass(Class<?> type, Field field, FieldType expectedType) {
    TypeFacade instance = aTypeFacade(type).build();

    // action
    FieldType actualFieldType = instance.getFieldType(field);

    // verify
    assertThat(actualFieldType, equalTo(expectedType));
  }

  @Test
  public void testGetFieldTypeForEnumField() throws SecurityException, NoSuchFieldException {
    // setup
    Class<?> classWithEnum = MetaDataGeneratorTestData.ClassWithEnumValues.class;
    Field enumValueField = classWithEnum.getDeclaredField("test");

    testGetFieldTypeForFieldOfClass(classWithEnum, enumValueField, FieldType.ENUM);
  }

  @Test
  public void testGetFieldTypeForEnumList() throws SecurityException, NoSuchFieldException {
    // setup
    Class<?> classWithEnum = MetaDataGeneratorTestData.ClassWithListOfEnumValues.class;
    Field enumListField = classWithEnum.getDeclaredField("test");

    testGetFieldTypeForFieldOfClass(classWithEnum, enumListField, FieldType.ENUM);
  }

  @Test
  public void testGetFieldTypeForConstantField() throws SecurityException, NoSuchFieldException {
    // setup
    Class<?> classWithConstant = MetaDataGeneratorTestData.ClassWithConstants.class;
    Field constantField = classWithConstant.getDeclaredField("TEST_STRING");

    testGetFieldTypeForFieldOfClass(classWithConstant, constantField, FieldType.CONSTANT);
  }

  @Test
  public void testGetFieldTypeForPoorMansEnumField() throws SecurityException, NoSuchFieldException {
    Class<?> classWithPoorMansEnum = MetaDataGeneratorTestData.ClassWithPoorMansEnum.class;
    Field poorMansEnumField = classWithPoorMansEnum.getDeclaredField("poorMansEnum");

    testGetFieldTypeForFieldOfClass(classWithPoorMansEnum, poorMansEnumField, FieldType.POOR_MANS_ENUM);
  }

  @Test
  public void testGetFieldTypeForPoorMansEnumList() throws SecurityException, NoSuchFieldException {
    Class<?> classWithPoorMansEnumList = MetaDataGeneratorTestData.ClassWithPoorMansEnumList.class;
    Field poorMansEnumListField = classWithPoorMansEnumList.getDeclaredField("poorMansEnum");

    testGetFieldTypeForFieldOfClass(classWithPoorMansEnumList, poorMansEnumListField, FieldType.POOR_MANS_ENUM);
  }

  @Test
  public void testGetFieldTypeForSimpleField() throws SecurityException, NoSuchFieldException {
    Class<?> classWithSimpleField = MetaDataGeneratorTestData.TestModel.class;
    Field simpleField = classWithSimpleField.getDeclaredField("testString");

    testGetFieldTypeForFieldOfClass(classWithSimpleField, simpleField, FieldType.DEFAULT);
  }

  @Test
  public void testGetFieldTypeForSimpleList() throws SecurityException, NoSuchFieldException {
    Class<?> classWithSimpleList = MetaDataGeneratorTestData.TypeWithGenericFields.class;
    Field simpleList = classWithSimpleList.getDeclaredField("testList");

    testGetFieldTypeForFieldOfClass(classWithSimpleList, simpleList, FieldType.DEFAULT);
  }

  @Test
  public void testGetFieldTypeForUnknownField() throws SecurityException, NoSuchFieldException {
    Class<?> classWithStaticField = MetaDataGeneratorTestData.TypeWithStaticFields.class;
    Field staticField = classWithStaticField.getDeclaredField("staticTest");

    testGetFieldTypeForFieldOfClass(classWithStaticField, staticField, FieldType.UNKNOWN);
  }

  @Test
  public void testGetFieldName() throws SecurityException, NoSuchFieldException {
    // setup
    Class<?> classWithSimpleField = MetaDataGeneratorTestData.TestModel.class;
    Field simpleField = classWithSimpleField.getDeclaredField("testString");

    FieldMapper fieldMapperMock = mock(FieldMapper.class);

    TypeFacade instance = aTypeFacade(classWithSimpleField).withFieldMapper(fieldMapperMock).build();

    // action
    instance.getFieldName(simpleField);

    // verify
    verify(fieldMapperMock).getFieldName(classWithSimpleField, simpleField);

  }

  @Test
  public void testGetTypeName() throws SecurityException, NoSuchFieldException {
    // setup
    Class<?> classWithSimpleField = MetaDataGeneratorTestData.TestModel.class;
    Field simpleField = classWithSimpleField.getDeclaredField("testString");
    TypeNameGenerator typeNameGeneratorMock = mock(TypeNameGenerator.class);

    TypeFacade instance = aTypeFacade(classWithSimpleField).withTypeNameGenerator(typeNameGeneratorMock).build();

    // when
    String expectedTypeName = "String";
    when(typeNameGeneratorMock.getTypeName(simpleField)).thenReturn(expectedTypeName);

    // action
    String actualTypeName = instance.getTypeNameOfField(simpleField);

    verify(typeNameGeneratorMock).getTypeName(simpleField);
    assertThat(actualTypeName, equalTo(expectedTypeName));
  }

  @Test
  public void testGetTypeNameForInnerClass() throws SecurityException, NoSuchFieldException {
    // setup
    Class<?> classWithTypeOfInnterClass = MetaDataGeneratorTestData.ClassWithTypeOfInnerClass.class;
    Field innerClassTypeField = classWithTypeOfInnterClass.getDeclaredField("testClass");
    TypeNameGenerator typeNameGeneratorMock = mock(TypeNameGenerator.class);

    TypeFacade instance = aTypeFacade(classWithTypeOfInnterClass).withTypeNameGenerator(typeNameGeneratorMock).build();

    // when
    when(typeNameGeneratorMock.getTypeName(innerClassTypeField)).thenReturn("InnerType");

    // action
    String actualTypeName = instance.getTypeNameOfField(innerClassTypeField);

    verify(typeNameGeneratorMock).getTypeName(innerClassTypeField);
    assertThat(actualTypeName, equalTo("ClassWithTypeOfInnerClass.InnerType"));
  }
}
