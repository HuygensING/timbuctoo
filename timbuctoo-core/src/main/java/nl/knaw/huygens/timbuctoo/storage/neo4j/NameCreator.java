package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NameCreator {

  private final PropertyBusinessRules propertyBusinessRules;

  public NameCreator(PropertyBusinessRules propertyBusinessRules) {
    this.propertyBusinessRules = propertyBusinessRules;
  }

  public String propertyName(Class<? extends Entity> containingType, Field field) {
    FieldType fieldType = propertyBusinessRules.getFieldType(containingType, field);

    if (fieldType == FieldType.REGULAR) {
      return String.format("%s:%s", internalTypeName(containingType), getFieldName(containingType, field));
    }

    return getFieldName(containingType, field);
  }

  /**
   * Gets the name of the specified field in the specified class, without a prefix.
   * It uses the name specified in {@code JsonProperty} annotations on the field
   * itself or the getter corresponding to the field (in that order).
   */
  private String getFieldName(Class<?> type, Field field) {
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
  private static Method getMethodByName(Class<?> type, String methodName) {
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

  public String internalTypeName(Class<? extends Entity> type) {
    return TypeNames.getInternalName(type);
  }

}
