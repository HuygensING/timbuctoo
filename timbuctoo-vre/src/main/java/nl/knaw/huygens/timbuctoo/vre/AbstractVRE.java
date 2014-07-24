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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexCollection;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.FacetedSearchResultConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractVRE implements VRE {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractVRE.class);

  private final Scope scope;

  private final IndexCollection indexCollection;

  private final FacetedSearchResultConverter facetedSearchResultConverterMock;

  public AbstractVRE() {
    this(new IndexCollection(), new FacetedSearchResultConverter());
  }

  public AbstractVRE(IndexCollection indexCollection, FacetedSearchResultConverter facetedSearchResultConverterMock) {
    this.indexCollection = indexCollection;
    this.facetedSearchResultConverterMock = facetedSearchResultConverterMock;
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

  @Override
  public <T extends FacetedSearchParameters<T>> SearchResult search(Class<? extends DomainEntity> type, FacetedSearchParameters<T> searchParameters) throws SearchException, SearchValidationException {

    Index index = indexCollection.getIndexByType(type);

    FacetedSearchResult facetedSearchResult = index.search(searchParameters);

    return facetedSearchResultConverterMock.convert(TypeNames.getInternalName(type), facetedSearchResult);
  }
}
