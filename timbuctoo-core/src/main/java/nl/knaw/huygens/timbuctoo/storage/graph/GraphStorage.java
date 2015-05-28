package nl.knaw.huygens.timbuctoo.storage.graph;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

public interface GraphStorage {

  <T extends DomainEntity> void addDomainEntity(Class<T> type, T entity, Change change) throws StorageException;

  <T extends SystemEntity> void addSystemEntity(Class<T> type, T entity) throws StorageException;

  <T extends Relation> void addRelation(Class<T> type, Relation relation, Change change) throws StorageException;

  <T extends Entity> T getEntity(Class<T> type, String id) throws StorageException;

  <T extends Entity> StorageIterator<T> getEntities(Class<T> type) throws StorageException;

  <T extends Relation> T getRelation(Class<T> type, String id) throws StorageException;

  <T extends Entity> void updateEntity(Class<T> type, T entity) throws StorageException;

  /**
   * Update a DomainEntity with a new variant.
   * @param type the type of the variant
   * @param variant the variant to add
   * @throws StorageException when the variant cannot be added
   */
  <T extends DomainEntity> void addVariant(Class<T> type, T variant) throws StorageException;

  <T extends Relation> void updateRelation(Class<T> type, Relation relation, Change change) throws StorageException;

  long countEntities(Class<? extends Entity> type);

  long countRelations(Class<? extends Relation> relationType);

  // TODO: Make equal to deleteSystemEntity see TIM-54
  <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id) throws StorageException;

  /**
   * Delete a variant of a DomainEntity.
   * @param variant the variant to delete
   * @throws StorageException when the deletion cannot be executed
   * @throws NoSuchEntityException when the entity does not exist
   * @throws IllegalArgumentException when the variant is a primitive
   */
  <T extends DomainEntity> void deleteVariant(T variant) throws StorageException, NoSuchEntityException, IllegalArgumentException;

  <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws StorageException;

  <T extends DomainEntity> T getDomainEntityRevision(Class<T> type, String id, int revision) throws StorageException;

  <T extends Relation> T getRelationRevision(Class<T> type, String id, int revision) throws StorageException;

  <T extends DomainEntity> void setDomainEntityPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException;

  <T extends Relation> void setRelationPID(Class<T> type, String id, String pid) throws NoSuchEntityException, ConversionException, StorageException;

  void close();

  boolean isAvailable();

  <T extends Entity> T findEntityByProperty(Class<T> type, String field, String value) throws StorageException;

  <T extends Relation> T findRelationByProperty(Class<T> type, String field, String value) throws StorageException;

  <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws StorageException;

  <T extends Relation> List<T> getAllVariationsOfRelation(Class<T> type, String id) throws StorageException;

  <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException;

  /**
   * Checks if a certain variant with a certain id exists.
   * @param type the type of the variant
   * @param id the id of the variant
   * @return true if it exists, false if not
   */
  boolean entityExists(Class<? extends Entity> type, String id);

  boolean relationExists(Class<? extends Relation> relationType, String id);

  <T extends Relation> T findRelation(Class<T> relationType, String sourceId, String targetId, String relationTypeId) throws StorageException;

  <T extends DomainEntity> List<String> getIdsOfNonPersistentDomainEntities(Class<T> type);

  // FIXME filter with projectType see TIM-143
  <T extends Relation> List<String> getIdsOfNonPersistentRelations(Class<T> type);

  // TODO make only available for DomainEntities see TIM-162
  <T extends Entity> T getDefaultVariation(Class<T> type, String id) throws StorageException;

  <T extends Relation> StorageIterator<T> findRelations(Class<T> relationType, String sourceId, String targetId, String relationTypeId);

  /**
   * Remove a property from a DomainEntity.
   * @param type the type of the Entity to remove from
   * @param id the id of the Entity to remove from
   * @param fieldName the property name to remove
   * @throws NoSuchEntityException when the entity cannot be found
   */
  <T extends DomainEntity> void removePropertyFromEntity(Class<T> type, String id, String fieldName) throws NoSuchEntityException;

  /**
   * Remove a property from a Relation.
   * @param type the type of the Relation to remove from
   * @param id the id of the Relation to remove from
   * @param fieldName the property name to remove
   */
  <T extends DomainEntity> void removePropertyFromRelation(Class<T> type, String id, String fieldName);

}