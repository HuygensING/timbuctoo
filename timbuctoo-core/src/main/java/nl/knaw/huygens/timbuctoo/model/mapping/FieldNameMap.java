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

  /**
   * Changes the keys of the input from the from-keys to the target-keys.
   * @param input the map to be remapped
   * @param <T> the type of the value of the map.
   * @return the remapped map
   */
  public <T> Map<String, T> remap(Map<String, T> input) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }
}
