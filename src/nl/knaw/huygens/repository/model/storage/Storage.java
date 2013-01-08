package nl.knaw.huygens.repository.model.storage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.model.Change;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.RevisionChanges;

public interface Storage {
  public <T extends Document> T getItem(String id, Class<T> cls);

  public <T extends Document> StorageIterator<T> getAllByType(Class<T> cls);

  public Map<String, String> getSimpleMap(Class<? extends Document> cls);

  public <T extends Document> StorageIterator<T> getByMultipleIds(List<String> ids, Class<T> entityCls);

  public <T extends Document> void addItem(T newItem, Class<T> cls) throws IOException;

  public <T extends Document> void addItems(List<T> items, Class<T> cls) throws IOException;

  public <T extends Document> void updateItem(String id, T updatedItem, Class<T> cls) throws IOException;

  public <T extends Document> void deleteItem(String id, Class<T> cls, Change change) throws IOException;

  public RevisionChanges getAllRevisions(String id, Class<? extends Document> baseCls);

  public void destroy();

  public List<Document> getLastChanged(int limit);

  public void empty();

  public <T extends Document> void fetchAll(List<GenericDBRef<T>> refs, Class<T> cls);

  public <T extends Document> List<String> getIdsForQuery(Class<T> cls, List<String> accessors, String[] id);

  public void ensureIndex(Class<? extends Document> cls, List<List<String>> accessorList);

  public <T extends Document> void removeReference(Class<T> cls, List<String> accessorList, List<String> referringIds, String referredId, Change change);
}
