package nl.knaw.huygens.timbuctoo.storage;

import java.util.Iterator;
import java.util.List;

/**
 * WARNING The current implementation does not allow you to safely 
 * update items while iterating.
 */
public interface StorageIterator<T> extends Iterator<T> {

  void close();

  int size();

  void skip(int count);

  List<T> getSome(int count);

}
