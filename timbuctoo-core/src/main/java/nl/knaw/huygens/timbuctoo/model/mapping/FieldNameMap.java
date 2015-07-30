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
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  public String get(String key) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  void put(String key, String value) {
    map.put(key, value);
  }
}
