package nl.knaw.huygens.repository.storage.mongo;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.storage.RevisionChanges;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.generic.GenericDBRef;

public interface MongoStorage {

  <T extends Document> T getItem(Class<T> type, String id) throws IOException;

  <T extends Document> StorageIterator<T> getAllByType(Class<T> type);

  <T extends Document> StorageIterator<T> getByMultipleIds(Class<T> type, Collection<String> ids);

  <T extends Document> void addItem(Class<T> type, T item) throws IOException;

  <T extends Document> void addItems(Class<T> type, List<T> items) throws IOException;

  <T extends Document> void updateItem(Class<T> type, String id, T item) throws IOException;

  <T extends Document> void setPID(Class<T> type, String pid, String id);

  <T extends Document> void deleteItem(Class<T> type, String id, Change change) throws IOException;

  <T extends Document> RevisionChanges<T> getAllRevisions(Class<T> type, String id) throws IOException;

  void destroy();

  List<Document> getLastChanged(int limit) throws IOException;

  void empty();

  <T extends Document> void fetchAll(Class<T> type, List<GenericDBRef<T>> refs);

  <T extends Document> List<String> getIdsForQuery(Class<T> type, List<String> accessors, String[] id);

  <T extends Document> void ensureIndex(Class<T> type, List<List<String>> accessorList);
}
