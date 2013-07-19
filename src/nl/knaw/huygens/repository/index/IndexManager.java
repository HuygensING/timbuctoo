package nl.knaw.huygens.repository.index;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.StorageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This manager is responsible for handling document changes on the index.
 *
 * The manager uses the Solr cores defined in the configuration file.
 * Each core corresponds to a primitive document type and stores data
 * for all subclasses of that document type.
 *
 * Since we are using the 'commitWithin' feature of Solr, there's no need
 * for flushing indexes, except when closing down.
 *
 * The client that instantiates this manager id responsible for calling
 * the close method in order to release the resources used.
 */
@Singleton
public class IndexManager {

  private final Logger LOG = LoggerFactory.getLogger(IndexManager.class);

  private final DocTypeRegistry registry;
  private final LocalSolrServer server;
  private final StorageManager storageManager;
  private Map<Class<? extends Document>, DocumentIndexer<? extends Document>> indexers;

  @Inject
  public IndexManager(Configuration config, DocTypeRegistry registry, LocalSolrServer server, StorageManager storageManager) {
    this.registry = registry;
    this.server = server;
    this.storageManager = storageManager;
    setupIndexers(config);
  }

  private void setupIndexers(Configuration config) {
    boolean error = false;
    indexers = Maps.newHashMap();
    for (String doctype : config.getSettings("indexeddoctypes")) {
      Class<? extends Document> type = registry.getTypeForIName(doctype);
      if (type == null) {
        LOG.error("Configuration error: '{}' is not a document type", doctype);
        error = true;
      } else if (type != registry.getBaseClass(type)) {
        LOG.error("Configuration error: '{}' is not a primitive document type", doctype);
        error = true;
      } else {
        indexers.put(type, SolrDocumentIndexer.newInstance(type, server));
      }
    }
    if (error) {
      throw new RuntimeException("Configuration error");
    }
  }

  private <T extends Document> DocumentIndexer<T> indexerForType(Class<T> type) {
    @SuppressWarnings("unchecked")
    DocumentIndexer<T> indexer = (DocumentIndexer<T>) indexers.get(type);
    return (indexer != null) ? indexer : new NoDocumentIndexer<T>();
  }

  public <T extends Document> void addDocument(Class<T> type, String id) throws IndexException {
    addBaseDocument(registry.getBaseClass(type), id);
  }

  private <T extends Document> void addBaseDocument(Class<T> type, String id) throws IndexException {
    List<T> docs = storageManager.getAllVariations(type, id);
    indexerForType(type).add(docs);
  }

  public <T extends Document> void updateDocument(Class<T> type, String id) throws IndexException {
    updateBaseDocument(registry.getBaseClass(type), id);
  }

  private <T extends Document> void updateBaseDocument(Class<T> type, String id) throws IndexException {
    List<T> docs = storageManager.getAllVariations(type, id);
    indexerForType(type).modify(docs);
  }

  public <T extends Document> void deleteDocument(Class<T> type, String id) throws IndexException {
    indexerForType(registry.getBaseClass(type)).remove(id);
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
