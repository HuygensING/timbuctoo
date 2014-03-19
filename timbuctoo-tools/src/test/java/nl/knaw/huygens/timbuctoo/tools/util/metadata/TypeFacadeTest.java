package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;
import nl.knaw.huygens.timbuctoo.tools.util.metadata.TypeFacade.FieldType;

import org.junit.Test;

public class TypeFacadeTest {

  private void testGetFieldTypeForFieldOfClass(Class<?> type, Field field, FieldType expectedType) {
    TypeFacade instance = new TypeFacade(type);

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

    TypeFacade instance = new TypeFacade(classWithSimpleField, fieldMapperMock);

    // action
    instance.getFieldName(simpleField);

    // verify
    verify(fieldMapperMock).getFieldName(classWithSimpleField, simpleField);

  }

}
