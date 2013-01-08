package nl.knaw.huygens.repository.storage.mongo;

import java.util.List;

import net.vz.mongodb.jackson.DBCursor;
import nl.knaw.huygens.repository.storage.AbstractStorageIterator;

public class MongoDBIteratorWrapper<T> extends AbstractStorageIterator<T> {

  private DBCursor<T> cursor;
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
