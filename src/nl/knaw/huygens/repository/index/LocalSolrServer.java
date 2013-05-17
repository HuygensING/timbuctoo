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

import nl.knaw.huygens.repository.util.Paths;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class LocalSolrServer {

  private static final String DESC_FIELD = "desc";

  private static final String ID_FIELD = "id";

  private final Logger LOG = LoggerFactory.getLogger(LocalSolrServer.class);

  private final String SOLR_DIRECTORY;

  private Set<String> modifiedCores = Sets.newHashSetWithExpectedSize(3);

  private static final String SOLR_DEFAULT_FIELD = ID_FIELD;

  // FIXME this is probably suboptimal:
  private static final int ROWS = 2000;
  private static final int FACET_LIMIT = 1000;

  private Map<String, SolrServer> solrServers;

  private CoreContainer container = null;

  @Inject
  public LocalSolrServer(@Named("paths.solr") String solrDir, @Named("indexeddoctypes") String coreNameList) {
    String[] coreNames = coreNameList.split(",");

    try {
      SOLR_DIRECTORY = Paths.pathInUserHome(solrDir);
      File configFile = new File(new File(SOLR_DIRECTORY, "conf"), "solr.xml");
      container = new CoreContainer(SOLR_DIRECTORY, configFile);
      solrServers = new HashMap<String, SolrServer>(coreNames.length);
      for (String coreName : coreNames) {
        solrServers.put(coreName, new EmbeddedSolrServer(container, coreName));
      }
    } catch (Exception e) {
      if (container != null) {
        try {
          container.shutdown();
        } catch (Exception e2) {
          // Do nothing;
        }
      }
      throw new RuntimeException(e);
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
      solrServers.get(core).deleteById(doc.getFieldValue(ID_FIELD).toString());
      solrServers.get(core).add(doc);
      modifiedCores.add(core);
    } catch (Exception e) {
      throw new IndexException(e.getMessage());
    }
  }

  public void delete(String core, String id) throws IndexException {
    try {
      solrServers.get(core).deleteById(id);
      modifiedCores.add(core);
    } catch (Exception e) {
      throw new IndexException(e.getMessage());
    }
  }

  /**
   * Delete all items for the specified core.
   */
  public void deleteAll(String core) throws IndexException {
    try {
      solrServers.get(core).deleteByQuery("*:*");
      modifiedCores.add(core);
    } catch (Exception e) {
      throw new IndexException(e.getMessage());
    }
  }

  public void commit(String core) throws SolrServerException, IOException {
    SolrServer server = solrServers.get(core);
    server.commit();
    modifiedCores.remove(core);
  }

  public void commitAllChanged() throws SolrServerException, IOException {
    Set<String> updatedCores = ImmutableSet.copyOf(modifiedCores);
    for (String s : updatedCores) {
      this.commit(s);
    }
  }

  public void setSolrServer(SolrServer solrServer, String core) {
    System.out.println("setSolrServer: " + core);
    this.solrServers.put(core, solrServer);
  }

  public QueryResponse getQueryResponse(String term, Collection<String> facetFieldNames, String sort, String core) throws SolrServerException {
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
    request.setFields(Collections.<String> emptyList());

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

  /**
   * Obtain a simple mapping of IDs to descriptions of the document
   * @param core
   * @return
   * @throws SolrServerException
   */
  public Map<String, String> getSimpleMap(String core) throws SolrServerException {
    SolrQuery query = new SolrQuery();
    query.setFields(ID_FIELD, DESC_FIELD);
    query.setQuery("*:*");
    QueryResponse response = solrServers.get(core).query(query);
    SolrDocumentList results = response.getResults();
    Map<String, String> rv = Maps.newHashMapWithExpectedSize(results.size());
    for (SolrDocument doc : results) {
      rv.put(doc.getFieldValue(ID_FIELD).toString(), doc.getFieldValue(DESC_FIELD).toString());
    }
    return rv;
  }
}
