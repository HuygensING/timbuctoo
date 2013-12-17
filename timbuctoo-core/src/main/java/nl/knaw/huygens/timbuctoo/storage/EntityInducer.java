package nl.knaw.huygens.timbuctoo.storage;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.BusinessRules;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
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
   * Converts a system entity to a Json tree.
   */
  public <T extends SystemEntity> JsonNode induceSystemEntity(Class<T> type, T entity) {
    checkArgument(BusinessRules.allowSystemEntityAdd(type));

    Map<String, Field> fieldMap = fieldMapper.getCompositeFieldMap(type, type, Entity.class);
    return createJsonTree(entity, fieldMap);
  }

  /**
   * Converts a system entity to a Json tree and combines it with an existing Json tree.
   */
  public <T extends SystemEntity> JsonNode induceSystemEntity(Class<T> type, T entity, ObjectNode tree) {
    Map<String, Field> fieldMap = fieldMapper.getSimpleFieldMap(type, type);
    return updateJsonTree(tree, entity, fieldMap);
  }

  /**
   * Converts a domain entity to a Json tree.
   */
  public <T extends DomainEntity> JsonNode induceDomainEntity(Class<T> type, T entity) {
    checkArgument(BusinessRules.allowDomainEntityAdd(type));

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

  /**
   * Converts a domain entity to a Json tree and combines it with an existing Json tree.
   * Note that this method only handles the variant that correspondeds with the specified type,
   * either a primitive domain entity or a project domain entity.
   */
  public <T extends DomainEntity> JsonNode induceDomainEntity(Class<T> type, T entity, ObjectNode tree) {
    Class<?> stopType = TypeRegistry.toBaseDomainEntity(type);
    Map<String, Field> fieldMap = fieldMapper.getCompositeFieldMap(type, type, stopType);
    tree = updateJsonTree(tree, entity, fieldMap);

    for (Role role : entity.getRoles()) {
      Class<?> roleType = role.getClass();
      Class<?> baseType = (roleType.getSuperclass() == Role.class) ? roleType : roleType.getSuperclass();
      fieldMap = fieldMapper.getCompositeFieldMap(roleType, roleType, baseType);
      tree = updateJsonTree(tree, role, fieldMap);
    }

    return tree;
  }

  public JsonNode adminSystemEntity(SystemEntity entity, ObjectNode tree) {
    Map<String, Field> fieldMap = fieldMapper.getCompositeFieldMap(SystemEntity.class, SystemEntity.class, Entity.class);
    return updateJsonTree(tree, entity, fieldMap);
  }

  public JsonNode adminDomainEntity(DomainEntity entity, ObjectNode tree) {
    Map<String, Field> fieldMap = fieldMapper.getCompositeFieldMap(DomainEntity.class, DomainEntity.class, Entity.class);
    return updateJsonTree(tree, entity, fieldMap);
  }

  // -------------------------------------------------------------------

  /**
   * Updates a Json tree given an object and a field map.
   */
  private ObjectNode updateJsonTree(ObjectNode tree, Object object, Map<String, Field> fieldMap) {
    ObjectNode newTree = createJsonTree(object, fieldMap);
    return merge(tree, newTree, fieldMap.keySet());
  }

  /**
   * Creates a Json tree given an object and a field map.
   */
  private ObjectNode createJsonTree(Object object, Map<String, Field> fieldMap) {
    PropertyMap properties = new PropertyMap(object, fieldMap);
    return jsonMapper.valueToTree(properties);
  }

  /**
   * Merges into a tree the values corresponding to the specified keys of the new tree.
   */
  private ObjectNode merge(ObjectNode tree, ObjectNode newTree, Set<String> keys) {
    for (String key : keys) {
      JsonNode newValue = newTree.get(key);
      if (newValue != null) {
        tree.put(key, newValue);
      } else {
        tree.remove(key);
      }
    }
    return tree;
  }

}
