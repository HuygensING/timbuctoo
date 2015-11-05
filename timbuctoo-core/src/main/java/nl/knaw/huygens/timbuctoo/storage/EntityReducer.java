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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

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

  private final Properties properties;
  private final TypeRegistry typeRegistry;

  @Inject
  public EntityReducer(Properties properties, TypeRegistry registry) {
    this.properties = properties;
    typeRegistry = registry;
  }

  public <T extends Entity> T reduceVariation(Class<T> type, JsonNode tree) throws StorageException {
    checkNotNull(tree);
    if (TypeRegistry.isSystemEntity(type)) {
      return reduceObject(tree, type, type);
    } else {
      Class<?> viewType = variationExists(tree, type) ? type : type.getSuperclass();
      return reduceObject(tree, type, viewType);
    }
  }

  // TODO This is the "old" behaviour, but we need to re-think the resposibilities
  // Who knows about the way variations are stored? It's either the storage layer.
  // in which case reduceAllVariations shouldn't be part of the reducer, or it is
  // the inducer/reducer, in which case adding maintaining the variation list
  // should be part of the inducer and not of MongoStorage.
  public <T extends Entity> List<T> reduceAllVariations(Class<T> type, JsonNode tree) throws StorageException {
    checkNotNull(tree);

    List<T> entities = Lists.newArrayList();

    JsonNode variations = tree.findValue(DomainEntity.VARIATIONS);
    if (variations != null) {
      for (JsonNode node : ImmutableList.copyOf(variations.elements())) {
        String variation = node.textValue();
        Class<? extends DomainEntity> varType = typeRegistry.getDomainEntityType(variation);
        if (varType != null && type.isAssignableFrom(varType)) {
          T entity = type.cast(reduceObject(tree, varType, varType));
          entities.add(entity);
        } else {
          LOG.error("Not a variation of {}: {}", type, variation);
        }
      }
    }

    return entities;
  }

  public <T extends Entity> List<T> reduceAllRevisions(Class<T> type, JsonNode tree) throws StorageException {
    checkNotNull(tree);

    ArrayNode versionsNode = (ArrayNode) tree.get("versions");

    List<T> revisions = Lists.newArrayList();
    for (int i = 0; versionsNode.hasNonNull(i); i++) {
      revisions.add(reduceVariation(type, versionsNode.get(i)));
    }
    return revisions;
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
   * Extracts the entity of the specified {@code type} from the specified Json tree.
   * The view type controls the variation that is actually stored in the entity.
   * For example, if the type is {@code BaseLanguage} and the view type is {@code Language}
   * this method returns a {@code BaseLanguage} entity with values of the {@code Language}
   * variation and default values of the fields that are defined in {@code BaseLanguage}.
   */
  private <T> T reduceObject(JsonNode tree, Class<T> type, Class<?> viewType) throws StorageException {
    try {
      T object = type.newInstance();
      String prefix = properties.propertyPrefix(viewType);
      FieldMap fields = FieldMap.getCombinedInstance(viewType);
      for (Map.Entry<String, Field> entry : fields.entrySet()) {
        String key = properties.propertyName(prefix, entry.getKey());
        JsonNode node = tree.findValue(key);
        if (node != null) {
          Field field = entry.getValue();
          Object value = properties.reduce(field, node);
          field.set(object, value);
        }
      }
      return object;
    } catch (Exception e) {
      LOG.error("Error while reducing object of type {}: {}", type, e.getMessage());
      throw new StorageException(e);
    }
  }

}
