package nl.knaw.huygens.timbuctoo.index;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.util.KV;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

  private final Configuration config;
  private final TypeRegistry registry;
  private final LocalSolrServer server;
  private Map<Class<? extends Entity>, EntityIndexer<? extends Entity>> indexers;

  @Inject
  public IndexManager(Configuration config, TypeRegistry registry, LocalSolrServer server, StorageManager storageManager, RelationManager relationManager) {
    this.config = config;
    this.registry = registry;
    this.server = server;
    setupIndexers(config, storageManager, relationManager);
  }

  private void setupIndexers(Configuration config, StorageManager storageManager, RelationManager relationManager) {
    indexers = Maps.newHashMap();
    indexers.put(Relation.class, new RelationIndexer(registry, server, storageManager, relationManager));

    Scope scope = config.getDefaultScope();
    for (Class<? extends Entity> type : scope.getBaseEntityTypes()) {
      if (type != Relation.class) {
        String coreName = registry.getINameForType(type);
        server.addCore("default", coreName);
        indexers.put(type, DomainEntityIndexer.newInstance(storageManager, server, coreName));
      }
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
    indexerForType(registry.getBaseClass(type)).remove(ids);
  }

  public void deleteAllDocuments() throws IndexException {
    try {
      server.deleteAll();
    } catch (Exception e) {
      throw new IndexException("Failed to delete all entities from index", e);
    }
  }

  public IndexStatus getStatus() {
    IndexStatus status = new IndexStatus();

    Set<Class<? extends DomainEntity>> types = Sets.newTreeSet(new Comparator<Class<? extends DomainEntity>>() {
      @Override
      public int compare(Class<? extends DomainEntity> o1, Class<? extends DomainEntity> o2) {
        return o1.getSimpleName().compareTo(o2.getSimpleName());
      }
    });
    types.addAll(config.getDefaultScope().getBaseEntityTypes());
    for (Class<? extends DomainEntity> type : types) {
      status.addDomainEntityCount(getCount(type));
    }

    return status;
  }

  private KV<Long> getCount(Class<? extends Entity> type) {
    try {
      String coreName = registry.getINameForType(type);
      return new KV<Long>(type.getSimpleName(), server.count(coreName));
    } catch (Exception e) {
      return new KV<Long>(type.getSimpleName(), (long) 0);
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
