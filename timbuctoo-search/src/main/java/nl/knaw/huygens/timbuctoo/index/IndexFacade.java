package nl.knaw.huygens.timbuctoo.index;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.NoSuchFacetException;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

public class IndexFacade implements SearchManager, IndexManager {

  private final ScopeManager scopeManager;
  private final TypeRegistry typeRegistry;
  private final StorageManager storageManager;

  public IndexFacade(ScopeManager scopeManager, TypeRegistry typeRegistry, StorageManager storageManager) {
    this.scopeManager = scopeManager; // TODO place functionality of ScopeManager in VREManager
    this.typeRegistry = typeRegistry;
    this.storageManager = storageManager;
  }

  @Override
  public <T extends DomainEntity> void addEntity(Class<T> type, String id) throws IndexException {
    IndexChanger indexAdder = new IndexChanger() {

      @Override
      public void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException {
        index.add(variations);

      }
    };
    changeIndex(type, id, indexAdder);

  }

  private <T extends DomainEntity> void changeIndex(Class<T> type, String id, IndexChanger indexChanger) throws IndexException {
    Class<? extends DomainEntity> baseType = TypeRegistry.toDomainEntity(typeRegistry.getBaseClass(type));
    List<? extends DomainEntity> variations = null;

    try {
      variations = storageManager.getAllVariations(baseType, id);
    } catch (IOException e) {
      throw new IndexException("Could not retrieve variations for type " + type + "with id " + id);
    }

    for (Scope scope : scopeManager.getAllScopes()) {
      Index index = scopeManager.getIndexFor(scope, baseType);
      List<? extends DomainEntity> filteredVariations = scope.filter(variations);

      indexChanger.executeIndexAction(index, filteredVariations);
    }
  }

  @Override
  public <T extends DomainEntity> void updateEntity(Class<T> type, String id) throws IndexException {
    IndexChanger indexUpdater = new IndexChanger() {

      @Override
      public void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException {
        index.update(variations);
      }
    };
    changeIndex(type, id, indexUpdater);
  }

  @Override
  public <T extends DomainEntity> void deleteEntity(Class<T> type, String id) throws IndexException {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends DomainEntity> void deleteBaseEntity(Class<T> type, String id) throws IndexException {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends DomainEntity> void deleteEntities(Class<T> type, List<String> ids) throws IndexException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteAllEntities() throws IndexException {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends DomainEntity> QueryResponse search(Scope scope, Class<T> type, SolrQuery query) throws IndexException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IndexStatus getStatus() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void commitAll() throws IndexException {
    // TODO Auto-generated method stub

  }

  @Override
  public void close() throws IndexException {
    // TODO Auto-generated method stub

  }

  @Override
  public Set<String> findSortableFields(Class<? extends DomainEntity> type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SearchResult search(Scope scope, Class<? extends DomainEntity> type, SearchParameters searchParameters) throws IndexException, NoSuchFacetException {
    // TODO Auto-generated method stub
    return null;
  }

  private static interface IndexChanger {
    void executeIndexAction(Index index, List<? extends DomainEntity> variations) throws IndexException;
  }
}
