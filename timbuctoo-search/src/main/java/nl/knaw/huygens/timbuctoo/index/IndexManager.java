package nl.knaw.huygens.timbuctoo.index;

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toDomainEntity;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This manager is responsible for handling entity changes on the index.
 *
 * The manager uses the scopes obtained from the configuration.
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

  private final List<Scope> scopes;
  private final TypeRegistry registry;
  private final LocalSolrServer server;
  private final StorageManager storageManager;

  @Inject
  public IndexManager(Configuration config, TypeRegistry registry, LocalSolrServer server, StorageManager storageManager) {
    this.registry = registry;
    this.server = server;
    this.storageManager = storageManager;
    scopes = config.getScopes();
    registerCores();
  }

  private void registerCores() {
    for (Scope scope : scopes) {
      for (Class<? extends DomainEntity> type : scope.getBaseEntityTypes()) {
        String collection = registry.getINameForType(type);
        String coreName = getCoreName(scope, collection);
        server.addCore(collection, coreName);
      }
    }
  }

  private <T extends DomainEntity> String getCoreName(Scope scope, Class<T> type) {
    return getCoreName(scope, registry.getINameForType(type));
  }

  private String getCoreName(Scope scope, String collection) {
    return String.format("%s.%s", scope.getId(), collection);
  }

  private <T extends DomainEntity> EntityIndex<T> getIndex(Scope scope, Class<T> type) {
    return DomainEntityIndex.newInstance(storageManager, server, getCoreName(scope, type));
  }

  public <T extends Entity> void addEntity(Class<T> type, String id) throws IndexException {
    addBaseEntity(toDomainEntity(registry.getBaseClass(type)), id);
  }

  private <T extends DomainEntity> void addBaseEntity(Class<T> type, String id) throws IndexException {
    try {
      List<T> variations = storageManager.getAllVariations(type, id);
      for (Scope scope : scopes) {
        List<T> filtered = filter(variations, scope);
        if (!filtered.isEmpty()) {
          server.add(getCoreName(scope, type), getSolrInputDocument(filtered));
        }
      }
    } catch (Exception e) {
      throw new IndexException("Failed to add entity", e);
    }
  }

  public <T extends Entity> void updateEntity(Class<T> type, String id) throws IndexException {
    updateBaseEntity(toDomainEntity(registry.getBaseClass(type)), id);
  }

  private <T extends DomainEntity> void updateBaseEntity(Class<T> type, String id) throws IndexException {
    for (Scope scope : scopes) {
      if (scope.inScope(type, id)) {
        getIndex(scope, type).modify(type, id);
      }
    }
  }

  public <T extends Entity> void deleteEntity(Class<T> type, String id) throws IndexException {
    deleteBaseEntity(toDomainEntity(registry.getBaseClass(type)), id);
  }

  public <T extends DomainEntity> void deleteBaseEntity(Class<T> type, String id) throws IndexException {
    for (Scope scope : scopes) {
      if (scope.inScope(type, id)) {
        getIndex(scope, type).remove(id);
      }
    }
  }

  public <T extends Entity> void deleteEntities(Class<T> type, List<String> ids) throws IndexException {
    for (String id : ids) {
      deleteEntity(type, id);
    }
  }

  public void deleteAllEntities() throws IndexException {
    try {
      server.deleteAll();
    } catch (Exception e) {
      throw new IndexException("Failed to delete all entities from index", e);
    }
  }

  public <T extends Entity> QueryResponse search(Class<T> type, SolrQuery query) throws IndexException {
    return searchBase(toDomainEntity(registry.getBaseClass(type)), query);
  }

  private <T extends DomainEntity> QueryResponse searchBase(Class<T> type, SolrQuery query) throws IndexException {
    return getIndex(scopes.get(0), type).search(type, query);
  }

  public IndexStatus getStatus() {
    IndexStatus status = new IndexStatus();
    try {
      for (Scope scope : scopes) {
        for (Class<? extends DomainEntity> type : scope.getBaseEntityTypes()) {
          long count = server.count(getCoreName(scope, type));
          status.addCount(scope, type, count);
        }
      }
    } catch (SolrServerException e) {
      LOG.error("Failed to obtain status: {}", e.getMessage());
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

  private <T extends DomainEntity> SolrInputDocument getSolrInputDocument(List<T> entities) {
    ModelIterator modelIterator = new ModelIterator();
    SolrInputDocument document = null;
    SolrInputDocGenerator indexer = null;
    for (T entity : entities) {
      if (document == null) {
        indexer = new SolrInputDocGenerator(entity);
      } else {
        indexer = new SolrInputDocGenerator(entity, document);
      }
      modelIterator.processClass(indexer, entity.getClass());
      document = indexer.getResult();
    }
    return document;
  }

  // TODO filter with predicate
  private <T extends DomainEntity> List<T> filter(List<T> entities, Scope scope) {
    List<T> list = Lists.newArrayList();
    for (T entity : entities) {
      if (scope.inScope(entity)) {
        list.add(entity);
      }
    }
    return list;
  }

}
