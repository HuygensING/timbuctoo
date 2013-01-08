package nl.knaw.huygens.repository.storage;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;

public class StorageUtils {
  public static <T extends Document> List<T> readFromIterator(StorageIterator<T> it, int offset, int limit) {
    if (offset > 0) {
      it.skip(offset);
    }
    return it.getSome(limit);
  }

  public static <T extends Document> List<T> resolveIterator(StorageIterator<T> it, int limit) {
    return resolveIterator(it, 0, limit);
  }

  public static <T extends Document> List<T> resolveIterator(StorageIterator<T> it, int offset, int limit) {
    if (offset > 0) {
      it.skip(offset);
    }
    List<T> rv = it.getSome(limit);
    it.close();
    return rv;
  }

  public static <T extends Document> List<T> readFromIterator(StorageIterator<T> it, int limit) {
    return readFromIterator(it, 0, limit);
  }

}
