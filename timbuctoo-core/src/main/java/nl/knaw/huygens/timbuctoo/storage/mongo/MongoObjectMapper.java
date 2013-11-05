package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNameGenerator;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This class converts a Java object to a map with a String key and value. 
 * The values are the current value of the object.
 * @author martijnm
 *
 */
@Singleton
public class MongoObjectMapper {
  private static final Class<JsonProperty> ANNOTATION_TO_RETRIEVE = JsonProperty.class;
  private static final String GET_ACCESSOR = "get";
  private static final String IS_ACCESSOR = "is"; //get accesor for booleans.
  private static final Logger LOG = LoggerFactory.getLogger(MongoObjectMapper.class);

  @Inject
  public MongoObjectMapper() {}

  /**
   * Convert the object to a Map ignoring the null keys.
   * @param type the type to convert, should extends Entity.
   * @param item the object to convert.
   * @return a map with all the non-null values of the {@code item}.
   */
  public <T> Map<String, Object> mapObject(Class<T> type, T item) {
    Preconditions.checkArgument(item != null);
    Preconditions.checkArgument(type != null);
    Map<String, Object> map = Maps.newHashMap();

    map.putAll(mapFields(type, item));

    return map;
  }

  private <T> Map<String, Object> mapFields(Class<T> type, T item) {
    Map<String, Object> objectMap = Maps.<String, Object> newHashMap();
    Field[] fields = type.getDeclaredFields();

    for (Field field : fields) {
      try {
        Class<?> fieldType = field.getType();

        if (isHumanReadable(fieldType)) {
          field.setAccessible(true);
          Object value = field.get(item);
          if (value != null) {
            objectMap.put(getFieldName(type, field), value);
          }
        } else if (Collection.class.isAssignableFrom(fieldType)) {
          field.setAccessible(true);
          Collection<?> value = (Collection<?>) field.get(item);
          if (isHumanReableCollection(value)) {
            objectMap.put(getFieldName(type, field), value);
          }
        }
      } catch (IllegalAccessException ex) {
        LOG.error("Field {} is not accessible in type {}.", field.getName(), type);
        LOG.debug("", ex);
      }
    }
    return objectMap;
  }

  /**
   * Generates a field map with for the {@code type} with object fields as keys and database fields as values.
   * @param type the type to create the map for.
   * @return the field map.
   */
  public Map<String, String> getFieldMap(Class<?> type) {
    Map<String, String> map = Maps.newHashMap();
    for (Field field : type.getDeclaredFields()) {
      map.put(field.getName(), getFieldName(type, field));
    }

    return map;
  }

  private boolean isHumanReadable(Class<?> type) {
    return type.isPrimitive() || isWrapperClass(type);
  }

  private boolean isWrapperClass(Class<?> type) {
    return Number.class.isAssignableFrom(type) || String.class.equals(type) || Boolean.class.equals(type) || Character.class.equals(type);
  }

  private boolean isHumanReableCollection(Collection<?> collection) {
    if (collection != null && !collection.isEmpty()) {
      return isHumanReadable(collection.toArray()[0].getClass());
    }
    return false;

  }

  private String getFieldName(Class<?> type, Field field) {
    JsonProperty annotation = field.getAnnotation(ANNOTATION_TO_RETRIEVE);

    if (annotation != null) {
      return formatFieldName(type, annotation.value());
    }

    Method method = getMethodOfField(type, field);

    if (method != null && method.getAnnotation(ANNOTATION_TO_RETRIEVE) != null) {
      return formatFieldName(type, method.getAnnotation(ANNOTATION_TO_RETRIEVE).value());
    }
    return formatFieldName(type, field.getName());
  }

  private String formatFieldName(Class<?> type, String fieldName) {
    if (type == Entity.class || type == DomainEntity.class || type == SystemEntity.class) {
      return fieldName;
    }
    return TypeNameGenerator.getInternalName(type) + "." + fieldName;
  }

  private Method getMethodOfField(Class<?> type, Field field) {
    Method method = null;
    String methodName = getMethodName(field);

    for (Method m : type.getMethods()) {
      if (m.getName().equals(methodName)) {
        method = m;
        break;
      }
    }

    return method;
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
