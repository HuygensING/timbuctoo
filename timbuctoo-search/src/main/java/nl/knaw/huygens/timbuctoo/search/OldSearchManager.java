package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo search
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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.solr.FacetInfo;
import nl.knaw.huygens.solr.FacetParameter;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SolrUtils;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.facet.FacetCount;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.vre.Scope;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OldSearchManager implements SearchManager {

  private final IndexManager server;
  private final FacetFinder facetFinder;
  private final AbstractFieldFinder fullTextSearchFieldFinder;
  private final SortableFieldFinder sortableFieldFinder;

  @Inject
  public OldSearchManager(IndexManager server) {
    this.server = server;
    this.facetFinder = new FacetFinder();
    this.fullTextSearchFieldFinder = new FullTextSearchFieldFinder();
    this.sortableFieldFinder = new SortableFieldFinder();
  }

  @Override
  public Set<String> findSortableFields(Class<? extends DomainEntity> type) {
    return sortableFieldFinder.findFields(type);
  }

  @Override
  public SearchResult search(VRE vre, Class<? extends DomainEntity> type, SearchParameters searchParameters) throws IndexException, NoSuchFacetException {
    Scope scope = vre.getScope();
    Map<String, FacetInfo> facetInfoMap = facetFinder.findFacets(type);
    Set<String> fullTextSearchFields = fullTextSearchFieldFinder.findFields(type);
    String searchTerm = createSearchTerm(type, searchParameters, facetInfoMap.keySet(), fullTextSearchFields);
    SolrQuery solrQuery = createFacettedSearchQuery(searchTerm, facetInfoMap.keySet(), searchParameters.getSort());

    QueryResponse response = server.search(scope, type, solrQuery);

    List<String> ids = Lists.newArrayList();
    for (SolrDocument document : response.getResults()) {
      ids.add(document.getFieldValue("id").toString());
    }

    SearchResult searchResult = new SearchResult(ids, TypeNames.getInternalName(type), searchTerm, searchParameters.getSort(), new Date());

    List<FacetCount> facets = getFacetCounts(response.getFacetFields(), facetInfoMap);
    searchResult.setFacets(facets);

    return searchResult;
  }

  // FIXME this is probably suboptimal:
  private static final int ROWS = 20000;
  private static final int FACET_LIMIT = 10000;

  private SolrQuery createFacettedSearchQuery(String query, Collection<String> facetFieldNames, String sortField) {
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setQuery(query);
    solrQuery.setFields("id");
    solrQuery.setRows(ROWS);
    solrQuery.addFacetField(facetFieldNames.toArray(new String[facetFieldNames.size()]));
    solrQuery.setFacetMinCount(0);
    solrQuery.setFacetLimit(FACET_LIMIT);
    solrQuery.setFilterQueries("!cache=false");
    solrQuery.setSort(new SortClause(sortField, SolrQuery.ORDER.asc));
    return solrQuery;
  }

  private String createSearchTerm(Class<? extends Entity> type, SearchParameters searchParameters, Set<String> existingFacets, Set<String> fullTextSearchFields) throws NoSuchFacetException {
    List<FacetParameter> facetValues = searchParameters.getFacetValues();
    boolean usesFacets = facetValues != null && !facetValues.isEmpty();
    StringBuilder builder = new StringBuilder();
    String prefix = "";
    if ("*".equals(searchParameters.getTerm()) || StringUtils.isBlank(searchParameters.getTerm())) {
      builder.append("*:*");
    } else {
      builder.append(usesFacets ? "+" : "").append("(");
      for (String field : fullTextSearchFields) {
        builder.append(prefix).append(field).append(":");
        builder.append(formatTerm(searchParameters.getTerm()));
        prefix = " ";
      }
      builder.append(")");
    }
    if (usesFacets) {
      for (FacetParameter facetParameter : facetValues) {
        String name = facetParameter.getName();
        if (existingFacets.contains(name)) {
          builder.append(" +").append(name).append(":");
          builder.append(formatFacetValues(facetParameter.getValues()));
        } else {
          throw new NoSuchFacetException(name);
        }
      }
    }
    return builder.toString();
  }

  private String formatFacetValues(List<String> values) {
    if (values.size() > 1) {
      StringBuilder builder = new StringBuilder();
      builder.append("(");
      String prefix = "";
      for (String value : values) {
        builder.append(prefix).append(SolrUtils.escapeFacetId(value));
        prefix = " ";
      }
      builder.append(")");
      return builder.toString();
    }
    return SolrUtils.escapeFacetId(values.get(0));
  }

  private String formatTerm(String term) {
    if (term.trim().contains(" ")) {
      return String.format("(%s)", term);
    }
    return term;
  }

  private List<FacetCount> getFacetCounts(List<FacetField> facetFields, Map<String, FacetInfo> facetInfoMap) {
    List<FacetCount> facets = Lists.newArrayList();
    for (FacetField facetField : facetFields) {
      FacetInfo info = facetInfoMap.get(facetField.getName());

      FacetCount facet = new FacetCount();
      facet.setName(facetField.getName());
      facet.setTitle(info.getTitle());
      facet.setType(info.getType());

      for (Count count : facetField.getValues()) {
        if (count.getCount() > 0) {
          facet.addOption(new FacetCount.Option().setName(count.getName()).setCount(count.getCount()));
        }
      }

      if (!facet.getOptions().isEmpty()) {
        facets.add(facet);
      }
    }

    return facets;
  }

}
