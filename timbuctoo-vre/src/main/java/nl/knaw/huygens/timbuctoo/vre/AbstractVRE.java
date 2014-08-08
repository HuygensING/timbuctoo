package nl.knaw.huygens.timbuctoo.vre;

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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexCollection;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;
import nl.knaw.huygens.timbuctoo.index.IndexStatus;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.FacetedSearchResultProcessor;
import nl.knaw.huygens.timbuctoo.search.FullTextSearchFieldFinder;
import nl.knaw.huygens.timbuctoo.search.converters.FacetedSearchResultConverter;
import nl.knaw.huygens.timbuctoo.search.converters.RegularFacetedSearchResultConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public abstract class AbstractVRE implements VRE {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractVRE.class);

  private final Scope scope;

  private IndexCollection indexCollection;

  private final RegularFacetedSearchResultConverter facetedSearchResultConverter;

  public AbstractVRE() {
    this(new IndexCollection(), new RegularFacetedSearchResultConverter());
  }

  public AbstractVRE(IndexCollection indexCollection, RegularFacetedSearchResultConverter facetedSearchResultConverter) {
    this.indexCollection = indexCollection;
    this.facetedSearchResultConverter = facetedSearchResultConverter;
    try {
      scope = createScope();
    } catch (IOException e) {
      LOG.error(e.getMessage());
      throw new IllegalStateException("Failed to create scope");
    }
  }

  protected abstract Scope createScope() throws IOException;

  @Override
  public String getDomainEntityPrefix() {
    return "";
  }

  @Override
  public List<String> getReceptionNames() {
    return Collections.emptyList();
  }

  @Override
  public Set<Class<? extends DomainEntity>> getBaseEntityTypes() {
    return scope.getBaseEntityTypes();
  }

  @Override
  public Set<Class<? extends DomainEntity>> getEntityTypes() {
    return scope.getEntityTypes();
  }

  @Override
  public <T extends DomainEntity> boolean inScope(Class<T> type) {
    return scope.inScope(type);
  }

  @Override
  public <T extends DomainEntity> boolean inScope(Class<T> type, String id) {
    return scope.inScope(type, id);
  }

  @Override
  public <T extends DomainEntity> boolean inScope(T entity) {
    return scope.inScope(entity);
  }

  @Override
  public <T extends DomainEntity> List<T> filter(List<T> entities) {
    return scope.filter(entities);
  }

  /******************************************************************************
   * Index methods 
   ******************************************************************************/

  @Override
  public <T extends FacetedSearchParameters<T>> SearchResult search(Class<? extends DomainEntity> type, FacetedSearchParameters<T> searchParameters) throws SearchException, SearchValidationException {

    return this.search(type, searchParameters, facetedSearchResultConverter);
  }

  @Override
  public <T extends FacetedSearchParameters<T>> SearchResult search(Class<? extends DomainEntity> type, FacetedSearchParameters<T> searchParameters,
      FacetedSearchResultConverter facetedSearchResultConverter, FacetedSearchResultProcessor... resultProcessors) throws SearchException, SearchValidationException {
    prepareSearchParameters(type, searchParameters);

    Index index = this.getIndexForType(type);

    FacetedSearchResult facetedSearchResult = index.search(searchParameters);

    for (FacetedSearchResultProcessor processor : resultProcessors) {
      processor.process(facetedSearchResult);
    }

    return facetedSearchResultConverter.convert(TypeNames.getInternalName(type), facetedSearchResult);
  }

  protected <T extends FacetedSearchParameters<T>> void prepareSearchParameters(Class<? extends DomainEntity> type, FacetedSearchParameters<T> searchParameters) {
    FullTextSearchFieldFinder ftsff = new FullTextSearchFieldFinder();
    searchParameters.setFullTextSearchFields(Lists.newArrayList(ftsff.findFields(type)));
  }

  /**
   * Returns the index if the index for the type can be found, 
   * else it returns an index that does nothing and returns an empty search result.
   * @param type the type to find the index for
   * @return the index
   */
  private Index getIndexForType(Class<? extends DomainEntity> type) {

    return indexCollection.getIndexByType(type);
  }

  @Override
  public void initIndexes(IndexFactory indexFactory) {
    this.indexCollection = IndexCollection.create(indexFactory, this);
  }

  @Override
  public Collection<Index> getIndexes() {
    return indexCollection.getAll();
  }

  @Override
  public void deleteFromIndex(Class<? extends DomainEntity> type, String id) throws IndexException {
    this.getIndexForType(type).deleteById(id);
  }

  @Override
  public void deleteFromIndex(Class<? extends DomainEntity> type, List<String> ids) throws IndexException {
    this.getIndexForType(type).deleteById(ids);
  }

  @Override
  public void clearIndexes() throws IndexException {
    for (Index index : indexCollection) {
      index.clear();
    }
  }

  @Override
  public void addToIndex(Class<? extends DomainEntity> type, List<? extends DomainEntity> variations) throws IndexException {
    indexCollection.getIndexByType(type).add(this.filter(variations));
  }

  @Override
  public void updateIndex(Class<? extends DomainEntity> type, List<? extends DomainEntity> variations) throws IndexException {
    indexCollection.getIndexByType(type).update(this.filter(variations));
  }

  @Override
  public void close() {
    for (Index index : indexCollection) {
      try {
        index.close();
      } catch (IndexException e) {
        LOG.error("closing of index {} went wrong", index.getName(), e);
      }
    }
  }

  @Override
  public void commitAll() throws IndexException {
    for (Index index : indexCollection) {
      index.commit();
    }
  }

  @Override
  public void addToIndexStatus(IndexStatus indexStatus) {

    for (Class<? extends DomainEntity> type : getBaseEntityTypes()) {
      Index index = indexCollection.getIndexByType(type);
      try {
        indexStatus.addCount(this, type, index.getCount());
      } catch (IndexException e) {
        LOG.error("Failed to obtain status: {}", e.getMessage());
      }
    }

  }
}
