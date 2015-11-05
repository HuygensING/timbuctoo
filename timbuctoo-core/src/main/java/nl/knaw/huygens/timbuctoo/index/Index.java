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

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;

import java.util.List;
import java.util.Map;

public interface Index {

  /**
   * Returns the name of the execute.
   *
   * @return the name of the execute.
   */
  String getName();

  /**
   * Adds new items to the execute.
   *
   * @param variations
   * @throws IndexException when the action fails.
   */
  void add(List<? extends DomainEntity> variations) throws IndexException;

  /**
   * Updates existing items in the execute.
   *
   * @param variations
   * @throws IndexException when the action fails.
   */
  void update(List<? extends DomainEntity> variations) throws IndexException;

  /**
   * Deletes an item from the execute.
   *
   * @param id the id of the item to delete.
   * @throws IndexException
   */
  void deleteById(String id) throws IndexException;

  /**
   * Deletes multiple items by id.
   *
   * @param ids the id's of the items to delete.
   * @throws IndexException
   */
  void deleteById(List<String> ids) throws IndexException;

  /**
   * Deletes all the items of the execute.
   *
   * @throws IndexException
   */
  void clear() throws IndexException;

  /**
   * Get the number of items in the execute.
   *
   * @return the number of items in the execute.
   * @throws IndexException
   */
  long getCount() throws IndexException;

  /**
   * Commits all the changes to the execute.
   *
   * @throws IndexException
   */
  void commit() throws IndexException;

  /**
   * Closes the execute.
   *
   * @throws IndexException
   */
  void close() throws IndexException;

  /**
   * Searches the execute.
   *
   * @param searchParameters
   * @return the search result.
   * @throws SearchException           when the search request could not be processed.
   * @throws SearchValidationException when the searchParameters are not valid.
   */
  <T extends FacetedSearchParameters<T>> FacetedSearchResult search(FacetedSearchParameters<T> searchParameters) throws SearchException, SearchValidationException;

  Iterable<Map<String, Object>> doRawSearch(String query, int start, int rows, Map<String, Object> additionalFilters) throws SearchException, RawSearchUnavailableException;

  List<Map<String, Object>> getDataByIds(List<String> ids, List<SortParameter> sort) throws SearchException;
}
