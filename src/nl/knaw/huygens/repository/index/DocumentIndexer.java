package nl.knaw.huygens.repository.index;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.util.RepositoryException;

public interface DocumentIndexer<T extends Document> {

  <Q extends T> void add(List<Q> entities) throws RepositoryException;

  <Q extends T> void modify(List<Q> entity) throws RepositoryException;

  void remove(List<T> docs) throws RepositoryException;

  void removeAll() throws RepositoryException;

  void flush() throws RepositoryException;

  Map<String, String> getAll();

}
