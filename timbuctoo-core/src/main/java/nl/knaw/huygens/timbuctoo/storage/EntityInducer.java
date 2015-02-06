package nl.knaw.huygens.timbuctoo.storage;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.BusinessRules;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

public class EntityInducer {

  private final ObjectMapper jsonMapper;

  @Inject
  public EntityInducer() {
    jsonMapper = new ObjectMapper();
  }

  /**
   * Converts a system entity to a Json tree.
   */
  public <T extends SystemEntity> JsonNode convertSystemEntityForAdd(Class<T> type, T entity) {
    checkArgument(BusinessRules.allowSystemEntityAdd(type));

    FieldMap fieldMap = FieldMap.getCombinedInstance(type);
    return createJsonTree(entity, type, fieldMap);
  }

  /**
   * Converts a system entity to a Json tree and combines it with an existing Json tree.
   */
  public <T extends SystemEntity> JsonNode convertSystemEntityForUpdate(Class<T> type, T entity, ObjectNode tree) {
    FieldMap fieldMap = FieldMap.getInstance(type);
    return updateJsonTree(tree, entity, type, fieldMap);
  }

  /**
   * Converts a domain entity to a Json tree.
   */
  public <T extends DomainEntity> JsonNode convertDomainEntityForAdd(Class<T> type, T entity) {
    checkArgument(BusinessRules.allowDomainEntityAdd(type));

    FieldMap fieldMap = FieldMap.getCombinedInstance(type);
    ObjectNode tree = createJsonTree(entity, type, fieldMap);

    Class<?> baseType = type.getSuperclass();
    fieldMap = FieldMap.getInstance(baseType);
    tree = updateJsonTree(tree, entity, baseType, fieldMap);

    return tree;
  }

  /**
   * Converts a domain entity to a Json tree and combines it with an existing Json tree.
   * Note that this method only handles the variant that correspondeds with {@code type},
   * either a primitive domain entity or a project domain entity.
   */
  public <T extends DomainEntity> JsonNode convertDomainEntityForUpdate(Class<T> type, T entity, ObjectNode tree) {
    Class<?> stopType = TypeRegistry.toBaseDomainEntity(type);
    if (type == stopType) {
      FieldMap fieldMap = FieldMap.getInstance(type);
      return updateJsonTree(tree, entity, type, fieldMap);
    } else {
      FieldMap fieldMap = FieldMap.getCombinedInstance(type, stopType);
      return updateJsonTree(tree, entity, type, fieldMap.removeSharedFields());
    }
  }

  public JsonNode adminSystemEntity(SystemEntity entity, ObjectNode tree) {
    FieldMap fieldMap = FieldMap.getCombinedInstance(SystemEntity.class);
    return updateJsonTree(tree, entity, SystemEntity.class, fieldMap);
  }

  public JsonNode adminDomainEntity(DomainEntity entity, ObjectNode tree) {
    FieldMap fieldMap = FieldMap.getCombinedInstance(DomainEntity.class);
    return updateJsonTree(tree, entity, DomainEntity.class, fieldMap);
  }

  // -------------------------------------------------------------------

  /**
   * Updates a Json tree given an object and a field map.
   */
  private ObjectNode updateJsonTree(ObjectNode tree, Object object, Class<?> viewType, FieldMap fieldMap) {
    ObjectNode newTree = createJsonTree(object, viewType, fieldMap);
    return merge(tree, newTree, viewType, fieldMap.keySet());
  }

  /**
   * Creates a Json tree given an object and a field map.
   */
  private ObjectNode createJsonTree(Object object, Class<?> viewType, FieldMap fieldMap) {
    Properties properties = new Properties(object, viewType, fieldMap);
    return jsonMapper.valueToTree(properties);
  }

  /**
   * Merges into a tree the values corresponding to the specified keys of the new tree.
   */
  private ObjectNode merge(ObjectNode tree, ObjectNode newTree, Class<?> type, Set<String> fieldNames) {
    String iname = TypeNames.getInternalName(type);
    for (String fieldName : fieldNames) {
      String key = Properties.propertyName(iname, fieldName);
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
