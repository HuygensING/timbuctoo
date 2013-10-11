package nl.knaw.huygens.timbuctoo.storage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.model.Entity;

public class StorageUtils {

  public static final String UNKNOWN_ID_PREFIX = "UNKN";

  /**
   * Returns the prefix of an entity id.
   */
  public static String getIDPrefix(Class<?> type) {
    if (type != null && Entity.class.isAssignableFrom(type)) {
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
  public static String formatEntityId(Class<? extends Entity> type, long counter) {
    return String.format("%s%012d", getIDPrefix(type), counter);
  }

  public static <T extends Entity> List<T> resolveIterator(StorageIterator<T> iterator, int offset, int limit) {
    if (offset > 0) {
      iterator.skip(offset);
    }
    List<T> rv = iterator.getSome(limit);
    iterator.close();
    return rv;
  }

  public static void sortEntitiesByLastChange(List<Entity> docs) {
    Collections.sort(docs, new Comparator<Entity>() {
      @Override
      public int compare(Entity o1, Entity o2) {
        long o1s = o1 != null && o1.getLastChange() != null ? o1.getLastChange().dateStamp : 0;
        long o2s = o2 != null && o2.getLastChange() != null ? o2.getLastChange().dateStamp : 0;
        long d = o2s - o1s;
        return d > 0 ? 1 : (d < 0 ? -1 : 0);
      }
    });
  }

}
