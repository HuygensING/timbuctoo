package nl.knaw.huygens.timbuctoo.storage.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.annotations.DBProperty;
import nl.knaw.huygens.timbuctoo.model.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.REGULAR;
import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.VIRTUAL;

public class PropertyBusinessRules {

  private boolean isVirtualProperty(Class<? extends Entity> containingType, Field field) {
    return isStatic(field);
  }

  private boolean isStatic(Field field) {
    return Modifier.isStatic(field.getModifiers());
  }

  public FieldType getFieldType(Class<? extends Entity> containingType, Field field) {
    if (isStatic(field)) {
      return VIRTUAL;
    }

    DBProperty dbProperty = field.getAnnotation(DBProperty.class);
    if (dbProperty != null) {
      return dbProperty.type();
    }

    return REGULAR;
  }

  /**
   * The name of the field used by the client.
   * @param containingType the type that contains the field
   * @param field the field to get the name for
   * @return the field name
   */
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

  /**
   * Gets the property name without any potential prefixes.
   * @param type the type to get the property name from
   * @param field the field to get the property name from
   * @return the property name
   */
  public String getPropertyName(Class<? extends Entity> type, Field field) {
    DBProperty annotation = field.getAnnotation(DBProperty.class);
    if(annotation != null){
      return annotation.value();
    }
    return field.getName();
  }
}
