package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.facetedsearch.model.parameters.IndexDescription;
import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.solr.AbstractSolrServerBuilder;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.solr.core.CoreDescriptor;

import com.google.inject.Inject;

public class SolrIndexFactory implements IndexFactory {

  private final SolrInputDocumentCreator solrDocumentCreator;
  private final AbstractSolrServerBuilder solrServerBuilder;
  private final IndexDescriptionFactory indexDescriptionFactory;
  private final FacetedSearchLibraryFactory facetedSearchLibraryFactory;

  @Inject
  public SolrIndexFactory(SolrInputDocumentCreator solrInputDocumentCreator, AbstractSolrServerBuilder solrServerBuilder, IndexDescriptionFactory indexDescriptionFactory,
      FacetedSearchLibraryFactory facetedSearchLibraryFactory) {
    this.solrDocumentCreator = solrInputDocumentCreator;
    this.solrServerBuilder = solrServerBuilder;
    this.indexDescriptionFactory = indexDescriptionFactory;
    this.facetedSearchLibraryFactory = facetedSearchLibraryFactory;
  }

  @Override
  public SolrIndex createIndexFor(Class<? extends DomainEntity> type, String name) {
    IndexDescription indexDescription = this.indexDescriptionFactory.create(type);
    AbstractSolrServer abstractSolrServer = this.solrServerBuilder.setCoreName(name) //
        //TODO extract the data dir name creation
        .addProperty(CoreDescriptor.CORE_DATADIR, "data/" + name.replace('.', '/')) //
        .build(indexDescription);
    FacetedSearchLibrary facetedSearchLibrary = this.facetedSearchLibraryFactory.create(abstractSolrServer);

    return new SolrIndex(name, solrDocumentCreator, abstractSolrServer, facetedSearchLibrary);
  }
}
