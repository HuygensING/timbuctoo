package nl.knaw.huygens.repository.facets;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.knaw.huygens.repository.events.Events.IndexChangedEvent;
import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.index.ModelIterator;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Search;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.pubsub.Subscribe;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class FacettedSearchManager {

  public static final String DOC_ID_FIELD = "id";

  private LocalSolrServer localSolrServer;

  private ModelIterator modelIterator;

  private Map<String, Map<String, Boolean>> facetFieldNameCache;

  private Map<String, Map<String, Boolean>> facetFieldFilterCache;

  private Map<String, Set<String>> solrFieldNameCache;

  public FacettedSearchManager(LocalSolrServer localSolrServer, ModelIterator modelIterator, Hub hub) {
    this.localSolrServer = localSolrServer;
    this.modelIterator = modelIterator;
    this.facetFieldNameCache = Maps.newHashMap();
    this.facetFieldFilterCache = Maps.newHashMap();
    this.solrFieldNameCache = Maps.newHashMap();
    hub.subscribe(this);
  }

  @Subscribe
  public void onIndexChangedEvent(IndexChangedEvent ev) {
    Collection<String> cores = this.localSolrServer.getCoreNames();
    for (String core : cores) {
      try {
        this.facetFieldNameCache.put(core, getFacetFieldNamesFromSolr(core));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public Collection<Facet> getFacets(String core) throws SolrServerException, IOException {
    QueryResponse response = localSolrServer.getQueryResponse("*:*", getFacetFieldNames(core).keySet(), "id", core);
    return getFacets(response, core, true);
  }

  public Collection<Facet> getFacets(Search search) throws SolrServerException, IOException {
    List<String> ids = search.getIds();
    if (ids.size() == 0) {
      return null;
    }
    String core = search.getSearchType();
    QueryResponse response = localSolrServer.getByIds(ids, getFacetFieldNames(core).keySet(), search.getSort(), core);
    return getFacets(response, core, false);
  }

  private Collection<Facet> getFacets(QueryResponse response, String core, boolean linkItems) throws SolrServerException, IOException {
    Collection<Facet> facetFields = Lists.newArrayList();
    Map<String, Boolean> facetFieldComplexity = getFacetFieldNames(core);
    for (Map.Entry<String, Boolean> facetEntry: facetFieldComplexity.entrySet()) {
      // Get the data from the structure into Facet instances:
      String fieldName = facetEntry.getKey();
      boolean facetIsComplex = facetEntry.getValue();
      Facet facet = new Facet(fieldName, facetIsComplex);
      facetFields.add(facet);

      FacetField solrFacet = response.getFacetField(fieldName);
      // Unfortunately Solr's facet implementation can have a null collection of values which needs to be caught:
      if (solrFacet == null || solrFacet.getValues() == null) {
        System.err.println("Empty facet: " + fieldName);
        continue;
      }
      List<FacetItem> facetItems = Lists.newArrayList();

      for (Count count : solrFacet.getValues()) {
        if (count == null) {
          System.err.println("Empty count?! in " + fieldName);
          continue;
        }
        String id = count.getName();
        String name;
        int semicolonIndex = id.indexOf(";");
        if (facetIsComplex && semicolonIndex != -1) {
          name = id.substring(semicolonIndex + 1);
          id = id.substring(0, semicolonIndex);
        } else {
          name = id;
        }
        facetItems.add(new FacetItem(id, name, count.getCount()));

      }

      facet.setFacetItems(facetItems);
    }
    return facetFields;
  }

  private Map<String, Boolean> getFacetFieldNames(String core) throws SolrServerException, IOException {
    if (!facetFieldNameCache.containsKey(core)) {
      facetFieldNameCache.put(core, getFacetFieldNamesFromSolr(core));
    }
    return facetFieldNameCache.get(core);
  }

  private Map<String, Boolean> getFacetFieldNamesFromSolr(String core) throws SolrServerException, IOException {
    // Get fields from SOLR:
    Set<String> rawIndexedFields = getRawIndexedFields(core);

    if (rawIndexedFields.equals(solrFieldNameCache.get(core))) {
      return facetFieldNameCache.get(core);
    }
    solrFieldNameCache.put(core, rawIndexedFields);

    // Get which fields are facets from the model:
    Map<String, Boolean> facettedFieldFilters = getFieldFiltersFromModel(core);
    Set<Entry<String, Boolean>> facettedFieldFilterEntrySet = facettedFieldFilters.entrySet();

    // Now filter the SOLR fields
    Map<String, Boolean> facetComplexityMap = Maps.newHashMap();
    for (String f : rawIndexedFields) {
      // Exact matches:
      if (facettedFieldFilters.containsKey(f)) {
        facetComplexityMap.put(f, facettedFieldFilters.get(f));
      } else {
        // Or regex ones... (this should really use a Trie, but I'm too lazy to write that up,
        // and this shouldn't run too often (and with small input sizes) so likely isn't a perf issue)
        for (Map.Entry<String, Boolean> filterEntry : facettedFieldFilterEntrySet) {
          if (f.matches(filterEntry.getKey())) {
            facetComplexityMap.put(f, filterEntry.getValue());
            break;
          }
        }
      }
    }

    // If there are fields which don't have any values (yet), we need to explicitly
    // add them so they don't get left out:
    String notARegexRegex = "^[a-z_-]*$"; // Recursive variable names, woo!
    for (Map.Entry<String, Boolean> filterEntry : facettedFieldFilterEntrySet) {
      String filterName = filterEntry.getKey();
      if (!facetComplexityMap.containsKey(filterName) && filterName.matches(notARegexRegex)) {
        facetComplexityMap.put(filterName, filterEntry.getValue());
      }
    }
    return facetComplexityMap;
  }

  private Set<String> getRawIndexedFields(String core) throws SolrServerException, IOException {
    return localSolrServer.getAllFields(core);
  }


  private Map<String, Boolean> getFieldFiltersFromModel(String core) {
    if (!facetFieldFilterCache.containsKey(core)) {
      Class<?> responseTypeCls = Document.getSubclassByString(core);
      FieldMapper mapper = new FieldMapper();
      modelIterator.processMethods(mapper, responseTypeCls.getMethods());
      facetFieldFilterCache.put(core, mapper.getResult());
    }
    return facetFieldFilterCache.get(core);
  }

  public Search search(String term, String sort, String core) throws SolrServerException, IOException {
    SolrDocumentList documents = localSolrServer.getQueryResponse(term, getFacetFieldNames(core).keySet(), sort, core).getResults();
    List<String> ids = Lists.newArrayList();
    for (SolrDocument document : documents) {
      ids.add(document.getFieldValue(DOC_ID_FIELD).toString());
    }
    return new Search(ids, core, term, sort, new Date().toString());
  }

}
