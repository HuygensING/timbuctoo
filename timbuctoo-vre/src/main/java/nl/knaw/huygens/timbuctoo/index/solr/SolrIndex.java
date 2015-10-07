package nl.knaw.huygens.timbuctoo.index.solr;

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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.facetedsearch.FacetedSearchException;
import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.NoSuchFieldInIndexException;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.facetedsearch.model.parameters.IndexDescription;
import nl.knaw.huygens.facetedsearch.model.parameters.SortDirection;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.RawSearchUnavailableException;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SolrIndex implements Index {

  protected static final SolrQuery COUNT_QUERY;

  private final SolrInputDocumentCreator solrDocumentCreator;
  private final AbstractSolrServer solrServer;
  private final String name;
  private final FacetedSearchLibrary facetedSearchLibrary;
  private final IndexDescription indexDescription;
  private final String rawSearchField;

  static {
    COUNT_QUERY = new SolrQuery();
    COUNT_QUERY.setQuery("*:*");
    COUNT_QUERY.setRows(0);
  }

  public SolrIndex(String name, String rawSearchField, IndexDescription indexDescription, SolrInputDocumentCreator solrDocumentCreator, AbstractSolrServer solrServer,
                   FacetedSearchLibrary facetedSearchLibrary) {
    this.name = name;
    this.indexDescription = indexDescription;
    this.solrDocumentCreator = solrDocumentCreator;
    this.solrServer = solrServer;
    this.facetedSearchLibrary = facetedSearchLibrary;
    this.rawSearchField = rawSearchField;
  }

  @Override
  public void add(List<? extends DomainEntity> variations) throws IndexException {
    updateIndex(variations);

  }

  private void updateIndex(List<? extends DomainEntity> variations) throws IndexException {
    if (variations == null || variations.isEmpty()) {
      return;
    }

    SolrInputDocument document = solrDocumentCreator.create(variations);

    try {
      solrServer.add(document);
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }
  }

  @Override
  public void update(List<? extends DomainEntity> variations) throws IndexException {
    updateIndex(variations);
  }

  @Override
  public void deleteById(String id) throws IndexException {
    if (id == null) {
      return;
    }

    try {
      solrServer.deleteById(id);
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }

  }

  @Override
  public void deleteById(List<String> ids) throws IndexException {
    try {
      if (ids != null && !ids.isEmpty()) {
        solrServer.deleteById(ids);
      }
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }

  }

  @Override
  public void clear() throws IndexException {
    try {
      solrServer.deleteByQuery("*:*");
      solrServer.commit();
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }
  }

  @Override
  public long getCount() throws IndexException {
    try {
      SolrDocumentList results = solrServer.search(COUNT_QUERY).getResults();
      return results.getNumFound();
    } catch (SolrServerException e) {
      throw new IndexException(e);
    }
  }

  @Override
  public void commit() throws IndexException {
    try {
      solrServer.commit();
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }

  }

  @Override
  public void close() throws IndexException {
    try {
      this.commit();
    } finally {
      try {
        solrServer.shutdown();
      } catch (SolrServerException e) {
        throw new IndexException(e);
      } catch (IOException e) {
        throw new IndexException(e);
      }
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public <T extends FacetedSearchParameters<T>> FacetedSearchResult search(FacetedSearchParameters<T> searchParameters) throws SearchException, SearchValidationException {
    try {
      List<String> facetFields = searchParameters.getFacetFields();
      if (facetFields == null || facetFields.isEmpty()) {
        searchParameters.setFacetFields(indexDescription.getFacetFields());
      }

      return facetedSearchLibrary.search(searchParameters);
    } catch (NoSuchFieldInIndexException e) {
      throw new SearchValidationException(e);
    } catch (FacetedSearchException e) {
      throw new SearchException(e);
    }
  }

  @Override
  public Iterable<Map<String, Object>> doRawSearch(String query, int start, int rows, Map<String, Object> additionalFilters) throws SearchException, RawSearchUnavailableException {
    QueryResponse queryResponse = null;
    String queryString = createSolrQuery(query, additionalFilters);
    SolrQuery solrQuery = new SolrQuery(queryString).setStart(start).setRows(rows);
    return getSingleRawResults(solrQuery);
  }

  private List<Map<String, Object>> getSingleRawResults(SolrQuery solrQuery) throws SearchException {
    QueryResponse queryResponse;
    try {
      queryResponse = solrServer.search(solrQuery);
    } catch (SolrServerException e) {
      throw new SearchException(e);
    }

    List<Map<String, Object>> results = Lists.newArrayList();
    for (SolrDocument doc : queryResponse.getResults()) {
      results.add(doc.getFieldValueMap());
    }
    return results;
  }

  private List<Map<String, Object>> getMultiRawResults(SolrQuery solrQuery) throws SearchException {
    QueryResponse queryResponse;
    try {
      queryResponse = solrServer.search(solrQuery);
    } catch (SolrServerException e) {
      throw new SearchException(e);
    }

    List<Map<String, Object>> results = Lists.newArrayList();
    for (SolrDocument doc : queryResponse.getResults()) {
      Map<String, Object> fieldValueMap = Maps.newHashMap();
      /*
       * Do not use doc.getFieldValueMap because it will return a map that returns only the first value on get.
       */
      for (Map.Entry<String, Object> entry : doc.entrySet()) {
        fieldValueMap.put(entry.getKey(), entry.getValue());
      }
      results.add(fieldValueMap);
    }
    return results;
  }

  private String createSolrQuery(String query, Map<String, Object> additionalFilters) throws RawSearchUnavailableException {
    if (Strings.isNullOrEmpty(rawSearchField)) {
      throw new RawSearchUnavailableException(name);
    }

    String baseQuery = String.format("%s:%s", rawSearchField, cleanUpSpecialCharaters(query));

    if (additionalFilters.isEmpty()) {
      return baseQuery;
    }


    StringBuilder sb = new StringBuilder(String.format("+(%s)", baseQuery));

    for (Map.Entry<String, Object> filter : additionalFilters.entrySet()) {
      sb.append(String.format(" +(%s:%s)", filter.getKey(), filter.getValue()));
    }
    return sb.toString();


  }

  @Override
  public List<Map<String, Object>> getDataByIds(List<String> ids, List<SortParameter> sort) throws SearchException {
    final int maxNumberOfIdsSolrSupports = 1000;
    List<List<String>> idsPart = Lists.partition(ids, maxNumberOfIdsSolrSupports);
    List<Map<String, Object>> results = Lists.newArrayList();

    List<SolrQuery.SortClause> sortClauses = Lists.newArrayList();

    for (SortParameter sortParameter : sort) {
      SolrQuery.ORDER order = SolrQuery.ORDER.desc;
      if(SortDirection.ASCENDING.equals(sortParameter.getDirection())){
        order = SolrQuery.ORDER.asc;
      }

      sortClauses.add(new SolrQuery.SortClause(sortParameter.getFieldname(), order));
    }


    for (List<String> part : idsPart) {
      addResultsOfPartialQuery(part, results, sortClauses);
    }


    return results;
  }

  private void addResultsOfPartialQuery(List<String> ids, List<Map<String, Object>> results, List<SolrQuery.SortClause> sortClauses) throws SearchException {
    StringBuilder queryBuilder = new StringBuilder(Entity.INDEX_FIELD_ID);
    queryBuilder.append(" : (");
    boolean isFirst = true;
    for (String id : ids) {
      if (!isFirst) {
        queryBuilder.append(" OR ");
      }
      queryBuilder.append(id);
      isFirst = false;
    }
    queryBuilder.append(")");

    SolrQuery query = new SolrQuery(queryBuilder.toString());

    query.setRows(ids.size());
    query.setSorts(sortClauses);

    results.addAll(getMultiRawResults(query));
  }

  private String cleanUpSpecialCharaters(String term) {

    return term.replace(":", " ");
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SolrIndex)) {
      return false;
    }

    SolrIndex other = (SolrIndex) obj;

    return new EqualsBuilder().append(name, other.name)//
      .append(rawSearchField, other.rawSearchField) //
      .append(solrDocumentCreator, other.solrDocumentCreator)//
      .append(facetedSearchLibrary, other.facetedSearchLibrary)//
      .append(solrServer, other.solrServer)//
      .append(indexDescription, other.indexDescription).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(name)//
      .append(rawSearchField) //
      .append(solrDocumentCreator)//
      .append(facetedSearchLibrary)//
      .append(solrServer)//
      .append(indexDescription).toHashCode();
  }
}
