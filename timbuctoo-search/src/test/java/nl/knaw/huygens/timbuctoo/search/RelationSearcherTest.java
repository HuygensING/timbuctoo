package nl.knaw.huygens.timbuctoo.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher.RelationSourceTargetPredicate;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class RelationSearcherTest {
  @Mock
  private FilterableSet<Relation> filterableRelationsMock;
  private VRE vreMock;
  private Repository repositoryMock;
  private CollectionConverter collectionConverterMock;
  private String sourceSearchId = "sourceSearchId";
  private String targetSearchId = "targetSearchId";
  private List<String> relationTypeIds = Lists.newArrayList("id1", "id2", "id3");;
  List<Relation> foundRelations = null;
  List<String> sourceIds = null;
  List<String> targetIds = null;
  private SearchResult sourceSearchResult = new SearchResult();
  private SearchResult targetSearchResult = new SearchResult();
  private Set<Relation> filteredRelations = Sets.newHashSet();
  private SearchResult relationSearchResult = new SearchResult();
  private RelationSearchResultCreator relationSearchResultCreatorMock;
  private RelationSearcher instance;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    vreMock = mock(VRE.class);
    repositoryMock = mock(Repository.class);
    collectionConverterMock = mock(CollectionConverter.class);
    relationSearchResultCreatorMock = mock(RelationSearchResultCreator.class);

    sourceSearchResult.setIds(sourceIds);
    targetSearchResult.setIds(targetIds);

    when(repositoryMock.getRelationsByType(relationTypeIds)).thenReturn(foundRelations);
    when(repositoryMock.getEntity(SearchResult.class, sourceSearchId)).thenReturn(sourceSearchResult);
    when(repositoryMock.getEntity(SearchResult.class, targetSearchId)).thenReturn(targetSearchResult);
    when(collectionConverterMock.toFilterableSet(foundRelations)).thenReturn(filterableRelationsMock);
    when(filterableRelationsMock.filter(Mockito.<RelationSourceTargetPredicate<Relation>> any())).thenReturn(filteredRelations);
    when(relationSearchResultCreatorMock.create(filteredRelations, sourceIds, targetIds)).thenReturn(relationSearchResult);

    instance = new RelationSearcher(repositoryMock, collectionConverterMock, relationSearchResultCreatorMock);
  }

  @Test
  public void testSearchWithRelationTypes() {
    RelationSearchParameters params = new RelationSearchParameters();
    params.setRelationTypeIds(relationTypeIds);
    params.setSourceSearchId(sourceSearchId);
    params.setTargetSearchId(targetSearchId);

    // action 
    SearchResult actualResult = instance.search(vreMock, params);

    // verify
    verify(repositoryMock).getRelationsByType(relationTypeIds);
    verify(repositoryMock).getEntity(SearchResult.class, sourceSearchId);
    verify(repositoryMock).getEntity(SearchResult.class, targetSearchId);
    verify(filterableRelationsMock).filter(Mockito.<RelationSourceTargetPredicate<Relation>> any());
    verify(relationSearchResultCreatorMock).create(filteredRelations, sourceIds, targetIds);
    assertThat(actualResult, equalTo(relationSearchResult));
  }

  @Test
  public void testSearchWithOutSpecifiedRelations() {
    // setup
    RelationSearchParameters params = new RelationSearchParameters();
    params.setSourceSearchId(sourceSearchId);
    params.setTargetSearchId(targetSearchId);

    List<String> relationTypeNames = Lists.newArrayList();

    when(vreMock.getReceptionNames()).thenReturn(relationTypeNames);
    when(repositoryMock.getRelationTypeIdsByName(relationTypeNames)).thenReturn(relationTypeIds);

    // action 
    SearchResult actualResult = instance.search(vreMock, params);

    // verify
    verify(repositoryMock).getRelationTypeIdsByName(relationTypeNames);
    verify(repositoryMock).getRelationsByType(relationTypeIds);
    verify(repositoryMock).getEntity(SearchResult.class, sourceSearchId);
    verify(repositoryMock).getEntity(SearchResult.class, targetSearchId);
    verify(filterableRelationsMock).filter(Mockito.<RelationSourceTargetPredicate<Relation>> any());
    verify(relationSearchResultCreatorMock).create(filteredRelations, sourceIds, targetIds);
    assertThat(actualResult, equalTo(relationSearchResult));
  }
}
