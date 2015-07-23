package nl.knaw.huygens.timbuctoo.vre;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;
import nl.knaw.huygens.timbuctoo.index.IndexStatus;
import nl.knaw.huygens.timbuctoo.index.RawSearchUnavailableException;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.FacetedSearchResultProcessor;

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

/**
 * Defines a Virtual Research Environment.
 */
public interface VRE extends Scope {

  /**
   * Returns the unique id of this VRE.
   */
  String getVreId();

  /**
   * Returns a description of this VRE.
   */
  String getDescription();

  /**
   * Maps the internal name of a primitive domain entity to the corresponding
   * domain entity type in this VRE. This is an internal name as used in the
   * definition of relations.
   *
   * @param iname the internal type name to map.
   * @param required indicates whether there must be a corresponding domain
   *     entity type or not.
   * @return the corresponding domain entity type, or {@code null} if no such
   *     exists and {@code required} is {@code false}.
   * @throws IllegalStateException thrown when no corresponding domain entity
   *     type exists and {@code required} is {@code true}.
   */
  Class<? extends DomainEntity> mapTypeName(String iname, boolean required) throws IllegalStateException;

  /**
   * Returns names of relation types that are considered to be receptions.
   */
  List<String> getReceptionNames();

  /**
   * Search the VRE for the items of the type of {@code entity}.
   * @param type the type to search.
   * @param parameters the parameters the entity has to comply to.
   * @param processors zero or more to process the faceted search result prior to converting it
   * @return the search result
   * @throws SearchException when the search request could not be processed.
   * @throws SearchValidationException when the searchParameters are not valid.
   */
  <T extends FacetedSearchParameters<T>> SearchResult search( //
      Class<? extends DomainEntity> type, //
      FacetedSearchParameters<T> parameters, //
      FacetedSearchResultProcessor... processors //
  ) throws SearchException, SearchValidationException;

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

  /**
   * Search the solr index and get the raw search result.
   *
   * @param type the type to search for
   * @param searchString the string to search for
   * @param start
   * @param numberOfResults 
   * @return a raw solr search result
   * @throws NotInScopeException when the type is not in scope of the VRE 
   * @throws SearchException when the search library throws an exception
   * @throws RawSearchUnavailableException when the type has no field defined to support raw search 
   */
  Iterable<Map<String, Object>> doRawSearch(Class<? extends DomainEntity> type, String searchString, int start, int numberOfResults) throws NotInScopeException, SearchException,
      RawSearchUnavailableException;
}
