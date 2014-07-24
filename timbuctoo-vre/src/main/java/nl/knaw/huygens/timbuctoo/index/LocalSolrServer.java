package nl.knaw.huygens.timbuctoo.index;

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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.Configuration;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Encapsulates a set of Lucene indexes with their configuration data
 * (referred to as Solr cores, or cores for short) which are handled
 * by an embedded Solr server.
 * Existing cores that are not referred to are ignored.
 */
@Singleton
class LocalSolrServer {

  private static final Logger LOG = LoggerFactory.getLogger(LocalSolrServer.class);

  private final CoreContainer container;
  private final Map<String, SolrServer> solrServers = Maps.newTreeMap();
  private final String solrHomeDir;
  private final int commitWithin;

  @Inject
  public LocalSolrServer(Configuration config) {
    solrHomeDir = config.getSolrHomeDir();
    LOG.info("Solr home directory: {}", solrHomeDir);

    commitWithin = config.getIntSetting("solr.commit_within", 10 * 1000);
    LOG.info("Maximum time before a commit: {} seconds", commitWithin / 1000);

    @SuppressWarnings("unused")
    File configFile = new File(new File(solrHomeDir, "conf"), "solr.xml");
    //      container = new CoreContainer(solrHomeDir, configFile);
    container = null;
  }

  /**
   * Adds a core for the specified collection, using the specified core name.
   */
  public void addCore(String collection, String coreName) {
    checkArgument(collection != null && collection.matches("^[a-z]+$"), "collection '%s'", collection);
    checkArgument(coreName != null && coreName.matches("^[a-z\\.]+$"), "coreName '%s'", coreName);

    @SuppressWarnings("unused")
    String schemaName = getSchemaName(collection);
    @SuppressWarnings("unused")
    String dataDir = "data/" + coreName.replace('.', '/');

    CoreDescriptor descriptor = new CoreDescriptor(container, coreName, solrHomeDir);
    //    descriptor.setSchemaName(schemaName);
    //    descriptor.setDataDir(dataDir);
    //    descriptor.setLoadOnStartup(true);

    SolrCore core = container.create(descriptor);
    container.register(coreName, core, true);
    SolrServer server = new EmbeddedSolrServer(container, coreName);

    solrServers.put(coreName, server);
  }

  /**
   * Returns the name of the schema file for the specified collection.
   * If a custom schema file exists it will be used, otherwise the
   * schema {@code file schema-tmpl.xml} will be used.
   */
  private String getSchemaName(String collection) {
    String schemaName = String.format("schema-%s.xml", collection);
    if (new File(new File(solrHomeDir, "conf"), schemaName).isFile()) {
      LOG.info("Schema for {} index: {}", collection, schemaName);
      return schemaName;
    } else {
      return "schema-tmpl.xml";
    }
  }

  public void add(String core, SolrInputDocument doc) throws SolrServerException, IOException {
    serverFor(core).add(doc, commitWithin);
  }

  public void deleteById(String core, String id) throws SolrServerException, IOException {
    serverFor(core).deleteById(id, commitWithin);
  }

  public void deleteById(String core, List<String> ids) throws SolrServerException, IOException {
    serverFor(core).deleteById(ids, commitWithin);
  }

  public void deleteByQuery(String core, String query) throws SolrServerException, IOException {
    serverFor(core).deleteByQuery(query, commitWithin);
  }

  public void deleteAll(String core) throws SolrServerException, IOException {
    LOG.info("Clearing index {}", core);
    SolrServer server = serverFor(core);
    server.deleteByQuery("*:*");
    server.commit();
  }

  public void deleteAll() throws SolrServerException, IOException {
    for (String core : getCoreNames()) {
      deleteAll(core);
    }
  }

  public void commit(String core) throws SolrServerException, IOException {
    serverFor(core).commit();
  }

  public void commitAll() throws SolrServerException, IOException {
    for (String core : getCoreNames()) {
      commit(core);
    }
  }

  public QueryResponse search(String core, SolrQuery query) throws SolrServerException {
    return serverFor(core).query(query);
  }

  public void shutdown() {
    if (container != null) {
      container.shutdown();
    }
  }

  public boolean coreExits(String coreName) {
    return solrServers.containsKey(coreName);
  }

  private Set<String> getCoreNames() {
    return solrServers.keySet();
  }

  private SolrServer serverFor(String core) throws SolrServerException {
    SolrServer server = solrServers.get(core);
    if (server == null) {
      LOG.error("Core {} does not exist.", core);
      throw new SolrServerException("No such core: " + core);
    }
    return server;
  }

}
