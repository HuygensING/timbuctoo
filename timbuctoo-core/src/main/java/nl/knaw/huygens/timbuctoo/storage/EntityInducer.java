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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EntityInducer {

  private static final Logger LOG = LoggerFactory.getLogger(EntityInducer.class);

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

    return createJsonTree(entity, fieldMap);
  }

  private <T extends DomainEntity> JsonNode induceDomainEntity(Class<T> type, T entity) {
    Map<String, Field> fieldMap = fieldMapper.getCompositeFieldMap(type, type, Entity.class);
    fieldMapper.addToFieldMap(type.getSuperclass(), type.getSuperclass(), fieldMap);
    ObjectNode tree = createJsonTree(entity, fieldMap);

    for (Role role : entity.getRoles()) {
      Class<? extends Role> roleType = role.getClass();
      if (BusinessRules.allowRoleAdd(roleType)) {
        fieldMap = fieldMapper.getCompositeFieldMap(roleType, roleType, Role.class);
        fieldMapper.addToFieldMap(roleType.getSuperclass(), roleType.getSuperclass(), fieldMap);
        tree = updateJsonTree(tree, role, fieldMap);
      } else {
        LOG.error("Not allowed to add {}", roleType);
        throw new IllegalStateException("Not allowed to add role");
      }
    }

    return tree;
  }

  private <T extends SystemEntity> JsonNode induceSystemEntity(Class<T> type, T entity, ObjectNode tree) {
    Map<String, Field> fieldMap = fieldMapper.getSimpleFieldMap(type, type);
    return updateJsonTree(tree, entity, fieldMap);
  }

  private <T extends DomainEntity> JsonNode induceDomainEntity(Class<T> type, T entity, ObjectNode tree) {
    Class<?> stopType = (type.getSuperclass() == DomainEntity.class) ? type : type.getSuperclass();
    Map<String, Field> fieldMap = fieldMapper.getCompositeFieldMap(type, type, stopType);
    tree = updateJsonTree(tree, entity, fieldMap);

    for (Role role : entity.getRoles()) {
      Class<? extends Role> roleType = role.getClass();
      fieldMap = fieldMapper.getSimpleFieldMap(roleType, roleType);
      tree = updateJsonTree(tree, role, fieldMap);
    }

    return tree;
  }

  private ObjectNode updateJsonTree(ObjectNode oldTree, Object object, Map<String, Field> fieldMap) {
    ObjectNode newTree = createJsonTree(object, fieldMap);
    return merge(oldTree, newTree, fieldMap.keySet());
  }

  /**
   * Creates a Json tree given a field map and an object.
   */
  private ObjectNode createJsonTree(Object object, Map<String, Field> fieldMap) {
    PropertyMap properties = new PropertyMap(object, fieldMap);
    return jsonMapper.valueToTree(properties);
  }

  /**
   * Merges the values corresponding to the specified keys of the new tree into the old tree.
   */
  private ObjectNode merge(ObjectNode oldTree, ObjectNode newTree, Set<String> keys) {
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
