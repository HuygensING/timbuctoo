package nl.knaw.huygens.repository.index;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class LocalSolrServer {

  private final Logger LOG = LoggerFactory.getLogger(LocalSolrServer.class);

  private final String SOLR_DIRECTORY;

  private Set<String> modifiedCores = Sets.newHashSetWithExpectedSize(3);

  private static final String SOLR_DEFAULT_FIELD = "id";

  // FIXME this is probably suboptimal:
  private static final int ROWS = 2000;
  private static final int FACET_LIMIT = 1000;

  private Map<String, SolrServer> solrServers;

  private CoreContainer container = null;

  public LocalSolrServer(String solrDir, String... coreNames) {

    try {
      SOLR_DIRECTORY = solrDir;
      File configFile = new File(new File(SOLR_DIRECTORY, "conf"), "solr.xml");
      container = new CoreContainer(SOLR_DIRECTORY, configFile);
      solrServers = new HashMap<String, SolrServer>(coreNames.length);
      for (String coreName : coreNames) {
        solrServers.put(coreName, new EmbeddedSolrServer(container, coreName));
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  protected LocalSolrServer(SolrServer solrServer, String core) {
    // Constructor for unit testing purposes:
    SOLR_DIRECTORY = "";
    container = new CoreContainer("");
    solrServers = new HashMap<String, SolrServer>(1);
    this.solrServers.put(core, solrServer);
  }

  public void add(String core, SolrInputDocument doc) throws IndexException {
    try {
      solrServers.get(core).add(doc);
      modifiedCores.add(core);
    } catch (Exception e) {
      throw new IndexException(e.getMessage());
    }
  }

  public void update(String core, SolrInputDocument doc) throws IndexException {
    try {
      solrServers.get(core).deleteById(doc.getFieldValue("id").toString());
      solrServers.get(core).add(doc);
      modifiedCores.add(core);
    } catch (Exception e) {
      throw new IndexException(e.getMessage());
    }
  }

  public void delete(String core, SolrInputDocument doc) throws IndexException {
    try {
      solrServers.get(core).deleteById(doc.getFieldValue("id").toString());
      modifiedCores.add(core);
    } catch (Exception e) {
      throw new IndexException(e.getMessage());
    }
  }

  public void commit(String core) throws SolrServerException, IOException {
    solrServers.get(core).commit();
    modifiedCores.remove(core);
  }

  public void commitAllChanged() throws SolrServerException, IOException {
    Set<String> updatedCores = ImmutableSet.copyOf(modifiedCores);
    for (String s : updatedCores) {
      this.commit(s);
    }
  }

  public void setSolrServer(SolrServer solrServer, String core) {
    this.solrServers.put(core, solrServer);
  }

  public QueryResponse getQueryResponse(String term, Collection<String> facetFieldNames, String sort, String core) throws SolrServerException, IOException {
    SolrQuery query = new SolrQuery();
    query.setQuery(term);
    query.setFields(SOLR_DEFAULT_FIELD);
    query.setRows(ROWS);
    query.addFacetField(facetFieldNames.toArray(new String[facetFieldNames.size()]));
    query.setFacetMinCount(0);
    query.setFacetLimit(FACET_LIMIT);
    query.setFilterQueries("!cache=false");
    query.setSortField(sort, SolrQuery.ORDER.asc);
    LOG.info("{}", query);
    SolrServer s = solrServers.get(core);
    return s.query(query);
  }

  public QueryResponse getByIds(List<String> ids, Collection<String> facetFieldNames, String sort, String core) throws SolrServerException, IOException {
    return getQueryResponse("id:(" + StringUtils.join(ids, " ") + ")", facetFieldNames, sort, core);
  }

  public Set<String> getAllFields(String core) throws SolrServerException, IOException {
    SolrServer solrServer = solrServers.get(core);
    LukeRequest request = new LukeRequest();
    request.setNumTerms(0);
    request.setFields(Collections.<String>emptyList());

    NamedList<Object> namedList = solrServer.request(request);

    @SuppressWarnings("unchecked")
    SimpleOrderedMap<Object> fields = (SimpleOrderedMap<Object>) namedList.get("fields");
    Iterator<Map.Entry<String, Object>> fieldIt = fields.iterator();
    Set<String> rv = Sets.newHashSetWithExpectedSize(fields.size());
    while (fieldIt.hasNext()) {
      rv.add(fieldIt.next().getKey());
    }
    return rv;
  }

  public void shutdown() {
    if (container != null) {
      container.shutdown();
    }
  }

  public Collection<String> getCoreNames() {
    return solrServers.keySet();
  }
}
