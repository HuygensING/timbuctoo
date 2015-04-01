package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import com.google.inject.Inject;

public class Neo4JLegacyStorageWrapper implements Storage {

  private final Neo4JStorage neo4JStorage;

  @Inject
  public Neo4JLegacyStorageWrapper(Neo4JStorage neo4JStorage) {
    this.neo4JStorage = neo4JStorage;
  }

  @Override
  public void createIndex(boolean unique, Class<? extends Entity> type, String... fields) throws StorageException {
    // FIXME indexes should be created in a different way for Neo4J TIM-109
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
    neo4JStorage.close();
  }

  @Override
  public boolean isAvailable() {
    return neo4JStorage.isAvailable();
  }

  @Override
  public <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws StorageException {
    return neo4JStorage.addSystemEntity(type, entity);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    if (Relation.class.isAssignableFrom(type)) {
      return neo4JStorage.addRelation((Class<? extends Relation>) type, (Relation) entity, change);
    } else {
      return neo4JStorage.addDomainEntity(type, entity, change);
    }
  }

  @Override
  public <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws StorageException {
    neo4JStorage.updateSystemEntity(type, entity);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    if (Relation.class.isAssignableFrom(type)) {
      neo4JStorage.updateRelation((Class<? extends Relation>) type, (Relation) entity, change);
    } else {
      neo4JStorage.updateDomainEntity(type, entity, change);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws StorageException {
    if (Relation.class.isAssignableFrom(type)) {
      neo4JStorage.setRelationPID((Class<? extends Relation>) type, id, pid);
    } else {
      neo4JStorage.setDomainEntityPID(type, id, pid);
    }
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws StorageException {
    return neo4JStorage.deleteSystemEntity(type, id);
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntities(Class<T> type) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends SystemEntity> int deleteByModifiedDate(Class<T> type, Date dateValue) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id, Change change) throws StorageException {
    neo4JStorage.deleteDomainEntity(type, id, change);
  }

  @Override
  public <T extends DomainEntity> void deleteNonPersistent(Class<T> type, List<String> ids) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");

  }

  @Override
  public void deleteVariation(Class<? extends DomainEntity> type, String id, Change change) throws IllegalArgumentException, NoSuchEntityException, StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");

  }

  @Override
  public void deleteRelationsOfEntity(Class<Relation> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");

  }

  @Override
  public void declineRelationsOfEntity(Class<? extends Relation> type, String id) throws IllegalArgumentException, StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");

  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Entity> boolean entityExists(Class<T> type, String id) throws StorageException {
    if (Relation.class.isAssignableFrom(type)) {
      return neo4JStorage.relationExists((Class<? extends Relation>) type, id);
    } else {
      return neo4JStorage.entityExists(type, id);
    }
  }

  @Override
  public <T extends Entity> T getEntityOrDefaultVariation(Class<T> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Entity> T getEntity(Class<T> type, String id) throws StorageException {
    if (Relation.class.isAssignableFrom(type)) {
      @SuppressWarnings("unchecked")
      T relationDomainEntity = (T) neo4JStorage.getRelation((Class<Relation>) type, id);
      return relationDomainEntity;
    } else {
      return neo4JStorage.getEntity(type, id);
    }

  }

  @Override
  public <T extends SystemEntity> StorageIterator<T> getSystemEntities(Class<T> type) throws StorageException {
    return neo4JStorage.getSystemEntities(type);
  }

  @Override
  public <T extends DomainEntity> StorageIterator<T> getDomainEntities(Class<T> type) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Entity> StorageIterator<T> getEntitiesByProperty(Class<T> type, String field, String value) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Entity> long count(Class<T> type) {
    if (Relation.class.isAssignableFrom(type)) {
      return neo4JStorage.countRelations((Class<? extends Relation>) type);
    } else {
      return neo4JStorage.countEntities(type);
    }
  }

  @Override
  public <T extends Entity> T findItemByProperty(Class<T> type, String field, String value) throws StorageException {
    if (Relation.class.isAssignableFrom(type)) {
      @SuppressWarnings("unchecked")
      T relation = (T) neo4JStorage.findRelationByProperty((Class<? extends Relation>) type, field, value);
      return relation;
    } else {
      return neo4JStorage.findEntityByProperty(type, field, value);
    }
  }

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws StorageException {
    return neo4JStorage.getAllVariations(type, id);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revision) throws StorageException {
    if (Relation.class.isAssignableFrom(type)) {
      return (T) neo4JStorage.getRelationRevision((Class<? extends Relation>) type, id, revision);
    } else {
      return neo4JStorage.getDomainEntityRevision(type, id, revision);
    }
  }

  @Override
  public <T extends DomainEntity> List<T> getAllRevisions(Class<T> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> T findRelation(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> StorageIterator<T> findRelations(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException {
    return neo4JStorage.getRelationsByEntityId(type, id);
  }

  @Override
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public List<String> getRelationIds(List<String> ids) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <T extends Relation> List<T> getRelationsByType(Class<T> type, List<String> relationTypeIds) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public boolean doesVariationExist(Class<? extends DomainEntity> type, String id) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
