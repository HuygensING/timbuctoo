package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo VRE
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

import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;
import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toBaseDomainEntity;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import com.google.common.collect.Maps;

/**
 * VRE implementation based on a scope defined by a single model package.
 */
public class PackageVRE implements VRE {

  private static final Logger LOG = LoggerFactory.getLogger(PackageVRE.class);

  private final String vreId;
  private final String description;
  private final List<String> receptions;

  private final Scope scope;
  private final Map<String, String> typeNameMap;

  private IndexCollection indexCollection;

  private final RegularFacetedSearchResultConverter facetedSearchResultConverter;

  public PackageVRE(String vreId, String description, String modelPackage, List<String> receptions) {
    this.vreId = vreId;
    this.description = description;
    this.receptions = receptions;
    this.indexCollection = new IndexCollection();
    this.facetedSearchResultConverter = new RegularFacetedSearchResultConverter();
    this.scope = createScope(modelPackage);
    this.typeNameMap = createTypeNameMap();
  }

  // For testing
  public PackageVRE(String vreId, String description, Scope scope, IndexCollection indexCollection, RegularFacetedSearchResultConverter facetedSearchResultConverter) {
    this.vreId = vreId;
    this.description = description;
    this.receptions = Lists.newArrayList();
    this.indexCollection = indexCollection;
    this.facetedSearchResultConverter = facetedSearchResultConverter;
    this.scope = scope;
    this.typeNameMap = createTypeNameMap();
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

  private Map<String, String> createTypeNameMap() {
    Map<String, String> map = Maps.newHashMap();
    for (Class<? extends DomainEntity> type : getEntityTypes()) {
      Class<? extends DomainEntity> baseType = toBaseDomainEntity(type);
      if (map.put(getInternalName(baseType), getInternalName(type)) != null) {
        LOG.error("Inconsistent type name map; multiple values for {}", getInternalName(baseType));
      }
    }
    return map;
  }

  @Override
  public String mapPrimitiveTypeName(String iname) {
    return typeNameMap.get(iname);
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
    for (Class<? extends DomainEntity> type : getPrimitiveEntityTypes()) {
      Index index = indexCollection.getIndexByType(type);
      try {
        indexStatus.addCount(this, type, index.getCount());
      } catch (IndexException e) {
        LOG.error("Failed to obtain status: {}", e.getMessage());
      }
    }
  }

}
