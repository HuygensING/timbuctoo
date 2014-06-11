package nl.knaw.huygens.timbuctoo.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.index.model.SubModel;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class IndexFacadeTest {

  private static final Class<ExplicitlyAnnotatedModel> BASE_TYPE = ExplicitlyAnnotatedModel.class;
  private static final Class<OtherIndexBaseType> OTHER_BASE_TYPE = OtherIndexBaseType.class;
  private static final String DEFAULT_ID = "id01234";
  private ScopeManager scopeManagerMock;
  private IndexFacade instance;
  private Repository repositoryMock;
  private Class<SubModel> type = SubModel.class;
  private IndexStatus indexStatusMock;
  private SortableFieldFinder sortableFieldFinderMock;
  private FacetedSearchResultConverter facetedSearchResultConverterMock;
  private VREManager vreManagerMock;

  @Before
  public void setUp() {
    indexStatusMock = mock(IndexStatus.class);
    repositoryMock = mock(Repository.class);
    scopeManagerMock = mock(ScopeManager.class);
    sortableFieldFinderMock = mock(SortableFieldFinder.class);
    facetedSearchResultConverterMock = mock(FacetedSearchResultConverter.class);
    vreManagerMock = mock(VREManager.class);
    instance = new IndexFacade(scopeManagerMock, repositoryMock, sortableFieldFinderMock, facetedSearchResultConverterMock, vreManagerMock) {
      @Override
      protected IndexStatus creatIndexStatus() {
        return indexStatusMock;
      }
    };
  }

  @Test
  public void testAddEntityInOneIndex() throws IndexException, IOException {
    // mock
    VRE vreMock = mock(VRE.class);
    Index indexMock = mock(Index.class);

    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(BASE_TYPE), mock(type));
    List<ExplicitlyAnnotatedModel> filteredVariations = Lists.newArrayList();
    filteredVariations.add(mock(SubModel.class));

    // when
    when(repositoryMock.getAllVariations(BASE_TYPE, DEFAULT_ID)).thenReturn(variations);
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock));
    when(scopeManagerMock.getIndexFor(vreMock, BASE_TYPE)).thenReturn(indexMock);
    when(vreMock.filter(variations)).thenReturn(filteredVariations);

    // action
    instance.addEntity(type, DEFAULT_ID);

    // verify
    verify(indexMock).add(filteredVariations);
  }

  @Test
  public void testAddEntityInMultipleIndexes() throws IndexException, IOException {
    // mock
    VRE vreMock1 = mock(VRE.class);
    VRE vreMock2 = mock(VRE.class);
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class), mock(SubModel.class));
    List<ExplicitlyAnnotatedModel> filteredVariations1 = Lists.newArrayList();
    filteredVariations1.add(mock(SubModel.class));
    List<ExplicitlyAnnotatedModel> filteredVariations2 = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class));

    // when
    when(repositoryMock.getAllVariations(BASE_TYPE, DEFAULT_ID)).thenReturn(variations);
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));
    when(scopeManagerMock.getIndexFor(vreMock1, BASE_TYPE)).thenReturn(indexMock1);
    when(scopeManagerMock.getIndexFor(vreMock2, BASE_TYPE)).thenReturn(indexMock2);
    when(vreMock1.filter(variations)).thenReturn(filteredVariations1);
    when(vreMock2.filter(variations)).thenReturn(filteredVariations2);

    // action
    instance.addEntity(type, DEFAULT_ID);

    // verify
    verify(indexMock1).add(filteredVariations1);
    verify(indexMock2).add(filteredVariations2);
  }

  @Test(expected = IndexException.class)
  public void testAddEntityStorageManagerReturnsEmptyList() throws IOException, IndexException {
    Class<SubModel> type = SubModel.class;
    Class<ExplicitlyAnnotatedModel> baseType = ExplicitlyAnnotatedModel.class;
    doReturn(Collections.emptyList()).when(repositoryMock).getAllVariations(baseType, DEFAULT_ID);

    try {
      // action
      instance.addEntity(type, DEFAULT_ID);
    } finally {
      // verify
      verify(repositoryMock).getAllVariations(baseType, DEFAULT_ID);
      verifyZeroInteractions(scopeManagerMock);
    }
  }

  @Test(expected = IndexException.class)
  public void testAddIndexThrowsAnIndexException() throws IOException, IndexException {
    // mock
    VRE vreMock = mock(VRE.class);
    Index indexMock = mock(Index.class);

    List<ExplicitlyAnnotatedModel> variations = Lists.newArrayList(mock(ExplicitlyAnnotatedModel.class), mock(SubModel.class));
    List<ExplicitlyAnnotatedModel> filteredVariations = Lists.newArrayList();
    filteredVariations.add(mock(SubModel.class));

    // when
    when(repositoryMock.getAllVariations(BASE_TYPE, DEFAULT_ID)).thenReturn(variations);
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock));
    when(scopeManagerMock.getIndexFor(vreMock, BASE_TYPE)).thenReturn(indexMock);
    when(vreMock.filter(variations)).thenReturn(filteredVariations);
    doThrow(IndexException.class).when(indexMock).add(filteredVariations);

    try {
      // action
      instance.addEntity(type, DEFAULT_ID);
    } finally {
      // verify
      verify(indexMock).add(filteredVariations);
    }
  }

  @Test
  public void testUpdateEntity() throws IOException, IndexException {
    // mock
    VRE vreMock = mock(VRE.class);
    Index indexMock = mock(Index.class);

    Class<? extends DomainEntity> type = SubModel.class;
    Class<? extends DomainEntity> baseType = ExplicitlyAnnotatedModel.class;
    List<DomainEntity> variations = Lists.newArrayList();
    SubModel model1 = mock(SubModel.class);
    variations.add(model1);
    List<DomainEntity> filteredVariations = Lists.newArrayList();
    filteredVariations.add(model1);

    // when
    doReturn(variations).when(repositoryMock).getAllVariations(baseType, DEFAULT_ID);
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock));
    when(scopeManagerMock.getIndexFor(vreMock, baseType)).thenReturn(indexMock);
    when(vreMock.filter(variations)).thenReturn(filteredVariations);

    // action
    instance.updateEntity(type, DEFAULT_ID);

    // verify
    verify(indexMock).update(filteredVariations);
  }

  @Test
  public void testDelete() throws IndexException {
    // setup
    VRE vreMock = mock(VRE.class);
    Index indexMock = mock(Index.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock));
    when(scopeManagerMock.getIndexFor(vreMock, BASE_TYPE)).thenReturn(indexMock);

    // action
    instance.deleteEntity(type, DEFAULT_ID);

    //verify
    verify(indexMock).deleteById(DEFAULT_ID);
  }

  @Test
  public void testDeleteMultipleScopes() throws IndexException {
    // setup
    VRE vreMock1 = mock(VRE.class);
    VRE vreMock2 = mock(VRE.class);
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));
    when(scopeManagerMock.getIndexFor(vreMock1, BASE_TYPE)).thenReturn(indexMock1);
    when(scopeManagerMock.getIndexFor(vreMock2, BASE_TYPE)).thenReturn(indexMock2);

    // action
    instance.deleteEntity(type, DEFAULT_ID);

    //verify
    verify(indexMock1).deleteById(DEFAULT_ID);
    verify(indexMock2).deleteById(DEFAULT_ID);
  }

  @Test(expected = IndexException.class)
  public void testDeleteMultipleScopesFirstThrowsAnException() throws IndexException {
    // setup
    VRE vreMock1 = mock(VRE.class);
    VRE vreMock2 = mock(VRE.class);
    Index indexMock1 = mock(Index.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));
    when(scopeManagerMock.getIndexFor(vreMock1, BASE_TYPE)).thenReturn(indexMock1);
    doThrow(IndexException.class).when(indexMock1).deleteById(DEFAULT_ID);

    try {
      // action
      instance.deleteEntity(type, DEFAULT_ID);
    } finally {
      //verify
      verify(scopeManagerMock).getIndexFor(vreMock1, BASE_TYPE);
      verify(indexMock1).deleteById(DEFAULT_ID);
      verifyNoMoreInteractions(scopeManagerMock);
    }
  }

  @Test
  public void testDeleteEntities() throws IndexException {
    // setup
    VRE vreMock = mock(VRE.class);
    Index indexMock = mock(Index.class);

    List<String> ids = Lists.newArrayList("id1", "id2", "id3");

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock));
    when(scopeManagerMock.getIndexFor(vreMock, BASE_TYPE)).thenReturn(indexMock);

    // action
    instance.deleteEntities(type, ids);

    // verify
    verify(indexMock).deleteById(ids);
  }

  @Test
  public void testDeleteAllEntities() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    // when
    List<Index> indexes = Lists.newArrayList(indexMock1, indexMock2);
    when(scopeManagerMock.getAllIndexes()).thenReturn(indexes);

    // action
    instance.deleteAllEntities();

    // verify
    verify(scopeManagerMock).getAllIndexes();
    verify(indexMock1).clear();
    verify(indexMock2).clear();
  }

  @Test(expected = IndexException.class)
  public void testDeleteAllEntitiesIndexClearThrowsAnIndexException() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    // when
    List<Index> indexes = Lists.newArrayList(indexMock1, indexMock2);
    when(scopeManagerMock.getAllIndexes()).thenReturn(indexes);
    doThrow(IndexException.class).when(indexMock1).clear();

    try {
      // action
      instance.deleteAllEntities();
    } finally {
      // verify
      verify(scopeManagerMock).getAllIndexes();
      verify(indexMock1).clear();
      verifyZeroInteractions(indexMock2);
    }
  }

  @Test
  public void testGetStatus() throws IndexException {
    // setup
    Set<Class<? extends DomainEntity>> baseTypes = Sets.newHashSet();
    baseTypes.add(BASE_TYPE);
    baseTypes.add(OTHER_BASE_TYPE);

    VRE vreMock1 = mock(VRE.class);
    Index vre1BaseTypeIndex = mock(Index.class);
    Index vre1OtherBaseTypeIndex = mock(Index.class);

    VRE vreMock2 = mock(VRE.class);
    Index vre2BaseTypeIndex = mock(Index.class);
    Index vre2OtherBaseTypeIndex = mock(Index.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock1, vreMock2));

    doReturn(baseTypes).when(vreMock1).getBaseEntityTypes();
    when(scopeManagerMock.getIndexFor(vreMock1, BASE_TYPE)).thenReturn(vre1BaseTypeIndex);
    when(scopeManagerMock.getIndexFor(vreMock1, OTHER_BASE_TYPE)).thenReturn(vre1OtherBaseTypeIndex);
    long itemCount1 = 42;
    when(vre1BaseTypeIndex.getCount()).thenReturn(itemCount1);
    long itemCount2 = 43;
    when(vre1OtherBaseTypeIndex.getCount()).thenReturn(itemCount2);

    doReturn(baseTypes).when(vreMock2).getBaseEntityTypes();
    when(scopeManagerMock.getIndexFor(vreMock2, BASE_TYPE)).thenReturn(vre2BaseTypeIndex);
    when(scopeManagerMock.getIndexFor(vreMock2, OTHER_BASE_TYPE)).thenReturn(vre2OtherBaseTypeIndex);
    long itemCount3 = 44;
    when(vre2BaseTypeIndex.getCount()).thenReturn(itemCount3);
    long itemCount4 = 45;
    when(vre2OtherBaseTypeIndex.getCount()).thenReturn(itemCount4);

    // action
    IndexStatus actualIndexStatus = instance.getStatus();

    // verify
    verify(indexStatusMock).addCount(vreMock1, BASE_TYPE, itemCount1);
    verify(indexStatusMock).addCount(vreMock1, OTHER_BASE_TYPE, itemCount2);
    verify(indexStatusMock).addCount(vreMock2, BASE_TYPE, itemCount3);
    verify(indexStatusMock).addCount(vreMock2, OTHER_BASE_TYPE, itemCount4);

    assertNotNull(actualIndexStatus);
  }

  @Test
  public void testGetStatusWhenIndexThrowsIndexException() throws IndexException {
    // setup
    Set<Class<? extends DomainEntity>> baseTypes = Sets.newHashSet();
    baseTypes.add(BASE_TYPE);
    baseTypes.add(OTHER_BASE_TYPE);

    VRE vreMock = mock(VRE.class);
    Index indexMock = mock(Index.class);

    // when
    when(vreManagerMock.getAllVREs()).thenReturn(Lists.newArrayList(vreMock));
    when(scopeManagerMock.getIndexFor(vreMock, BASE_TYPE)).thenReturn(indexMock);
    when(scopeManagerMock.getIndexFor(vreMock, OTHER_BASE_TYPE)).thenReturn(indexMock);

    when(vreMock.getBaseEntityTypes()).thenReturn(baseTypes);
    doThrow(IndexException.class).when(indexMock).getCount();

    // action
    IndexStatus actualStatus = instance.getStatus();

    // verify
    verify(indexMock, times(2)).getCount();

    verifyZeroInteractions(indexStatusMock);

    assertNotNull(actualStatus);
  }

  @Test
  public void testCommitAll() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    List<Index> indexes = Lists.newArrayList(indexMock1, indexMock2);

    // when
    when(scopeManagerMock.getAllIndexes()).thenReturn(indexes);

    // action
    instance.commitAll();

    // verify
    verify(scopeManagerMock).getAllIndexes();
    verify(indexMock1).commit();
    verify(indexMock2).commit();
  }

  @Test(expected = IndexException.class)
  public void testCommitAllFirstIndexThrowsAnIndexException() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    List<Index> indexes = Lists.newArrayList(indexMock1, indexMock2);

    // when
    when(scopeManagerMock.getAllIndexes()).thenReturn(indexes);
    doThrow(IndexException.class).when(indexMock1).commit();

    try {
      // action
      instance.commitAll();
    } finally {
      // verify
      verify(scopeManagerMock).getAllIndexes();
      verify(indexMock1).commit();
      verifyZeroInteractions(indexMock2);
    }
  }

  @Test
  public void testClose() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    // when
    when(scopeManagerMock.getAllIndexes()).thenReturn(Lists.newArrayList(indexMock1, indexMock2));

    // action
    instance.close();

    // verify
    verify(indexMock1).close();
    verify(indexMock2).close();
  }

  @Test
  public void testCloseFirstThrowsIndexException() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    // when
    when(scopeManagerMock.getAllIndexes()).thenReturn(Lists.newArrayList(indexMock1, indexMock2));
    doThrow(IndexException.class).when(indexMock1).close();

    // action
    instance.close();

    // verify
    verify(indexMock1).close();
    verify(indexMock2).close();
  }

  @Test
  public void testFindSortableFields() {

    // action 
    instance.findSortableFields(BASE_TYPE);

    // verify
    verify(sortableFieldFinderMock).findFields(BASE_TYPE);
  }

  @Test
  public void testSearch() throws SearchException {
    // setup
    Index indexMock = mock(Index.class);
    VRE vreMock = mock(VRE.class);
    DefaultFacetedSearchParameters searchParameters = new DefaultFacetedSearchParameters();
    SearchResult searchResult = new SearchResult();
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
    String typeString = "explicitlyannotatedmodel";

    // when
    when(scopeManagerMock.getIndexFor(vreMock, BASE_TYPE)).thenReturn(indexMock);
    when(indexMock.search(searchParameters)).thenReturn(facetedSearchResult);
    when(facetedSearchResultConverterMock.convert(typeString, facetedSearchResult)).thenReturn(searchResult);

    SearchResult actualSearchResult = instance.search(vreMock, BASE_TYPE, searchParameters);

    // verify
    verify(indexMock).search(searchParameters);
    verify(facetedSearchResultConverterMock).convert(typeString, facetedSearchResult);
    assertThat(actualSearchResult, is(searchResult));
  }

  @Test(expected = SearchException.class)
  public void testSearchIndexThrowsSearchException() throws SearchException {
    // setup
    Index indexMock = mock(Index.class);
    VRE vreMock = mock(VRE.class);
    DefaultFacetedSearchParameters searchParameters = new DefaultFacetedSearchParameters();

    // when
    when(scopeManagerMock.getIndexFor(vreMock, BASE_TYPE)).thenReturn(indexMock);
    doThrow(SearchException.class).when(indexMock).search(searchParameters);

    instance.search(vreMock, BASE_TYPE, searchParameters);
  }

  private static class OtherIndexBaseType extends DomainEntity {

    @Override
    public String getDisplayName() {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
