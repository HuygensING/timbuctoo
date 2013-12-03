package nl.knaw.huygens.timbuctoo.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.isSystemEntity;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.BusinessRules;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EntityInducer {

  private final FieldMapper fieldMapper;
  private final ObjectMapper jsonMapper;

  public EntityInducer() {
    fieldMapper = new FieldMapper();
    jsonMapper = new ObjectMapper();
  }

  /**
   * Converts an entity to a JsonTree.
   */
  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induceNewEntity(Class<? super T> type, T entity) throws IOException {
    if (isSystemEntity(type)) {
      checkArgument(BusinessRules.allowSystemEntityAdd(type));
      return induceSystemEntity((Class<SystemEntity>) type, (SystemEntity) entity);
    } else {
      checkArgument(BusinessRules.allowDomainEntityAdd(type));
      return induceDomainEntity((Class<DomainEntity>) type, (DomainEntity) entity);
    }
  }

  /**
   * Converts an entity to a Json tree and combines it with an existing Json tree.
   */
  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induceOldEntity(Class<T> type, T entity, ObjectNode node) throws IOException {
    if (isSystemEntity(type)) {
      return induceSystemEntity((Class<SystemEntity>) type, (SystemEntity) entity, node);
    } else {
      return induceDomainEntity((Class<DomainEntity>) type, (DomainEntity) entity, node);
    }
  }

  // -------------------------------------------------------------------

  private <T extends SystemEntity> JsonNode induceSystemEntity(Class<T> type, T entity) {
    Map<String, Field> fieldMap = fieldMapper.getCompositeFieldMap(type, type, Entity.class);

    return createTree(fieldMap, entity);
  }

  private <T extends DomainEntity> JsonNode induceDomainEntity(Class<T> type, T entity) {
    Map<String, Field> fieldMap = fieldMapper.getCompositeFieldMap(type, type, Entity.class);
    fieldMapper.addToFieldMap(type.getSuperclass(), type.getSuperclass(), fieldMap);
    ObjectNode tree = createTree(fieldMap, entity);

    for (Role role : entity.getRoles()) {
      fieldMap = fieldMapper.getSimpleFieldMap(role.getClass(), role.getClass());
      updateTree(fieldMap, role, tree);
    }

    return tree;
  }

  private <T extends SystemEntity> JsonNode induceSystemEntity(Class<T> type, T entity, ObjectNode tree) {
    Map<String, Field> fieldMap = fieldMapper.getSimpleFieldMap(type, type);
    updateTree(fieldMap, entity, tree);

    return tree;
  }

  private <T extends DomainEntity> JsonNode induceDomainEntity(Class<T> type, T entity, ObjectNode tree) {
    Class<?> stopType = (type.getSuperclass() == DomainEntity.class) ? type : type.getSuperclass();
    Map<String, Field> fieldMap = fieldMapper.getCompositeFieldMap(type, type, stopType);
    updateTree(fieldMap, entity, tree);

    for (Role role : entity.getRoles()) {
      fieldMap = fieldMapper.getSimpleFieldMap(role.getClass(), role.getClass());
      updateTree(fieldMap, role, tree);
    }

    return tree;
  }

  private void updateTree(Map<String, Field> fieldMap, Object object, ObjectNode oldTree) {
    ObjectNode newTree = createTree(fieldMap, object);
    merge(fieldMap.keySet(), newTree, oldTree);
  }

  /**
   * Creates a Json tree given a field map and an object.
   */
  private ObjectNode createTree(Map<String, Field> fieldMap, Object object) {
    PropertyMap properties = new PropertyMap(fieldMap, object);
    return jsonMapper.valueToTree(properties);
  }

  /**
   * Merges the values corresponding to the specified keys of the new tree into the old tree.
   */
  private void merge(Set<String> keys, ObjectNode newTree, ObjectNode oldTree) {
    for (String key : keys) {
      JsonNode newValue = newTree.get(key);
      if (newValue != null) {
        oldTree.put(key, newValue);
      } else {
        oldTree.remove(key);
      }
    }
  }

}
