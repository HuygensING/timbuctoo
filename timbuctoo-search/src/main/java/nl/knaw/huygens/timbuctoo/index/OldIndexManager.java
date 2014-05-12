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

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toDomainEntity;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.apache.solr.client.solrj.SolrQuery;
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
public class OldIndexManager implements IndexManager {

  private static final Logger LOG = LoggerFactory.getLogger(OldIndexManager.class);

  private final List<Scope> scopes;
  private final TypeRegistry registry;
  private final LocalSolrServer server;
  private final StorageManager storageManager;

  @Inject
  public OldIndexManager(Configuration config, TypeRegistry registry, LocalSolrServer server, StorageManager storageManager, VREManager vreManager) {
    this.registry = registry;
    this.server = server;
    this.storageManager = storageManager;
    scopes = vreManager.getAllScopes();
    registerCores();
  }

  private void registerCores() {
    for (Scope scope : scopes) {
      for (Class<? extends DomainEntity> type : scope.getBaseEntityTypes()) {
        String collection = TypeNames.getInternalName(type);
        String coreName = getCoreName(scope, collection);
        server.addCore(collection, coreName);
      }
    }
  }

  private <T extends DomainEntity> String getCoreName(Scope scope, Class<T> type) {
    return getCoreName(scope, TypeNames.getInternalName(type));
  }

  private String getCoreName(Scope scope, String collection) {
    return String.format("%s.%s", scope.getId(), collection);
  }

  @Override
  public <T extends DomainEntity> void addEntity(Class<T> type, String id) throws IndexException {
    addBaseEntity(toDomainEntity(registry.getBaseClass(type)), id);
  }

  @Override
  public <T extends DomainEntity> void updateEntity(Class<T> type, String id) throws IndexException {
    // For Solr "add" and "update" are the same thing
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

  @Override
  public <T extends DomainEntity> void deleteEntity(Class<T> type, String id) throws IndexException {
    deleteBaseEntity(toDomainEntity(registry.getBaseClass(type)), id);
  }

  private <T extends DomainEntity> void deleteBaseEntity(Class<T> type, String id) throws IndexException {
    // No need to check for being in scope: it doesn't harm to remove an item that's not there
    try {
      for (Scope scope : scopes) {
        String coreName = getCoreName(scope, type);
        server.deleteById(coreName, id);
      }
    } catch (Exception e) {
      throw new IndexException("Failed to delete entity", e);
    }
  }

  @Override
  public <T extends DomainEntity> void deleteEntities(Class<T> type, List<String> ids) throws IndexException {
    // No need to check for being in scope: it doesn't harm to remove an item that's not there
    if (ids == null || ids.isEmpty()) {
      return;
    }

    try {
      for (Scope scope : scopes) {
        if (!ids.isEmpty()) {
          String coreName = getCoreName(scope, toDomainEntity(registry.getBaseClass(type)));
          // It is needed to check if the core exists. If it does not exist an exception will be thrown.
          if (server.coreExits(coreName)) {
            server.deleteById(coreName, ids);
          }
        }
      }
    } catch (Exception e) {
      throw new IndexException("Failed to delete entities", e);
    }
  }

  @Override
  public void deleteAllEntities() throws IndexException {
    try {
      server.deleteAll();
    } catch (Exception e) {
      throw new IndexException("Failed to delete all entities", e);
    }
  }

  @Override
  public <T extends DomainEntity> QueryResponse search(Scope scope, Class<T> type, SolrQuery query) throws IndexException {
    try {
      String coreName = getCoreName(scope, toDomainEntity(registry.getBaseClass(type)));
      return server.search(coreName, query);
    } catch (Exception e) {
      throw new IndexException("Failed to search", e);
    }
  }

  @Override
  public IndexStatus getStatus() {
    IndexStatus status = new IndexStatus();
    try {
      SolrQuery query = new SolrQuery("*:*").setRows(0); // no data
      for (Scope scope : scopes) {
        for (Class<? extends DomainEntity> type : scope.getBaseEntityTypes()) {
          long count = search(scope, type, query).getResults().getNumFound();
          status.addCount(scope, type, count);
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to obtain status: {}", e.getMessage());
    }
    return status;
  }

  @Override
  public void commitAll() throws IndexException {
    try {
      server.commitAll();
    } catch (Exception e) {
      throw new IndexException("Failed to commit", e);
    }
  }

  @Override
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
