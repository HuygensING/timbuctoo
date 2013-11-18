package nl.knaw.huygens.timbuctoo.storage;

import java.util.Collections;
import java.util.List;

/**
 * An iterator over an empty collection entities.
 */
public class EmptyStorageIterator<T> implements StorageIterator<T> {

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public T next() {
    throw new IllegalStateException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {}

  @Override
  public int size() {
    return 0;
  }

  @Override
  public void skip(int count) {}

  @Override
  public List<T> getSome(int limit) {
    return Collections.emptyList();
  }

}
