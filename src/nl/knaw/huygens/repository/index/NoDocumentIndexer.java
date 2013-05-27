package nl.knaw.huygens.repository.index;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.util.RepositoryException;

import com.google.common.collect.Maps;

class NoDocumentIndexer<T extends Document> implements DocumentIndexer<T> {

  @Override
  public <Q extends T> void add(List<Q> entities) throws RepositoryException {}

  @Override
  public <Q extends T> void modify(List<Q> entity) throws RepositoryException {}

  @Override
  public void remove(List<T> docs) throws RepositoryException {}

  @Override
  public void removeAll() throws RepositoryException {}

  @Override
  public void flush() throws RepositoryException {}

  @Override
  public Map<String, String> getAll() {
    return Maps.newHashMap();
  }

}
