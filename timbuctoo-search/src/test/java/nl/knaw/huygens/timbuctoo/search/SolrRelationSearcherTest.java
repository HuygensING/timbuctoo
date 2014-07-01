package nl.knaw.huygens.timbuctoo.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.SearchException;
import nl.knaw.huygens.timbuctoo.index.SearchValidationException;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.converters.RelationFacetedSearchResultConverter;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

public class SolrRelationSearcherTest {
  private SolrRelationSearcher instance;
  private String typeString = "relation";
  private FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
  private SearchResult searchResult = new SearchResult();
  private SearchParametersV1 searchParametersV1 = new SearchParametersV1();
  private Class<? extends DomainEntity> type = Relation.class;
  private RelationSearchParametersConverter relationSearcherParametersConverterMock;
  private Index indexMock;
  private VREManager vreManagerMock;
  private VRE vreMock;
  private TypeRegistry typeRegistryMock;
  private RelationFacetedSearchResultConverter searchResultConverterMock;
  private Repository repositoryMock;
  private ArrayList<String> relationTypeIds = Lists.newArrayList("id1", "id2");

  @Before
  public void setup() throws Exception {
    relationSearcherParametersConverterMock = mock(RelationSearchParametersConverter.class);
    indexMock = mock(Index.class);
    vreManagerMock = mock(VREManager.class);
    vreMock = mock(VRE.class);
    typeRegistryMock = mock(TypeRegistry.class);
    searchResultConverterMock = mock(RelationFacetedSearchResultConverter.class);
    repositoryMock = mock(Repository.class);

    doReturn(type).when(typeRegistryMock).getDomainEntityType(typeString);
    when(vreManagerMock.getIndexFor(vreMock, type)).thenReturn(indexMock);
    when(indexMock.search(searchParametersV1)).thenReturn(facetedSearchResult);
    when(searchResultConverterMock.convert(typeString, facetedSearchResult)).thenReturn(searchResult);

    instance = new SolrRelationSearcher(repositoryMock, vreManagerMock, relationSearcherParametersConverterMock, typeRegistryMock, searchResultConverterMock);
  }

  @Test
  public void testSearchWithRelationTypes() throws Exception {
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setTypeString(typeString);
    relationSearchParameters.setRelationTypeIds(relationTypeIds);

    when(relationSearcherParametersConverterMock.toSearchParamtersV1(relationSearchParameters)).thenReturn(searchParametersV1);

    // action
    SearchResult actualResult = instance.search(vreMock, relationSearchParameters);

    // verify
    verify(relationSearcherParametersConverterMock).toSearchParamtersV1(relationSearchParameters);
    verify(indexMock).search(searchParametersV1);
    verify(searchResultConverterMock).convert(typeString, facetedSearchResult);
    assertThat(actualResult, equalTo(searchResult));

  }

  @Test
  public void testSearchWithOutRelationTypesSpecified() throws Exception {
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setTypeString(typeString);
    List<String> relationTypeNames = Lists.newArrayList();

    when(relationSearcherParametersConverterMock.toSearchParamtersV1(relationSearchParameters)).thenReturn(searchParametersV1);
    when(vreMock.getReceptionNames()).thenReturn(relationTypeNames);
    when(repositoryMock.getRelationTypeIdsByName(relationTypeNames)).thenReturn(relationTypeIds);

    // action
    SearchResult actualResult = instance.search(vreMock, relationSearchParameters);

    // verify
    InOrder inOrder = Mockito.inOrder(repositoryMock, relationSearcherParametersConverterMock);
    inOrder.verify(repositoryMock).getRelationTypeIdsByName(relationTypeNames);
    inOrder.verify(relationSearcherParametersConverterMock).toSearchParamtersV1(relationSearchParameters);
    verify(indexMock).search(searchParametersV1);
    verify(searchResultConverterMock).convert(typeString, facetedSearchResult);
    assertThat(actualResult, equalTo(searchResult));
  }

  @Test(expected = SearchValidationException.class)
  public void testSearchIndexThrowsASearchValidationException() throws Exception {
    testSearchIndexThrowsAnException(SearchValidationException.class);
  }

  @Test(expected = SearchException.class)
  public void testSearchIndexThrowsASearchException() throws Exception {
    testSearchIndexThrowsAnException(SearchException.class);
  }

  private void testSearchIndexThrowsAnException(Class<? extends Exception> exceptionToBeThrown) throws SearchException, SearchValidationException {
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setTypeString(typeString);
    relationSearchParameters.setRelationTypeIds(relationTypeIds);

    when(relationSearcherParametersConverterMock.toSearchParamtersV1(relationSearchParameters)).thenReturn(searchParametersV1);
    doThrow(exceptionToBeThrown).when(indexMock).search(searchParametersV1);

    // action
    instance.search(vreMock, relationSearchParameters);

    // verify
    verify(relationSearcherParametersConverterMock).toSearchParamtersV1(relationSearchParameters);
    verify(indexMock).search(searchParametersV1);
  }
}
