package nl.knaw.huygens.repository.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.storage.generic.GenericDBRef;

public interface Storage {

  <T extends Document> T getItem(String id, Class<T> cls) throws IOException;

  <T extends Document> StorageIterator<T> getAllByType(Class<T> cls);

  <T extends Document> StorageIterator<T> getByMultipleIds(Collection<String> ids, Class<T> entityCls);

  <T extends Document> void addItem(T newItem, Class<T> cls) throws IOException;

  <T extends Document> void addItems(List<T> items, Class<T> cls) throws IOException;

  <T extends Document> void updateItem(String id, T updatedItem, Class<T> cls) throws IOException;

  <T extends Document> void setPID(Class<T> cls, String pid, String id);

  <T extends Document> void deleteItem(String id, Class<T> cls, Change change) throws IOException;

  <T extends Document> RevisionChanges<T> getAllRevisions(String id, Class<T> baseCls);

  void destroy();

  List<Document> getLastChanged(int limit) throws IOException;

  void empty();

  <T extends Document> void fetchAll(List<GenericDBRef<T>> refs, Class<T> cls);

  <T extends Document> List<String> getIdsForQuery(Class<T> cls, List<String> accessors, String[] id);

  void ensureIndex(Class<? extends Document> cls, List<List<String>> accessorList);

  /**
   * NB: this is technically a variation-storage specific API, only I (Gijs) was too lazy to
   * refactor everything for just one method. Oops.
   */
  <T extends Document> List<T> getAllVariations(String id, Class<T> cls) throws IOException;

}
