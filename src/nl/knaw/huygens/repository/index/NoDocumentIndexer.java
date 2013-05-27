package nl.knaw.huygens.repository.index;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;

/**
 * DocumentIndexer that does nothing...
 */
class NoDocumentIndexer<T extends Document> implements DocumentIndexer<T> {

  @Override
  public <U extends T> void add(List<U> entities) {}

  @Override
  public <U extends T> void modify(List<U> entity) {}

  @Override
  public void remove(String id) {}

  @Override
  public void removeAll() {}

  @Override
  public void flush() {}

}
