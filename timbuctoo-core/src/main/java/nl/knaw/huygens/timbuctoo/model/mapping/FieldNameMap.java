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
   * @param <T>   the type of the value of the map.
   * @return the remapped map
   */
  public <T> Map<String, T> remap(Map<String, T> input) {
    Map<String, T> remapped = Maps.newHashMap();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      if (input.containsKey(key)) {
        remapped.put(entry.getValue(), input.get(key));
      }
    }

    return entity.createRelSearchRep(remapped);
  }

  @Override
  public String toString() {
    return map.toString();
  }
}
