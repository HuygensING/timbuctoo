package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.collect.Lists;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.variation.VariationReducer;

public class MongoDBVariationIteratorWrapper<T extends Document> implements StorageIterator<T> {

  private final DBCursor delegate;
  private final VariationReducer reducer;
  private final Class<T> cls;

  public MongoDBVariationIteratorWrapper(DBCursor delegate, VariationReducer reducer, Class<T> cls) {
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
  public List<T> getSome(int count) {
    List<T> rv = Lists.newArrayListWithCapacity(count);
    while (count-- > 0) {
      try {
        rv.add(reducer.reduceDBObject(delegate.next(), cls));
      } catch (IOException e) {
        e.printStackTrace();
        rv.add(null);
      }
    }
    return rv;
  }

  @Override
  public boolean hasNext() {
    return delegate.hasNext();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    delegate.close();
  }

}
