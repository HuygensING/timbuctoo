package nl.knaw.huygens.timbuctoo.util;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Preconditions;

public class DefaultingMap<K, V> implements Map<K,V> {

  public static <K, V> Map<K, V> newHashMap(V defaultValue) {
    return new DefaultingMap<K, V>(new HashMap<K, V>(), defaultValue);
  }

  public static <K, V> Map<K, V> newTreeMap(V defaultValue) {
    return new DefaultingMap<K, V>(new TreeMap<K, V>(), defaultValue);
  }

  // ---------------------------------------------------------------------------

  private final Map<K, V> map;
  private final V defaultValue;

  public DefaultingMap(Map<K,V> map, V defaultValue) {
    this.map = Preconditions.checkNotNull(map);
    this.defaultValue = defaultValue;
  }

  @Override
  public V get(Object key) {
    V value = map.get(key);
    return (value != null) ? value : defaultValue;
  }

  // The other methods simply delegate to {@code map}.

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return map.entrySet();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public V put(K key, V value) {
    return map.put(key, value);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    map.putAll(m);
  }

  @Override
  public V remove(Object key) {
    return map.remove(key);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public Collection<V> values() {
    return map.values();
  }

}
