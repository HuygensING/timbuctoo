package nl.knaw.huygens.repository.managers;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.model.SearchResult;
import nl.knaw.huygens.solr.FacetCount;
import nl.knaw.huygens.solr.FacetedSearchParameters;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SearchManager {

  private final LocalSolrServer server;

  @Inject
  public SearchManager(LocalSolrServer server) {
    this.server = server;
  }

  public SearchResult search(String core, FacetedSearchParameters searchParameters) throws SolrServerException {
    QueryResponse response = server.getQueryResponse(searchParameters.getTerm(), getFacetFieldNames(), searchParameters.getSort(), core);
    SolrDocumentList documents = response.getResults();

    List<FacetCount> facets = getFacetCounts(response.getFacetFields());

    List<String> ids = Lists.newArrayList();

    for (SolrDocument document : documents) {
      ids.add(document.getFieldValue("id").toString());
    }

    SearchResult searchResult = new SearchResult(ids, core, searchParameters.getTerm(), searchParameters.getSort(), new Date().toString());
    searchResult.setFacets(facets);

    return searchResult;
  }

  private List<FacetCount> getFacetCounts(List<FacetField> facetFields) {
    List<FacetCount> facets = Lists.newArrayList();
    for (FacetField facetField : facetFields) {
      FacetCount facet = new FacetCount();
      facet.setName(facetField.getName());
      facet.setTitle(facetField.getName());

      for (Count count : facetField.getValues()) {
        facet.addOption(new FacetCount.Option().setName(count.getName()).setCount(count.getCount()));
      }

      if (facet.getOptions().size() > 0) {
        facets.add(facet);
      }
    }

    return facets;
  }

  private Collection<String> getFacetFieldNames() {
    return Sets.newHashSet("facet_s_birthDate");
  }

}
