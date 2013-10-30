package nl.knaw.huygens.timbuctoo.index;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
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
 * The client that instantiates this manager is responsible for calling
 * the close method in order to release the resources used.
 */
@Singleton
public class IndexManager {

  private static final Logger LOG = LoggerFactory.getLogger(IndexManager.class);

  private final Configuration config;
  private final TypeRegistry registry;
  private final LocalSolrServer server;
  private Map<Class<? extends Entity>, EntityIndex<? extends Entity>> indexes;

  @Inject
  public IndexManager(Configuration config, TypeRegistry registry, LocalSolrServer server, StorageManager storageManager, RelationManager relationManager) {
    this.config = config;
    this.registry = registry;
    this.server = server;
    setupIndexes(config, storageManager, relationManager);
  }

  private void setupIndexes(Configuration config, StorageManager storageManager, RelationManager relationManager) {
    indexes = Maps.newHashMap();

    for (Scope scope : config.getScopes()) {
      for (Class<? extends Entity> type : scope.getBaseEntityTypes()) {
        String collection = registry.getINameForType(type);
        String coreName = String.format("%s.%s", scope.getName(), collection);
        server.addCore(collection, coreName);
        if (type == Relation.class) {
          indexes.put(Relation.class, new RelationIndex(registry, server, storageManager, relationManager));
        } else {
          indexes.put(type, DomainEntityIndex.newInstance(storageManager, server, coreName));
        }
      }
      break;
    }
  }

  private <T extends Entity> EntityIndex<T> indexForType(Class<T> type) {
    @SuppressWarnings("unchecked")
    EntityIndex<T> index = (EntityIndex<T>) indexes.get(type);
    return (index != null) ? index : new NoEntityIndex<T>();
  }

  public <T extends Entity> void addDocument(Class<T> type, String id) throws IndexException {
    addBaseDocument(registry.getBaseClass(type), id);
  }

  private <T extends Entity> void addBaseDocument(Class<T> type, String id) throws IndexException {
    indexForType(type).add(type, id);
  }

  public <T extends Entity> void updateDocument(Class<T> type, String id) throws IndexException {
    updateBaseDocument(registry.getBaseClass(type), id);
  }

  private <T extends Entity> void updateBaseDocument(Class<T> type, String id) throws IndexException {
    indexForType(type).modify(type, id);
  }

  public <T extends Entity> void deleteDocument(Class<T> type, String id) throws IndexException {
    indexForType(registry.getBaseClass(type)).remove(id);
  }

  public <T extends Entity> void deleteDocuments(Class<T> type, List<String> ids) throws IndexException {
    indexForType(registry.getBaseClass(type)).remove(ids);
  }

  public void deleteAllDocuments() throws IndexException {
    try {
      server.deleteAll();
    } catch (Exception e) {
      throw new IndexException("Failed to delete all entities from index", e);
    }
  }

  public <T extends Entity> QueryResponse search(Class<T> type, SolrQuery query) throws IndexException {
    return searchBase(registry.getBaseClass(type), query);
  }

  private <T extends Entity> QueryResponse searchBase(Class<T> type, SolrQuery query) throws IndexException {
    return indexForType(type).search(type, query);
  }

  public IndexStatus getStatus() {
    IndexStatus status = new IndexStatus();
    try {
      for (Scope scope : config.getScopes()) {
        for (Class<? extends DomainEntity> type : scope.getBaseEntityTypes()) {
          String collection = registry.getINameForType(type);
          String coreName = String.format("%s.%s", scope.getName(), collection);
          status.addDomainEntityCount(scope, type, server.count(coreName));
        }
        break;
      }
    } catch (SolrServerException e) {
      LOG.error("Failed obtain status: {}", e.getMessage());
    }
    return status;
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
