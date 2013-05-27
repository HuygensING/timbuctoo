package nl.knaw.huygens.repository.index;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.util.RepositoryException;

public interface DocumentIndexer<T extends Document> {

  <U extends T> void add(List<U> entities) throws RepositoryException;

  <U extends T> void modify(List<U> entity) throws RepositoryException;

  void remove(String id) throws RepositoryException;

  void removeAll() throws RepositoryException;

  void flush() throws RepositoryException;

}
