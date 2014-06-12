package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.solr.AbstractSolrServer;

import com.google.inject.Inject;

public class SolrIndexFactory implements IndexFactory {

  private final SolrInputDocumentCreator solrDocumentCreator;
  private final AbstractSolrServer solrServer;
  private final FacetedSearchLibrary facetedSearchLibrary;

  @Inject
  public SolrIndexFactory(SolrInputDocumentCreator solrInputDocumentCreator, AbstractSolrServer solrServer, FacetedSearchLibrary facetedSearchLibrary) {
    this.solrDocumentCreator = solrInputDocumentCreator;
    this.solrServer = solrServer;
    this.facetedSearchLibrary = facetedSearchLibrary;

  }

  @Override
  public Index createIndexFor(String name) {
    return new SolrIndex(name, solrDocumentCreator, solrServer, facetedSearchLibrary);
  }
}
