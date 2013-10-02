package nl.knaw.huygens.repository.storage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.model.Document;

public class StorageUtils {

  public static final String UNKNOWN_ID_PREFIX = "UNKN";

  /**
   * Returns the prefix of an entity id.
   */
  public static String getIDPrefix(Class<?> type) {
    if (type != null && Document.class.isAssignableFrom(type)) {
      IDPrefix annotation = type.getAnnotation(IDPrefix.class);
      if (annotation != null) {
        return annotation.value();
      } else {
        return getIDPrefix(type.getSuperclass());
      }
    }
    return UNKNOWN_ID_PREFIX;
  }

  /**
   * Returns a formatted entity id.
   */
  public static String formatEntityId(Class<? extends Document> type, long counter) {
    return String.format("%s%012d", getIDPrefix(type), counter);
  }

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

  public static void sortDocumentsByLastChange(List<Document> docs) {
    Collections.sort(docs, new Comparator<Document>() {
      @Override
      public int compare(Document o1, Document o2) {
        long o1s = o1 != null && o1.getLastChange() != null ? o1.getLastChange().dateStamp : 0;
        long o2s = o2 != null && o2.getLastChange() != null ? o2.getLastChange().dateStamp : 0;
        long d = o2s - o1s;
        return d > 0 ? 1 : (d < 0 ? -1 : 0);
      }
    });
  }

}
