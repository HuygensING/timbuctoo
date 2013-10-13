package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import com.google.common.collect.Lists;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

class MongoDBVariationIterator<T extends Entity> implements StorageIterator<T> {

  private final DBCursor delegate;
  private final VariationReducer reducer;
  private final Class<T> cls;
  private boolean closed;

  public MongoDBVariationIterator(DBCursor delegate, VariationReducer reducer, Class<T> cls) {
    this.delegate = delegate;
    this.reducer = reducer;
    this.cls = cls;
  }

  @Override
  public T next() {
    try {
      DBObject next = delegate.next();
      return reducer.reduceDBObject(next, cls);
    } catch (NoSuchElementException ex) {
      close();
      throw ex;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public void skip(int count) {
    delegate.skip(count);
  }

  @Override
  public List<T> getSome(int limit) {
    List<T> list = Lists.newArrayList();
    while (limit-- > 0 && delegate.hasNext()) {
      DBObject next;
      try {
        next = delegate.next();
      } catch (Exception ex) {
        // Gotta love how mongo documents that there's a nomoreelements exception... nuh-uh.
        close();
        break;
      }

      try {
        list.add(reducer.reduceDBObject(next, cls));
      } catch (IOException e) {
        e.printStackTrace();
        list.add(null);
      }
    }
    return list;
  }

  @Override
  public boolean hasNext() {
    boolean rv = delegate.hasNext();
    if (!rv) {
      close();
    }
    return rv;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    synchronized (this) {
      if (!closed) {
        closed = true;
        delegate.close();
      }
    }
  }

}
