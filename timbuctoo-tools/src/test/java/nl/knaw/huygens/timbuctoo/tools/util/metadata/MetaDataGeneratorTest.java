package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;
import nl.knaw.huygens.timbuctoo.tools.util.metadata.MetaDataGenerator;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MetaDataGeneratorTest {
  private MetaDataGenerator instance;

  @Before
  public void setUp() {
    instance = new MetaDataGenerator(new FieldMapper());
  }

  @Test
  public void testGenerateSimpleObject() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testInt", "int");
    expectedMap.put("testString", "String");
    expectedMap.put("testDouble", "double");
    expectedMap.put("testLong", "Long");
    expectedMap.put("nestedObject", "TestModel");

    testGenerate(expectedMap, TestModel.class);
  }

  private void testGenerate(Map<String, Object> expectedMap, Class<?> type) throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> actualMap = instance.generate(type);

    assertThat(actualMap, is(expectedMap));
  }

  @Test
  public void testGenerateOneInheritanceLayer() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testInt", "int");
    expectedMap.put("testString", "String");
    expectedMap.put("testDouble", "double");
    expectedMap.put("testLong", "Long");
    expectedMap.put("nestedObject", "TestModel");
    expectedMap.put("test", "String");

    testGenerate(expectedMap, OtherModel.class);

  }

  @Test
  public void testGenerateMultipleInheritanceLayers() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testInt", "int");
    expectedMap.put("testString", "String");
    expectedMap.put("testDouble", "double");
    expectedMap.put("testLong", "Long");
    expectedMap.put("nestedObject", "TestModel");
    expectedMap.put("test", "String");
    expectedMap.put("subOtherInt", "Integer");
    expectedMap.put("subSubOtherInt", "int");

    testGenerate(expectedMap, SubSubOtherModel.class);
  }

  @Test
  public void testGenerateInheritenceSubTypeWithoutDeclaredFields() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testInt", "int");
    expectedMap.put("testString", "String");
    expectedMap.put("testDouble", "double");
    expectedMap.put("testLong", "Long");
    expectedMap.put("nestedObject", "TestModel");

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
    expectedMap.put("test", "String");

    testGenerate(expectedMap, TypeWithStaticFields.class);
  }

  @Test
  public void testGenerateClassWithGenericFields() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("testList", "List of (String)");
    expectedMap.put("testMap", "Map of (String, String)");
    expectedMap.put("testNestedGenerics", "List of (List of (String))");

    testGenerate(expectedMap, TypeWithGenericFields.class);
  }

  @Test
  public void testGenerateClassWithAnnotatedFields() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("@annotated", "String");

    testGenerate(expectedMap, TypeWithAnnotatedField.class);
  }

  @Test
  public void testGenerateClassWithFieldWithAnnotatedGetter() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("@annotated", "String");

    testGenerate(expectedMap, TypeWithAnnotatedGetter.class);
  }

  @Test
  public void testGenerateClassWithFieldWithAnnotatedSetter() throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("annotatedString", "String");

    testGenerate(expectedMap, TypeWithAnnotatedSetter.class);
  }

  @Test
  public void testGenerateClassWithConstants() throws IllegalArgumentException, IllegalAccessException {

    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("TEST_STRING", "String <TEST>");
    expectedMap.put("TEST_INT", "int <1234>");

    testGenerate(expectedMap, ClassWithConstants.class);

  }

  @Test
  public void testGenerateClassWithEnumValues() throws IllegalArgumentException, IllegalAccessException {

    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("value", Lists.newArrayList("TEST1", "TEST2"));

    testGenerate(expectedMap, ClassWithEnumValues.class);

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

    private TestEnum value;
  }
}
