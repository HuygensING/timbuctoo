package nl.knaw.huygens.repository.storage;

import java.util.Iterator;
import java.util.List;

public interface StorageIterator<T> extends Iterator<T> {

  void close();

  int size();

  void skip(int count);

  List<T> getSome(int count);

}
