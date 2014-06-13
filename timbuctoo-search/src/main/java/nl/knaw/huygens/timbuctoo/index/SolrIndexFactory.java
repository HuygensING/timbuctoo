package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.facetedsearch.model.FacetDefinition;
import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.solr.AbstractSolrServerBuilder;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.FacetFinder;

import com.google.inject.Inject;

public class SolrIndexFactory implements IndexFactory {

  private final SolrInputDocumentCreator solrDocumentCreator;
  private final AbstractSolrServerBuilder solrServerBuilder;
  private final FacetFinder facetFinderMock;
  private final FacetedSearchLibraryFactory facetedSearchLibraryFactory;

  @Inject
  public SolrIndexFactory(SolrInputDocumentCreator solrInputDocumentCreator, AbstractSolrServerBuilder solrServerBuilder, FacetFinder facetFinderMock,
      FacetedSearchLibraryFactory facetedSearchLibraryFactory) {
    this.solrDocumentCreator = solrInputDocumentCreator;
    this.solrServerBuilder = solrServerBuilder;
    this.facetFinderMock = facetFinderMock;
    this.facetedSearchLibraryFactory = facetedSearchLibraryFactory;
  }

  @Override
  public SolrIndex createIndexFor(Class<? extends DomainEntity> type, String name) {
    List<FacetDefinition> facetDefinitions = this.facetFinderMock.findFacetDefinitions(type);
    AbstractSolrServer abstractSolrServer = this.solrServerBuilder.build(facetDefinitions);
    FacetedSearchLibrary facetedSearchLibrary = this.facetedSearchLibraryFactory.create(abstractSolrServer);

    return new SolrIndex(name, solrDocumentCreator, abstractSolrServer, facetedSearchLibrary);
  }
}
