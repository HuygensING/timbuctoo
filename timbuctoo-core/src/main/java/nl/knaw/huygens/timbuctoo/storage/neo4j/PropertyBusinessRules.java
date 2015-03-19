package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.FieldType.ADMINISTRATIVE;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.FieldType.REGULAR;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.FieldType.VIRTUAL;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import nl.knaw.huygens.timbuctoo.annotations.DBIgnore;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PropertyBusinessRules {

  private boolean isAdministrativeProperty(Class<? extends Entity> containingType, Field field) {
    String fieldName = getFieldName(containingType, field);
    return StringUtils.startsWithAny(fieldName, "_", "^");
  }

  private boolean isVirtualProperty(Class<? extends Entity> containingType, Field field) {
    String fieldName = getFieldName(containingType, field);
    return field.isAnnotationPresent(DBIgnore.class) || StringUtils.startsWith(fieldName, "@");
  }

  public FieldType getFieldType(Class<? extends Entity> containingType, Field field) {
    if (isVirtualProperty(containingType, field)) {
      return VIRTUAL;
    }

    if (isAdministrativeProperty(containingType, field)) {
      return ADMINISTRATIVE;
    }

    return REGULAR;
  }

  public String getFieldName(Class<? extends Entity> containingType, Field field) {
    JsonProperty annotation = field.getAnnotation(JsonProperty.class);
    if (annotation != null) {
      return annotation.value();
    }

    Method method = getMethodByName(containingType, getMethodName(field));
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

}
