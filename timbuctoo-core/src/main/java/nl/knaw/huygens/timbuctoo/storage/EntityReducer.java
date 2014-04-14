package nl.knaw.huygens.timbuctoo.storage;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoChanges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * This is the complementary class of {@link EntityInducer}.
 * It contains various ways of reducing a Json tree retrieved from storage.
 * Such Json trees always correspond with an entity collection for the type
 * specified in the public methods of this class.
 * System entities are always immediate subclasses of {@link SystemEntity}.
 * Domain entities are primitive, immediate subclasses of {@link DomainEntity},
 * or derived, immediate subclasses of primitive domain entities.
 */
public class EntityReducer {

  private static final Logger LOG = LoggerFactory.getLogger(EntityReducer.class);

  private final TypeRegistry typeRegistry;
  private final ObjectMapper jsonMapper;
  private final FieldMapper fieldMapper;

  public EntityReducer(TypeRegistry registry) {
    typeRegistry = registry;
    jsonMapper = new ObjectMapper();
    fieldMapper = new FieldMapper();
  }

  public <T extends Entity> T reduceVariation(Class<T> type, JsonNode tree) throws IOException {
    checkNotNull(tree);
    if (TypeRegistry.isSystemEntity(type)) {
      return reduceObject(tree, null, type, type, Entity.class);
    } else {
      Set<String> prefixes = getPrefixes(tree);
      Class<?> viewType = variationExists(tree, type) ? type : type.getSuperclass();
      return reduceObject(tree, prefixes, type, viewType, Entity.class);
    }
  }

  // TODO This is the "old" behaviour, but we need to re-think the resposibilities
  // Who knows about the way variations are stored? It's either the storage layer.
  // in which case reduceAllVariations shouldn't be part of the reducer, or it is
  // the inducer/reducer, in which case adding maintaining the variation list
  // should be part of the inducer and not of MongoStorage.
  public <T extends Entity> List<T> reduceAllVariations(Class<T> type, JsonNode tree) throws IOException {
    checkNotNull(tree);

    List<T> entities = Lists.newArrayList();

    JsonNode variations = tree.findValue(DomainEntity.VARIATIONS);
    if (variations != null) {
      Set<String> prefixes = getPrefixes(tree);
      for (JsonNode node : ImmutableList.copyOf(variations.elements())) {
        String variation = node.textValue();
        Class<? extends Entity> varType = typeRegistry.getTypeForIName(variation);
        if (varType != null && type.isAssignableFrom(varType)) {
          T entity = type.cast(reduceObject(tree, prefixes, varType, varType, Entity.class));
          entities.add(entity);
        } else {
          LOG.error("Not a variation of {}: {}", type, variation);
        }
      }
    }

    return entities;
  }

  public <T extends Entity> MongoChanges<T> reduceAllRevisions(Class<T> type, JsonNode tree) throws IOException {
    checkNotNull(tree);

    ArrayNode versionsNode = (ArrayNode) tree.get("versions");
    MongoChanges<T> changes = null;

    for (int i = 0; versionsNode.hasNonNull(i); i++) {
      T item = reduceVariation(type, versionsNode.get(i));
      if (i == 0) {
        changes = new MongoChanges<T>(item.getId(), item);
      } else {
        changes.getRevisions().add(item);
      }
    }

    return changes;
  }

  // -------------------------------------------------------------------

  /**
   * Returns the names of the variations in the specified Json tree.
   */
  private List<String> getVariations(JsonNode tree) {
    List<String> variations = Lists.newArrayList();
    JsonNode variationsNode = tree.findValue(DomainEntity.VARIATIONS);
    if (variationsNode != null) {
      Iterator<JsonNode> iterator = variationsNode.elements();
      while (iterator.hasNext()) {
        variations.add(iterator.next().textValue());
      }
    }
    return variations;
  }

  private boolean variationExists(JsonNode tree, Class<?> type) {
    return getVariations(tree).contains(TypeNames.getInternalName(type));
  }

  /**
   * Returns the prefixes of the fields in the specified Json tree.
   * These prefixes correpond with the names of entities and roles.
   */
  private Set<String> getPrefixes(JsonNode tree) {
    Set<String> prefixes = Sets.newTreeSet();
    Iterator<String> iterator = tree.fieldNames();
    while (iterator.hasNext()) {
      String name = iterator.next();
      int pos = name.indexOf(FieldMapper.SEPARATOR_CHAR);
      if (pos > 0) {
        prefixes.add(name.substring(0, pos));
      }
    }
    return prefixes;
  }

  /**
   * Extracts the entity of the specified {@code type} from the specified Json tree.
   * The view type controls the variation that is actually stored in the entity.
   * For example, if the type is {@code BaseLanguage} and the view type is {@code Language}
   * this method returns a {@code BaseLanguage} entity with values of the {@code Language}
   * variation and default values of the fields that are defined in {@code BaseLanguage}.
   */
  private <T> T reduceObject(JsonNode tree, Set<String> prefixes, Class<T> type, Class<?> viewType, Class<?> stopType) throws StorageException {
    try {
      T object = newInstance(type);

      Map<String, Field> fieldMap = fieldMapper.getCompositeFieldMap(viewType, viewType, stopType);
      for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
        String key = entry.getKey();
        JsonNode node = tree.findValue(key);
        if (node != null) {
          Field field = entry.getValue();
          Object value = convertJsonNodeToValue(field.getType(), node);
          setValue(object, field, value);
          LOG.debug("Assigned: {} := {}", field.getName(), value);
        } else {
          LOG.debug("No value for property {}", key);
        }
      }

      if (TypeRegistry.isDomainEntity(type)) {
        DomainEntity entity = DomainEntity.class.cast(object);
        for (Class<? extends Role> role : typeRegistry.getAllowedRolesFor(type)) {
          if (prefixes.contains(TypeNames.getInternalName(role))) {
            entity.addRole(reduceObject(tree, prefixes, role, role, Role.class));
          }
        }
      }

      return object;
    } catch (Exception e) {
      // TODO improve error handling
      throw new StorageException(e);
    }
  }

  /**
   * Encapsulates creation of an object.
   */
  private <T> T newInstance(Class<T> type) {
    try {
      return type.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Encapsulates assigning of a value to the field of an object.
   */
  private void setValue(Object object, Field field, Object value) {
    try {
      field.setAccessible(true);
      field.set(object, value);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> Object convertJsonNodeToValue(Class<T> fieldType, JsonNode node) throws IOException {
    if (node.isArray()) {
      return createCollection(node);
    } else if (fieldType == Integer.class || fieldType == int.class) {
      return node.asInt();
    } else if (fieldType == Boolean.class || fieldType == boolean.class) {
      return node.asBoolean();
    } else if (fieldType == Character.class || fieldType == char.class) {
      return node.asText().charAt(0);
    } else if (fieldType == Double.class || fieldType == double.class) {
      return node.asDouble();
    } else if (fieldType == Float.class || fieldType == float.class) {
      return (float) node.asDouble();
    } else if (fieldType == Long.class || fieldType == long.class) {
      return node.asLong();
    } else if (fieldType == Short.class || fieldType == short.class) {
      return (short) node.asInt();
    } else if (Datable.class.isAssignableFrom(fieldType)) {
      return new Datable(node.asText());
    } else {
      return jsonMapper.convertValue(node, fieldType);
    }
  }

  private Object createCollection(JsonNode value) throws IOException, JsonParseException, JsonMappingException {
    return jsonMapper.readValue(value.toString(), new TypeReference<List<? extends Object>>() {});
  }

}
