package nl.knaw.huygens.timbuctoo.v5.util;

import java.util.Iterator;

public class AutoCloseableIteratorWrapper<T> implements AutoCloseableIterator<T> {

  private final Iterator<T> iterator;

  public AutoCloseableIteratorWrapper(Iterator<T> iterator) {
    this.iterator = iterator;
  }

  @Override
  public void close() {
    //ignore
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public T next() {
    return iterator.next();
  }
}
