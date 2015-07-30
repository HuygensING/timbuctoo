package nl.knaw.huygens.timbuctoo.model.mapping;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;

public class FieldNameMap {

  private final Map<String, String> map;

  public FieldNameMap() {
    map = Maps.newHashMap();
  }

  public Set<String> getKeys() {
    return map.keySet();
  }

  public String get(String key) {
    return map.get(key);
  }

  void put(String key, String value) {
    map.put(key, value);
  }
}
