package nl.knaw.huygens.timbuctoo.search;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.converters.RelationFacetedSearchResultConverter;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Test;

public class SolrRelationSearcherTest {
  @Test
  public void testSearchWithRelationTypes() throws Exception {
    // setup
    RelationSearchParametersConverter relationSearcherParametersConverterMock = mock(RelationSearchParametersConverter.class);

    String typeString = null;
    FacetedSearchResult facetedSearchResult = null;
    RelationSearchParameters searchParamters = null;
    SearchParametersV1 searchParameters = null;
    Class<? extends DomainEntity> type = null;
    Index indexMock = mock(Index.class);

    RelationFacetedSearchResultConverter searchResultConverter = mock(RelationFacetedSearchResultConverter.class);
    VREManager vreManagerMock = mock(VREManager.class);
    VRE vreMock = mock(VRE.class);
    Index index = mock(Index.class);

    when(vreManagerMock.getIndexFor(vreMock, type)).thenReturn(indexMock);

    // verify
    verify(relationSearcherParametersConverterMock).toSearchParamtersV1(searchParamters);
    verify(index).search(searchParameters);
    verify(searchResultConverter).convert(typeString, facetedSearchResult);

  }

  @Test
  public void testSearchWithOutRelationTypesSpecified() {
    fail("Yet to be implemented");
  }
}
