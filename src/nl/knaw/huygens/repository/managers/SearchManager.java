package nl.knaw.huygens.repository.managers;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.SearchResult;
import nl.knaw.huygens.solr.FacetCount;
import nl.knaw.huygens.solr.FacetParameter;
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

  public SearchResult search(Class<? extends Document> type, String core, FacetedSearchParameters searchParameters) throws SolrServerException {

    String searchTerm = createSearchTerm(type, searchParameters);
    QueryResponse response = server.getQueryResponse(searchTerm, getFacetFieldNames(type), searchParameters.getSort(), core);
    SolrDocumentList documents = response.getResults();

    List<FacetCount> facets = getFacetCounts(response.getFacetFields());

    List<String> ids = Lists.newArrayList();

    for (SolrDocument document : documents) {
      ids.add(document.getFieldValue("id").toString());
    }

    SearchResult searchResult = new SearchResult(ids, core, searchTerm, searchParameters.getSort(), new Date().toString());
    searchResult.setFacets(facets);

    return searchResult;
  }

  private String createSearchTerm(Class<? extends Document> type, FacetedSearchParameters searchParameters) {
    List<FacetParameter> facetValues = searchParameters.getFacetValues();
    if (facetValues != null && !facetValues.isEmpty()) {
      StringBuffer buffer = new StringBuffer(String.format("+%s:(%s)", getFullTextSearchFields(type).get(0), searchParameters.getTerm()));
      for (FacetParameter facetParameter : facetValues) {
        buffer.append(" +");
        buffer.append(facetParameter.getName());
        buffer.append(":(");

        boolean isFirstValue = true;
        List<String> values = facetParameter.getValues();
        for (String value : values) {
          if (isFirstValue) {
            isFirstValue = false;
          } else {
            buffer.append(" ");
          }

          buffer.append(value);
        }
        buffer.append(")");
      }
      return buffer.toString();

    }

    return String.format("facet_t_name:(%s)", searchParameters.getTerm());
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

  private Collection<String> getFacetFieldNames(Class<? extends Document> type) {
    if (Person.class.equals(type)) {
      return Sets.newHashSet("facet_t_name", "facet_s_birthDate", "facet_s_deathDate");
    }

    return Sets.newHashSet();
  }

  private List<String> getFullTextSearchFields(Class<? extends Document> type) {
    if (Person.class.equals(type)) {
      return Lists.newArrayList("facet_t_name");
    }
    return Lists.newArrayList();

  }

}
