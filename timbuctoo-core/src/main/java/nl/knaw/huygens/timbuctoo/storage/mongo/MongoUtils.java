package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mongodb.DBObject;

public class MongoUtils {

  public static void sortDocumentsByLastChange(List<DBObject> docs) {
    Collections.sort(docs, new Comparator<DBObject>() {
      @Override
      public int compare(DBObject o1, DBObject o2) {
        long ds1 = getDS(o1);
        long ds2 = getDS(o2);
        long d = ds2 - ds1;
        return d > 0 ? 1 : (d < 0 ? -1 : 0);
      }

      private long getDS(DBObject o1) {
        Object o1s = o1 != null ? o1.get("^lastChange") : null;
        o1s = (o1s != null && o1s instanceof DBObject) ? ((DBObject) o1s).get("^lastChange") : null;
        o1s = (o1s != null && o1s instanceof DBObject) ? ((DBObject) o1s).get("dateStamp") : null;
        return o1s != null ? (Long) o1s : -1;
      }
    });
  }

}
