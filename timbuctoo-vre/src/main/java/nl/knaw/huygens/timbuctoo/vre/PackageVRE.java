package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo VRE
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
import com.google.common.collect.Maps;
import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexCollection;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;
import nl.knaw.huygens.timbuctoo.index.IndexStatus;
import nl.knaw.huygens.timbuctoo.index.RawSearchUnavailableException;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.FacetedSearchResultProcessor;
import nl.knaw.huygens.timbuctoo.search.FullTextSearchFieldFinder;
import nl.knaw.huygens.timbuctoo.search.converters.SearchResultConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;
import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toBaseDomainEntity;

/**
 * VRE implementation based on a scope defined by a single model package.
 */
public class PackageVRE implements VRE {

  private static final Logger LOG = LoggerFactory.getLogger(PackageVRE.class);

  private final String vreId;
  private final String description;
  private final List<String> receptions;

  private final Scope scope;
  /**
   * Maps internal names of primitive types to this VRE.
   */
  private final Map<String, Class<? extends DomainEntity>> typeMap;

  private IndexCollection indexCollection;

  private final SearchResultConverter searchResultConverter;

  private final Repository repository;

  public PackageVRE(String vreId, String description, String modelPackage, List<String> receptions, Repository repository) {
    this.vreId = vreId;
    this.description = description;
    this.receptions = receptions;
    this.repository = repository;
    this.indexCollection = new IndexCollection();
    this.searchResultConverter = new SearchResultConverter(vreId);
    this.scope = createScope(modelPackage);
    this.typeMap = createTypeMap();
  }

  // For testing
  PackageVRE(String vreId, String description, Scope scope, IndexCollection indexCollection, SearchResultConverter searchResultConverter, Repository repository) {
    this.vreId = vreId;
    this.description = description;
    this.repository = repository;
    this.receptions = Lists.newArrayList();
    this.indexCollection = indexCollection;
    this.searchResultConverter = searchResultConverter;
    this.scope = scope;
    this.typeMap = createTypeMap();
  }

  @Override
  public String getVreId() {
    return vreId;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public List<String> getReceptionNames() {
    return receptions;
  }

  private Scope createScope(String modelPackage) {
    try {
      return new PackageScope(modelPackage);
    } catch (IOException e) {
      LOG.error("Failed to create scope for package {}: {}", modelPackage, e.getMessage());
      throw new IllegalStateException("Failed to create scope");
    }
  }

  private Map<String, Class<? extends DomainEntity>> createTypeMap() {
    Map<String, Class<? extends DomainEntity>> map = Maps.newHashMap();
    for (Class<? extends DomainEntity> type : getEntityTypes()) {
      Class<? extends DomainEntity> baseType = toBaseDomainEntity(type);
      if (map.put(getInternalName(baseType), type) != null) {
        LOG.error("Inconsistent type map; duplicate value for {}", baseType.getSimpleName());
      }
    }
    return map;
  }

  @Override
  public Class<? extends DomainEntity> mapTypeName(String iname, boolean required) throws IllegalStateException {
    Class<? extends DomainEntity> type = typeMap.get(iname);
    if (type == null && required) {
      LOG.error("No entity with type name {} in VRE {}", iname, vreId);
      throw new IllegalStateException("No entity with type name " + iname);
    }
    return type;
  }

  @Override
  public Set<Class<? extends DomainEntity>> getPrimitiveEntityTypes() {
    return scope.getPrimitiveEntityTypes();
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

  @Override
  public Class<? extends DomainEntity> mapToScopeType(Class<? extends DomainEntity> baseType) throws NotInScopeException {
    return scope.mapToScopeType(baseType);
  }

  /******************************************************************************
   * Index methods
   ******************************************************************************/

  @Override
  public <T extends FacetedSearchParameters<T>> SearchResult search( //
      Class<? extends DomainEntity> type, //
      FacetedSearchParameters<T> parameters, //
      FacetedSearchResultProcessor... processors //
  ) throws SearchException, SearchValidationException {

    prepareSearchParameters(type, parameters);
    FacetedSearchResult facetedSearchResult = getIndexForType(type).search(parameters);
    for (FacetedSearchResultProcessor processor : processors) {
      processor.process(facetedSearchResult);
    }
    return searchResultConverter.convert(TypeNames.getInternalName(type), facetedSearchResult);
  }

  protected <T extends FacetedSearchParameters<T>> void prepareSearchParameters(Class<? extends DomainEntity> type, FacetedSearchParameters<T> searchParameters) {
    FullTextSearchFieldFinder ftsff = new FullTextSearchFieldFinder();
    searchParameters.setFullTextSearchFields(Lists.newArrayList(ftsff.findFields(type)));
  }

  /**
   * Returns the execute if the execute for the type can be found,
   * else it returns an execute that does nothing and returns an empty search result.
   *
   * @param type the type to find the execute for
   * @return the execute
   */
  private Index getIndexForType(Class<? extends DomainEntity> type) {
    return indexCollection.getIndexByType(type);
  }

  @Override
  public void initIndexes(IndexFactory indexFactory) {
    indexCollection = new IndexCollection();
    for (Class<? extends DomainEntity> type : getEntityTypes()) {
      Index index = indexFactory.createIndexFor(this, type);
      indexCollection.addIndex(type, index);
    }
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

    IndexChanger indexAdder = new IndexChanger() {
      @Override
      public void change(Class<? extends DomainEntity> type, List<? extends DomainEntity> variations) throws IndexException {
        indexCollection.getIndexByType(type).add(variations);
      }
    };
    changeIndex(indexAdder, type, variations);
  }

  private void changeIndex(IndexChanger changer, Class<? extends DomainEntity> type, List<? extends DomainEntity> variations) throws IndexException {
    List<? extends DomainEntity> filteredVariations = this.filter(variations);
    for (DomainEntity variation : filteredVariations) {
      repository.addDerivedProperties(this, variation);
    }
    changer.change(type, filteredVariations);
  }

  @Override
  public void updateIndex(Class<? extends DomainEntity> type, List<? extends DomainEntity> variations) throws IndexException {
    IndexChanger indexUpdater = new IndexChanger() {
      @Override
      public void change(Class<? extends DomainEntity> type, List<? extends DomainEntity> variations) throws IndexException {
        indexCollection.getIndexByType(type).update(variations);
      }
    };
    changeIndex(indexUpdater, type, variations);
  }

  @Override
  public void close() {
    for (Index index : indexCollection) {
      try {
        index.close();
      } catch (IndexException e) {
        LOG.error("closing of execute {} went wrong", index.getName(), e);
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
    for (Class<? extends DomainEntity> type : getPrimitiveEntityTypes()) {
      Index index = indexCollection.getIndexByType(type);
      try {
        indexStatus.addCount(this, type, index.getCount());
      } catch (IndexException e) {
        LOG.error("Failed to obtain status: {}", e.getMessage());
      }
    }
  }

  @Override
  public Iterable<Map<String, Object>> doRawSearch(Class<? extends DomainEntity> type, String query, int start, int rows, Map<String, Object> additionalFilters) throws NotInScopeException, SearchException, RawSearchUnavailableException {
    throwNotInScopeExceptionWhenNotInScope(type);

    return getIndexForType(type).doRawSearch(query, start, rows, additionalFilters);
  }

  private void throwNotInScopeExceptionWhenNotInScope(Class<? extends DomainEntity> type) throws NotInScopeException {
    if (!inScope(type)) {
      throw NotInScopeException.typeIsNotInScope(type, vreId);
    }
  }

  @Override
  public List<Map<String, Object>> getRawDataFor(Class<? extends DomainEntity> type, List<String> ids, List<SortParameter> sort) throws NotInScopeException, SearchException {
    throwNotInScopeExceptionWhenNotInScope(type);

    return getIndexForType(type).getDataByIds(ids, sort);
  }

  @Override
  public String searchRelations(Class<? extends Relation> type, RelationSearchParameters parameters) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private interface IndexChanger {
    void change(Class<? extends DomainEntity> type, List<? extends DomainEntity> variations) throws IndexException;
  }

}
