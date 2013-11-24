package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

class MongoStorageIterator<T extends Entity> implements StorageIterator<T> {

  private final Class<T> type;
  private final DBCursor delegate;
  private final VariationReducer reducer;
  private boolean closed;

  public MongoStorageIterator(Class<T> type, DBCursor delegate, VariationReducer reducer) {
    this.type = type;
    this.delegate = Preconditions.checkNotNull(delegate);
    this.reducer = reducer;
  }

  @Override
  public T next() {
    try {
      DBObject next = delegate.next();
      return reducer.reduceVariation(type, toJsonNode(next), null);
    } catch (NoSuchElementException e) {
      close();
      throw e;
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

  @SuppressWarnings("unchecked")
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
        list.add((T) reducer.reduceVariation((Class<? extends DomainEntity>) type, toJsonNode(next), null));
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

  @SuppressWarnings("unchecked")
  private JsonNode toJsonNode(DBObject object) throws IOException {
    if (object instanceof JacksonDBObject) {
      return (((JacksonDBObject<JsonNode>) object).getObject());
    } else if (object instanceof DBJsonNode) {
      return ((DBJsonNode) object).getDelegate();
    } else {
      throw new IOException("Unknown DBObject type");
    }
  }

}
