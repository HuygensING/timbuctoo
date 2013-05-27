package nl.knaw.huygens.repository.index;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.util.RepositoryException;

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

}
