package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.util.List;

import nl.knaw.huygens.timbuctoo.storage.AbstractStorageIterator;

import org.mongojack.DBCursor;

public class MongoDBIteratorWrapper<T> extends AbstractStorageIterator<T> {

  protected DBCursor<T> cursor;

  public MongoDBIteratorWrapper(DBCursor<T> delegate) {
    super(delegate);
    cursor = delegate;
  }

  @Override
  protected void closeInternal() {
    cursor.close();
  }

  @Override
  public int size() {
    return cursor.count();
  }

  @Override
  public void skip(int count) {
    cursor.skip(count);
  }

  @Override
  public List<T> getSome(int count) {
    return cursor.toArray(count);
  }

}
