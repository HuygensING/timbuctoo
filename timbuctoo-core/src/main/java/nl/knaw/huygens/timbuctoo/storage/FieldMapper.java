package nl.knaw.huygens.timbuctoo.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNameGenerator;

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

  /** Returns the name of a key from its parts. */
  public static String propertyName(String prefix, String field) {
    checkArgument(field != null && field.length() != 0);

    StringBuilder builder = new StringBuilder();
    if (Character.isLetter(field.charAt(0))) {
      builder.append(prefix).append(SEPARATOR_CHAR);
    }
    builder.append(field);
    return builder.toString();
  }

  /** Returns the name of a key from its parts. */
  public static String propertyName(Class<?> type, String field) {
    return propertyName(TypeNameGenerator.getInternalName(type), field);
  }

  private static final String GET_ACCESSOR = "get";
  private static final String IS_ACCESSOR = "is"; // get accesor for booleans.

  /**
   * Generates a field map with for the {@code type} with object fields as keys and database fields as values.
   * @param type the type to create the map for.
   * @return the field map.
   */
  public Map<String, String> getFieldMap(Class<?> type) {
    Map<String, String> map = Maps.newHashMap();
    for (Field field : type.getDeclaredFields()) {
      if (isProperty(field)) {
        map.put(field.getName(), getFieldName(type, field));
      }
    }
    return map;
  }

  /**
   * Indicates whether a field qualifies as property.
   */
  public boolean isProperty(Field field) {
    int modifiers = field.getModifiers();
    return !Modifier.isStatic(modifiers);
  }

  /**
   * Checks if the there is variation possible for this field.
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
   * Gets a field name for a field in combination with a class. 
   * The method will not check if the {@code type} contains the field. It will just create a {@code String). 
   * @param type the type needed for the prefix.
   * @param field the field to get the name for.
   * @return the field name.
   */
  public String getFieldName(Class<?> type, Field field) {
    JsonProperty annotation = field.getAnnotation(JsonProperty.class);
    if (annotation != null) {
      return propertyName(type, annotation.value());
    }

    Method method = getMethodOfField(type, field);
    if (method != null && method.getAnnotation(JsonProperty.class) != null) {
      return propertyName(type, method.getAnnotation(JsonProperty.class).value());
    }

    return propertyName(type, field.getName());
  }

  private Method getMethodOfField(Class<?> type, Field field) {
    String methodName = getMethodName(field);
    for (Method method : type.getMethods()) {
      if (method.getName().equals(methodName)) {
        return method;
      }
    }
    return null;
  }

  private String getMethodName(Field field) {
    char[] fieldNameChars = field.getName().toCharArray();

    fieldNameChars[0] = Character.toUpperCase(fieldNameChars[0]);

    if (field.getType() == boolean.class || field.getType() == Boolean.class) {
      return IS_ACCESSOR.concat(String.valueOf(fieldNameChars));
    }

    return GET_ACCESSOR.concat(String.valueOf(fieldNameChars));
  }

}
