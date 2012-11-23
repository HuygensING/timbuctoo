package nl.knaw.huygens.repository.model;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

public class IDMapper {
  private static final Map<Class<?>, String> prefixMap;
  static {
    Map<Class<?>, String> aMap = Maps.newHashMap();
    aMap.put(Search.class, "QRY");
    prefixMap = Collections.unmodifiableMap(aMap);
  }

  public static String getPrefix(Class<? extends Document> cls) {
    String rv = prefixMap.get(cls);
    if (rv == null) {
      rv = "UNK";
    }
    return rv;
  }

  public static String getFullId(Class<? extends Document> cls, int counter) {
    return getPrefix(cls) + String.format("%1$010d", counter);
  }
}
