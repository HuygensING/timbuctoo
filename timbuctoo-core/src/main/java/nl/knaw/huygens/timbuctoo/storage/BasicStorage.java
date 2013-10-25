package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;
import java.util.Date;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;

public interface BasicStorage {

  void empty();

  /**
   * Closes the underlying storage.
   */
  void close();

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

  <T extends Entity> void deleteItem(Class<T> type, String id, Change change) throws IOException;

  <T extends Entity> RevisionChanges<T> getAllRevisions(Class<T> type, String id) throws IOException;

  // --- system entities -----------------------------------------------

  /**
   * Find a system entity which has the specified key/value pair.
   */
  <T extends SystemEntity> T findItemByKey(Class<T> type, String key, String value) throws IOException;

  /**
   * Find a system entity which has the non-null properties of the example object.
   */
  <T extends SystemEntity> T findItem(Class<T> type, T example) throws IOException;

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

  <T extends Entity> void setPID(Class<T> type, String id, String pid);

}
