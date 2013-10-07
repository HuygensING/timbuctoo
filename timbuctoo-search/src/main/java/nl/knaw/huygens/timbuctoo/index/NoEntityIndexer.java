package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;

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
  public void remove(List<String> ids) {}

  @Override
  public void removeAll() {}

  @Override
  public void flush() {}

}
