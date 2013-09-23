package nl.knaw.huygens.repository.index;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;

// T must be a base type
public interface DocumentIndexer<T extends Document> {

  <U extends T> void add(List<U> variations) throws IndexException;

  <U extends T> void modify(List<U> variations) throws IndexException;

  void remove(String id) throws IndexException;

  void removeAll() throws IndexException;

  void flush() throws IndexException;

}
