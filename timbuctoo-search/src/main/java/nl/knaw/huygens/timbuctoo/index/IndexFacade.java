package nl.knaw.huygens.timbuctoo.index;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.NoSuchFacetException;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

public class IndexFacade implements SearchManager, IndexManager {

  private final AbstractSolrServer abstractSolrServer;
  private final SolrInputDocumentCreator solrInputDocCreator;
  private final StorageManager storageManager;
  private final TypeRegistry typeRegistry;

  public IndexFacade(AbstractSolrServer abstractSolrServer, SolrInputDocumentCreator solrInputDocCreator, StorageManager storageManager, TypeRegistry typeRegistry) {
    this.abstractSolrServer = abstractSolrServer;
    this.solrInputDocCreator = solrInputDocCreator;
    this.storageManager = storageManager;
    this.typeRegistry = typeRegistry;
  }

  @Override
  public <T extends DomainEntity> void addEntity(Class<T> type, String id) throws IndexException {
    Class<? extends DomainEntity> baseClass = TypeRegistry.toDomainEntity(typeRegistry.getBaseClass(type));
    List<? extends DomainEntity> allVariations = null;

    try {
      allVariations = storageManager.getAllVariations(baseClass, id);
    } catch (IOException e) {
      throw new IndexException(e);
    }
    SolrInputDocument doc = solrInputDocCreator.create(allVariations);

    try {
      abstractSolrServer.add(doc);
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
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
