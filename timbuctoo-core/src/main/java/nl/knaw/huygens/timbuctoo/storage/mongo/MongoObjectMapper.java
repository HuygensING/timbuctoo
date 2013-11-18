package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * This class converts a Java object to a map with a String key and value. 
 * The values are the current value of the object.
 * @author martijnm
 */
public class MongoObjectMapper {

  private static final Logger LOG = LoggerFactory.getLogger(MongoObjectMapper.class);

  private final FieldMapper fieldMapper;

  public MongoObjectMapper() {
    fieldMapper = new FieldMapper();
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

    Map<String, Object> map = Maps.newHashMap();
    for (Field field : type.getDeclaredFields()) {
      if (fieldMapper.isProperty(field)) {
        try {
          field.setAccessible(true);
          Class<?> fieldType = field.getType();
          if (isHumanReadable(fieldType)) {
            Object value = field.get(item);
            if (value != null) {
              map.put(fieldMapper.getFieldName(type, field), value);
            }
          } else if (Collection.class.isAssignableFrom(fieldType)) {
            Collection<?> value = (Collection<?>) field.get(item);
            if (isHumanReableCollection(value)) {
              map.put(fieldMapper.getFieldName(type, field), value);
            }
          } else if (Class.class.isAssignableFrom(fieldType)) {
            Class<?> cls = (Class<?>) field.get(item);
            if (cls != null) {
              map.put(fieldMapper.getFieldName(type, field), cls.getName());
            }
          } else if (Datable.class.isAssignableFrom(fieldType)) {
            Datable datable = (Datable) field.get(item);
            if (datable != null) {
              map.put(fieldMapper.getFieldName(type, field), datable.getEDTF());
            }
          } else if (PersonName.class.isAssignableFrom(fieldType)) {
            // Quick fix for serialize an Object.
            Object value = field.get(item);
            if (value != null) {
              ObjectMapper om = new ObjectMapper();
              try {
                Map<String, Object> nameMap = om.readValue(om.writeValueAsString(value), new TypeReference<Map<String, Object>>() {});
                map.put(fieldMapper.getFieldName(type, field), nameMap);
              } catch (JsonParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              } catch (JsonMappingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          }
        } catch (IllegalAccessException e) {
          LOG.error("Field {} is not accessible in type {}.", field.getName(), type);
          LOG.debug("", e);
        }
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
