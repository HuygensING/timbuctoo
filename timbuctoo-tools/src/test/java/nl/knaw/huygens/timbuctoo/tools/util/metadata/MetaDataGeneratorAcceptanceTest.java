package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MetaDataGeneratorAcceptanceTest {
  private MetaDataGenerator instance;

  @Before
  public void setUp() {
    instance = new MetaDataGenerator(new FieldMetaDataGeneratorFactory(new TypeNameGenerator(), new FieldMapper()));
  }

  @Test
  public void testGenerateSimpleObject() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testInt", createMapForSimpleField("int"));
    expectedMap.put("testString", createMapForSimpleField("String"));
    expectedMap.put("testDouble", createMapForSimpleField("double"));
    expectedMap.put("testLong", createMapForSimpleField("Long"));
    expectedMap.put("nestedObject", createMapForSimpleField("TestModel"));

    testGenerate(expectedMap, TestModel.class);
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

    testGenerate(expectedMap, OtherModel.class);

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

    testGenerate(expectedMap, SubSubOtherModel.class);
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

    testGenerate(expectedMap, SubTypeWithoutDeclaredFields.class);
  }

  @Test
  public void testGenerateInterface() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();

    testGenerate(expectedMap, TestInterface.class);
  }

  @Test
  public void testGenerateClassWithStaticFields() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("test", createMapForSimpleField("String"));

    testGenerate(expectedMap, TypeWithStaticFields.class);
  }

  @Test
  public void testGenerateClassWithGenericFields() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testList", createMapForSimpleField("List of (String)"));
    expectedMap.put("testMap", createMapForSimpleField("Map of (String, String)"));
    expectedMap.put("testNestedGenerics", createMapForSimpleField("List of (List of (String))"));

    testGenerate(expectedMap, TypeWithGenericFields.class);
  }

  @Test
  public void testGenerateClassWithAnnotatedFields() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("@annotated", createMapForSimpleField("String"));

    testGenerate(expectedMap, TypeWithAnnotatedField.class);
  }

  @Test
  public void testGenerateClassWithFieldWithAnnotatedGetter() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("@annotated", createMapForSimpleField("String"));

    testGenerate(expectedMap, TypeWithAnnotatedGetter.class);
  }

  @Test
  public void testGenerateClassWithFieldWithAnnotatedSetter() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("annotatedString", createMapForSimpleField("String"));

    testGenerate(expectedMap, TypeWithAnnotatedSetter.class);
  }

  @Test
  public void testGenerateClassWithConstants() throws IllegalArgumentException, IllegalAccessException {

    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("TEST_STRING", createConstantFieldMap("String", "TEST"));
    expectedMap.put("TEST_INT", createConstantFieldMap("int", 1234));

    testGenerate(expectedMap, ClassWithConstants.class);

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

    testGenerate(expectedMap, ClassWithEnumValues.class);
  }

  @Test
  public void testGenerateClassWithListOfEnumValues() throws IllegalArgumentException, IllegalAccessException {

    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("test", createEnumValueMap("List of (TestEnum)", Lists.newArrayList("TEST1", "TEST2")));

    testGenerate(expectedMap, ClassWithListOfEnumValues.class);
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

    testGenerate(expectedMap, ClassWithPoorMansEnum.class);
  }

  @Test
  public void testGenerateClassWithListWithPoorMansEnumValues() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("poorMansEnum", createEnumValueMap("List of (String)", Lists.newArrayList("TEST1", "TEST2")));

    testGenerate(expectedMap, ClassWithPoorMansEnumList.class);
  }

  private static class TestModel {
    protected int testInt;
    private String testString;
    public double testDouble;
    Long testLong;
    private TestModel nestedObject;
  }

  private static class OtherModel extends TestModel {
    private String test;
  }

  private static class SubOtherModel extends OtherModel {
    private Integer subOtherInt;
  }

  private static class SubSubOtherModel extends SubOtherModel {
    private int subSubOtherInt;
  }

  private static class SubTypeWithoutDeclaredFields extends TestModel {

  }

  private static interface TestInterface {

  }

  private static class TypeWithStaticFields {
    private static String staticTest;
    private String test;
  }

  private static class TypeWithGenericFields {
    private List<String> testList;
    private Map<String, String> testMap;
    private List<List<String>> testNestedGenerics;
  }

  public static class TypeWithAnnotatedField {
    @JsonProperty("@annotated")
    private String annotatedString;
  }

  public static class TypeWithAnnotatedGetter {
    private String annotatedString;

    @JsonProperty("@annotated")
    public String getAnnotatedString() {
      return annotatedString;
    }
  }

  public static class TypeWithAnnotatedSetter {
    private String annotatedString;

    @JsonProperty("@annotated")
    public void setAnnotatedString(String annotatedString) {
      this.annotatedString = annotatedString;
    }
  }

  private static class ClassWithConstants {
    private static final String TEST_STRING = "TEST";
    public static final int TEST_INT = 1234;
  }

  private static class ClassWithEnumValues {
    private enum TestEnum {
      TEST1, TEST2
    };

    private TestEnum test;
  }

  private static class ClassWithListOfEnumValues {
    private enum TestEnum {
      TEST1, TEST2
    };

    private List<TestEnum> test;
  }

  private static class ClassWithPoorMansEnum {
    private static class PoorMansEnum {
      public static final String VALUE1 = "TEST1";
      public static final String VALUE2 = "TEST2";
    }

    private String poorMansEnum;
  }

  private static class ClassWithPoorMansEnumList {
    private static class PoorMansEnum {
      public static final String VALUE1 = "TEST1";
      public static final String VALUE2 = "TEST2";
    }

    private List<String> poorMansEnum;
  }
}
