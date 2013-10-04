package nl.knaw.huygens.repository.index;

import java.util.List;

import nl.knaw.huygens.repository.model.Entity;

// T must be a base type
public interface EntityIndexer<T extends Entity> {

  void add(Class<T> docType, String docId) throws IndexException;

  void modify(Class<T> docType, String docId) throws IndexException;

  void remove(String docId) throws IndexException;

  /**
   * Remove multiple entries from the index.
   * 
   * @param ids the ids of of the entries to remove
   * @throws IndexException encapsulates the exceptions generated while deleting.
   */
  void remove(List<String> ids) throws IndexException;

  void removeAll() throws IndexException;

  void flush() throws IndexException;

}
