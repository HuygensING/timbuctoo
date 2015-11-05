package nl.knaw.huygens.timbuctoo;

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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.EntityMappers;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DerivedProperty;
import nl.knaw.huygens.timbuctoo.model.DerivedRelationDescription;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.RelationTypes;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.storage.StorageStatus;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.util.KV;
import nl.knaw.huygens.timbuctoo.util.RelationRefAdder;
import nl.knaw.huygens.timbuctoo.util.RelationRefAdderFactory;
import nl.knaw.huygens.timbuctoo.util.RepositoryException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkState;
import static nl.knaw.huygens.timbuctoo.DerivedRelation.aDerivedRelation;

@Singleton
public class Repository {

  private static final Logger LOG = LoggerFactory.getLogger(Repository.class);

  private static final boolean REQUIRED = true;

  private final TypeRegistry registry;
  private final Storage storage;
  private final EntityMappers entityMappers;
  private final RelationTypes relationTypes;
  private final RelationRefAdderFactory relationRefCreatorFactory;

  @Inject
  public Repository(TypeRegistry registry, Storage storage, RelationRefAdderFactory relationRefCreatorFactory, RelationTypes relationTypes) throws StorageException {
    this.registry = registry;
    this.storage = storage;
    this.relationRefCreatorFactory = relationRefCreatorFactory;
    this.relationTypes = relationTypes;
    entityMappers = new EntityMappers(registry.getDomainEntityTypes());
    createIndexes();

  }

  /**
   * Create indexes, if they don't already exist.
   */
  private void createIndexes() throws StorageException {
    storage.createIndex(false, Relation.class, Relation.TYPE_ID);
    storage.createIndex(false, Relation.class, Relation.SOURCE_ID);
    storage.createIndex(false, Relation.class, Relation.TARGET_ID);
    storage.createIndex(false, Relation.class, Relation.SOURCE_ID, Relation.TARGET_ID);
    storage.createIndex(true, Language.class, Language.CODE);
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
   * <p/>
   * Even though a variation of an entity may always be retrieved, it may be "virtual", i.e. constructed
   * with default vales for the fields that do not occur in the corresponding primitive domain entity.
   * This method makes sure that the variation is actually stored.
   */
  public <T extends DomainEntity> void ensureVariation(Class<T> type, String id, Change change) throws StorageException {
    T entity = getEntityOrDefaultVariation(type, id);
    if (entity != null && !entity.hasVariation(type)) {
      updateDomainEntity(type, entity, change);
    }
  }

  // --- delete entities -------------------------------------------------------

  public <T extends SystemEntity> void deleteSystemEntity(T entity) throws StorageException {
    storage.deleteSystemEntity(entity.getClass(), entity.getId());
  }

  /**
   * Deletes a DomainEntity from the database. When the DomainEntity is a primitive the complete DomainEntity
   * and it's Relations are removed. When the DomainEntity is a project variation the
   * Variation is removed and the Relations are declined for the project.
   * <p/>
   * TODO: Make available for deleting primitives:
   * - The PID's should be updated
   * - The versions should be deleted
   * - The relations should be deleted:
   * Update PID's of the relations
   *
   * @param entity
   * @return
   * @throws StorageException
   */
  public <T extends DomainEntity> List<String> deleteDomainEntity(T entity) throws StorageException {
    String id = entity.getId();
    Change change = entity.getModified();
    Class<? extends DomainEntity> type = entity.getClass();
    if (TypeRegistry.isPrimitiveDomainEntity(type)) {
      //storage.deleteDomainEntity(type, id, change);
      //storage.deleteRelationsOfEntity(Relation.class, id);
      // TODO Remove versions and update PID's
      throw new UnsupportedOperationException("Not yet available for primitives yet");
    } else {
      storage.deleteVariation(type, id, change);
      EntityMapper mapper = entityMappers.getEntityMapper(type);
      @SuppressWarnings("unchecked")
      Class<? extends Relation> relation = (Class<? extends Relation>) mapper.map(Relation.class);
      storage.declineRelationsOfEntity(relation, id, change);
      return storage.getRelationIds(Lists.newArrayList(id));
    }
  }

  /**
   * Deletes non-persistent domain entities with the specified type and id's..
   * The idea behind this method is that domain entities without persistent identifier are not validated yet.
   * After a bulk import non of the imported entity will have a persistent identifier, until a user has agreed with the imported collection.
   *
   * @param <T>  extends {@code DomainEntity}, because system entities have no persistent identifiers.
   * @param type the type all of the objects should removed permanently from
   * @param ids  the id's to remove permanently
   * @throws StorageException when the storage layer throws an exception it will be forwarded
   */
  public <T extends DomainEntity> void deleteNonPersistent(Class<T> type, List<String> ids) throws StorageException {
    storage.deleteNonPersistent(type, ids);
  }

  public int deleteAllSearchResults() throws StorageException {
    return storage.deleteSystemEntities(SearchResult.class);
  }

  public int deleteSearchResultsBefore(Date date) throws StorageException {
    return storage.deleteByModifiedDate(SearchResult.class, date);
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

  public <T extends Entity> T getEntityOrDefaultVariation(Class<T> type, String id) {
    try {
      return storage.getEntityOrDefaultVariation(type, id);
    } catch (StorageException e) {
      LOG.error("Error in getEntity({}, {}): {}", type.getSimpleName(), id, e.getMessage());
      return null;
    }
  }

  public <T extends DomainEntity> T getEntityOrDefaultVariationWithRelations(Class<T> type, String id) {
    T entity = null;
    try {
      entity = storage.getEntityOrDefaultVariation(type, id);
      if (entity != null) {
        addRelationsToEntity(entity);
      }
    } catch (StorageException e) {
      logError("getEntityWithRelations", e, type, id);
    }
    return entity;
  }

  private <T extends DomainEntity> void logError(String action, StorageException e, Class<T> type, String id) {
    LOG.error("Error while handling ({}) {} {}", action, type.getName(), id);
    LOG.debug("Exception", e);
  }

  private <T extends Entity> void logError(String action, Class<T> type, StorageException e) {
    LOG.error("Error while handling ({}) {}", action, type.getName());
    LOG.debug("Exception", e);
  }

  public <T extends DomainEntity> T getRevisionWithRelations(Class<T> type, String id, int revision) {
    T entity = null;
    try {
      entity = storage.getRevision(type, id, revision);
      if (entity != null) {
        addRelationsToEntity(entity);
      }
    } catch (StorageException e) {
      logError("getRevisionWithRelations", e, type, id);
    }
    return entity;
  }

  public <T extends Entity> T findEntity(Class<T> type, String field, String value) {
    try {
      return storage.findItemByProperty(type, field, value);
    } catch (StorageException e) {
      logError("findEntity by field", type, e);
      return null;
    }
  }

  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) {
    try {
      List<T> variations = storage.getAllVariations(type, id);
      for (T variation : variations) {
        addRelationsToEntity(variation);
      }
      return variations;
    } catch (StorageException e) {
      logError("getAllVariations", e, type, id);
      return Collections.emptyList();
    }
  }

  public <T extends DomainEntity> void addRelationsToEntity(T entity) throws StorageException {
    // entity.addRelations(this, DEFAULT_RELATION_LIMIT, entityMappers, relationRefCreator);
    addRelationsTo(entity, 1000, entityMappers);
  }

  public <T extends DomainEntity> List<T> getVersions(Class<T> type, String id) {
    try {
      return storage.getAllRevisions(type, id);
    } catch (StorageException e) {
      logError("getVersions", e, type, id);
      return Lists.newArrayList();
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
   * Retrieves the relation type with the specified id.
   *
   * @throws IllegalStateException when the relation type is required and does not exist.
   */
  public RelationType getRelationTypeById(String id, boolean required) throws IllegalStateException {
    return relationTypes.getById(id, required);
  }

  /**
   * Retrieves the relation type with the specified name, regular or inverse.
   *
   * @throws IllegalStateException when the relation type is required and does not exist.
   */
  public RelationType getRelationTypeByName(String name, boolean required) throws IllegalStateException {
    return relationTypes.getByName(name, required);
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
   * Returns the id's of the relations, connected to the entities with the input id's.
   * The input id's can be the source id as well as the target id of the Relation.
   */
  public List<String> getRelationIds(List<String> ids) throws StorageException {
    return storage.getRelationIds(ids);
  }

  public List<Relation> getRelationsByEntityId(String id, int limit) throws StorageException {
    return storage.getRelationsByEntityId(Relation.class, id).getSome(limit);
  }

  private List<? extends Relation> getRelationsByEntityId(String entityId, int limit, Class<? extends Relation> type) throws StorageException {
    return storage.getRelationsByEntityId(type, entityId).getSome(limit);
  }

  /**
   * Get all the relations that have the type in {@code relationTypIds}.
   *
   * @param variation       the project specific variation of the relation to get.
   * @param relationTypeIds the relation type should be in this collection.
   * @return a collection with the found relations.
   * @throws StorageException
   * @deprecated will be removed when the MongoRelationSearcher will be removed.
   */
  @Deprecated
  public <T extends Relation> List<T> getRelationsByType(Class<T> variation, List<String> relationTypeIds) throws StorageException {
    return storage.getRelationsByType(variation, relationTypeIds);
  }

  /**
   * Get all the relation types that have a name in the relationNames collection.
   *
   * @param relationTypeNames collection to get the relation types for.
   * @return the found relation types.
   */
  public List<String> getRelationTypeIdsByName(List<String> relationTypeNames) {
    return relationTypes.getRelationTypeIdsByName(relationTypeNames);
  }

  /**
   * Adds relations for the specified entity as virtual properties.
   * <p/>
   * NOTE We retrieve relations where the entity is source or target with one query;
   * handling them separately would cause complications with reflexive relations.
   *
   * @param entityMappers
   */
  private <T extends DomainEntity> void addRelationsTo(T entity, int limit, EntityMappers entityMappers) throws StorageException {
    if (entity != null && limit > 0) {
      String entityId = entity.getId();
      Class<? extends DomainEntity> entityType = entity.getClass();
      EntityMapper mapper = entityMappers.getEntityMapper(entityType);
      checkState(mapper != null, "No EntityMapper for type %s", entityType);
      @SuppressWarnings("unchecked")
      Class<? extends Relation> mappedType = (Class<? extends Relation>) mapper.map(Relation.class);
      RelationRefAdder relationRefCreator = relationRefCreatorFactory.create(mappedType);

      for (Relation relation : getRelationsByEntityId(entityId, limit, mappedType)) {
        RelationType relType = getRelationTypeById(relation.getTypeId(), REQUIRED);

        relationRefCreator.addRelation(entity, mapper, relation, relType);
      }
      addDerivedRelations(entity, mapper, relationRefCreator);
    }
  }

  public StorageIterator<Relation> findRelations(String sourceId, String targetId, String relationTypeId) throws StorageException {
    return storage.findRelations(Relation.class, sourceId, targetId, relationTypeId);
  }

  /**
   * Returns all relations for the entity with the specified id with the specified relation type id.
   * If {@code regular} is true the entity must be the source entity, else it must be the target entity.
   */
  private List<Relation> findRelations(String entityId, String relationTypeId, boolean regular) throws StorageException {
    if (regular) {
      return storage.findRelations(Relation.class, entityId, null, relationTypeId).getAll();
    } else {
      return storage.findRelations(Relation.class, null, entityId, relationTypeId).getAll();
    }
  }

  /**
   * Adds derived relations for the specified entity.
   * Makes sure each relation is added only once.
   *
   * @param relationRefCreator TODO
   */
  private <T extends DomainEntity> void addDerivedRelations(T entity, EntityMapper mapper, RelationRefAdder relationRefCreator) throws StorageException {
    String sourceTypeName = TypeNames.getInternalName(entity.getClass());
    String sourceId = entity.getId();

    for (DerivedRelationDescription drDescription : entity.getDerivedRelationDescriptions()) {
      Set<DerivedRelation> derivedRelations = Sets.newHashSet();

      RelationType relationType = getRelationTypeByName(drDescription.getSecundaryTypeName(), REQUIRED);
      boolean regular = relationType.getRegularName().equals(drDescription.getSecundaryTypeName());
      for (RelationRef ref : entity.getRelations(drDescription.getPrimaryTypeName())) {
        for (Relation relation : findRelations(ref.getId(), relationType.getId(), regular)) {
          DerivedRelation derivedRelation = aDerivedRelation() //
            .withDescription(drDescription) //
            .withSource(sourceTypeName, sourceId);
          if (regular) {
            derivedRelations.add(derivedRelation //
              .withTarget(relationType.getTargetTypeName(), relation.getTargetId()));
          } else {
            derivedRelations.add(derivedRelation //
              .withTarget(relationType.getSourceTypeName(), relation.getSourceId()));
          }

        }
      }

      for (DerivedRelation derivedRelation : derivedRelations) {
        relationRefCreator.addRelation(entity, mapper, derivedRelation, derivedRelation.getRelationType());
      }

    }
  }

  public <T extends DomainEntity> void addDerivedProperties(VRE vre, T entity) {
    for (DerivedProperty property : entity.getDerivedProperties()) {
      try {
        String relationName = property.getRelationName();
        RelationType relationType = getRelationTypeByName(relationName, REQUIRED);
        boolean regular = relationType.getRegularName().equals(relationName);
        List<Relation> list = findRelations(entity.getId(), relationType.getId(), regular);
        if (!list.isEmpty()) {
          Relation relation = list.get(0);
          String iname = regular ? relation.getTargetType() : relation.getSourceType();
          Class<? extends DomainEntity> type = vre.mapTypeName(iname, REQUIRED);
          String id = regular ? relation.getTargetId() : relation.getSourceId();
          Object value = type.getMethod(property.getAccessor()).invoke(getEntityOrDefaultVariation(type, id));
          entity.addProperty(property.getPropertyName(), value);
        }
      } catch (Exception e) {
        LOG.error("Failed to add derived property: {}", e.getMessage());
      }
    }
  }

  // --- languages -------------------------------------------------------------

  public <T extends Language> T getLanguageByCode(Class<T> type, String code) {
    return findEntity(type, Language.CODE, code);
  }

  /**
   * Checks of a variation of the entity.
   *
   * @param type the type of the variation
   * @param id   the id of the entity, that should contain the variation
   * @return true if the variation exist false if not.
   * @throws StorageException wrapped exception around the database exceptions
   */
  public boolean doesVariationExist(Class<? extends DomainEntity> type, String id) throws StorageException {
    return storage.doesVariationExist(type, id);
  }

  public <T extends DomainEntity> List<T> getAllRevisions(Class<T> type, String id) throws StorageException {
    return storage.getAllRevisions(type, id);
  }

  public Iterator<RelationType> getRelationTypes(Class<? extends DomainEntity> sourceType, Class<? extends DomainEntity> targetType) throws RepositoryException {

    try {
      StorageIterator<RelationType> relationTypes = storage.getSystemEntities(RelationType.class);
      IsRelationTypeBetween isRelationTypeBetween = new IsRelationTypeBetween(sourceType, targetType);

      UnmodifiableIterator<RelationType> filteredRelationTypes = Iterators.filter(relationTypes, relationType -> isRelationTypeBetween.test(relationType));

      return filteredRelationTypes;
    } catch (StorageException e) {
      throw new RepositoryException(e);
    }
  }

  private static class IsRelationTypeBetween implements Predicate<RelationType> {

    private final String typeName1;
    private final String typeName2;

    public IsRelationTypeBetween(Class<? extends DomainEntity> type1, Class<? extends DomainEntity> type2) {
      typeName1 = TypeNames.getInternalName(TypeRegistry.toBaseDomainEntity(type1));
      typeName2 = TypeNames.getInternalName(TypeRegistry.toBaseDomainEntity(type2));
    }


    @Override
    public boolean test(RelationType relationType) {
      return isRegularMatch(relationType) || isInverseMatch(relationType);
    }

    private boolean isInverseMatch(RelationType relationType) {
      return typeName1.equals(relationType.getTargetTypeName()) && typeName2.equals(relationType.getSourceTypeName());
    }

    private boolean isRegularMatch(RelationType relationType) {
      return typeName1.equals(relationType.getSourceTypeName()) && typeName2.equals(relationType.getTargetTypeName());
    }
  }
}
