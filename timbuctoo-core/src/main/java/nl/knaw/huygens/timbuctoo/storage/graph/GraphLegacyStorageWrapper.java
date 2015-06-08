package nl.knaw.huygens.timbuctoo.storage.graph;

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toBaseDomainEntity;
import static nl.knaw.huygens.timbuctoo.model.DomainEntity.PID;

import java.util.Date;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.NoSuchRelationException;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class GraphLegacyStorageWrapper implements Storage {

  private static final Class<Relation> RELATION_TYPE = Relation.class;
  private final GraphStorage graphStorage;
  private final IdGenerator idGenerator;
  private TimbuctooQueryFactory queryFactory;

  @Inject
  public GraphLegacyStorageWrapper(GraphStorage graphStorage, TimbuctooQueryFactory queryFactory) {
    this(graphStorage, queryFactory, new IdGenerator());
  }

  public GraphLegacyStorageWrapper(GraphStorage graphStorage, TimbuctooQueryFactory queryFactory, IdGenerator idGenerator) {
    this.graphStorage = graphStorage;
    this.idGenerator = idGenerator;
    this.queryFactory = queryFactory;
  }

  @Override
  public void createIndex(boolean unique, Class<? extends Entity> type, String... fields) throws StorageException {
    // FIXME indexes should be created in a different way for GraphStorage TIM-109
    //    throw new UnsupportedOperationException("Yet to be implemented");

  }

  @Override
  public <T extends Entity> String getStatistics(Class<T> type) {
    // FIXME TIM-121 What kind of information do we want to show?
    // We cannot reproduce information similar to Mongo's.
    return "";
  }

  @Override
  public void close() {
    graphStorage.close();
  }

  @Override
  public boolean isAvailable() {
    return graphStorage.isAvailable();
  }

  @Override
  public <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws StorageException {
    String id = addAdministrativeValues(type, entity);
    graphStorage.addSystemEntity(type, entity);
    return id;
  }

  @Override
  public <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    removePIDFromEntity(entity); // to make sure no bogus PID is set to the entity
    String id = addAdministrativeValues(type, entity);

    if (isRelation(type)) {
      graphStorage.addRelation(asRelation(type), (Relation) entity, change);
    } else {
      graphStorage.addDomainEntity(type, entity, change);
    }

    return id;
  }

  /**
   * Adds the administrative values to the entity.
   * @param type the type to generate the id for
   * @param entity the entity to add the values to
   * @return the generated id
   */
  private <T extends Entity> String addAdministrativeValues(Class<T> type, T entity) {
    String id = idGenerator.nextIdFor(type);
    Change change = Change.newInternalInstance();

    entity.setCreated(change);
    entity.setModified(change);
    entity.setId(id);
    updateRevision(entity);

    return id;
  }

  private <T extends DomainEntity> void removePIDFromEntity(T entity) {
    entity.setPid(null);
  }

  private <T extends Entity> void updateAdministrativeValues(T entity) {
    entity.setModified(Change.newInternalInstance());
    updateRevision(entity);
  }

  private <T extends Entity> void updateRevision(T entity) {
    int rev = entity.getRev();
    entity.setRev(++rev);
  }

  @Override
  public <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws StorageException {
    updateAdministrativeValues(entity);
    graphStorage.updateEntity(type, entity);
  }

  @Override
  public <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    updateAdministrativeValues(entity);
    if (isRelation(type)) {
      Class<? extends Relation> relationType = asRelation(type);
      removePIDFromDatabase(type, entity.getId());
      graphStorage.updateRelation(relationType, (Relation) entity, change);
    } else {
      if (baseTypeExists(type, entity) && variantExists(type, entity)) {
        removePIDFromDatabase(type, entity.getId());
        graphStorage.updateEntity(type, entity);
      } else if (baseTypeExists(type, entity)) {
        removePIDFromDatabase(toBaseDomainEntity(type), entity.getId());
        graphStorage.addVariant(type, entity);
      } else {
        throw new UpdateException(String.format("%s with id %s does not exist.", type, entity.getId()));
      }
    }
  }

  private <T extends DomainEntity> boolean variantExists(Class<T> type, T entity) {
    return graphStorage.entityExists(type, entity.getId());
  }

  private <T extends DomainEntity> boolean baseTypeExists(Class<T> type, T entity) {
    return graphStorage.entityExists(TypeRegistry.getBaseClass(type), entity.getId());
  }

  @Override
  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws StorageException {
    if (isRelation(type)) {
      graphStorage.setRelationPID(asRelation(type), id, pid);
    } else {
      graphStorage.setDomainEntityPID(type, id, pid);
    }
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws StorageException {
    return graphStorage.deleteSystemEntity(type, id);
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntities(Class<T> type) throws StorageException {
    int numberOfDeletions = 0;
    for (StorageIterator<T> iterator = graphStorage.getEntities(type); iterator.hasNext();) {
      numberOfDeletions += graphStorage.deleteSystemEntity(type, iterator.next().getId());
    }

    return numberOfDeletions;
  }

  @Override
  public <T extends SystemEntity> int deleteByModifiedDate(Class<T> type, Date dateValue) throws StorageException {
    int numberOfDeletions = 0;
    long timeStampToDelete = dateValue.getTime();
    for (StorageIterator<T> entities = graphStorage.getEntities(type); entities.hasNext();) {
      T entity = entities.next();
      Change modified = entity.getModified();

      if (modified.getTimeStamp() <= timeStampToDelete) {
        graphStorage.deleteSystemEntity(type, entity.getId());
        numberOfDeletions++;
      }
    }

    return numberOfDeletions;
  }

  @Override
  public <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id, Change change) throws StorageException {
    if (RELATION_TYPE.isAssignableFrom(type)) {
      graphStorage.deleteRelation(asRelation(type), id);
    } else {
      graphStorage.deleteDomainEntity(type, id);
    }
  }

  // FIXME let this method find the non persistent and delete them. See TIM-145.
  @Override
  public <T extends DomainEntity> void deleteNonPersistent(Class<T> type, List<String> ids) throws StorageException {
    /* 
     * deleteNonPersistent for relations is always used in combination with 
     * deleteNonPersistent for entities. Because the deletion of an entity makes sure
     * the relations of that entity are deleted as well, this method has no need for 
     * for functionality for deleting non persistent relations.  
     */
    if (isRelation(type)) {
      return;
    }
    for (String id : ids) {
      graphStorage.deleteDomainEntity(TypeRegistry.toBaseDomainEntity(type), id);
    }
  }

  @Override
  public <T extends DomainEntity> void deleteVariation(Class<T> type, String id, Change change) throws IllegalArgumentException, NoSuchEntityException, StorageException {
    if (TypeRegistry.isPrimitiveDomainEntity(type)) {
      throw new IllegalArgumentException("Use deleteDomainEntity for removing primitives.");
    }

    /* 
     * A strange way to remove a variation, this is due to the fact that we have 
     * to to decide how to organize the life cycle management. See TIM-196
     */
    T entity = graphStorage.getEntity(type, id);

    if (entity == null) {
      throw new NoSuchEntityException(type, id);
    }

    removePIDFromDatabase(type, id);
    updateAdministrativeValues(entity);

    graphStorage.deleteVariant(entity);
  }

  /**
   * Remove the PID from the database of Entity or Relation.
   * @param type the type of the to remove the PID from 
   * @param id the id to remove the PID from
   * @throws NoSuchEntityException when the Entity could not be found
   * @throws NoSuchRelationException when the Relation could not be found
   */
  @SuppressWarnings("unchecked")
  private <T extends DomainEntity> void removePIDFromDatabase(Class<T> type, String id) throws NoSuchEntityException, NoSuchRelationException {
    if (RELATION_TYPE.isAssignableFrom(type)) {
      graphStorage.removePropertyFromRelation((Class<? extends Relation>) type, id, PID);
    } else {
      graphStorage.removePropertyFromEntity(type, id, PID);
    }
  }

  @Override
  public void deleteRelationsOfEntity(Class<Relation> type, String id) throws StorageException {
    for (StorageIterator<Relation> relations = graphStorage.getRelationsByEntityId(type, id); relations.hasNext();) {
      graphStorage.deleteRelation(RELATION_TYPE, relations.next().getId());
    }

  }

  @Override
  public <T extends Relation> void declineRelationsOfEntity(Class<T> type, String id) throws IllegalArgumentException, StorageException {
    if (TypeRegistry.isPrimitiveDomainEntity(type)) {
      throw new IllegalArgumentException("Use deleteRelation for removing primitive relation.");
    }

    for (StorageIterator<T> relationsOfEntity = graphStorage.getRelationsByEntityId(type, id); relationsOfEntity.hasNext();) {
      T relation = relationsOfEntity.next();
      declineRelation(type, relation);
    }
  }

  private <T extends Relation> void declineRelation(Class<T> type, T relation) throws StorageException {
    relation.setAccepted(false);
    this.updateDomainEntity(type, relation, Change.newInternalInstance());
  }

  @Override
  public <T extends Entity> boolean entityExists(Class<T> type, String id) throws StorageException {
    if (isRelation(type)) {
      return graphStorage.relationExists(asRelation(type), id);
    } else {
      return graphStorage.entityExists(type, id);
    }
  }

  @Override
  public <T extends Entity> T getEntityOrDefaultVariation(Class<T> type, String id) throws StorageException {
    if (graphStorage.entityExists(type, id)) {
      return graphStorage.getEntity(type, id);
    } else {
      return graphStorage.getDefaultVariation(type, id);
    }
  }

  @Override
  public <T extends Entity> T getEntity(Class<T> type, String id) throws StorageException {
    if (isRelation(type)) {
      @SuppressWarnings("unchecked")
      T relationDomainEntity = (T) graphStorage.getRelation((Class<Relation>) type, id);
      return relationDomainEntity;
    } else {
      return graphStorage.getEntity(type, id);
    }

  }

  @Override
  public <T extends SystemEntity> StorageIterator<T> getSystemEntities(Class<T> type) throws StorageException {
    return graphStorage.getEntities(type);
  }

  @Override
  public <T extends DomainEntity> StorageIterator<T> getDomainEntities(Class<T> type) throws StorageException {
    return graphStorage.getEntities(type);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getEntitiesByProperty(Class<T> type, String field, String value) throws StorageException {
    TimbuctooQuery query = queryFactory.newQuery(type);
    query.hasNotNullProperty(field, value);
    query.searchByType(true);

    return graphStorage.findEntities(type, query);
  }

  @Override
  public <T extends Entity> long count(Class<T> type) {
    if (isRelation(type)) {
      return graphStorage.countRelations(asRelation(type));
    } else {
      return graphStorage.countEntities(type);
    }
  }

  @Override
  public <T extends Entity> T findItemByProperty(Class<T> type, String field, String value) throws StorageException {
    if (isRelation(type)) {
      @SuppressWarnings("unchecked")
      T relation = (T) graphStorage.findRelationByProperty(asRelation(type), field, value);
      return relation;
    } else {
      return graphStorage.findEntityByProperty(type, field, value);
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Entity> Class<? extends Relation> asRelation(Class<T> type) {
    return (Class<? extends Relation>) type;
  }

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws StorageException {
    if (isRelation(type)) {
      @SuppressWarnings("unchecked")
      List<T> variations = (List<T>) graphStorage.getAllVariationsOfRelation(asRelation(type), id);
      return variations;
    }

    return graphStorage.getAllVariations(type, id);
  }

  private <T extends Entity> boolean isRelation(Class<T> type) {
    return RELATION_TYPE.isAssignableFrom(type);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revision) throws StorageException {
    if (isRelation(type)) {
      return (T) graphStorage.getRelationRevision(asRelation(type), id, revision);
    } else {
      return graphStorage.getDomainEntityRevision(type, id, revision);
    }
  }

  @Override
  public <T extends DomainEntity> List<T> getAllRevisions(Class<T> type, String id) throws StorageException {
    TimbuctooQuery query = queryFactory.newQuery(type) //
        .hasNotNullProperty(Entity.ID_DB_PROPERTY_NAME, id)//
        .hasDistinctValue(Entity.REVISION_PROPERTY_NAME) //
        .searchLatestOnly(false);

    if (RELATION_TYPE.isAssignableFrom(type)) {
      query.searchByType(false);
      @SuppressWarnings("unchecked")
      List<T> revisions = (List<T>) graphStorage.findRelations(asRelation(type), query).getAll();
      return revisions;
    }
    query.searchByType(true);
    return graphStorage.findEntities(type, query).getAll();
  }

  @Override
  public <T extends Relation> T findRelation(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    return graphStorage.findRelation(type, sourceId, targetId, relationTypeId);
  }

  @Override
  public <T extends Relation> StorageIterator<T> findRelations(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    return graphStorage.findRelations(type, sourceId, targetId, relationTypeId);
  }

  @Override
  public <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException {
    return graphStorage.getRelationsByEntityId(type, id);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws StorageException {
    if (isRelation(type)) {
      return graphStorage.getIdsOfNonPersistentRelations((Class<Relation>) type);
    } else {
      return graphStorage.getIdsOfNonPersistentDomainEntities(type);
    }
  }

  @Override
  public List<String> getRelationIds(List<String> ids) throws StorageException {
    Set<String> relationIds = Sets.newHashSet();
    for (String id : ids) {
      StorageIterator<Relation> iterator = graphStorage.getRelationsByEntityId(RELATION_TYPE, id);

      for (; iterator.hasNext();) {
        relationIds.add(iterator.next().getId());
      }

    }

    return Lists.newArrayList(relationIds);
  }

  @Override
  public <T extends Relation> List<T> getRelationsByType(Class<T> type, List<String> relationTypeIds) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean doesVariationExist(Class<? extends DomainEntity> type, String id) throws StorageException {
    if (RELATION_TYPE.isAssignableFrom(type)) {
      return graphStorage.relationExists((Class<? extends Relation>) type, id);
    }

    return graphStorage.entityExists(type, id);
  }

}
