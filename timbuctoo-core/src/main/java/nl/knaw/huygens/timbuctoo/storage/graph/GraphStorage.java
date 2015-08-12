package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.NoSuchRelationException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import java.util.List;

public interface GraphStorage {

  <T extends DomainEntity> void addDomainEntity(Class<T> type, T entity, Change change) throws StorageException;

  <T extends SystemEntity> void addSystemEntity(Class<T> type, T entity) throws StorageException;

  <T extends Relation> void addRelation(Class<T> type, Relation relation, Change change) throws StorageException;

  <T extends Entity> T getEntity(Class<T> type, String id) throws StorageException;

  <T extends Entity> StorageIterator<T> getEntities(Class<T> type) throws StorageException;

  <T extends Relation> StorageIterator<T> getRelations(Class<T> relationType) throws StorageException;

  <T extends Relation> T getRelation(Class<T> type, String id) throws StorageException;

  <T extends Entity> void updateEntity(Class<T> type, T entity) throws StorageException;

  /**
   * Update a DomainEntity with a new variant.
   *
   * @param type    the type of the variant
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
   *
   * @param variant the variant to delete
   * @throws StorageException         when the deletion cannot be executed
   * @throws NoSuchEntityException    when the entity does not exist
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

  /**
   * Find the relations where the entity of the id is the source or target.
   *
   * @param type the type of the relation to find
   * @param id   the id of entity that should contain the relations
   * @return the found relations
   * @throws StorageException
   */
  <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException;

  /**
   * Checks if a certain variant with a certain id exists.
   *
   * @param type the type of the variant
   * @param id   the id of the variant
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

  /**
   * The method to get the default variantion of a Relation similar to getDefaultVariation.
   * @param relationType the type to find
   * @param id the id of the relation to find
   * @param <T> the type of relation
   * @return the found relation or null
   */
  <T extends Relation> T getDefaultRelation(Class<T> relationType, String id) throws StorageException;

  <T extends Relation> StorageIterator<T> findRelations(Class<T> relationType, String sourceId, String targetId, String relationTypeId);

  /**
   * Remove a property from a DomainEntity.
   *
   * @param type      the type of the Entity to remove from
   * @param id        the id of the Entity to remove from
   * @param fieldName the field name that corresponds with the property to remove
   * @throws NoSuchEntityException when the entity cannot be found
   */
  <T extends DomainEntity> void removePropertyFromEntity(Class<T> type, String id, String fieldName) throws NoSuchEntityException;

  /**
   * Remove a property from a Relation.
   *
   * @param type      the type of the Relation to remove from
   * @param id        the id of the Relation to remove from
   * @param fieldName the field name that corresponds with the property to remove
   * @throws NoSuchRelationException when the relation cannot be found
   */
  <T extends Relation> void removePropertyFromRelation(Class<T> type, String id, String fieldName) throws NoSuchRelationException;

  <T extends Relation> void deleteRelation(Class<T> type, String id);

  /**
   * Find entities of a certain type that match with the query.
   *
   * @param type  the type to find the entities for
   * @param query the query to match
   * @return a StorageIterator with the found entities
   */
  <T extends Entity> StorageIterator<T> findEntities(Class<T> type, TimbuctooQuery query);

  /**
   * Find relations of a certain type that match with the query.
   *
   * @param type  the type to find the relations for
   * @param query the query to match
   * @return a StorageIterator with the found relations
   */
  <T extends Relation> StorageIterator<T> findRelations(Class<T> type, TimbuctooQuery query);

  /**
   * Add a database index.
   *
   * @param type  the type to add the index to
   * @param field the field to add the index to
   */
  void createIndex(Class<? extends Entity> type, String field);



}
