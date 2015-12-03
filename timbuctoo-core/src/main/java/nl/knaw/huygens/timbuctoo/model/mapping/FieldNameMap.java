package nl.knaw.huygens.timbuctoo.model.mapping;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
    return value;
  }

  private boolean isRange(String key) {
    return key.startsWith("dynamic_i");
  }

  @Override
  public String toString() {
    return map.toString();
  }
}
