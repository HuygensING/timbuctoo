package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;

public interface BasicStorage {

  void empty();

  /**
   * Closes the underlying storage.
   */
  void close();

  <T extends Entity> void ensureIndex(Class<T> type, List<List<String>> accessorList);

  <T extends Entity> T getItem(Class<T> type, String id) throws IOException;

  <T extends Entity> StorageIterator<T> getAllByType(Class<T> type);

  <T extends Entity> StorageIterator<T> getByMultipleIds(Class<T> type, Collection<String> ids);

  /**
   * Find a system entity which has the specified key/value pair
   */
  <T extends Entity> T findItem(Class<T> type, String property, String value) throws IOException;

  /**
   * Find a system entity which has the non-null properties of the example object.
   */
  <T extends Entity> T findItem(Class<T> type, T example) throws IOException;

  /**
   * Adds the specified entity to the storage; returns its assigned id.
   */
  <T extends Entity> String addItem(Class<T> type, T item) throws IOException;

  <T extends Entity> void updateItem(Class<T> type, String id, T item) throws IOException;

  <T extends Entity> void setPID(Class<T> type, String pid, String id);

  <T extends Entity> void deleteItem(Class<T> type, String id, Change change) throws IOException;

  <T extends Entity> RevisionChanges<T> getAllRevisions(Class<T> type, String id) throws IOException;

  <T extends Entity> List<String> getIdsForQuery(Class<T> type, List<String> accessors, String[] id);

  /**
   * Removes all system douments with the specified type.
   * @return The number of entities removed.
   */
  <T extends Entity> int removeAll(Class<T> type);

  /**
   * Removes system entities that have a value of the specified date field
   * that is older than the specified date.
   * @return The number of entities removed.
   */
  <T extends Entity> int removeByDate(Class<T> type, String dateField, Date dateValue);

}
