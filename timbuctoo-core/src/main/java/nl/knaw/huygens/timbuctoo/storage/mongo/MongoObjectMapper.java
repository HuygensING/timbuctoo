package nl.knaw.huygens.timbuctoo.storage.mongo;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Role;
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
   * Converts the object to a Map ignoring the null values.
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
        Object value = convertValue(fieldName, field.getType(), field.get(item));
        if (value != null) {
          map.put(mappedName, value);
        }
      } catch (NoSuchFieldException e) {
        LOG.error("Error for field {} type {} {}", fieldName, type.getSimpleName(), e.getClass().getSimpleName());
      } catch (IllegalAccessException e) {
        LOG.error("Error for field {} type {} {}", fieldName, type.getSimpleName(), e.getClass().getSimpleName());
      } catch (IOException e) {
        LOG.error("Error for field {} type {} {}", fieldName, type.getSimpleName(), e.getClass().getSimpleName());
      }
    }

    return map;
  }

  private Object convertValue(String fieldName, Class<?> fieldType, Object value) throws IOException {
    if (value == null) {
      return null;
    } else if (isSimpleType(fieldType)) {
      return value;
    } else if (Collection.class.isAssignableFrom(fieldType)) {
      Collection<?> collection = Collection.class.cast(value);
      if (collection.isEmpty()) {
        // Because of type erase the element type is unknown...
        return null;
      }
      Class<?> elementType = collection.iterator().next().getClass();
      if (isSimpleType(elementType)) {
        return value;
      } else if (Role.class.isAssignableFrom(elementType)) {
        // Explicitly handled by inducer and reducer
        return null;
      }
    } else if (Class.class.isAssignableFrom(fieldType)) {
      return Class.class.cast(value).getName();
    } else if (Datable.class.isAssignableFrom(fieldType)) {
      return Datable.class.cast(value).getEDTF();
    } else if (PersonName.class.isAssignableFrom(fieldType)) {
      // Quick fix for serialize an Object.
      ObjectMapper om = new ObjectMapper();
      return om.readValue(om.writeValueAsString(value), new TypeReference<Map<String, Object>>() {});
    }
    LOG.error("Cannot convert field {} with type '{}' and value '{}'", fieldName, fieldType.getSimpleName(), value);
    throw new IllegalStateException("Cannot convert type " + fieldType.getSimpleName());
  }

  private boolean isSimpleType(Class<?> type) {
    return type.isPrimitive() || Number.class.isAssignableFrom(type) || String.class == type || Boolean.class == type || Character.class == type;
  }

}
