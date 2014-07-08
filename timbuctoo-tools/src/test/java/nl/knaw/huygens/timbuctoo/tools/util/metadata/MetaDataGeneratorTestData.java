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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetaDataGeneratorTestData {

  @SuppressWarnings("unused")
  static class TestModel {
    protected int testInt;
    private String testString;
    public double testDouble;
    Long testLong;
    private TestModel nestedObject;
  }

  @SuppressWarnings("unused")
  static class OtherModel extends TestModel {
    private String test;
  }

  @SuppressWarnings("unused")
  static class SubOtherModel extends OtherModel {
    private Integer subOtherInt;
  }

  @SuppressWarnings("unused")
  static class SubSubOtherModel extends SubOtherModel {
    private int subSubOtherInt;
  }

  static class SubTypeWithoutDeclaredFields extends TestModel {

  }

  static interface TestInterface {

  }

  @SuppressWarnings("unused")
  static class TypeWithStaticFields {
    private static String staticTest;
    private String test;
  }

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
  public static class TypeWithAnnotatedSetter {
    private String annotatedString;

    @JsonProperty("@annotated")
    public void setAnnotatedString(String annotatedString) {
      this.annotatedString = annotatedString;
    }
  }

  @SuppressWarnings("unused")
  static class ClassWithConstants {
    private static final String TEST_STRING = "TEST";
    public static final int TEST_INT = 1234;
  }

  @SuppressWarnings("unused")
  static class ClassWithEnumValues {
    private enum TestEnum {
      TEST1, TEST2
    };

    private TestEnum test;
  }

  @SuppressWarnings("unused")
  static class ClassWithListOfEnumValues {
    private enum TestEnum {
      TEST1, TEST2
    };

    private List<TestEnum> test;
  }

  @SuppressWarnings("unused")
  static class ClassWithPoorMansEnum {
    static class PoorMansEnum {
      public static final String VALUE1 = "TEST1";
      public static final String VALUE2 = "TEST2";
    }

    private String poorMansEnum;
  }

  @SuppressWarnings("unused")
  static class ClassWithPoorMansEnumList {
    static class PoorMansEnum {
      public static final String VALUE1 = "TEST1";
      public static final String VALUE2 = "TEST2";
    }

    private List<String> poorMansEnum;
  }

  @SuppressWarnings("unused")
  public static class ClassWithTypeOfInnerClass {
    public static class InnerClass {
      private String test;
    }

    private InnerClass testClass;
  }

}
