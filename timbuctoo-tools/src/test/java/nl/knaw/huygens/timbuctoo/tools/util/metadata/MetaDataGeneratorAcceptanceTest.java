package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;

import java.util.ArrayList;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MetaDataGeneratorAcceptanceTest {
  private MetaDataGenerator instance;

  @Before
  public void setUp() {
    instance = new MetaDataGenerator(new FieldMetaDataGeneratorFactory(new TypeNameGenerator()));
  }

  @Test
  public void testGenerateSimpleObject() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testInt", createMapForSimpleField("int"));
    expectedMap.put("testString", createMapForSimpleField("String"));
    expectedMap.put("testDouble", createMapForSimpleField("double"));
    expectedMap.put("testLong", createMapForSimpleField("Long"));
    expectedMap.put("nestedObject", createMapForSimpleField("TestModel"));

    testGenerate(expectedMap, MetaDataGeneratorTestData.TestModel.class);
  }

  private void testGenerate(Map<String, Object> expectedMap, Class<?> type) throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> actualMap = instance.generate(type);

    assertThat(actualMap.entrySet(), everyItem(isIn(expectedMap.entrySet())));
    assertThat(expectedMap.entrySet(), everyItem(isIn(actualMap.entrySet())));
  }

  @Test
  public void testGenerateOneInheritanceLayer() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testInt", createMapForSimpleField("int"));
    expectedMap.put("testString", createMapForSimpleField("String"));
    expectedMap.put("testDouble", createMapForSimpleField("double"));
    expectedMap.put("testLong", createMapForSimpleField("Long"));
    expectedMap.put("nestedObject", createMapForSimpleField("TestModel"));
    expectedMap.put("test", createMapForSimpleField("String"));

    testGenerate(expectedMap, MetaDataGeneratorTestData.OtherModel.class);

  }

  @Test
  public void testGenerateMultipleInheritanceLayers() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testInt", createMapForSimpleField("int"));
    expectedMap.put("testString", createMapForSimpleField("String"));
    expectedMap.put("testDouble", createMapForSimpleField("double"));
    expectedMap.put("testLong", createMapForSimpleField("Long"));
    expectedMap.put("nestedObject", createMapForSimpleField("TestModel"));
    expectedMap.put("test", createMapForSimpleField("String"));
    expectedMap.put("subOtherInt", createMapForSimpleField("Integer"));
    expectedMap.put("subSubOtherInt", createMapForSimpleField("int"));

    testGenerate(expectedMap, MetaDataGeneratorTestData.SubSubOtherModel.class);
  }

  private Map<String, Object> createMapForSimpleField(String typeName) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("type", typeName);
    return map;
  }

  @Test
  public void testGenerateInheritenceSubTypeWithoutDeclaredFields() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testInt", createMapForSimpleField("int"));
    expectedMap.put("testString", createMapForSimpleField("String"));
    expectedMap.put("testDouble", createMapForSimpleField("double"));
    expectedMap.put("testLong", createMapForSimpleField("Long"));
    expectedMap.put("nestedObject", createMapForSimpleField("TestModel"));

    testGenerate(expectedMap, MetaDataGeneratorTestData.SubTypeWithoutDeclaredFields.class);
  }

  @Test
  public void testGenerateInterface() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();

    testGenerate(expectedMap, MetaDataGeneratorTestData.TestInterface.class);
  }

  @Test
  public void testGenerateClassWithStaticFields() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("test", createMapForSimpleField("String"));

    testGenerate(expectedMap, MetaDataGeneratorTestData.TypeWithStaticFields.class);
  }

  @Test
  public void testGenerateClassWithGenericFields() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testList", createMapForSimpleField("List of (String)"));
    expectedMap.put("testMap", createMapForSimpleField("Map of (String, String)"));
    expectedMap.put("testNestedGenerics", createMapForSimpleField("List of (List of (String))"));

    testGenerate(expectedMap, MetaDataGeneratorTestData.TypeWithGenericFields.class);
  }

  @Test
  public void testGenerateClassWithAnnotatedFields() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("@annotated", createMapForSimpleField("String"));

    testGenerate(expectedMap, MetaDataGeneratorTestData.TypeWithAnnotatedField.class);
  }

  @Test
  public void testGenerateClassWithFieldWithAnnotatedGetter() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("@annotated", createMapForSimpleField("String"));

    testGenerate(expectedMap, MetaDataGeneratorTestData.TypeWithAnnotatedGetter.class);
  }

  @Test
  public void testGenerateClassWithFieldWithAnnotatedSetter() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("annotatedString", createMapForSimpleField("String"));

    testGenerate(expectedMap, MetaDataGeneratorTestData.TypeWithAnnotatedSetter.class);
  }

  @Test
  public void testGenerateClassWithConstants() throws IllegalArgumentException, IllegalAccessException {

    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("TEST_STRING", createConstantFieldMap("String", "TEST"));
    expectedMap.put("TEST_INT", createConstantFieldMap("int", 1234));

    testGenerate(expectedMap, MetaDataGeneratorTestData.ClassWithConstants.class);

  }

  private Map<String, Object> createConstantFieldMap(String type, Object value) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("type", type);
    map.put("value", value);

    return map;
  }

  @Test
  public void testGenerateClassWithEnumValues() throws IllegalArgumentException, IllegalAccessException {

    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("test", createEnumValueMap("TestEnum", Lists.newArrayList("TEST1", "TEST2")));

    testGenerate(expectedMap, MetaDataGeneratorTestData.ClassWithEnumValues.class);
  }

  @Test
  public void testGenerateClassWithListOfEnumValues() throws IllegalArgumentException, IllegalAccessException {

    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("test", createEnumValueMap("List of (TestEnum)", Lists.newArrayList("TEST1", "TEST2")));

    testGenerate(expectedMap, MetaDataGeneratorTestData.ClassWithListOfEnumValues.class);
  }

  private Map<String, Object> createEnumValueMap(String typeName, ArrayList<String> values) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("type", typeName);
    map.put("value", values);

    return map;
  }

  @Test
  public void testGenerateClassWithPoorMansEnumValues() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("poorMansEnum", createEnumValueMap("String", Lists.newArrayList("TEST1", "TEST2")));

    testGenerate(expectedMap, MetaDataGeneratorTestData.ClassWithPoorMansEnum.class);
  }

  @Test
  public void testGenerateClassWithListWithPoorMansEnumValues() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("poorMansEnum", createEnumValueMap("List of (String)", Lists.newArrayList("TEST1", "TEST2")));

    testGenerate(expectedMap, MetaDataGeneratorTestData.ClassWithPoorMansEnumList.class);
  }

  @Ignore
  @Test
  public void testGenerateClassWithPropertyWithTypeOfInnerClass() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testClass", createMapForSimpleField("ClassWithTypeOfInnterClass.InnerClass"));

    testGenerate(expectedMap, MetaDataGeneratorTestData.ClassWithTypeOfInnterClass.class);
  }

}
