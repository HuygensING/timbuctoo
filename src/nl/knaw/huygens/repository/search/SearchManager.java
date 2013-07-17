package nl.knaw.huygens.repository.search;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.SearchResult;
import nl.knaw.huygens.solr.FacetCount;
import nl.knaw.huygens.solr.FacetInfo;
import nl.knaw.huygens.solr.FacetParameter;
import nl.knaw.huygens.solr.FacetedSearchParameters;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SearchManager {

  private final LocalSolrServer server;
  private final FacetFinder facetFinder;
  private final FullTextSearchFieldFinder fullTextSearchFieldFinder;

  @Inject
  public SearchManager(LocalSolrServer server, FacetFinder facetFinder, FullTextSearchFieldFinder fullTextSearchFieldFinder) {
    this.server = server;
    this.facetFinder = facetFinder;
    this.fullTextSearchFieldFinder = fullTextSearchFieldFinder;
  }

  public SearchResult search(Class<? extends Document> type, String core, FacetedSearchParameters searchParameters) throws SolrServerException, FacetDoesNotExistException {
    Map<String, FacetInfo> facetInfoMap = facetFinder.findFacets(type);
    Set<String> fullTextSearchFields = fullTextSearchFieldFinder.findFullTextSearchFields(type);
    String searchTerm = createSearchTerm(type, searchParameters, facetInfoMap.keySet(), fullTextSearchFields);
    QueryResponse response = server.getQueryResponse(searchTerm, facetInfoMap.keySet(), searchParameters.getSort(), core);
    SolrDocumentList documents = response.getResults();

    List<FacetCount> facets = getFacetCounts(response.getFacetFields(), facetInfoMap);

    List<String> ids = Lists.newArrayList();

    for (SolrDocument document : documents) {
      ids.add(document.getFieldValue("id").toString());
    }

    SearchResult searchResult = new SearchResult(ids, core, searchTerm, searchParameters.getSort(), new Date().toString());
    searchResult.setFacets(facets);

    return searchResult;
  }

  private String createSearchTerm(Class<? extends Document> type, FacetedSearchParameters searchParameters, Set<String> existingFacets, Set<String> fullTextSearchFields)
      throws FacetDoesNotExistException {
    List<FacetParameter> facetValues = searchParameters.getFacetValues();
    boolean usesFacets = facetValues != null && !facetValues.isEmpty();
    StringBuilder builder = new StringBuilder();
    boolean isFirst = true;
    for (String fullTextSearchField : fullTextSearchFields) {
      if (!isFirst) {
        builder.append(" ");
      }
      if (usesFacets) {
        builder.append(String.format("+%s:(%s)", fullTextSearchField, searchParameters.getTerm()));
      } else {
        builder.append(String.format("%s:(%s)", fullTextSearchField, searchParameters.getTerm()));
      }
      isFirst = false;
    }
    if (usesFacets) {
      for (FacetParameter facetParameter : facetValues) {
        if (!existingFacets.contains(facetParameter.getName())) {
          throw new FacetDoesNotExistException("Facet " + facetParameter.getName() + " does not exist.");
        }
        builder.append(" +");
        builder.append(facetParameter.getName());
        builder.append(":(");
        builder.append(StringUtils.join(facetParameter.getValues(), " "));
        builder.append(")");
      }
    }

    return builder.toString();
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
