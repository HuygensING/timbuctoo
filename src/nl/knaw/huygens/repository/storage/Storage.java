package nl.knaw.huygens.repository.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.storage.generic.GenericDBRef;

public interface Storage {
  public <T extends Document> T getItem(String id, Class<T> cls) throws IOException;

  public <T extends Document> StorageIterator<T> getAllByType(Class<T> cls);

  public <T extends Document> StorageIterator<T> getByMultipleIds(Collection<String> ids, Class<T> entityCls);

  public <T extends Document> void addItem(T newItem, Class<T> cls) throws IOException;

  public <T extends Document> void addItems(List<T> items, Class<T> cls) throws IOException;

  public <T extends Document> void updateItem(String id, T updatedItem, Class<T> cls) throws IOException;

  public <T extends Document> void deleteItem(String id, Class<T> cls, Change change) throws IOException;

  public <T extends Document> RevisionChanges<T> getAllRevisions(String id, Class<T> baseCls);

  public void destroy();

  public List<Document> getLastChanged(int limit) throws IOException;

  public void empty();

  public <T extends Document> void fetchAll(List<GenericDBRef<T>> refs, Class<T> cls);

  public <T extends Document> List<String> getIdsForQuery(Class<T> cls, List<String> accessors, String[] id);

  public void ensureIndex(Class<? extends Document> cls, List<List<String>> accessorList);
}
