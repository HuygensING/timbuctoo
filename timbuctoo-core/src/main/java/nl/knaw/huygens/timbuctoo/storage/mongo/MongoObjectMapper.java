package nl.knaw.huygens.timbuctoo.storage.mongo;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * This class converts a Java object to a map with a String key and value. 
 * The values are the current value of the object.
 */
public class MongoObjectMapper {

  private static final Logger LOG = LoggerFactory.getLogger(MongoObjectMapper.class);

  private final FieldMapper fieldMapper;

  public MongoObjectMapper() {
    fieldMapper = new FieldMapper();
  }

  /**
   * Maps an object and its superclasses.
   */
  public <T> Map<String, Object> mapObject(Class<? super T> stopType, Class<? super T> type, T item) {
    checkArgument(stopType.isAssignableFrom(type), "type must extend stopType");
    Map<String, Object> map = Maps.newHashMap();
    if (type != stopType) {
      map.putAll(mapObject(stopType, type.getSuperclass(), item));
    }
    map.putAll(mapObject(type, item));
    return map;
  }

  /**
   * Convert the object to a Map ignoring the null values.
   */
  public <T> Map<String, Object> mapObject(Class<? super T> type, T item) {
    Preconditions.checkArgument(item != null);
    Preconditions.checkArgument(type != null);

    Map<String, Object> map = Maps.newHashMap();
    for (Map.Entry<String, String> entry : fieldMapper.getFieldMap(type).entrySet()) {
      String fieldName = entry.getKey();
      String mappedName = entry.getValue();
      try {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        Class<?> fieldType = field.getType();
        if (isHumanReadable(fieldType)) {
          Object value = field.get(item);
          if (value != null) {
            map.put(mappedName, value);
          }
        } else if (Collection.class.isAssignableFrom(fieldType)) {
          Collection<?> value = (Collection<?>) field.get(item);
          if (isHumanReableCollection(value)) {
            map.put(mappedName, value);
          }
        } else if (Class.class.isAssignableFrom(fieldType)) {
          Class<?> cls = (Class<?>) field.get(item);
          if (cls != null) {
            map.put(mappedName, cls.getName());
          }
        } else if (Datable.class.isAssignableFrom(fieldType)) {
          Datable datable = (Datable) field.get(item);
          if (datable != null) {
            map.put(mappedName, datable.getEDTF());
          }
        } else if (PersonName.class.isAssignableFrom(fieldType)) {
          // Quick fix for serialize an Object.
          Object value = field.get(item);
          if (value != null) {
            ObjectMapper om = new ObjectMapper();
            Map<String, Object> nameMap = om.readValue(om.writeValueAsString(value), new TypeReference<Map<String, Object>>() {});
            map.put(mappedName, nameMap);
          }
        }
      } catch (Exception e) {
        LOG.error("Error for field {} type {} {}", fieldName, type.getSimpleName(), e.getClass().getSimpleName());
      }
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

}
