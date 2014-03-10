package nl.knaw.huygens.timbuctoo.index;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.NoSuchFacetException;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

public class IndexFacade implements SearchManager, IndexManager {

  private AbstractSolrServer abstractSolrServer;
  private SolrInputDocumentCreator solrInputDocCreator;

  public IndexFacade(AbstractSolrServer abstractSolrServer, SolrInputDocumentCreator solrInputDocCreator) {
    this.abstractSolrServer = abstractSolrServer;
    this.solrInputDocCreator = solrInputDocCreator;
  }

  @Override
  public <T extends DomainEntity> void addEntity(Class<T> type, String id) throws IndexException {
    SolrInputDocument solrInputDocument = solrInputDocCreator.create(type, id);
    try {
      abstractSolrServer.add(solrInputDocument);
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
