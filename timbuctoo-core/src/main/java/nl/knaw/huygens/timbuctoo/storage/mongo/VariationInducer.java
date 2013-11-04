package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNameGenerator;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Role;

import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mongodb.DBObject;

class VariationInducer extends VariationConverter {
  private final MongoObjectMapper mongoMapper;

  public VariationInducer(TypeRegistry registry, MongoObjectMapper mongoMapper) {
    super(registry);
    this.mongoMapper = mongoMapper;
  }

  /**
   * Convenience method for {@code induce(type, item, null)}.
   * @param type
   * @param item
   * @return
   * @throws VariationException
   */
  public <T extends Entity> JsonNode induce(Class<T> type, T item) throws VariationException {
    return induce(type, item, (ObjectNode) null);
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induce(Class<T> type, T item, DBObject existingItem) throws VariationException {
    ObjectNode node = null;
    if (existingItem instanceof JacksonDBObject) {
      node = (ObjectNode) (((JacksonDBObject<JsonNode>) existingItem).getObject());
    } else if (existingItem instanceof DBJsonNode) {
      node = (ObjectNode) ((DBJsonNode) existingItem).getDelegate();
    } else if (existingItem != null) {
      throw new VariationException("Unknown type of DBObject!");
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
   * @throws VariationException
   */
  public <T extends Entity> JsonNode induce(Class<T> type, T item, ObjectNode existingItem) throws VariationException {
    Preconditions.checkArgument(item != null);
    Preconditions.checkArgument(type != null);

    Map<String, Object> map = Maps.newHashMap();

    Map<String, Object> entityMap = getEntityMap(type, item);
    if (existingItem != null && DomainEntity.class.isAssignableFrom(type)) {
      map.putAll(updateFieldNames(type, entityMap, existingItem));
    } else {
      map.putAll(entityMap);
    }

    if (DomainEntity.class.isAssignableFrom(type)) {
      List<Role> roles = ((DomainEntity) item).getRoles();
      if (roles != null) {
        for (Role role : roles) {
          Map<String, Object> roleMap = Maps.newHashMap();
          Class<? extends Role> roleType = role.getClass();
          roleMap.putAll(getRoleMap(roleType, roleType.cast(role)));
          if (existingItem != null) {
            map.putAll(updateFieldNames(roleType, roleMap, existingItem));
          } else {
            map.putAll(roleMap);
          }
        }
      }
    }

    ObjectNode newNode = mapper.valueToTree(map);

    if (existingItem != null && DomainEntity.class.isAssignableFrom(type)) {
      newNode = merge(type, existingItem, newNode);
    }

    return cleanUp(newNode);
  }

  @SuppressWarnings("unchecked")
  private <T extends Entity> Map<String, Object> getEntityMap(Class<T> type, T item) {
    Map<String, Object> map = Maps.newHashMap();
    if (type != Entity.class) {
      map.putAll(getEntityMap((Class<T>) type.getSuperclass(), item));
    }

    map.putAll(mongoMapper.mapObject(type, item));

    return map;
  }

  //TODO make method work with T role instead of Role role
  @SuppressWarnings("unchecked")
  private <T extends Role> Map<String, Object> getRoleMap(Class<T> type, Role role) {
    Map<String, Object> map = Maps.newHashMap();
    if (type != Role.class) {
      map.putAll(getRoleMap((Class<T>) type.getSuperclass(), role));
    }

    map.putAll(mongoMapper.mapObject(type, (T) role));

    return map;
  }

  private Map<String, Object> updateFieldNames(Class<?> type, Map<String, Object> mapToMerge, ObjectNode existingNode) {
    Map<String, Object> updatedMap = Maps.newHashMap();

    for (String fieldName : mapToMerge.keySet()) {
      if (existingNode.has(fieldName) && fieldName.contains(".")) {
        String updatedFieldName = fieldName.replace(fieldName.substring(0, fieldName.indexOf('.')), TypeNameGenerator.getInternalName(type));
        updatedMap.put(updatedFieldName, mapToMerge.get(fieldName));
      } else {
        updatedMap.put(fieldName, mapToMerge.get(fieldName));
      }
    }
    return updatedMap;
  }

  private ObjectNode merge(Class<? extends Entity> type, ObjectNode existingNode, JsonNode newNode) {
    Iterator<String> fieldNames = newNode.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      if (existingNode.has(fieldName)) {
        existingNode.remove(fieldName);
      }
      existingNode.put(fieldName, newNode.get(fieldName));
    }
    return existingNode;
  }

  // remove all the runtime fields from the node
  private ObjectNode cleanUp(ObjectNode node) {
    Iterator<String> fieldNames = node.fieldNames();
    // deep copy is needed, because during iteration over the fields the fields cannot be removed.
    ObjectNode nodeToCleanUp = node.deepCopy();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      if (fieldName.startsWith("!")) {
        nodeToCleanUp.remove(fieldName);
      }
    }
    return nodeToCleanUp;
  }
}
