package nl.knaw.huygens.timbuctoo.index;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.NoSuchFacetException;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

public class IndexFacade implements SearchManager, IndexManager {

  private final ScopeManager scopeManager;
  private final TypeRegistry typeRegistry;

  public IndexFacade(ScopeManager scopeManagerMock, TypeRegistry typeRegistry) {
    this.scopeManager = scopeManagerMock;
    this.typeRegistry = typeRegistry;
  }

  @Override
  public <T extends DomainEntity> void addEntity(Class<T> type, String id) throws IndexException {
    Class<? extends DomainEntity> baseType = TypeRegistry.toDomainEntity(typeRegistry.getBaseClass(type));

    for (Scope scope : scopeManager.getAllScopes()) {
      Index index = scopeManager.getIndexFor(scope, baseType);

      index.add(baseType, id);
    }

  }

  @Override
  public <T extends DomainEntity> void updateEntity(Class<T> type, String id) throws IndexException {
    // TODO Auto-generated method stub

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

}
