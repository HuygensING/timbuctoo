package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;

/**
 * Defines the API for storage and retrieval of entities.
 */
public interface Storage {

  /**
   * Closes the underlying storage.
   */
  void close();

  // --- add entities --------------------------------------------------

  /**
   * Adds the specified system entity to the storage; returns its assigned id.
   */
  <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws IOException;

  /**
   * Adds the specified domain entity to the storage; returns its assigned id.
   */
  <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws IOException;

  // --- update entities -----------------------------------------------

  /**
   * Updates the specified system entity in the storage.
   * The id and the revision of the entity must match with the stored entity.
   */
  <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws IOException;

  /**
   * Updates the specified doamin entity in the storage.
   * The id and the revision of the entity must match with the stored entity.
   */
  <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws IOException;

  <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws IOException;

  // --- delete entities -----------------------------------------------

  /**
   * Deletes the spcified system entity.
   */
  <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws IOException;

  /**
   * Deletes all system entities with the specified type.
   * @return The number of entities removed.
   */
  <T extends SystemEntity> int deleteAll(Class<T> type);

  /**
   * Deletes system entities that have a value of the specified date field
   * that is older than the specified date.
   * @return The number of entities removed.
   */
  <T extends SystemEntity> int deleteByDate(Class<T> type, String dateField, Date dateValue);

  <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id, Change change) throws IOException;

  /**
   * Deletes non-persistent domain entities with the specified type and id's..
   */
  <T extends DomainEntity> void deleteNonPersistent(Class<T> type, List<String> ids) throws IOException;

  // -------------------------------------------------------------------

  <T extends Entity> T getItem(Class<T> type, String id) throws IOException;

  <T extends Entity> StorageIterator<T> getAllByType(Class<T> type);

  /**
   * Returns the number of items in the collection corresponding with the specified type.
   */
  <T extends Entity> long count(Class<T> type);

  /**
   * Find a system entity which has the specified key/value pair.
   */
  <T extends SystemEntity> T findItemByKey(Class<T> type, String key, String value) throws IOException;

  /**
   * Find a system entity which has the non-null properties of the example object.
   */
  <T extends SystemEntity> T findItem(Class<T> type, T example) throws IOException;

  /**
   * Get the given variation of an entity.
   */
  <T extends DomainEntity> T getVariation(Class<T> type, String id, String variation) throws IOException;

  <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws IOException;

  <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws IOException;

  <T extends DomainEntity> RevisionChanges<T> getAllRevisions(Class<T> type, String id) throws IOException;

  /**
   * Is the specified relation present in the storage?
   */
  boolean relationExists(Relation relation) throws IOException;

  /**
   * Returns an iterator for all relations involving the specified domain entity,
   * either as 'source' or as 'target' (or both).
   */
  StorageIterator<Relation> getRelationsOf(Class<? extends DomainEntity> type, String id) throws IOException;

  void addRelationsTo(Class<? extends DomainEntity> type, String id, DomainEntity entity);

  /**
  * Returns the id's of the domain entities of the specified type, that are not persisted.
  * 
  * Note that by design the method does not return variations of a type
  * that already has been persisted.
  * For example, if {@code Person} is a primitive type and a variation
  * {@code XyzPerson} of an existing entity has been added, this method
  * will not retrieve the id of that entity.
  */
  <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws IOException;

  /**
   * Returns the id's of all relations involving the entities with the specified id's,
   * either as 'source' or as 'target' (or both).
   * 
   * @param ids a list of id's to find the relations for
   * @return a list of id's of the corresponding relations
   * @throws IOException wrapped exception around the database exceptions
   */
  List<String> getRelationIds(List<String> ids) throws IOException;

}
