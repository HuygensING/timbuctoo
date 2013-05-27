package nl.knaw.huygens.repository.index;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;

public interface DocumentIndexer<T extends Document> {

  <U extends T> void add(List<U> entities) throws IndexException;

  <U extends T> void modify(List<U> entity) throws IndexException;

  void remove(String id) throws IndexException;

  void removeAll() throws IndexException;

  void flush() throws IndexException;

}
