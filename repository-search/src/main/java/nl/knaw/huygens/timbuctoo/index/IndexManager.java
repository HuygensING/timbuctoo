package nl.knaw.huygens.timbuctoo.index;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This manager is responsible for handling entity changes on the index.
 *
 * The manager uses the Solr cores defined in the configuration file.
 * Each core corresponds to a primitive entity type and stores data
 * for all subclasses of that entity type.
 * Relations are basic infrastructure and need not be specified.
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
  private Map<Class<? extends Entity>, EntityIndexer<? extends Entity>> indexers;

  @Inject
  public IndexManager(Configuration config, DocTypeRegistry registry, LocalSolrServer server, StorageManager storageManager, RelationManager relationManager) {
    this.registry = registry;
    this.server = server;
    setupIndexers(config, storageManager, relationManager);
  }

  private void setupIndexers(Configuration config, StorageManager storageManager, RelationManager relationManager) {
    boolean error = false;
    indexers = Maps.newHashMap();
    indexers.put(Relation.class, new RelationIndexer(registry, server, storageManager, relationManager));
    for (String doctype : config.getSettings("indexeddoctypes")) {
      Class<? extends Entity> type = registry.getTypeForIName(doctype);
      if (type == null) {
        LOG.error("Configuration: '{}' is not a entity type", doctype);
        error = true;
      } else if (type != registry.getBaseClass(type)) {
        LOG.error("Configuration: '{}' is not a primitive entity type", doctype);
        error = true;
      } else if (indexers.containsKey(type)) {
        LOG.warn("Configuration: ignoring entry '{}' in indexeddoctypes", doctype);
      } else {
        String core = type.getSimpleName().toLowerCase();
        indexers.put(type, DomainEntityIndexer.newInstance(storageManager, server, core));
      }
    }
    if (error) {
      throw new RuntimeException("Configuration error");
    }
  }

  private <T extends Entity> EntityIndexer<T> indexerForType(Class<T> type) {
    @SuppressWarnings("unchecked")
    EntityIndexer<T> indexer = (EntityIndexer<T>) indexers.get(type);
    return (indexer != null) ? indexer : new NoEntityIndexer<T>();
  }

  public <T extends Entity> void addDocument(Class<T> type, String id) throws IndexException {
    addBaseDocument(registry.getBaseClass(type), id);
  }

  private <T extends Entity> void addBaseDocument(Class<T> type, String id) throws IndexException {
    indexerForType(type).add(type, id);
  }

  public <T extends Entity> void updateDocument(Class<T> type, String id) throws IndexException {
    updateBaseDocument(registry.getBaseClass(type), id);
  }

  private <T extends Entity> void updateBaseDocument(Class<T> type, String id) throws IndexException {
    indexerForType(type).modify(type, id);
  }

  public <T extends Entity> void deleteDocument(Class<T> type, String id) throws IndexException {
    indexerForType(registry.getBaseClass(type)).remove(id);
  }

  public <T extends Entity> void deleteDocuments(Class<T> type, List<String> ids) throws IndexException {
    indexerForType(type).remove(ids);
  }

  public void deleteAllDocuments() throws IndexException {
    try {
      server.deleteAll();
    } catch (Exception e) {
      throw new IndexException("Failed to delete all entities from index", e);
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
