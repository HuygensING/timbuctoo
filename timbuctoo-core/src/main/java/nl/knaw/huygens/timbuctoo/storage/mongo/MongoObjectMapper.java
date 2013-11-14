package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.util.Datable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOG = LoggerFactory.getLogger(MongoObjectMapper.class);

  private final MongoFieldMapper mongoFieldMapper;

  @Inject
  public MongoObjectMapper(MongoFieldMapper mongoFieldMapper) {
    this.mongoFieldMapper = mongoFieldMapper;
  }

  /**
   * Convert the object to a Map ignoring the null keys.
   * @param type the type to convert, should extends Entity.
   * @param item the object to convert.
   * @return a map with all the non-null values of the {@code item}.
   */
  public <T> Map<String, Object> mapObject(Class<T> type, T item) {
    Preconditions.checkArgument(item != null);
    Preconditions.checkArgument(type != null);

    Map<String, Object> objectMap = Maps.<String, Object> newHashMap();
    for (Field field : type.getDeclaredFields()) {
      try {
        Class<?> fieldType = field.getType();

        if (isHumanReadable(fieldType)) {
          field.setAccessible(true);
          Object value = field.get(item);
          if (value != null) {
            objectMap.put(mongoFieldMapper.getFieldName(type, field), value);
          }
        } else if (Collection.class.isAssignableFrom(fieldType)) {
          field.setAccessible(true);
          Collection<?> value = (Collection<?>) field.get(item);
          if (isHumanReableCollection(value)) {
            objectMap.put(mongoFieldMapper.getFieldName(type, field), value);
          }
        } else if (Datable.class.isAssignableFrom(fieldType)) {
          field.setAccessible(true);
          Datable datable = (Datable) field.get(item);
          if (datable != null) {
            objectMap.put(mongoFieldMapper.getFieldName(type, field), datable.getEDTF());
          }
        } else {
          // Temporary only import simple properties.
          //          objectMap.putAll(mapNestedObject(type, item, field, fieldType));
        }
      } catch (IllegalAccessException e) {
        LOG.error("Field {} is not accessible in type {}.", field.getName(), type);
        LOG.debug("", e);
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

}
