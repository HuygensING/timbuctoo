package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class PropertyMap extends TreeMap<String, Object> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(PropertyMap.class);

  public PropertyMap() {}

  public PropertyMap(Map<String, Field> fieldMap, Object object) {
    if (fieldMap != null && object != null) {
      for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
        try {
          Field field = entry.getValue();
          field.setAccessible(true);
          Object value = convertValue(field.getType(), field.get(object));
          if (value != null) {
            put(entry.getKey(), value);
          }
        } catch (Exception e) {
          LOG.error("Error for field {}", entry.getValue().getName());
          throw new RuntimeException(e);
        }
      }
    }
  }

  private Object convertValue(Class<?> fieldType, Object value) throws IOException {
    if (value == null) {
      return null;
    } else if (isSimpleType(fieldType)) {
      return value;
    } else if (Collection.class.isAssignableFrom(fieldType)) {
      Collection<?> collection = Collection.class.cast(value);
      if (collection.isEmpty()) {
        // Because of type erasure the element type is unknown...
        return null;
      }
      Class<?> elementType = collection.iterator().next().getClass();
      if (isSimpleType(elementType)) {
        return value;
      } else if (Role.class.isAssignableFrom(elementType)) {
        // Explicitly handled by inducer and reducer
        @SuppressWarnings("unchecked")
        Iterator<Role> roles = (Iterator<Role>) collection.iterator();
        return convertRoles(roles);
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
    LOG.error("Cannot convert type '{}' with value '{}'", fieldType.getSimpleName(), value);
    throw new IllegalStateException("Cannot convert type " + fieldType.getSimpleName());
  }

  private boolean isSimpleType(Class<?> type) {
    return type.isPrimitive() || Number.class.isAssignableFrom(type) || String.class == type || Boolean.class == type || Character.class == type;
  }

  private List<String> convertRoles(Iterator<Role> roles) {
    List<String> roleNames = Lists.newArrayList();
    while (roles.hasNext()) {
      roleNames.add(roles.next().getRoleName());
    }
    return roleNames;
  }

}
