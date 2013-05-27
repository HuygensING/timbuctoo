package nl.knaw.huygens.repository.index;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.util.RepositoryException;

import com.google.common.collect.Maps;

/**
 * DocumentIndexer that does nothing...
 */
class NoDocumentIndexer<T extends Document> implements DocumentIndexer<T> {

  @Override
  public <U extends T> void add(List<U> entities) throws RepositoryException {}

  @Override
  public <U extends T> void modify(List<U> entity) throws RepositoryException {}

  @Override
  public void remove(String id) throws RepositoryException {}

  @Override
  public void removeAll() throws RepositoryException {}

  @Override
  public void flush() throws RepositoryException {}

  @Override
  public Map<String, String> getAll() {
    return Maps.newHashMap();
  }

}
