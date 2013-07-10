package nl.knaw.huygens.repository.index;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.StorageManager;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This manager is responsible for handling document changes on the index.
 *
 * Since we are using the 'commitWithin' feature of Solr, there's no need
 * for flushing indexes, except when closing down.
 *
 * The client that instantiates this manager id responsible for calling
 * the close method in order to release the resources used.
 */
@Singleton
public class IndexManager {

  private final LocalSolrServer server;
  private final StorageManager storageManager;
  private Map<Class<? extends Document>, DocumentIndexer<? extends Document>> indexers;

  @Inject
  public IndexManager(Configuration config, DocTypeRegistry registry, LocalSolrServer server, StorageManager storageManager) {
    this.server = server;
    this.storageManager = storageManager;
    setupIndexers(config, registry, server);
  }

  private void setupIndexers(Configuration config, DocTypeRegistry registry, LocalSolrServer server) {
    indexers = Maps.newHashMap();
    for (String doctype : config.getSettings("indexeddoctypes")) {
      Class<? extends Document> type = registry.getClassFromWebServiceTypeString(doctype);
      // Better safe than sorry, this is also checked by the configuration validator...
      if (type != null) {
        indexers.put(type, SolrDocumentIndexer.newInstance(type, server, null));
      }
    }
  }

  private <T extends Document> DocumentIndexer<T> indexerForType(Class<T> type) {
    @SuppressWarnings("unchecked")
    DocumentIndexer<T> indexer = (DocumentIndexer<T>) indexers.get(type);
    return (indexer != null) ? indexer : new NoDocumentIndexer<T>();
  }

  // TODO obtain this from DocTypeRegistry, also simplifying setup
  public Set<Class<? extends Document>> getIndexedTypes() {
    return Collections.unmodifiableSet(indexers.keySet());
  }

  public <T extends Document> void addDocument(Class<T> type, String id) throws IndexException {
    List<T> docs = storageManager.getAllVariations(type, id);
    indexerForType(type).add(docs);
  }

  public <T extends Document> void updateDocument(Class<T> type, String id) throws IndexException {
    List<T> docs = storageManager.getAllVariations(type, id);
    indexerForType(type).modify(docs);
  }

  public <T extends Document> void deleteDocument(Class<T> type, String id) throws IndexException {
    indexerForType(type).remove(id);
  }

  public void deleteAllDocuments() throws IndexException {
    try {
      server.deleteAll();
    } catch (Exception e) {
      throw new IndexException("Failed to delete all documents from index", e);
    }
  }

  public void close() throws IndexException {
    try {
      server.commitAll();
      server.shutdown();
    } catch (Exception e) {
      throw new IndexException("Failed to release IndexManager resources", e);
    }
  }

}
