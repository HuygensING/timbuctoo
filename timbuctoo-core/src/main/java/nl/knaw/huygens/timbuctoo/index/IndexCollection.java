package nl.knaw.huygens.timbuctoo.index;

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

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toBaseDomainEntity;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.util.DefaultingMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexCollection implements Iterable<Index> {

  private static final Logger LOG = LoggerFactory.getLogger(IndexCollection.class);

  private final Map<Class<? extends DomainEntity>, Index> indexMap;

  public IndexCollection() {
    Index defaultIndex = new NoOpIndex();
    indexMap = DefaultingMap.newHashMap(defaultIndex);
  }

  /**
   * Returns the index if the index for the type can be found, 
   * else it returns an index that does nothing and returns an empty search result.
   * @param type the type to find the index for
   * @return the index
   */
  public Index getIndexByType(Class<? extends DomainEntity> type) {
    return indexMap.get(toBaseDomainEntity(type));
  }

  /**
   * Add an index for a certain type.
   * @param type the type that belongs to the index.
   * @param index the index to add.
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

  /**
   * A <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">null object</a> class, 
   * for missing indexes. 
   */
  static class NoOpIndex implements Index {

    @Override
    public void add(List<? extends DomainEntity> variations) {}

    @Override
    public void update(List<? extends DomainEntity> variations) throws IndexException {}

    @Override
    public void deleteById(String id) {}

    @Override
    public void deleteById(List<String> ids) {}

    @Override
    public void clear() {}

    @Override
    public long getCount() {
      return 0;
    }

    @Override
    public void commit() {}

    @Override
    public void close() {}

    @Override
    public String getName() {
      return null;
    }

    @Override
    public <T extends FacetedSearchParameters<T>> FacetedSearchResult search(FacetedSearchParameters<T> searchParamaters) {
      LOG.warn("Searching on a non existing index");
      return new FacetedSearchResult();
    }
  }

}
