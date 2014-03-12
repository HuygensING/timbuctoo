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
  public void add(List<? extends DomainEntity> variationsToAdd) throws IndexException {
    SolrInputDocument document = solrDocumentCreator.create(variationsToAdd);

    try {
      solrServer.add(document);
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }

  }
}
