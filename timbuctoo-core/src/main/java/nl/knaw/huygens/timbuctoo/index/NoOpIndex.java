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

import com.google.common.collect.Lists;
import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.RawSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * A <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">null object</a> class, 
 * for missing indexes. 
 */
public class NoOpIndex implements Index {
  private static Logger LOG = LoggerFactory.getLogger(NoOpIndex.class);

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

  @Override
  public RawSearchResult doRawSearch(String query, int start, int rows, Map<String, Object> additionalFilters) {
    return new RawSearchResult(0, Lists.<Map<String, Object>>newArrayList());
  }
}
