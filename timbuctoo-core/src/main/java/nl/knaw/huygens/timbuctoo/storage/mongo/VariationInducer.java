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
import nl.knaw.huygens.timbuctoo.storage.PropertyMapper;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

class VariationInducer extends VariationConverter {

  private static final Logger LOG = LoggerFactory.getLogger(VariationInducer.class);

  private final PropertyMapper propertyMapper;

  public VariationInducer(TypeRegistry registry) {
    super(registry);
    propertyMapper = new PropertyMapper();
  }

  // --- public API ----------------------------------------------------

  /**
   * Converts an entity to a JsonTree.
   */
  public <T extends Entity> JsonNode induceNewEntity(Class<T> type, T entity) throws IOException {
    checkArgument(entity != null);

    if (TypeRegistry.isSystemEntity(type)) {
      return induceSystemEntity(type, entity);
    } else {
      return induceNewDomainEntity(type, entity);
    }
  }

  /**
   * Converts an entity to a Json tree and combines it with an existing Json tree.
   */
  public <T extends Entity> JsonNode induceOldEntity(Class<T> type, T entity, JsonNode node) throws IOException {
    checkArgument(entity != null);
    checkArgument(node != null);

    if (TypeRegistry.isSystemEntity(type)) {
      // TODO Decide: do we want to ignore dbObject?
      return induceSystemEntity(type, entity);
    } else {
      return induceOldDomainEntity(type, entity, node);
    }
  }

  // -------------------------------------------------------------------

  private <T extends Entity> JsonNode induceSystemEntity(Class<T> type, T entity) {
    Map<String, Object> map = propertyMapper.mapObject(Entity.class, type, entity);
    return jsonMapper.valueToTree(map);
  }

  private <T extends Entity> JsonNode induceNewDomainEntity(Class<T> type, T entity) {
    checkArgument(TypeRegistry.isDomainEntity(type));

    Map<String, Object> map = propertyMapper.mapObject(Entity.class, type, entity);

    for (Role role : ((DomainEntity) entity).getRoles()) {
      Class<? extends Role> roleType = role.getClass();
      map.putAll(getRoleMap(roleType, roleType.cast(role)));
    }

    ObjectNode newNode = jsonMapper.valueToTree(map);
    return cleanUp(newNode);
  }

  private <T extends Entity> JsonNode induceOldDomainEntity(Class<T> type, T entity, JsonNode existingItem) {
    checkArgument(TypeRegistry.isDomainEntity(type));
    checkArgument(existingItem != null);

    Map<String, Object> map = propertyMapper.mapObject(Entity.class, type, entity);
    map = merge(type, map, existingItem);

    List<Role> roles = ((DomainEntity) entity).getRoles();
    for (Role role : roles) {
      Class<? extends Role> roleType = role.getClass();
      Map<String, Object> roleMap = getRoleMap(roleType, roleType.cast(role));
      roleMap = merge(roleType, roleMap, existingItem);
      map.putAll(roleMap);
    }

    ObjectNode newNode = jsonMapper.valueToTree(map);
    return cleanUp(newNode);
  }

  // TODO make method work with T role instead of Role role
  @SuppressWarnings("unchecked")
  private <T extends Role> Map<String, Object> getRoleMap(Class<T> type, Role role) {
    Map<String, Object> map = Maps.newHashMap();
    if (type != Role.class) {
      map.putAll(getRoleMap((Class<T>) type.getSuperclass(), role));
    }

    map.putAll(propertyMapper.mapObject(type, (T) role));

    return map;
  }

  private Map<String, Object> merge(Class<?> type, Map<String, Object> newValues, JsonNode existingNode) {
    Map<String, Object> mergedMap = Maps.newHashMap();
    for (String key : newValues.keySet()) {
      if (existingNode.has(key)) {
        if (fieldMapper.isFieldWithVariation(key) && !isSameValue(key, newValues, existingNode)) {
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

  private boolean isSameValue(String key, Map<String, Object> newValues, JsonNode existingNode) {
    return isSameValue(key, key, newValues, existingNode);
  }

  protected boolean isSameValue(String key, String keyInNode, Map<String, Object> newValues, JsonNode existingNode) {
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
