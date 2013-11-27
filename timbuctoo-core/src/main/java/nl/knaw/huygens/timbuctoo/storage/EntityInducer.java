package nl.knaw.huygens.timbuctoo.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.isDomainEntity;
import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.isSystemEntity;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.BusinessRules;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoObjectMapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

public class EntityInducer {

  protected final TypeRegistry typeRegistry;
  protected final ObjectMapper jsonMapper;
  protected final FieldMapper fieldMapper;
  protected final MongoObjectMapper propertyMapper;

  public EntityInducer(TypeRegistry registry) {
    typeRegistry = registry;
    jsonMapper = new ObjectMapper();
    fieldMapper = new FieldMapper();
    propertyMapper = new MongoObjectMapper(fieldMapper);
  }

  // --- public API ----------------------------------------------------

  /**
   * Converts an entity to a JsonTree.
   */
  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induceNewEntity(Class<? super T> type, T entity) throws IOException {
    checkArgument(type != null && (isSystemEntity(type) || isDomainEntity(type)));
    checkArgument(entity != null);

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
    checkArgument(entity != null);
    checkArgument(node != null);

    if (isSystemEntity(type)) {
      return induceSystemEntity((Class<SystemEntity>) type, (SystemEntity) entity, node);
    } else {
      return induceDomainEntity(type, entity, node);
    }
  }

  // -------------------------------------------------------------------

  private <T extends SystemEntity> JsonNode induceSystemEntity(Class<T> type, T entity) {
    checkArgument(BusinessRules.allowSystemEntityAdd(type));

    Map<String, Object> map = Maps.newTreeMap();

    // Add (primitive) system entity
    Class<? super T> viewType = type;
    while (Entity.class.isAssignableFrom(viewType)) {
      propertyMapper.addObject(type, viewType, entity, map);
      viewType = viewType.getSuperclass();
    }

    return jsonMapper.valueToTree(map);
  }

  private <T extends SystemEntity> JsonNode induceSystemEntity(final Class<? super T> type, T entity, JsonNode existingItem) {
    Map<String, Object> map = Maps.newHashMap();
    propertyMapper.addObject(type, type, entity, map);
    JsonNode newTree = jsonMapper.valueToTree(map);

    return merge(fieldMapper.getFieldMap(type, type).values(), newTree, (ObjectNode) existingItem);
  }

  private <T extends DomainEntity> JsonNode induceDomainEntity(final Class<T> type, T entity) {
    checkArgument(BusinessRules.allowDomainEntityAdd(type));

    Map<String, Object> map = Maps.newTreeMap();

    // Add (derived) domain entity
    Class<? super T> viewType = type;
    while (Entity.class.isAssignableFrom(viewType)) {
      propertyMapper.addObject(type, viewType, entity, map);
      viewType = viewType.getSuperclass();
    }

    // Add primitive domain entity
    Class<? super T> baseType = type.getSuperclass();
    propertyMapper.addObject(baseType, baseType, entity, map);

    return jsonMapper.valueToTree(map);
  }

  private <T extends Entity> JsonNode induceDomainEntity(Class<T> type, T entity, JsonNode existingItem) {
    Map<String, Object> map = Maps.newHashMap();
    propertyMapper.addObject(type, type, entity, map);
    JsonNode newTree = jsonMapper.valueToTree(map);

    return merge(fieldMapper.getFieldMap(type, type).values(), newTree, (ObjectNode) existingItem);
  }

  /**
   * Merges the values corresponding to the specified keys of the new tree into the old tree.
   */
  private JsonNode merge(Collection<String> keys, JsonNode newTree, ObjectNode oldTree) {
    for (String key : keys) {
      JsonNode newValue = newTree.get(key);
      JsonNode oldValue = oldTree.get(key);
      if (newValue != null) {
        oldTree.put(key, newValue);
      } else if (oldValue != null) {
        oldTree.remove(key);
      }
    }
    return oldTree;
  }

}
