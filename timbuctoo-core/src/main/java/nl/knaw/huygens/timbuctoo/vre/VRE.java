package nl.knaw.huygens.timbuctoo.vre;

import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;
import nl.knaw.huygens.timbuctoo.index.IndexStatus;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.FacetedSearchResultProcessor;
import nl.knaw.huygens.timbuctoo.search.converters.FacetedSearchResultConverter;

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

/**
 * Defines a Virtual Research Environment.
 */
public interface VRE extends Scope {

  /**
   * Returns the unique id of this VRE.
   */
  String getScopeId();

  /**
   * Returns the unique name of this VRE.
   */
  String getName();

  /**
   * Returns the unique name of this VRE.
   */
  String getDescription();

  /**
   * Returns the prefix for domain entities to derive their internal name
   * from the internal name of the primitive domain entities.
   * NOTE. This solution can be made more general.
   */
  String getDomainEntityPrefix();

  /**
   * Returns names of relation types that are considered to be receptions.
   */
  List<String> getReceptionNames();

  /**
   * Search the VRE for the items of the type of {@code entity}.
   * @param type the type to search.
   * @param searchParameters the parameters the entity has to comply to.
   * @return the search result
   * @throws SearchException when the search request could not be processed.
   * @throws SearchValidationException when the searchParameters are not valid.
   */
  // TODO Do we want a wrapper arround FacetedSearchParameters, so we have no references to the faceted search tools library?
  <T extends FacetedSearchParameters<T>> SearchResult search(Class<? extends DomainEntity> type, FacetedSearchParameters<T> searchParameters) throws SearchException, SearchValidationException;

  /**
   * Search the VRE for the items of the type of {@code entity}.
   * @param type the type to search.
   * @param searchParameters the parameters the entity has to comply to.
   * @param facetedSearchResultConverter convert the faceted search result in a special way 
   * @param facetedSearchResultProcessors zero or more to process the faceted search result prior to converting it
   * @return the search result
   * @throws SearchException when the search request could not be processed.
   * @throws SearchValidationException when the searchParameters are not valid.
   */
  <T extends FacetedSearchParameters<T>> SearchResult search(Class<? extends DomainEntity> type, FacetedSearchParameters<T> searchParameters,
      FacetedSearchResultConverter facetedSearchResultConverter, FacetedSearchResultProcessor... facetedSearchResultProcessors) throws SearchException, SearchValidationException;

  /**
   * Creates the indexes for types of the VRE.
   * @param indexFactory the helper that creates the actual indexes.
   */
  void initIndexes(IndexFactory indexFactory);

  /**
   * @return all the indexes of the VRE.
   */
  Collection<Index> getIndexes();

  /**
   * Delete an entity from the index.
   * @param type the type to delete.
   * @param id the id of the type to delete.
   * @throws IndexException throw when the deletion does not succeed.
   */
  void deleteFromIndex(Class<? extends DomainEntity> type, String id) throws IndexException;

  /**
   * Delete multiple entities from the index.
   * @param type the type to delete.
   * @param ids the id's of the type to delete.
   * @throws IndexException throw when the deletion does not succeed.
   */
  void deleteFromIndex(Class<? extends DomainEntity> type, List<String> ids) throws IndexException;

  /**
   * Clears all the indexes of this VRE.
   * @throws IndexException thrown when the clearing of an index fails.
   */
  void clearIndexes() throws IndexException;

  /**
   * Add one or more variations of an entity to an index.
   * @param type the type to determine the index for
   * @param variations all the variations of some model
   * @throws IndexException when the adding to the index fails
   */
  void addToIndex(Class<? extends DomainEntity> type, List<? extends DomainEntity> variations) throws IndexException;

  /**
   * Updates an entity that is already in the index.
   * @param type the type to determine the index for
   * @param variations all the variations of the entity.
   * @throws IndexException when the adding to the index fails
   */
  void updateIndex(Class<? extends DomainEntity> type, List<? extends DomainEntity> variations) throws IndexException;

  /**
   * Closes the VRE and all it's resources like {@link Index}es. 
   */
  void close();

  /**
   * Commits all the changes on each of it's indexes.
   * @throws IndexException thrown when a commit of one index failed.
   */
  void commitAll() throws IndexException;

  /**
   * Adds the status of all the indexes to the index status.
   * @param indexStatus
   */
  void addToIndexStatus(IndexStatus indexStatus);

}
