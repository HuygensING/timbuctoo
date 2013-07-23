package nl.knaw.huygens.repository.storage;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractStorageIterator<T> implements StorageIterator<T> {

  private final Iterator<T> delegate;
  private boolean closed = false;

  public AbstractStorageIterator(Iterator<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean hasNext() {
    return delegate.hasNext();
  }

  @Override
  public T next() {
    try {
      return delegate.next();
    } catch (NoSuchElementException ex) {
      close();
      throw ex;
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    closeInternal();
  }

  protected abstract void closeInternal();

  @Override
  public void finalize() {
    close();
  }

}
