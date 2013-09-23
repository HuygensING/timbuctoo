package nl.knaw.huygens.repository.index;

import nl.knaw.huygens.repository.model.Document;

/**
 * DocumentIndexer that does nothing...
 */
class NoDocumentIndexer<T extends Document> implements DocumentIndexer<T> {

  @Override
  public void add(Class<T> type, String id) throws IndexException {}

  @Override
  public void modify(Class<T> type, String id) throws IndexException {}

  @Override
  public void remove(String id) {}

  @Override
  public void removeAll() {}

  @Override
  public void flush() {}

}
