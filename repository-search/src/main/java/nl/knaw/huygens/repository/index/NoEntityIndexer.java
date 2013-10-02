package nl.knaw.huygens.repository.index;

import nl.knaw.huygens.repository.model.Entity;

/**
 * An {@code EntityIndexer} that does nothing...
 */
class NoEntityIndexer<T extends Entity> implements EntityIndexer<T> {

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
