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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.validation.ValidationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RelationManager {

  private static final Logger LOG = LoggerFactory.getLogger(RelationManager.class);

  private final TypeRegistry registry;
  private final StorageManager storageManager;
  private int duplicateRelationCount;

  @Inject
  public RelationManager(TypeRegistry registry, StorageManager storageManager) {
    this.registry = registry;
    this.storageManager = storageManager;
    duplicateRelationCount = 0;
  }

  public int getDuplicateRelationCount() {
    return duplicateRelationCount;
  }

  /**
   * Stores the specified relation type.
   */
  public void addRelationType(RelationType type) throws IOException, ValidationException {
    storageManager.addSystemEntity(RelationType.class, type);
  }

  private static final String REGULAR_NAME = FieldMapper.propertyName(RelationType.class, "regularName");

  /**
   * Returns the relation type with the specified name,
   * or {@code null} if it does not exist.
   */
  public RelationType getRelationTypeByName(String name) {
    return storageManager.findEntity(RelationType.class, REGULAR_NAME, name);
  }

  /**
   * Returns the relation type with the specified id,
   * or {@code null} if it does not exist.
   */
  public RelationType getRelationTypeById(String id) {
    return storageManager.getEntity(RelationType.class, id);
  }

  /**
   * Returns a map for retrieving relation types by their regular name.
   */
  public Map<String, RelationType> getRelationTypeMap() {
    Map<String, RelationType> map = Maps.newHashMap();
    StorageIterator<RelationType> iterator = storageManager.getAll(RelationType.class);
    while (iterator.hasNext()) {
      RelationType type = iterator.next();
      map.put(type.getRegularName(), type);
    }
    return map;
  }

  /**
   * Returns the relation types in which an entity with the specified internal name
   * can participate, either as "source" or as "target".
   * If {@code iname} is {@code null} or empty all relation types are returned.
   */
  public List<RelationType> getRelationTypesForEntity(String iname) {
    boolean shouldFilter = !StringUtils.isEmpty(iname);
    List<RelationType> types = Lists.newArrayList();
    Iterator<RelationType> iterator = storageManager.getAll(RelationType.class);
    while (iterator.hasNext()) {
      RelationType type = iterator.next();
      if (!shouldFilter || isApplicable(iname, type)) {
        types.add(type);
      }
    }
    return types;
  }

  protected boolean isApplicable(String iname, RelationType type) {
    Class<? extends DomainEntity> requestType = TypeRegistry.toDomainEntity(convertToType(iname));
    Class<? extends DomainEntity> sourceType = TypeRegistry.toDomainEntity(convertToType(type.getSourceTypeName()));
    Class<? extends DomainEntity> targetType = TypeRegistry.toDomainEntity(convertToType(type.getTargetTypeName()));

    // iname is assignable from source or target of relation
    boolean isAssignable = isAssignable(sourceType, requestType) || isAssignable(targetType, requestType);

    boolean isSourceCompatible = isCompatible(requestType, sourceType);
    boolean isTargetCompatible = isCompatible(requestType, targetType);

    boolean isRequestTypePrimitive = TypeRegistry.isPrimitiveDomainEntity(requestType);

    boolean isPrimitiveCompatible = isRequestTypePrimitive && isAssignable && (isSourceCompatible || isTargetCompatible);
    boolean isCompatibleForProjectType = isAssignable && isSourceCompatible && isTargetCompatible;

    return isPrimitiveCompatible || isCompatibleForProjectType;
  }

  private boolean isCompatible(Class<? extends DomainEntity> requestType, Class<? extends DomainEntity> typeFromRelation) {

    return registry.isFromSameProject(requestType, typeFromRelation) || //
        TypeRegistry.isPrimitiveDomainEntity(requestType) || // 
        TypeRegistry.isPrimitiveDomainEntity(typeFromRelation) || //
        DomainEntity.class.equals(requestType) || //
        DomainEntity.class.equals(typeFromRelation);
  }

  /**
   * Returns the relation type with the specified reference,
   * or {@code null} if it does not exist.
   */
  public RelationType getRelationType(Reference reference) {
    checkArgument(reference.refersToType(RelationType.class), "got type %s", reference.getType());
    return getRelationTypeById(reference.getId());
  }

  public <T extends Relation> String storeRelation(Class<T> type, Reference sourceRef, Reference relTypeRef, Reference targetRef, Change change) {
    RelationBuilder<T> builder = getBuilder(type);

    RelationType relationType = getRelationType(relTypeRef);
    builder.type(relTypeRef);
    /* 
     * If the relationType is symmetric, order the relation on id.
     * This way we can be sure the relation is saved once.  
     */
    if (relationType.isSymmetric() && sourceRef.getId().compareTo(targetRef.getId()) > 0) {
      builder.source(targetRef).target(sourceRef);
    } else {
      builder.source(sourceRef).target(targetRef);
    }
    T relation = builder.build();
    if (relation != null) {
      try {
        if (storageManager.relationExists(relation)) {
          duplicateRelationCount++;
          LOG.debug("Ignored duplicate {}", relation.getDisplayName());
        } else {
          return storageManager.addDomainEntity(type, relation, change);
        }
      } catch (IOException e) {
        LOG.error("Failed to add {}; {}", relation.getDisplayName(), e.getMessage());
      } catch (ValidationException e) {
        LOG.error("Failed to add {}; {}", relation.getDisplayName(), e.getMessage());
      }
    }
    return null;
  }

  // -------------------------------------------------------------------

  private <T extends Relation> RelationBuilder<T> getBuilder(Class<T> type) {
    checkArgument(type != null && type.getSuperclass() == Relation.class);
    return new RelationBuilder<T>(type);
  }

  private class RelationBuilder<T extends Relation> {
    private T relation;

    public RelationBuilder(Class<T> type) {
      try {
        relation = type.newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Failed to create instance of " + type);
      }
    }

    public RelationBuilder<T> type(Reference ref) {
      relation.setTypeRef(ref);
      return this;
    }

    public RelationBuilder<T> source(Reference ref) {
      relation.setSourceRef(ref);
      return this;
    }

    public RelationBuilder<T> target(Reference ref) {
      relation.setTargetRef(ref);
      return this;
    }

    public T build() {
      if (relation.getTypeRef() == null) {
        LOG.error("Missing relation type ref");
        return null;
      }
      if (relation.getSourceType() == null || relation.getSourceId() == null) {
        LOG.error("Missing source ref");
        return null;
      }
      if (relation.getTargetType() == null || relation.getTargetId() == null) {
        LOG.error("Missing target ref");
        return null;
      }
      RelationType relationType = storageManager.getEntity(RelationType.class, relation.getTypeRef().getId());
      if (relationType == null) {
        LOG.error("Unknown relation type {}", relation.getTypeRef().getId());
        return null;
      }
      Class<? extends Entity> sourceType = convertToType(relationType.getSourceTypeName());
      String iname = relation.getSourceType();
      Class<? extends Entity> actualType = registry.getTypeForIName(iname);
      if (sourceType == null || !sourceType.isAssignableFrom(actualType)) {
        LOG.error("Source type {} is incompatible with {}", iname, relationType.getSourceTypeName());
        throw new IllegalArgumentException();
      }
      Class<? extends Entity> targetType = convertToType(relationType.getTargetTypeName());
      iname = relation.getTargetRef().getType();
      actualType = registry.getTypeForIName(iname);
      if (targetType == null || !targetType.isAssignableFrom(actualType)) {
        LOG.error("Target type {} is incompatible with {}", iname, relationType.getTargetTypeName());
        throw new IllegalArgumentException();
      }
      return relation;
    }
  }

  private Class<? extends Entity> convertToType(String iname) {
    return "domainentity".equals(iname) ? DomainEntity.class : registry.getTypeForIName(iname);
  }

  /**
   * Convenience method for deciding assignability of an entity to another entity,
   * given the internal names of the target entity type and the source entity type.
   */
  private boolean isAssignable(Class<? extends DomainEntity> targetType, Class<? extends DomainEntity> sourceType) {
    return targetType != null && sourceType != null && targetType.isAssignableFrom(sourceType);
  }

}
