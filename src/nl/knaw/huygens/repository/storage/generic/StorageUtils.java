package nl.knaw.huygens.repository.storage.generic;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.StorageIterator;

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
