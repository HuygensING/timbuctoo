package nl.knaw.huygens.timbuctoo.index;

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

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toBaseDomainEntity;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.util.DefaultingMap;

public class IndexCollection implements Iterable<Index> {

  private final Map<Class<? extends DomainEntity>, Index> indexMap;

  public IndexCollection() {
    Index defaultIndex = new NoOpIndex();
    indexMap = DefaultingMap.newHashMap(defaultIndex);
  }

  /**
   * Returns the execute if the execute for the type can be found,
   * else it returns an execute that does nothing and returns an empty search result.
   * @param type the type to find the execute for
   * @return the execute
   */
  public Index getIndexByType(Class<? extends DomainEntity> type) {
    return indexMap.get(toBaseDomainEntity(type));
  }

  /**
   * Add an execute for a certain type.
   * @param type the type that belongs to the execute.
   * @param index the execute to add.
   */
  public void addIndex(Class<? extends DomainEntity> type, Index index) {
    indexMap.put(toBaseDomainEntity(type), index);
  }

  /**
   * Get all the indexes of this collection.
   * @return a collection with indexes
   */
  public Collection<Index> getAll() {
    return indexMap.values();
  }

  @Override
  public Iterator<Index> iterator() {
    return getAll().iterator();
  }

  // ---------------------------------------------------------------------------


}
