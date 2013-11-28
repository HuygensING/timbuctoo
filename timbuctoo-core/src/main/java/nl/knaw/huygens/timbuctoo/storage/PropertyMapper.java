package nl.knaw.huygens.timbuctoo.storage;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;

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
public class PropertyMapper {

  private static final Logger LOG = LoggerFactory.getLogger(PropertyMapper.class);

  private final FieldMapper fieldMapper;

  public PropertyMapper() {
    fieldMapper = new FieldMapper();
  }

  public PropertyMapper(FieldMapper fieldMapper) {
    this.fieldMapper = fieldMapper;
  }

  public Map<String, Object> getPropertyMap(Map<String, Field> fieldMap, Object object) {
    Map<String, Object> map = Maps.newHashMap();
    if (fieldMap != null && object != null) {
      for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
        Field field = entry.getValue();
        try {
          field.setAccessible(true);
          Object value = convertValue(field.getName(), field.getType(), field.get(object));
          if (value != null) {
            map.put(entry.getKey(), value);
          }
        } catch (IllegalAccessException e) {
          LOG.error("Field {}: {}", field.getName(), e.getClass().getSimpleName());
        } catch (IOException e) {
          LOG.error("Field {}: {}", field.getName(), e.getClass().getSimpleName());
        }
      }
    }
    return map;
  }

  /**
   * Adds properties to a map, ignoring null values.
   */
  public <T> void addObject(Class<?> prefixType, Class<? super T> type, T object, Map<String, Object> map) {
    Preconditions.checkNotNull(object);
    Preconditions.checkNotNull(map);

    for (Map.Entry<String, String> entry : fieldMapper.getFieldMap(prefixType, type).entrySet()) {
      String fieldName = entry.getKey();
      String mappedName = entry.getValue();
      try {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = convertValue(fieldName, field.getType(), field.get(object));
        if (value != null) {
          map.put(mappedName, value);
        }
      } catch (NoSuchFieldException e) {
        LOG.error("Field {}, type {}: {}", fieldName, type.getSimpleName(), e.getClass().getSimpleName());
      } catch (IllegalAccessException e) {
        LOG.error("Field {}, type {}: {}", fieldName, type.getSimpleName(), e.getClass().getSimpleName());
      } catch (IOException e) {
        LOG.error("Field {}, type {}: {}", fieldName, type.getSimpleName(), e.getClass().getSimpleName());
      }
    }
  }

  /**
   * Converts the object to a Map ignoring the null values.
   */
  public <T> Map<String, Object> mapObject(Class<? super T> type, T object) {
    Preconditions.checkArgument(object != null);

    Map<String, Object> map = Maps.newHashMap();
    addObject(type, type, object, map);
    return map;
  }

  /**
   * Maps an object and its superclasses.
   */
  public <T> Map<String, Object> mapObject(Class<? super T> stopType, Class<? super T> type, T object) {
    checkArgument(stopType.isAssignableFrom(type), "type must extend stopType");

    Map<String, Object> map = Maps.newHashMap();
    if (type != stopType) {
      map.putAll(mapObject(stopType, type.getSuperclass(), object));
    }
    map.putAll(mapObject(type, object));
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
