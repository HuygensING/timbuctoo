package nl.knaw.huygens.timbuctoo.index;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

public class SolrIndex implements Index {

  private final SolrInputDocumentCreator solrDocumentCreator;
  private final AbstractSolrServer solrServer;

  public SolrIndex(SolrInputDocumentCreator solrDocumentCreator, AbstractSolrServer solrServer) {
    this.solrDocumentCreator = solrDocumentCreator;
    this.solrServer = solrServer;
  }

  @Override
  public void add(List<? extends DomainEntity> variations) throws IndexException {
    updateIndex(variations);

  }

  private void updateIndex(List<? extends DomainEntity> variations) throws IndexException {
    SolrInputDocument document = solrDocumentCreator.create(variations);

    try {
      solrServer.add(document);
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }
  }

  @Override
  public void update(List<? extends DomainEntity> variations) throws IndexException {
    updateIndex(variations);
  }

  @Override
  public void deleteById(String id) throws IndexException {
    try {
      solrServer.deleteById(id);
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }

  }

}
