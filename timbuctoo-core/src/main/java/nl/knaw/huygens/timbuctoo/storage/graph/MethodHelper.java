package nl.knaw.huygens.timbuctoo.storage.graph;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MethodHelper {
  static final String GET_ACCESSOR = "get";
  static final String IS_ACCESSOR = "is"; // get accesor for booleans.


  /**
   * Searches for a public method in the specified class or its superclasses
   * and -interfaces that matches the specified name and has no parameters.
   */
  public static Method getMethodByName(Class<?> type, String methodName) {
    try {
      // TODO decide: use type.getDeclaredMethod(methodName)?
      return type.getMethod(methodName);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  public static String getGetterName(Field field) {
    char[] fieldNameChars = field.getName().toCharArray();

    fieldNameChars[0] = Character.toUpperCase(fieldNameChars[0]);

    String accessor = isBoolean(field.getType()) ? IS_ACCESSOR : GET_ACCESSOR;
    return accessor.concat(String.valueOf(fieldNameChars));
  }

  private static boolean isBoolean(Class<?> cls) {
    return cls == boolean.class || cls == Boolean.class;
  }
}
