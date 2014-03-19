package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetaDataGeneratorTestData {
  static class TestModel {
    protected int testInt;
    private String testString;
    public double testDouble;
    Long testLong;
    private TestModel nestedObject;
  }

  static class OtherModel extends TestModel {
    private String test;
  }

  static class SubOtherModel extends OtherModel {
    private Integer subOtherInt;
  }

  static class SubSubOtherModel extends SubOtherModel {
    private int subSubOtherInt;
  }

  static class SubTypeWithoutDeclaredFields extends TestModel {

  }

  static interface TestInterface {

  }

  static class TypeWithStaticFields {
    private static String staticTest;
    private String test;
  }

  static class TypeWithGenericFields {
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

  static class ClassWithConstants {
    private static final String TEST_STRING = "TEST";
    public static final int TEST_INT = 1234;
  }

  static class ClassWithEnumValues {
    private enum TestEnum {
      TEST1, TEST2
    };

    private TestEnum test;
  }

  static class ClassWithListOfEnumValues {
    private enum TestEnum {
      TEST1, TEST2
    };

    private List<TestEnum> test;
  }

  static class ClassWithPoorMansEnum {
    static class PoorMansEnum {
      public static final String VALUE1 = "TEST1";
      public static final String VALUE2 = "TEST2";
    }

    private String poorMansEnum;
  }

  static class ClassWithPoorMansEnumList {
    static class PoorMansEnum {
      public static final String VALUE1 = "TEST1";
      public static final String VALUE2 = "TEST2";
    }

    private List<String> poorMansEnum;
  }

  public static class ClassWithTypeOfInnerClass {
    public static class InnerClass {
      private String test;
    }

    private InnerClass testClass;
  }
}
