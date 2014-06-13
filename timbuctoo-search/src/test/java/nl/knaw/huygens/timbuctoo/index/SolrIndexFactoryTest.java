package nl.knaw.huygens.timbuctoo.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.facetedsearch.model.FacetDefinition;
import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.solr.AbstractSolrServerBuilder;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.FacetFinder;

import org.junit.Test;

import test.timbuctoo.index.model.BaseType1;

import com.google.common.collect.Lists;

public class SolrIndexFactoryTest {
  @Test
  public void testCreate() {
    // It should create a list of facet definitions.
    // It should create an AbstractSolrServer.
    // It should create a FacetedSearchLibrary

    // setup
    AbstractSolrServer solrServerMock = mock(AbstractSolrServer.class);
    List<FacetDefinition> facetDefinitions = Lists.newArrayList();
    FacetedSearchLibrary facetedSearchLibraryMock = mock(FacetedSearchLibrary.class);
    SolrInputDocumentCreator solrInputDocumentCreatorMock = mock(SolrInputDocumentCreator.class);

    FacetFinder facetFinderMock = mock(FacetFinder.class);
    AbstractSolrServerBuilder solrServerBuilderMock = mock(AbstractSolrServerBuilder.class);
    FacetedSearchLibraryFactory facetedSearchLibraryFactoryMock = mock(FacetedSearchLibraryFactory.class);

    String name = "test";
    Class<? extends DomainEntity> type = BaseType1.class;

    Index expectedSolrIndex = new SolrIndex(name, solrInputDocumentCreatorMock, solrServerMock, facetedSearchLibraryMock);

    when(facetFinderMock.findFacetDefinitions(type)).thenReturn(facetDefinitions);
    when(solrServerBuilderMock.build(facetDefinitions)).thenReturn(solrServerMock);
    when(facetedSearchLibraryFactoryMock.create(solrServerMock)).thenReturn(facetedSearchLibraryMock);

    SolrIndexFactory instance = new SolrIndexFactory(solrInputDocumentCreatorMock, solrServerBuilderMock, facetFinderMock, facetedSearchLibraryFactoryMock);

    // action
    SolrIndex actualSolrIndex = instance.createIndexFor(type, name);

    // verify
    assertThat(actualSolrIndex, is(equalTo(expectedSolrIndex)));
  }
}
