package nl.knaw.huygens.repository.index;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class LocalSolrServer {

  // FIXME this is probably suboptimal:
  private static final int ROWS = 2000;
  private static final int FACET_LIMIT = 1000;

  private static final String DESC_FIELD = "desc";
  private static final String ID_FIELD = "id";
  private static final String SOLR_DEFAULT_FIELD = ID_FIELD;

  private final Logger LOG = LoggerFactory.getLogger(LocalSolrServer.class);

  private CoreContainer container = null;
  private final Map<String, SolrServer> solrServers;
  private final Set<String> coreNames;
  private final int commitWithin;

  @Inject
  public LocalSolrServer( //
      @Named("paths.solr") String solrDir, //
      @Named("indexeddoctypes") String coreNameList, //
      @Named("solr.commit_within") String commitWithinSpec //
  ) {

    commitWithin = stringToInt(commitWithinSpec, 10 * 1000);
    LOG.info("Maximum time before a commit: {} seconds", commitWithin / 1000);

    try {
      String solrDirectory = Paths.pathInUserHome(solrDir);
      File configFile = new File(new File(solrDirectory, "conf"), "solr.xml");
      container = new CoreContainer(solrDirectory, configFile);
      solrServers = Maps.newHashMap();
      for (String coreName : coreNameList.split(",")) {
        solrServers.put(coreName, new EmbeddedSolrServer(container, coreName));
      }
      coreNames = Collections.unmodifiableSet(solrServers.keySet());
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

  public void add(String core, SolrInputDocument doc) throws SolrServerException, IOException {
    serverFor(core).add(doc);
  }

  public void delete(String core, String id) throws SolrServerException, IOException {
    serverFor(core).deleteById(id);
  }

  public void deleteAll(String core) throws SolrServerException, IOException {
    serverFor(core).deleteByQuery("*:*");
  }

  public void commit(String core) throws SolrServerException, IOException {
    serverFor(core).commit();
  }

  public void commitAll() throws SolrServerException, IOException {
    for (String core : coreNames) {
      serverFor(core).commit();
    }
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
    return serverFor(core).query(query);
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

  public Set<String> getCoreNames() {
    return coreNames;
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

  private SolrServer serverFor(String core) {
    return solrServers.get(core);
  }

  private int stringToInt(String text, int defaulValue) {
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException e) {
      return defaulValue;
    }
  }

}
