package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * This class converts a Java object to a map with a String key and value. 
 * The values are the current value of the object.
 * @author martijnm
 *
 */
public class MongoObjectMapper {
  private static final Class<JsonProperty> ANNOTATION_TO_RETRIEVE = JsonProperty.class;
  private static final String GET_ACCESSOR = "get";
  private static final Logger LOG = LoggerFactory.getLogger(MongoObjectMapper.class);
  private final TypeRegistry typeRegistry;

  @Inject
  public MongoObjectMapper(TypeRegistry typeRegistry) {
    this.typeRegistry = typeRegistry;
  }

  /**
   * Convert the object to a Map ignoring the null keys.
   * @param type the type to convert, should extends Entity.
   * @param item the object to convert.
   * @param mapParentClassValues if set to true the mapping does not stop at the current 
   * class of {@code type}, but iterates through all the fields of the super classes aswell.
   * @return a map with all the non-null values of the {@code item}.
   */
  @SuppressWarnings("unchecked")
  public <T extends Entity> Map<String, Object> mapObject(Class<T> type, T item, boolean mapParentClassValues) {
    Preconditions.checkArgument(item != null);
    Preconditions.checkArgument(type != null);
    Map<String, Object> map = Maps.newHashMap();

    if (mapParentClassValues && !(Entity.class == type)) {
      map.putAll(mapObject((Class<T>) type.getSuperclass(), item, true));
    }

    map.putAll(mapFields(type, item));

    return map;
  }

  protected <T extends Entity> Map<String, Object> mapFields(Class<T> type, T item) {
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

  private String getFieldName(Class<? extends Entity> type, Field field) {
    JsonProperty annotation = field.getAnnotation(ANNOTATION_TO_RETRIEVE);

    if (annotation != null) {
      return formatFieldName(type, annotation.value());
    }

    Method method = getMethodOfField(type, field.getName());

    if (method != null && method.getAnnotation(ANNOTATION_TO_RETRIEVE) != null) {
      return formatFieldName(type, method.getAnnotation(ANNOTATION_TO_RETRIEVE).value());
    }
    return formatFieldName(type, field.getName());
  }

  private String formatFieldName(Class<? extends Entity> type, String fieldName) {
    if (type == Entity.class || type == DomainEntity.class || type == SystemEntity.class) {
      return fieldName;
    }
    return typeRegistry.getINameForType(type) + "." + fieldName;
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
