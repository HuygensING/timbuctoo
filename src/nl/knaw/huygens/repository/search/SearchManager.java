package nl.knaw.huygens.repository.search;

import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.SearchResult;
import nl.knaw.huygens.repository.model.atlg.ATLGArchive;
import nl.knaw.huygens.repository.model.atlg.ATLGArchiver;
import nl.knaw.huygens.repository.model.atlg.ATLGLegislation;
import nl.knaw.huygens.solr.FacetCount;
import nl.knaw.huygens.solr.FacetInfo;
import nl.knaw.huygens.solr.FacetParameter;
import nl.knaw.huygens.solr.FacetType;
import nl.knaw.huygens.solr.FacetedSearchParameters;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
    Map<String, FacetInfo> facetInfoMap = getFacetFieldNames(type);
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

  private String createSearchTerm(Class<? extends Document> type, FacetedSearchParameters searchParameters) {
    List<FacetParameter> facetValues = searchParameters.getFacetValues();
    if (facetValues != null && !facetValues.isEmpty()) {
      StringBuffer buffer = new StringBuffer(String.format("+%s:(%s)", getFullTextSearchFields(type).get(0), searchParameters.getTerm()));
      for (FacetParameter facetParameter : facetValues) {
        buffer.append(" +");
        buffer.append(facetParameter.getName());
        buffer.append(":(");

        buffer.append(StringUtils.join(facetParameter.getValues(), " "));

        buffer.append(")");
      }
      return buffer.toString();

    }

    return String.format("facet_t_name:(%s)", searchParameters.getTerm());
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

  // TODO refactor to reflective code
  private Map<String, FacetInfo> getFacetFieldNames(Class<? extends Document> type) {
    Map<String, FacetInfo> facetMap = Maps.newHashMap();
    if (Person.class.equals(type)) {
      facetMap.put("facet_t_name", new FacetInfo().setName("facet_t_name").setType(FacetType.LIST).setTitle("Name"));
      facetMap.put("facet_s_birthDate", new FacetInfo().setName("facet_s_birthDate").setType(FacetType.LIST).setTitle("Birth date"));
      facetMap.put("facet_s_deathDate", new FacetInfo().setName("facet_s_deathDate").setType(FacetType.LIST).setTitle("Death date"));
    } else if (ATLGArchive.class.equals(type)) {
      facetMap.put("facet_s_refcode", new FacetInfo().setName("facet_s_refcode").setType(FacetType.LIST).setTitle("Reference code"));
      facetMap.put("facet_s_place", new FacetInfo().setName("facet_s_place").setType(FacetType.LIST).setTitle("Place"));
      facetMap.put("facet_s_subject", new FacetInfo().setName("facet_s_subject").setType(FacetType.LIST).setTitle("Subject"));
      facetMap.put("facet_s_person", new FacetInfo().setName("facet_s_person").setType(FacetType.LIST).setTitle("Person"));
      facetMap.put("facet_t_text", new FacetInfo().setName("facet_t_text").setType(FacetType.LIST).setTitle("Notes"));
    } else if (ATLGLegislation.class.equals(type)) {
      facetMap.put("facet_t_text", new FacetInfo().setName("facet_t_text").setType(FacetType.LIST).setTitle("Reference"));
      facetMap.put("facet_s_date", new FacetInfo().setName("facet_s_date").setType(FacetType.LIST).setTitle("Date"));
      facetMap.put("facet_s_place", new FacetInfo().setName("facet_s_place").setType(FacetType.LIST).setTitle("Place"));
      facetMap.put("facet_s_subject", new FacetInfo().setName("facet_s_subject").setType(FacetType.LIST).setTitle("Subject"));
      facetMap.put("facet_s_person", new FacetInfo().setName("facet_s_person").setType(FacetType.LIST).setTitle("Person"));
    } else if (ATLGArchiver.class.equals(type)) {
      facetMap.put("facet_t_text", new FacetInfo().setName("facet_t_text").setType(FacetType.LIST).setTitle("History"));
      facetMap.put("facet_s_place", new FacetInfo().setName("facet_s_place").setType(FacetType.LIST).setTitle("Place"));
      facetMap.put("facet_s_subject", new FacetInfo().setName("facet_s_subject").setType(FacetType.LIST).setTitle("Subject"));
      facetMap.put("facet_s_person", new FacetInfo().setName("facet_s_person").setType(FacetType.LIST).setTitle("Person"));
    }
    return facetMap;
  }

  private List<String> getFullTextSearchFields(Class<? extends Document> type) {
    if (Person.class.equals(type)) {
      return Lists.newArrayList("facet_t_name");
    }
    return Lists.newArrayList();

  }

}
