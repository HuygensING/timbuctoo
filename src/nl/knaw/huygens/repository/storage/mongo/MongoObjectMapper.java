package nl.knaw.huygens.repository.storage.mongo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

/**
 * This class converts a Java object to a map with a String key and value. 
 * The keys are the fieldnames used in the mongo database. 
 * The values are the current value of the object.
 * @author martijnm
 *
 */
public class MongoObjectMapper {
  private static final Class<JsonProperty> ANNOTATION_TO_RETRIEVE = JsonProperty.class;
  private static final String GET_ACCESSOR = "get";

  public <T> Map<String, String> mapObject(Class<T> type, T example) {
    if (type == null) {
      throw new IllegalArgumentException("'type' cannot be null.");
    }

    if (example == null) {
      throw new IllegalArgumentException("'example' cannot be null.");
    }

    Map<String, String> objectMap = Maps.<String, String> newHashMap();
    Field[] fields = type.getDeclaredFields();

    try {
      for (Field field : fields) {
        Class<?> fieldType = field.getType();
        if (fieldType.isPrimitive() || String.class.equals(fieldType)) {
          field.setAccessible(true);
          Object value = field.get(example);
          if (value != null) {
            objectMap.put(getFieldName(type, field), String.valueOf(value));
          }
        }
      }
    } catch (IllegalAccessException ex) {
      ex.printStackTrace();
    }

    return objectMap;
  }

  private String getFieldName(Class<?> type, Field field) {
    JsonProperty annotation = field.getAnnotation(ANNOTATION_TO_RETRIEVE);
    if (annotation != null) {
      return annotation.value();
    }

    Method method = getMethodOfField(type, field.getName());

    if (method != null && method.getAnnotation(ANNOTATION_TO_RETRIEVE) != null) {
      return method.getAnnotation(ANNOTATION_TO_RETRIEVE).value();
    }
    return field.getName();
  }

  private Method getMethodOfField(Class<?> type, String fieldName) {
    Method method = null;
    String methodName = getMethodName(fieldName, MongoObjectMapper.GET_ACCESSOR);

    for (Method m : type.getMethods()) {
      if (m.getName().equals(methodName)) {
        method = m;
        break;
      }
    }

    return method;
  }

  private String getMethodName(String fieldName, String accessor) {
    char[] fieldNameChars = fieldName.toCharArray();

    fieldNameChars[0] = Character.toUpperCase(fieldNameChars[0]);

    return accessor.concat(String.valueOf(fieldNameChars));
  }
}
