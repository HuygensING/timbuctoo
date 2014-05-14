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

import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.EntityMappers;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationEntityRef;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.util.KV;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StorageManager {

  private static final Logger LOG = LoggerFactory.getLogger(StorageManager.class);

  /** Maximum number of relations added to an entity. */
  private static final int DEFAULT_RELATION_LIMIT = 100;

  private final TypeRegistry registry;
  private final Storage storage;
  private final EntityMappers entityMappers;

  @Inject
  public StorageManager(TypeRegistry registry, Storage storage) {
    this.registry = registry;
    this.storage = storage;
    entityMappers = new EntityMappers(registry.getDomainEntityTypes());
    setupRelationTypeCache();
  }

  /**
   * Closes the data store.
   */
  public void close() {
    logCacheStats();
    storage.close();
  }

  // ---------------------------------------------------------------------------

  public StorageStatus getStatus() {
    StorageStatus status = new StorageStatus();
    for (Class<? extends SystemEntity> type : registry.getSystemEntityTypes()) {
      status.addSystemEntityCount(getCount(type));
    }
    for (Class<? extends DomainEntity> type : registry.getPrimitiveDomainEntityTypes()) {
      status.addDomainEntityCount(getCount(type));
    }
    return status;
  }

  private KV<Long> getCount(Class<? extends Entity> type) {
    return new KV<Long>(type.getSimpleName(), storage.count(type));
  }

  // --- add entities ----------------------------------------------------------

  public <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws StorageException, ValidationException {
    entity.normalize(registry, this);
    entity.validateForAdd(registry, this);
    return storage.addSystemEntity(type, entity);
  }

  public <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws StorageException, ValidationException {
    entity.normalize(registry, this);
    entity.validateForAdd(registry, this);
    return storage.addDomainEntity(type, entity, change);
  }

  // --- update entities -------------------------------------------------------

  public <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws StorageException {
    entity.normalize(registry, this);
    storage.updateSystemEntity(type, entity);
  }

  public <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    entity.normalize(registry, this);
    storage.updateDomainEntity(type, entity, change);
  }

  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws StorageException {
    storage.setPID(type, id, pid);
  }

  // --- delete entities -------------------------------------------------------

  public <T extends SystemEntity> int deleteSystemEntities(Class<T> type) throws StorageException {
    return storage.deleteAll(type);
  }

  public <T extends SystemEntity> void deleteSystemEntity(T entity) throws StorageException {
    storage.deleteSystemEntity(entity.getClass(), entity.getId());
  }

  public <T extends DomainEntity> void deleteDomainEntity(T entity) throws StorageException {
    storage.deleteDomainEntity(entity.getClass(), entity.getId(), entity.getModified());
  }

  /**
   * Deletes non-persistent domain entities with the specified type and id's..
   * The idea behind this method is that domain entities without persistent identifier are not validated yet.
   * After a bulk import non of the imported entity will have a persistent identifier, until a user has agreed with the imported collection.  
   * 
   * @param <T> extends {@code DomainEntity}, because system entities have no persistent identifiers.
   * @param type the type all of the objects should removed permanently from
   * @param ids the id's to remove permanently
   * @throws StorageException when the storage layer throws an exception it will be forwarded
   */
  public <T extends DomainEntity> void deleteNonPersistent(Class<T> type, List<String> ids) throws StorageException {
    storage.deleteNonPersistent(type, ids);
  }

  public int deleteAllSearchResults() throws StorageException {
    return storage.deleteAll(SearchResult.class);
  }

  public int deleteSearchResultsBefore(Date date) throws StorageException {
    return storage.deleteByDate(SearchResult.class, SearchResult.DATE_FIELD, date);
  }

  // ---------------------------------------------------------------------------

  public <T extends Entity> T getEntity(Class<T> type, String id) {
    try {
      return storage.getItem(type, id);
    } catch (StorageException e) {
      LOG.error("Error in getEntity({}.class, {}): " + e.getMessage(), type.getSimpleName(), id);
      return null;
    }
  }

  public <T extends DomainEntity> T getEntityWithRelations(Class<T> type, String id) {
    T entity = null;
    try {
      entity = storage.getItem(type, id);
      addRelationsTo(entity, DEFAULT_RELATION_LIMIT);
    } catch (StorageException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
    }
    return entity;
  }

  public <T extends DomainEntity> T getRevisionWithRelations(Class<T> type, String id, int revision) {
    T entity = null;
    try {
      entity = storage.getRevision(type, id, revision);
      addRelationsTo(entity, DEFAULT_RELATION_LIMIT);
    } catch (StorageException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
    }
    return entity;
  }

  public <T extends Entity> T findEntity(Class<T> type, String field, String value) {
    try {
      return storage.findItemByProperty(type, field, value);
    } catch (StorageException e) {
      LOG.error("Error while handling {}", type.getName());
      return null;
    }
  }

  /**
   * Returns a single entity matching the non-null fields of
   * the specified entity, or null if no such entity exists.
   */
  public <T extends Entity> T findEntity(Class<T> type, T example) {
    try {
      return storage.findItem(type, example);
    } catch (StorageException e) {
      LOG.error("Error while handling {} {}", type.getName(), example.getId());
      return null;
    }
  }

  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) {
    try {
      List<T> variations = storage.getAllVariations(type, id);
      for (T variation : variations) {
        addRelationsTo(variation, DEFAULT_RELATION_LIMIT);
      }
      return variations;
    } catch (StorageException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return Collections.emptyList();
    }
  }

  public <T extends Entity> StorageIterator<T> getEntities(Class<T> type) {
    try {
      return storage.getEntities(type);
    } catch (StorageException e) {
      // TODO handle properly
      return null;
    }
  }

  public <T extends DomainEntity> RevisionChanges<T> getVersions(Class<T> type, String id) {
    try {
      return storage.getAllRevisions(type, id);
    } catch (StorageException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  /**
   * Retrieves all the id's of type {@code <T>} that does not have a persistent id. 
   * 
   * @param type the type of the id's that should be retrieved
   * @return a list with all the ids.
   * @throws StorageException when the storage layer throws an exception it will be forwarded.
   */
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws StorageException {
    return storage.getAllIdsWithoutPIDOfType(type);
  }

  public <T extends Entity> List<T> getAllLimited(Class<T> type, int offset, int limit) {
    try {
      return storage.getEntities(type).skip(offset).getSome(limit);
    } catch (StorageException e) {
      // TODO handle properly
      return null;
    }
  }

  public <T extends Entity> List<T> getEntitiesByProperty(Class<T> type, String field, String value) {
    try {
      return storage.getEntitiesByProperty(type, field, value).getAll();
    } catch (StorageException e) {
      // TODO handle properly
      return null;
    }
  }

  // --- relation types --------------------------------------------------------

  private LoadingCache<String, RelationType> relationTypeCache;

  private void setupRelationTypeCache() {
    relationTypeCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, RelationType>() {
      @Override
      public RelationType load(String id) throws StorageException {
        // Not allowed to return null
        RelationType relationType = storage.getItem(RelationType.class, id);
        if (relationType == null) {
          throw new StorageException("item does not exist");
        }
        return relationType;
      }
    });
  }

  public void logCacheStats() {
    if (relationTypeCache != null) {
      LOG.info("RelationType {}", relationTypeCache.stats());
    }
  }

  /**
   * Returns the relation type with the specified id,
   * or {@code null} if no such relation type exists.
   */
  public RelationType getRelationType(String id) {
    try {
      return relationTypeCache.get(id);
    } catch (ExecutionException e) {
      LOG.debug("Failed to retrieve relation type {}: {}", id, e.getMessage());
      return null;
    }
  }

  /*
   * Returns a map for retrieving relation types by their regular name.
   */
  public Map<String, RelationType> getRelationTypeMap() {
    Map<String, RelationType> map = Maps.newHashMap();
    for (RelationType type : getEntities(RelationType.class).getAll()) {
      map.put(type.getRegularName(), type);
    }
    return map;
  }

  // --- relations -------------------------------------------------------------

  /**
   * Returns the id's of the relations that satisfy the following requirements:<Ul>
   * <li>the source id occurs in the {@code sourceIds} list;</li>
   * <li>the target id occurs in the {@code targetIds} list;</li>
   * <li>the relation type id occurs in the {@code relationTypeIds} list.</li>
   * </ul>
   */
  public <T extends Relation> List<String> findRelations(Class<T> type, List<String> sourceIds, List<String> targetIds, List<String> relationTypeIds) throws StorageException {
    return storage.findRelations(type, sourceIds, targetIds, relationTypeIds);
  }

  /**
   * Returns the id's of the relations, connected to the entities with the input id's.
   * The input id's can be the source id as well as the target id of the Relation. 
   */
  public List<String> getRelationIds(List<String> ids) throws StorageException {
    return storage.getRelationIds(ids);
  }
  /**
   * Adds relations for the specified entity as virtual properties.
   *
   * NOTE We retrieve relations where the entity is source or target with one query;
   * handling them separately would cause complications with reflexive relations.
   */
  private <T extends DomainEntity> void addRelationsTo(T entity, int limit) throws StorageException {
    if (entity != null && limit > 0) {
      String entityId = entity.getId();
      Class<? extends DomainEntity> entityType = entity.getClass();
      EntityMapper mapper = entityMappers.getEntityMapper(entityType);
      checkState(mapper != null, "No EntityMapper for type %s", entityType);
      @SuppressWarnings("unchecked")
      Class<? extends Relation> mappedType = (Class<? extends Relation>) mapper.map(Relation.class);
      for (Relation relation : storage.getRelationsByEntityId(mappedType, entityId).getSome(limit)) {
        RelationType relType = getRelationType(relation.getTypeId());
        checkState(relType != null, "Failed to retrieve relation type");
        if (relation.hasSourceId(entityId)) {
          EntityRef entityRef = getEntityRef(mapper, relation.getTargetRef(), relation.getId(), relation.isAccepted(), relation.getRev());
          entity.addRelation(relType.getRegularName(), entityRef);
        } else if (relation.hasTargetId(entityId)) {
          EntityRef entityRef = getEntityRef(mapper, relation.getSourceRef(), relation.getId(), relation.isAccepted(), relation.getRev());
          entity.addRelation(relType.getInverseName(), entityRef);
        }
      }
    }
  }

  // Relations are defined between primitive domain entities
  // Map to a domain entity in the package from which an entity is requested
  private EntityRef getEntityRef(EntityMapper mapper, Reference reference, String relationId, boolean accepted, int rev) throws StorageException {
    String iname = reference.getType();
    Class<? extends DomainEntity> type = registry.getDomainEntityType(iname);
    type = mapper.map(type);
    iname = TypeNames.getInternalName(type);
    String xname = registry.getXNameForIName(iname);
    DomainEntity entity = storage.getItem(type, reference.getId());

    return new RelationEntityRef(iname, xname, reference.getId(), entity.getDisplayName(), relationId, accepted, rev);
  }

  // --- languages -------------------------------------------------------------

  public <T extends Language> T getLanguageByCode(Class<T> type, String code) {
    return findEntity(type, Language.CODE, code);
  }

}
