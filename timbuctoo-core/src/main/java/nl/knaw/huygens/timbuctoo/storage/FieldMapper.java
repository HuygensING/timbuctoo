package nl.knaw.huygens.timbuctoo.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

/**
 * A class that contains all the information about how the class fields are mapped 
 * to the fields in the database.
 */
public class FieldMapper {

  /** Separator between parts of a property name, as string. */
  public static final String SEPARATOR = ":";

  /** Separator between parts of a key, as character. */
  public static final char SEPARATOR_CHAR = ':';

  /** Returns the name of a property from its parts. */
  public static String propertyName(String prefix, String field) {
    checkArgument(field != null && field.length() != 0);

    StringBuilder builder = new StringBuilder();
    if (Character.isLetter(field.charAt(0))) {
      builder.append(prefix).append(SEPARATOR_CHAR);
    }
    builder.append(field);
    return builder.toString();
  }

  /** Returns the name of a property from its parts. */
  public static String propertyName(Class<?> type, String field) {
    return propertyName(TypeNames.getInternalName(type), field);
  }

  // -------------------------------------------------------------------

  /**
   * Generates a map for {@code type} with object fields as keys and database fields as values.
   */
  public Map<String, String> getFieldMap(Class<?> type) {
    Map<String, String> map = Maps.newHashMap();
    for (Field field : type.getDeclaredFields()) {
      if (isProperty(field)) {
        String mappedName = propertyName(type, getFieldName(type, field));
        if (mappedName.indexOf('@') < 0) { // exclude virtual properties
          map.put(field.getName(), mappedName);
        }
      }
    }
    return map;
  }

  /**
   * Indicates whether a field qualifies as property.
   */
  private boolean isProperty(Field field) {
    return !Modifier.isStatic(field.getModifiers());
  }

  /**
   * Checks if there is variation possible for this field.
   */
  public boolean isFieldWithVariation(String fieldName) {
    return fieldName.contains(SEPARATOR);
  }

  /**
   * A method to retrieve the name of the type the field belongs to.
   * @param fieldName the field to retrieve the type name from.
   * @return the type name or {@code null} if the field does not belong to a type.
   * @throws NullPointerException is thrown if {@code fieldName} is {@code null}.
   */
  public String getTypeNameOfFieldName(String fieldName) {
    checkNotNull(fieldName);
    int pos = fieldName.indexOf(SEPARATOR_CHAR);
    return (pos < 0) ? null : fieldName.substring(0, pos);
  }

  /**
   * Gets the name of the specified field in the specified class, without a prefix.
   * It uses the name specified in {@code JsonProperty} annotations on the field
   * itself or the getter corresponding to the field (in that order).
   */
  public String getFieldName(Class<?> type, Field field) {
    JsonProperty annotation = field.getAnnotation(JsonProperty.class);
    if (annotation != null) {
      return annotation.value();
    }

    Method method = getMethodByName(type, getMethodName(field));
    if (method != null && method.getAnnotation(JsonProperty.class) != null) {
      return method.getAnnotation(JsonProperty.class).value();
    }

    return field.getName();
  }

  /**
   * Searches for a public method in the specified class or its superclasses
   * and -interfaces that matches the specified name and has no parameters.
   */
  private Method getMethodByName(Class<?> type, String methodName) {
    try {
      // TODO decide: use type.getDeclaredMethod(methodName)?
      return type.getMethod(methodName);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private static final String GET_ACCESSOR = "get";
  private static final String IS_ACCESSOR = "is"; // get accesor for booleans.

  private String getMethodName(Field field) {
    char[] fieldNameChars = field.getName().toCharArray();

    fieldNameChars[0] = Character.toUpperCase(fieldNameChars[0]);

    String accessor = isBoolean(field.getType()) ? IS_ACCESSOR : GET_ACCESSOR;
    return accessor.concat(String.valueOf(fieldNameChars));
  }

  private boolean isBoolean(Class<?> cls) {
    return cls == boolean.class || cls == Boolean.class;
  }

}
