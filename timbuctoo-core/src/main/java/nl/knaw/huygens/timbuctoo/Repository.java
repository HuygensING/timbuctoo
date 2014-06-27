package nl.knaw.huygens.timbuctoo;

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
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.EntityMappers;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.neww.WWPerson;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.RelationTypes;
import nl.knaw.huygens.timbuctoo.storage.RevisionChanges;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.storage.StorageStatus;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.util.KV;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Repository {

  private static final Logger LOG = LoggerFactory.getLogger(Repository.class);

  /** Maximum number of relations added to an entity. */
  private static final int DEFAULT_RELATION_LIMIT = 1000;

  private final TypeRegistry registry;
  private final Storage storage;
  private final EntityMappers entityMappers;
  private final RelationTypes relationTypes;

  @Inject
  public Repository(TypeRegistry registry, Storage storage) throws StorageException {
    this.registry = registry;
    this.storage = storage;
    entityMappers = new EntityMappers(registry.getDomainEntityTypes());
    ensureIndexes();
    relationTypes = new RelationTypes(storage);
  }

  public Repository(TypeRegistry registry, Storage storage, RelationTypes relationTypes) throws StorageException {
    this.registry = registry;
    this.storage = storage;
    entityMappers = new EntityMappers(registry.getDomainEntityTypes());
    ensureIndexes();
    this.relationTypes = relationTypes;
  }

  /**
   * Create indexes, if they don't already exist.
   */
  private void ensureIndexes() throws StorageException {
    storage.ensureIndex(false, Relation.class, "^typeId");
    storage.ensureIndex(false, Relation.class, "^sourceId");
    storage.ensureIndex(false, Relation.class, "^targetId");
    storage.ensureIndex(false, Relation.class, "^sourceId", "^targetId");
    storage.ensureIndex(true, Language.class, Language.CODE);
  }

  /**
   * Closes the data store.
   */
  public void close() {
    logCacheStats();
    storage.close();
  }

  public StorageStatus getStatus() {
    StorageStatus status = new StorageStatus();
    for (Class<? extends SystemEntity> type : registry.getSystemEntityTypes()) {
      if (storage.count(type) != 0) {
        status.addSystemEntityStats(getStats(type));
      }
    }
    for (Class<? extends DomainEntity> type : registry.getPrimitiveDomainEntityTypes()) {
      if (storage.count(type) != 0) {
        status.addDomainEntityStats(getStats(type));
      }
    }
    return status;
  }

  private KV<Long> getStats(Class<? extends Entity> type) {
    return new KV<Long>(type.getSimpleName(), storage.count(type), storage.getStatistics(type));
  }

  public TypeRegistry getTypeRegistry() {
    return registry;
  }

  // --- add entities ----------------------------------------------------------

  public <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws StorageException, ValidationException {
    entity.normalize(this);
    entity.validateForAdd(this);
    return storage.addSystemEntity(type, entity);
  }

  public <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws StorageException, ValidationException {
    entity.normalize(this);
    entity.validateForAdd(this);
    return storage.addDomainEntity(type, entity, change);
  }

  // --- update entities -------------------------------------------------------

  public <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws StorageException {
    entity.normalize(this);
    storage.updateSystemEntity(type, entity);
  }

  public <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    entity.normalize(this);
    storage.updateDomainEntity(type, entity, change);
  }

  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws StorageException {
    storage.setPID(type, id, pid);
  }

  /**
   * Ensures that the specified domain entity has a variation of the appropriate type.
   *
   * Even though a variation of an entity may always be retrieved, it may be "virtual", i.e. constructed
   * with default vales for the fields that do not occur in the corresponding primitive domain entity.
   * This method makes sure that the variation is actually stored.
   */
  public <T extends DomainEntity> void ensureVariation(Class<T> type, String id, Change change) throws StorageException {
    T entity = getEntity(type, id);
    if (entity != null && !entity.hasVariation(type)) {
      updateDomainEntity(type, entity, change);
    }
  }

  // --- delete entities -------------------------------------------------------

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
    return storage.deleteSystemEntities(SearchResult.class);
  }

  public int deleteSearchResultsBefore(Date date) throws StorageException {
    return storage.deleteByDate(SearchResult.class, SearchResult.DATE_FIELD, date);
  }

  // ---------------------------------------------------------------------------

  public <T extends Entity> boolean entityExists(Class<T> type, String id) {
    try {
      return storage.entityExists(type, id);
    } catch (StorageException e) {
      LOG.error("Error in entityExists({}, {}): {}", type.getSimpleName(), id, e.getMessage());
      return false;
    }
  }

  public <T extends Entity> T getEntity(Class<T> type, String id) {
    try {
      return storage.getItem(type, id);
    } catch (StorageException e) {
      LOG.error("Error in getEntity({}, {}): {}", type.getSimpleName(), id, e.getMessage());
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

  public <T extends DomainEntity> RevisionChanges<T> getVersions(Class<T> type, String id) {
    try {
      return storage.getAllRevisions(type, id);
    } catch (StorageException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  public <T extends SystemEntity> StorageIterator<T> getSystemEntities(Class<T> type) {
    try {
      return storage.getSystemEntities(type);
    } catch (StorageException e) {
      LOG.error("Failed to retrieve entities of type {}", type);
      return StorageIteratorStub.newInstance();
    }
  }

  public <T extends DomainEntity> StorageIterator<T> getDomainEntities(Class<T> type) {
    try {
      return storage.getDomainEntities(type);
    } catch (StorageException e) {
      LOG.error("Failed to retrieve entities of type {}", type);
      return StorageIteratorStub.newInstance();
    }
  }

  public <T extends Entity> StorageIterator<T> getEntitiesByProperty(Class<T> type, String field, String value) {
    try {
      return storage.getEntitiesByProperty(type, field, value);
    } catch (StorageException e) {
      // TODO handle properly
      return StorageIteratorStub.newInstance();
    }
  }

  /**
   * Retrieves all the id's of type {@code <T>} that does not have a persistent id. 
   * 
   * @param type the type of the id's that should be retrieved
   * @return a list with all the ids.
   * @throws StorageException when the storage layer throws an exception it will be forwarded.
   */
  public <T extends DomainEntity> List<String> getAllIdsWithoutPID(Class<T> type) throws StorageException {
    return storage.getAllIdsWithoutPIDOfType(type);
  }

  // --- relation types --------------------------------------------------------

  public void logCacheStats() {
    relationTypes.logCacheStats();
  }

  /**
   * Returns the relation type with the specified id,
   * or {@code null} if no such relation type exists.
   */
  public RelationType getRelationTypeById(String id) {
    return relationTypes.getById(id);
  }

  /**
   * Returns the relation type with the specified name, regular or inverse,
   * or {@code null} if no such relation type exists.
   */
  public RelationType getRelationTypeByName(String name) {
    return relationTypes.getByName(name);
  }

  // --- relations -------------------------------------------------------------

  /**
   * Returns a relation instance with source, target and relation type id's as in the specified relation.
   * Returns null if either of these id's is null, or if no such relation is present in the store.
   */
  public <T extends Relation> T findRelation(Class<T> type, T relation) throws StorageException {
    return storage.findRelation(type, relation.getSourceId(), relation.getTargetId(), relation.getTypeId());
  }

  /**
   * Returns the id's of the relations that satisfy the following requirements:<Ul>
   * <li>the source id occurs in the {@code sourceIds} list;</li>
   * <li>the target id occurs in the {@code targetIds} list;</li>
   * <li>the relation type id occurs in the {@code relationTypeIds} list.</li>
   * </ul>
   */
  public <T extends Relation> List<String> findRelations(Class<T> type, List<String> sourceIds, List<String> targetIds, List<String> relationTypeIds) throws StorageException {
    return storage.findRelationIds(type, sourceIds, targetIds, relationTypeIds);
  }

  /**
   * Returns the id's of the relations, connected to the entities with the input id's.
   * The input id's can be the source id as well as the target id of the Relation. 
   */
  public List<String> getRelationIds(List<String> ids) throws StorageException {
    return storage.getRelationIds(ids);
  }

  public List<Relation> getRelationsByEntityId(String id, int limit) throws StorageException {
    return storage.getRelationsByEntityId(Relation.class, id).getSome(limit);
  }

  /**
   * Get all the relations that have the type in {@code relationTypIds}.
   * @param variation the project specific variation of the relation to get.
   * @param relationTypeIds the relation type should be in this collection.
   * @return a collection with the found relations.
   * @throws StorageException 
   */
  public <T extends Relation> List<T> getRelationsByType(Class<T> variation, List<String> relationTypeIds) throws StorageException {
    return storage.getRelationsByType(variation, relationTypeIds);
  }

  /**
   * Get all the relation types that have a name in the relationNames collection. 
   * @param relationTypeNames collection to get the relation types for.
   * @return the found relation types.
   */
  public List<String> getRelationTypeIdsByName(List<String> relationTypeNames) {
    return relationTypes.getRelationTypeIdsByName(relationTypeNames);
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
        RelationType relType = getRelationTypeById(relation.getTypeId());
        checkState(relType != null, "Failed to retrieve relation type");
        if (relation.hasSourceId(entityId)) {
          RelationRef ref = newRelationRef(mapper, relation.getTargetRef(), relation.getId(), relation.isAccepted(), relation.getRev());
          entity.addRelation(relType.getRegularName(), ref);
        } else if (relation.hasTargetId(entityId)) {
          RelationRef ref = newRelationRef(mapper, relation.getSourceRef(), relation.getId(), relation.isAccepted(), relation.getRev());
          entity.addRelation(relType.getInverseName(), ref);
        }
      }
      addDerivedRelationsTo(entity);
    }
  }

  // Relations are defined between primitive domain entities
  // Map to a domain entity in the package from which an entity is requested
  private RelationRef newRelationRef(EntityMapper mapper, Reference reference, String relationId, boolean accepted, int rev) throws StorageException {
    String iname = reference.getType();
    Class<? extends DomainEntity> type = registry.getDomainEntityType(iname);
    type = mapper.map(type);
    iname = TypeNames.getInternalName(type);
    String xname = registry.getXNameForIName(iname);
    DomainEntity entity = storage.getItem(type, reference.getId());

    return new RelationRef(iname, xname, reference.getId(), entity.getDisplayName(), relationId, accepted, rev);
  }

  // Implements a single derived relation
  // Note that it pertains to WWPerson only
  private <T extends DomainEntity> void addDerivedRelationsTo(T entity) throws StorageException {
    if (entity.getClass() == WWPerson.class) {
      Set<String> languageIds = Sets.newHashSet();
      String isCreatedById = relationTypes.getByName("isCreatedBy").getId();
      String hasWorkLanguageId = relationTypes.getByName("hasWorkLanguage").getId();

      // if other relations are already attached, we don't need this query...
      StorageIterator<Relation> iterator1 = storage.findRelations(Relation.class, null, entity.getId(), isCreatedById);
      while (iterator1.hasNext()) {
        Relation relation1 = iterator1.next();
        StorageIterator<Relation> iterator2 = storage.findRelations(Relation.class, relation1.getSourceId(), null, hasWorkLanguageId);
        while (iterator2.hasNext()) {
          Relation relation2 = iterator2.next();
          languageIds.add(relation2.getTargetId());
        }
        iterator2.close();
      }
      iterator1.close();

      for (String languageId : languageIds) {
        Language language = storage.getItem(Language.class, languageId);
        if (language != null) {
          RelationRef ref = new RelationRef("language", "languages", languageId, language.getDisplayName(), null, true, 0);
          entity.addRelation("hasPersonLanguage", ref);
        }
      }
    }
  }

  // --- languages -------------------------------------------------------------

  public <T extends Language> T getLanguageByCode(Class<T> type, String code) {
    return findEntity(type, Language.CODE, code);
  }

}
