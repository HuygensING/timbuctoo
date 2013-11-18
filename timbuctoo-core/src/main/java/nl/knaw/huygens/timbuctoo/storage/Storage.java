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

  // --- life cycle ----------------------------------------------------

  /**
   * Closes the underlying storage.
   */
  void close();

  // -------------------------------------------------------------------

  <T extends Entity> T getItem(Class<T> type, String id) throws IOException;

  <T extends Entity> StorageIterator<T> getAllByType(Class<T> type);

  /**
   * Returns the number of items in the collection corresponding with the specified type.
   */
  <T extends Entity> long count(Class<T> type);

  /**
   * Adds the specified entity to the storage; returns its assigned id.
   */
  <T extends Entity> String addItem(Class<T> type, T item) throws IOException;

  <T extends Entity> void updateItem(Class<T> type, String id, T item) throws IOException;

  /**
   * Find a system entity which has the specified key/value pair.
   */
  <T extends SystemEntity> T findItemByKey(Class<T> type, String key, String value) throws IOException;

  /**
   * Find a system entity which has the non-null properties of the example object.
   */
  <T extends SystemEntity> T findItem(Class<T> type, T example) throws IOException;

  /**
   * Removes the spcified system entity.
   */
  <T extends SystemEntity> int removeItem(Class<T> type, String id) throws IOException;

  /**
   * Removes all system entities with the specified type.
   * @return The number of entities removed.
   */
  <T extends SystemEntity> int removeAll(Class<T> type);

  /**
   * Removes system entities that have a value of the specified date field
   * that is older than the specified date.
   * @return The number of entities removed.
   */
  <T extends SystemEntity> int removeByDate(Class<T> type, String dateField, Date dateValue);

  <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws IOException;

  /**
   * Get the given variation of an entity.
   */
  <T extends DomainEntity> T getVariation(Class<T> type, String id, String variation) throws IOException;

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

  <T extends DomainEntity> void setPID(Class<T> type, String id, String pid);

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

  /**
   * Removes non-persistent domain entities with the specified type and id's..
   */
  <T extends DomainEntity> void removeNonPersistent(Class<T> type, List<String> ids) throws IOException;

  <T extends DomainEntity> void deleteItem(Class<T> type, String id, Change change) throws IOException;

}
