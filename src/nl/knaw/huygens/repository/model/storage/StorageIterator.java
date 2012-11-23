package nl.knaw.huygens.repository.model.storage;

import java.util.Iterator;
import java.util.List;

public interface StorageIterator<T> extends Iterator<T> {
  public void close();

  public int size();

  public void skip(int count);

  public List<T> getSome(int count);
}