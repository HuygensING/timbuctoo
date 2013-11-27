package nl.knaw.huygens.timbuctoo.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.isSystemEntity;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.BusinessRules;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

public class EntityInducer {

  protected final ObjectMapper jsonMapper;
  protected final FieldMapper fieldMapper;
  protected final PropertyMapper propertyMapper;

  public EntityInducer() {
    jsonMapper = new ObjectMapper();
    fieldMapper = new FieldMapper();
    propertyMapper = new PropertyMapper(fieldMapper);
  }

  /**
   * Converts an entity to a JsonTree.
   */
  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induceNewEntity(Class<? super T> type, T entity) throws IOException {
    if (isSystemEntity(type)) {
      return induceSystemEntity((Class<SystemEntity>) type, (SystemEntity) entity);
    } else {
      return induceDomainEntity((Class<DomainEntity>) type, (DomainEntity) entity);
    }
  }

  /**
   * Converts an entity to a Json tree and combines it with an existing Json tree.
   */
  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induceOldEntity(Class<T> type, T entity, JsonNode node) throws IOException {
    if (isSystemEntity(type)) {
      return induceSystemEntity((Class<SystemEntity>) type, (SystemEntity) entity, node);
    } else {
      return induceDomainEntity((Class<DomainEntity>) type, (DomainEntity) entity, node);
    }
  }

  // -------------------------------------------------------------------

  private <T extends SystemEntity> JsonNode induceSystemEntity(Class<T> type, T entity) {
    checkArgument(BusinessRules.allowSystemEntityAdd(type));

    Map<String, Object> map = Maps.newHashMap();

    // Add (primitive) system entity
    Class<? super T> viewType = type;
    while (Entity.class.isAssignableFrom(viewType)) {
      propertyMapper.addObject(type, viewType, entity, map);
      viewType = viewType.getSuperclass();
    }

    return jsonMapper.valueToTree(map);
  }

  private <T extends DomainEntity> JsonNode induceDomainEntity(final Class<T> type, T entity) {
    checkArgument(BusinessRules.allowDomainEntityAdd(type));

    Map<String, Object> map = Maps.newHashMap();

    // Add (derived) domain entity
    Class<? super T> viewType = type;
    while (Entity.class.isAssignableFrom(viewType)) {
      propertyMapper.addObject(type, viewType, entity, map);
      viewType = viewType.getSuperclass();
    }

    // Add primitive domain entity
    Class<? super T> baseType = type.getSuperclass();
    propertyMapper.addObject(baseType, baseType, entity, map);

    // TODO handle roles

    return jsonMapper.valueToTree(map);
  }

  private <T extends DomainEntity> JsonNode induceDomainEntity(Class<T> type, T entity, JsonNode existingItem) {
    checkArgument(entity != null);
    checkArgument(existingItem != null);

    Map<String, Object> map = Maps.newHashMap();
    propertyMapper.addObject(type, type, entity, map);
    JsonNode newTree = jsonMapper.valueToTree(map);

    return merge(fieldMapper.getFieldMap(type, type).values(), newTree, (ObjectNode) existingItem);
  }

  private <T extends SystemEntity> JsonNode induceSystemEntity(Class<T> type, T entity, JsonNode existingItem) {
    checkArgument(entity != null);
    checkArgument(existingItem != null);

    Map<String, Object> map = Maps.newHashMap();
    propertyMapper.addObject(type, type, entity, map);
    JsonNode newTree = jsonMapper.valueToTree(map);

    // TODO handle roles

    return merge(fieldMapper.getFieldMap(type, type).values(), newTree, (ObjectNode) existingItem);
  }

  /**
   * Merges the values corresponding to the specified keys of the new tree into the old tree.
   */
  private JsonNode merge(Collection<String> keys, JsonNode newTree, ObjectNode oldTree) {
    for (String key : keys) {
      JsonNode newValue = newTree.get(key);
      if (newValue != null) {
        oldTree.put(key, newValue);
      } else {
        oldTree.remove(key);
      }
    }
    return oldTree;
  }

}
