package nl.knaw.huygens.timbuctoo.storage.mongo;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.storage.FieldMapper;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.apache.commons.lang.StringUtils;
import org.mongojack.internal.stream.JacksonDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.DBObject;

class VariationInducer extends VariationConverter {

  private static final Logger LOG = LoggerFactory.getLogger(VariationInducer.class);

  public VariationInducer(TypeRegistry registry) {
    super(registry);
  }

  /**
   * Convenience method for {@code induce(type, item, null)}.
   */
  public <T extends Entity> JsonNode induce(Class<T> type, T item) throws StorageException {
    return induce(type, item, (ObjectNode) null);
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induce(Class<T> type, T item, DBObject existingItem) throws StorageException {
    ObjectNode node = null;
    if (existingItem instanceof JacksonDBObject) {
      node = (ObjectNode) (((JacksonDBObject<JsonNode>) existingItem).getObject());
    } else if (existingItem instanceof DBJsonNode) {
      node = (ObjectNode) ((DBJsonNode) existingItem).getDelegate();
    } else if (existingItem != null) {
      throw new StorageException("Unknown type of DBObject!");
    }
    return induce(type, item, node);
  }

  /**
   * Converts an Entity to a JsonTree and combines it with the {@code existionItem}. 
   * If the {@code existionItem} is null it creates a new item. 
   * @param type the type of the item to convert.
   * @param item the new item to convert.
   * @param existingItem the existing item.
   * @return the converted and combined item.
   * @throws StorageException
   */
  public <T extends Entity> JsonNode induce(Class<T> type, T item, ObjectNode existingItem) throws StorageException {
    checkArgument(item != null);
    checkArgument(type != null);

    Map<String, Object> map = getEntityMap(type, item);
    if (existingItem != null && TypeRegistry.isDomainEntity(type)) {
      map = merge(type, map, existingItem);
    }

    if (TypeRegistry.isSystemEntity(type)) {
      return mapper.valueToTree(map);
    }

    List<Role> roles = ((DomainEntity) item).getRoles();
    for (Role role : roles) {
      Map<String, Object> roleMap = Maps.newHashMap();
      Class<? extends Role> roleType = role.getClass();
      roleMap.putAll(getRoleMap(roleType, roleType.cast(role)));
      if (existingItem != null) {
        map.putAll(merge(roleType, roleMap, existingItem));
      } else {
        map.putAll(roleMap);
      }
    }

    ObjectNode newNode = mapper.valueToTree(map);
    return cleanUp(newNode);
  }

  @SuppressWarnings("unchecked")
  private <T extends Entity> Map<String, Object> getEntityMap(Class<T> type, T item) {
    Map<String, Object> map = Maps.newHashMap();
    if (type != Entity.class) {
      map.putAll(getEntityMap((Class<T>) type.getSuperclass(), item));
    }

    map.putAll(mongoObjectMapper.mapObject(type, item));

    return map;
  }

  //TODO make method work with T role instead of Role role
  @SuppressWarnings("unchecked")
  private <T extends Role> Map<String, Object> getRoleMap(Class<T> type, Role role) {
    Map<String, Object> map = Maps.newHashMap();
    if (type != Role.class) {
      map.putAll(getRoleMap((Class<T>) type.getSuperclass(), role));
    }

    map.putAll(mongoObjectMapper.mapObject(type, (T) role));

    return map;
  }

  /**
   * Checks if the there is variation possible for this field.
   */
  protected boolean isFieldWithVariation(String fieldName) {
    return fieldName.contains(FieldMapper.SEPARATOR);
  }

  private Map<String, Object> merge(Class<?> type, Map<String, Object> newValues, ObjectNode existingNode) {
    Map<String, Object> mergedMap = Maps.newHashMap();
    for (String key : newValues.keySet()) {
      if (existingNode.has(key)) {
        if (isFieldWithVariation(key) && !isSameValue(key, newValues, existingNode)) {
          String typeName = fieldMapper.getTypeNameOfFieldName(key);
          String variationName = StringUtils.replace(key, typeName, typeRegistry.getIName(type));
          mergedMap.put(variationName, newValues.get(key));
          mergedMap.put(key, existingNode.get(key));
        } else {
          mergedMap.put(key, newValues.get(key));
        }
      } else {
        mergedMap.put(key, newValues.get(key));
      }
    }

    Iterator<String> keys = existingNode.fieldNames();

    while (keys.hasNext()) {
      String key = keys.next();
      Set<String> similarKeys = getSimilarKeys(mergedMap, key);

      if (similarKeys.isEmpty()) {
        mergedMap.put(key, existingNode.get(key));
      } else {
        for (String similarKey : similarKeys) {
          String typeName = fieldMapper.getTypeNameOfFieldName(similarKey);
          if (typeName != null && typeRegistry.getForIName(typeName).isAssignableFrom(type) && !isSameValue(similarKey, key, mergedMap, existingNode)) {
            mergedMap.put(key, existingNode.get(key));
            break;
          }
        }
      }
    }

    return mergedMap;
  }

  private Set<String> getSimilarKeys(Map<String, Object> map, String key) {
    checkNotNull(key);
    Set<String> similarKeys = Sets.newHashSet();
    int pos = key.indexOf(FieldMapper.SEPARATOR_CHAR);
    if (pos >= 0) {
      String fieldName = key.substring(pos + 1);
      for (String similarKey : map.keySet()) {
        if (StringUtils.contains(similarKey, fieldName)) {
          similarKeys.add(similarKey);
        }
      }
    }
    return similarKeys;
  }

  private boolean isSameValue(String key, Map<String, Object> newValues, ObjectNode existingNode) {
    return isSameValue(key, key, newValues, existingNode);
  }

  protected boolean isSameValue(String key, String keyInNode, Map<String, Object> newValues, ObjectNode existingNode) {
    Object newValue = newValues.get(key);

    if (newValue == null) {
      return false;
    }

    Object existingValue = null;
    try {
      existingValue = convertValue(newValue.getClass(), existingNode.get(keyInNode));
    } catch (IOException e) {
      LOG.error("Value \"{}\" cannot be converted.", existingNode.get(keyInNode));
    }

    return Objects.equal(newValue, existingValue);
  }

  // remove all the runtime fields from the node
  private ObjectNode cleanUp(ObjectNode node) {
    Iterator<String> fieldNames = node.fieldNames();
    // deep copy is needed, because during iteration over the fields the fields cannot be removed.
    ObjectNode nodeToCleanUp = node.deepCopy();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      if (fieldName.startsWith("@")) {
        nodeToCleanUp.remove(fieldName);
      }
    }
    return nodeToCleanUp;
  }

}
