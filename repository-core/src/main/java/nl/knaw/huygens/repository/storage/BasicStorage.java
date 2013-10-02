package nl.knaw.huygens.repository.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;

public interface BasicStorage {

  void empty();

  /**
   * Closes the underlying storage.
   */
  void close();

  <T extends Document> void ensureIndex(Class<T> type, List<List<String>> accessorList);

  <T extends Document> T getItem(Class<T> type, String id) throws IOException;

  /**
   * Searches on the non-null properties of the example object.
   * @param type
   * @param example
   * @return
   * @throws IOException
   */
  <T extends Document> T searchItem(Class<T> type, T example) throws IOException;

  <T extends Document> StorageIterator<T> getAllByType(Class<T> type);

  <T extends Document> StorageIterator<T> getByMultipleIds(Class<T> type, Collection<String> ids);

  /**
   * Adds the specified document to the storage; returns its assigned id.
   */
  <T extends Document> String addItem(Class<T> type, T item) throws IOException;

  <T extends Document> void updateItem(Class<T> type, String id, T item) throws IOException;

  <T extends Document> void setPID(Class<T> type, String pid, String id);

  <T extends Document> void deleteItem(Class<T> type, String id, Change change) throws IOException;

  <T extends Document> RevisionChanges<T> getAllRevisions(Class<T> type, String id) throws IOException;

  List<Document> getLastChanged(int limit) throws IOException;

  <T extends Document> void fetchAll(Class<T> type, List<GenericDBRef<T>> refs);

  <T extends Document> List<String> getIdsForQuery(Class<T> type, List<String> accessors, String[] id);

  /**
   * Removes all system douments with the specified type.
   * @return The number of documents removed.
   */
  <T extends Document> int removeAll(Class<T> type);

  /**
   * Removes system documents that have a value of the specified date field
   * that is older than the specified date.
   * @return The number of documents removed.
   */
  <T extends Document> int removeByDate(Class<T> type, String dateField, Date dateValue);

}
