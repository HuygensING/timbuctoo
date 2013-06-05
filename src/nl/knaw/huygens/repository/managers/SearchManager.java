package nl.knaw.huygens.repository.managers;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.model.SearchResult;

import org.apache.solr.client.solrj.SolrServerException;
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

  public SearchResult search(String core, String q, String sort) throws SolrServerException {
    SolrDocumentList documents = server.getQueryResponse(q, getFacetFieldNames(), sort, core).getResults();
    List<String> ids = Lists.newArrayList();
    for (SolrDocument document : documents) {
      ids.add(document.getFieldValue("id").toString());
    }
    return new SearchResult(ids, core, q, sort, new Date().toString());
  }

  private Collection<String> getFacetFieldNames() {
    return Sets.newHashSet("facet_s_birthDate");
  }

}
