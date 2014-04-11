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
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.EntityMappers;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Archive;
import nl.knaw.huygens.timbuctoo.model.Archiver;
import nl.knaw.huygens.timbuctoo.model.Collective;
import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.Keyword;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.Legislation;
import nl.knaw.huygens.timbuctoo.model.Location;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Place;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationEntityRef;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
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
    storage.close();
  }

  // ---------------------------------------------------------------------------

  public StorageStatus getStatus() {
    StorageStatus status = new StorageStatus();

    // TODO determine list dynamically
    status.addSystemEntityCount(getCount(RelationType.class));
    status.addSystemEntityCount(getCount(SearchResult.class));
    status.addSystemEntityCount(getCount(User.class));
    status.addSystemEntityCount(getCount(VREAuthorization.class));

    // TODO determine list dynamically
    status.addDomainEntityCount(getCount(Archive.class));
    status.addDomainEntityCount(getCount(Archiver.class));
    status.addDomainEntityCount(getCount(Collective.class));
    status.addDomainEntityCount(getCount(Document.class));
    status.addDomainEntityCount(getCount(Keyword.class));
    status.addDomainEntityCount(getCount(Language.class));
    status.addDomainEntityCount(getCount(Legislation.class));
    status.addDomainEntityCount(getCount(Location.class));
    status.addDomainEntityCount(getCount(Person.class));
    status.addDomainEntityCount(getCount(Place.class));
    status.addDomainEntityCount(getCount(Relation.class));

    return status;
  }

  private KV<Long> getCount(Class<? extends Entity> type) {
    return new KV<Long>(type.getSimpleName(), storage.count(type));
  }

  // --- add entities ----------------------------------------------------------

  public <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws IOException, ValidationException {
    entity.normalize(registry, this);
    entity.validateForAdd(registry, this);
    return storage.addSystemEntity(type, entity);
  }

  public <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws IOException, ValidationException {
    entity.normalize(registry, this);
    entity.validateForAdd(registry, this);
    return storage.addDomainEntity(type, entity, change);
  }

  // --- update entities -------------------------------------------------------

  public <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws IOException {
    entity.normalize(registry, this);
    storage.updateSystemEntity(type, entity);
  }

  public <T extends DomainEntity> void updatePrimitiveDomainEntity(Class<T> type, T entity, Change change) throws IOException {
    checkArgument(TypeRegistry.isPrimitiveDomainEntity(type), "%s must be a primitive domain entity", type.getSimpleName());
    entity.normalize(registry, this);
    storage.updateDomainEntity(type, entity, change);
  }

  public <T extends DomainEntity> void updateProjectDomainEntity(Class<T> type, T entity, Change change) throws IOException {
    checkArgument(!TypeRegistry.isPrimitiveDomainEntity(type), "%s must be a project domain entity", type.getSimpleName());
    entity.normalize(registry, this);
    storage.updateDomainEntity(type, entity, change);
  }

  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws IOException {
    storage.setPID(type, id, pid);
  }

  // --- delete entities -------------------------------------------------------

  public <T extends SystemEntity> int deleteSystemEntities(Class<T> type) throws IOException {
    return storage.deleteAll(type);
  }

  public <T extends SystemEntity> void deleteSystemEntity(T entity) throws IOException {
    storage.deleteSystemEntity(entity.getClass(), entity.getId());
  }

  public <T extends DomainEntity> void deleteDomainEntity(T entity) throws IOException {
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
   * @throws IOException when the storage layer throws an exception it will be forwarded
   */
  public <T extends DomainEntity> void deleteNonPersistent(Class<T> type, List<String> ids) throws IOException {
    storage.deleteNonPersistent(type, ids);
  }

  public int deleteAllSearchResults() throws IOException {
    return storage.deleteAll(SearchResult.class);
  }

  public int deleteSearchResultsBefore(Date date) throws IOException {
    return storage.deleteByDate(SearchResult.class, SearchResult.DATE_FIELD, date);
  }

  // ---------------------------------------------------------------------------

  public <T extends Entity> T getEntity(Class<T> type, String id) {
    try {
      return storage.getItem(type, id);
    } catch (IOException e) {
      LOG.error("Error in getEntity({}.class, {}): " + e.getMessage(), type.getSimpleName(), id);
      return null;
    }
  }

  public <T extends DomainEntity> T getEntityWithRelations(Class<T> type, String id) {
    T entity = null;
    try {
      entity = storage.getItem(type, id);
      addRelationsTo(entity);
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
    }
    return entity;
  }

  public <T extends DomainEntity> T getRevisionWithRelations(Class<T> type, String id, int revision) {
    T entity = null;
    try {
      entity = storage.getRevision(type, id, revision);
      addRelationsTo(entity);
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
    }
    return entity;
  }

  public <T extends Entity> T findEntity(Class<T> type, String key, String value) {
    try {
      return storage.findItemByKey(type, key, value);
    } catch (IOException e) {
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
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), example.getId());
      return null;
    }
  }

  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) {
    try {
      List<T> variations = storage.getAllVariations(type, id);
      for (T variation : variations) {
        addRelationsTo(variation);
      }
      return variations;
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return Collections.emptyList();
    }
  }

  public <T extends Entity> StorageIterator<T> getAll(Class<T> type) {
    return storage.getAllByType(type);
  }

  public <T extends Entity> List<T> getAllByIds(Class<T> type, List<String> ids) {
    return resolveIterator(storage.getAllByIds(type, ids), 0, ids.size());
  }

  public <T extends DomainEntity> RevisionChanges<T> getVersions(Class<T> type, String id) {
    try {
      return storage.getAllRevisions(type, id);
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  /**
   * Retrieves all the id's of type {@code <T>} that does not have a persistent id. 
   * 
   * @param type the type of the id's that should be retrieved
   * @return a list with all the ids.
   * @throws IOException when the storage layer throws an exception it will be forwarded.
   */
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws IOException {
    return storage.getAllIdsWithoutPIDOfType(type);
  }

  public <T extends Entity> List<T> getAllLimited(Class<T> type, int offset, int limit) {
    if (limit == 0) {
      return Collections.<T> emptyList();
    }
    return resolveIterator(storage.getAllByType(type), offset, limit);
  }

  private <T extends Entity> List<T> resolveIterator(StorageIterator<T> iterator, int offset, int limit) {
    if (offset > 0) {
      iterator.skip(offset);
    }
    List<T> list = iterator.getSome(limit);
    iterator.close();
    return list;
  }

  // --- relation types --------------------------------------------------------

  private LoadingCache<String, RelationType> relationTypeCache;

  private void setupRelationTypeCache() {
    relationTypeCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, RelationType>() {
      @Override
      public RelationType load(String id) throws IOException {
        // Not allowed to return null
        RelationType relationType = storage.getItem(RelationType.class, id);
        if (relationType == null) {
          throw new IOException("item does not exist");
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
  public RelationType getRelationTypeById(String id) {
    try {
      return relationTypeCache.get(id);
    } catch (ExecutionException e) {
      LOG.debug("Failed to retrieve relation type {}: {}", id, e.getMessage());
      return null;
    }
  }

  /**
   * Returns a map for retrieving relation types by their regular name.
   */
  public Map<String, RelationType> getRelationTypeMap() {
    Map<String, RelationType> map = Maps.newHashMap();
    StorageIterator<RelationType> iterator = getAll(RelationType.class);
    while (iterator.hasNext()) {
      RelationType type = iterator.next();
      map.put(type.getRegularName(), type);
    }
    return map;
  }

  // --- relations -------------------------------------------------------------

  /**
   * Returns the id's of the relations, connected to the entities with the input id's.
   * The input id's can be the source id as well as the target id of the Relation. 
   * 
   * @param ids a list of id's to find the relations for
   * @return a list of id's of the corresponding relations
   * @throws IOException re-throws the IOExceptions of the storage
   */
  public List<String> getRelationIds(List<String> ids) throws IOException {
    return storage.getRelationIds(ids);
  }

  /**
   * Adds all relations for the specified entity as virtual properties.
   */
  private <T extends DomainEntity> void addRelationsTo(T entity) {
    if (entity != null) {
      String id = entity.getId();
      Class<? extends DomainEntity> type = entity.getClass();
      EntityMapper mapper = entityMappers.getEntityMapper(type);
      checkState(mapper != null, "No EntityMapper for type %s", type);
      @SuppressWarnings("unchecked")
      Class<? extends Relation> mappedType = (Class<? extends Relation>) mapper.map(Relation.class);
      StorageIterator<? extends Relation> iterator = null;
      try {
        iterator = storage.getRelationsForEntityId(mappedType, id); // db access
        while (iterator.hasNext()) {
          Relation relation = iterator.next(); // db access
          RelationType relType = getRelationTypeById(relation.getTypeRef().getId());
          checkState(relType != null, "Failed to retrieve relation type");
          if (relation.hasSource() && relation.hasTarget()) {
            if (relation.hasSourceId(id)) {
              EntityRef entityRef = getEntityRef(mapper, relation.getTargetRef(), relation.getId(), relation.isAccepted(), relation.getRev());
              entity.addRelation(relType.getRegularName(), entityRef); // db access
            } else if (relation.hasTargetId(id)) {
              EntityRef entityRef = getEntityRef(mapper, relation.getSourceRef(), relation.getId(), relation.isAccepted(), relation.getRev());
              entity.addRelation(relType.getInverseName(), entityRef); // db access
            }
          }
        }
      } catch (IOException e) {
        LOG.error("Error while handling {} {}", type.getSimpleName(), id);
      } finally {
        if (iterator != null) {
          iterator.close();
        }
      }
    }
  }

  // Relations are defined between primitive domain entities
  // Map to a domain entity in the package from which an entity is requested
  private EntityRef getEntityRef(EntityMapper mapper, Reference reference, String relationId, boolean accepted, int rev) throws StorageException, IOException {
    String iname = reference.getType();
    Class<? extends DomainEntity> type = TypeRegistry.toDomainEntity(registry.getTypeForIName(iname));
    type = mapper.map(type);
    iname = TypeNames.getInternalName(type);
    String xname = registry.getXNameForIName(iname);
    DomainEntity entity = storage.getItem(type, reference.getId());

    return new RelationEntityRef(iname, xname, reference.getId(), entity.getDisplayName(), relationId, accepted, rev);
  }

}
