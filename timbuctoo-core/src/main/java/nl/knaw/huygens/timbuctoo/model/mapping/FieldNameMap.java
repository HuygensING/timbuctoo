package nl.knaw.huygens.timbuctoo.model.mapping;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import java.util.Map;
import java.util.Set;

public class FieldNameMap {

  public final DomainEntity entity;

  private final Map<String, String> map;

  public FieldNameMap(DomainEntity entity) {
    this.entity = entity;
    map = Maps.newHashMap();
  }

  public Set<String> getFromNames() {
    return map.keySet();
  }

  public String translate(String from) {
    return map.get(from);
  }

  void put(String from, String to) {
    map.put(from, to);
  }

  /**
   * Changes the keys of the input from the from-keys to the target-keys.
   *
   * @param input the map to be remapped
   * @return the remapped map
   */
  public Map<String, String> remap(Map<String, String> input) {
    Map<String, String> remapped = Maps.newHashMap();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();

      if (isRange(key)) {
        addRangeValue(entry, input, remapped);
      }

      if (input.containsKey(key)) {
        remapped.put(entry.getValue(), input.get(key));
      }
    }

    return entity.createRelSearchRep(remapped);
  }

  private String formatKey(String key) {
    if (isRange(key)) {
      if (key.endsWith("_low")) {
        return key.substring(0, key.indexOf("_low"));
      } else {
        if (key.endsWith("_high")) {
          return key.substring(0, key.indexOf("_high"));
        }
      }
    }
    return key;
  }

  private <T> void addRangeValue(Map.Entry<String, String> entry, Map<String, String> source, Map<String, String> target) {
    String key = entry.getKey() + "_low";
    if (source.containsKey(key)) {
      target.put(entry.getValue(), getRangeValue(source, key));
    }

  }

  private String getRangeValue(Map<String, String> source, String key) {
    String value = source.get(key);
    return value.substring(0, 4);
  }

  private boolean isRange(String key) {
    return key.startsWith("dynamic_i");
  }

  @Override
  public String toString() {
    return map.toString();
  }
}
