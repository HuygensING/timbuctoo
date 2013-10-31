package nl.knaw.huygens.timbuctoo.index;

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
 * The manager uses the scopes defined in the configuration file.
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

  private final List<Scope> scopes;
  private final TypeRegistry registry;
  private final LocalSolrServer server;
  private final StorageManager storageManager;

  @Inject
  public IndexManager(Configuration config, TypeRegistry registry, LocalSolrServer server, StorageManager storageManager) {
    this.scopes = config.getScopes();
    this.registry = registry;
    this.server = server;
    this.storageManager = storageManager;
    registerCores();
  }

  private void registerCores() {
    for (Scope scope : scopes) {
      for (Class<? extends DomainEntity> type : scope.getBaseEntityTypes()) {
        String collection = registry.getINameForType(type);
        String coreName = String.format("%s.%s", scope.getId(), collection);
        server.addCore(collection, coreName);
      }
    }
  }

  private <T extends DomainEntity> EntityIndex<T> getIndex(Scope scope, Class<T> type) {
    String collection = registry.getINameForType(type);
    String coreName = String.format("%s.%s", scope.getId(), collection);
    return DomainEntityIndex.newInstance(storageManager, server, coreName);
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> void addEntity(Class<T> type, String id) throws IndexException {
    addBaseEntity((Class<? extends DomainEntity>) registry.getBaseClass(type), id);
  }

  private <T extends DomainEntity> void addBaseEntity(Class<T> type, String id) throws IndexException {
    try {
      List<T> variations = storageManager.getAllVariations(type, id);
      for (Scope scope : scopes) {
        if (scope.inScope(type, id)) {
          String collection = registry.getINameForType(type);
          String coreName = String.format("%s.%s", scope.getId(), collection);
          List<T> filtered = filter(type, variations, scope);
          server.add(coreName, getSolrInputDocument(filtered));
        }
      }
    } catch (Exception e) {
      // handle
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> void updateEntity(Class<T> type, String id) throws IndexException {
    updateBaseEntity((Class<? extends DomainEntity>) registry.getBaseClass(type), id);
  }

  private <T extends DomainEntity> void updateBaseEntity(Class<T> type, String id) throws IndexException {
    for (Scope scope : scopes) {
      if (scope.inScope(type, id)) {
        getIndex(scope, type).modify(type, id);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> void deleteEntity(Class<T> type, String id) throws IndexException {
    deleteBaseEntity((Class<? extends DomainEntity>) registry.getBaseClass(type), id);
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

  @SuppressWarnings("unchecked")
  public <T extends Entity> QueryResponse search(Class<T> type, SolrQuery query) throws IndexException {
    return searchBase((Class<? extends DomainEntity>) registry.getBaseClass(type), query);
  }

  private <T extends DomainEntity> QueryResponse searchBase(Class<T> type, SolrQuery query) throws IndexException {
    return getIndex(scopes.get(0), type).search(type, query);
  }

  public IndexStatus getStatus() {
    IndexStatus status = new IndexStatus();
    try {
      for (Scope scope : scopes) {
        for (Class<? extends DomainEntity> type : scope.getBaseEntityTypes()) {
          String collection = registry.getINameForType(type);
          String coreName = String.format("%s.%s", scope.getId(), collection);
          status.addDomainEntityCount(scope, type, server.count(coreName));
        }
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
  private <T extends DomainEntity> List<T> filter(Class<T> type, List<T> entities, Scope scope) {
    List<T> list = Lists.newArrayList();
    for (T entity : entities) {
      if (scope.inScope(type, entity)) {
        list.add(entity);
      }
    }
    return list;
  }

}
