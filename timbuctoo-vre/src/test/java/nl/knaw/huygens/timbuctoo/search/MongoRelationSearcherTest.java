package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo VRE
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class MongoRelationSearcherTest {

  private static final Class<SearchResult> SEARCH_RESULT_TYPE = SearchResult.class;
  private static final Class<Relation> RELATION_VARIATION = Relation.class;
  private static final String VRE_ID = "vreId";
  private static final String TYPE_STRING = "relationSubType";
  private static final String SOURCE_TYPE = "sourceType";
  private static final String TARGET_TYPE = "targetType";

  @Mock
  private FilterableSet<Relation> filterableRelationsMock;
  private VRE vreMock;
  private Repository repositoryMock;
  private CollectionConverter collectionConverterMock;
  private final String sourceSearchId = "sourceSearchId";
  private final String targetSearchId = "targetSearchId";
  private final List<String> relationTypeIds = Lists.newArrayList("id1", "id2", "id3");;
  private final List<Relation> foundRelations = null;
  private final List<String> sourceIds = null;
  private final List<String> targetIds = null;
  private SearchResult sourceSearchResult;
  private SearchResult targetSearchResult;
  private final Set<Relation> filteredRelations = Sets.newHashSet();
  private final SearchResult relationSearchResult = new SearchResult();
  private RelationSearchResultCreator relationSearchResultCreatorMock;
  private MongoRelationSearcher instance;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);

    vreMock = mock(VRE.class);
    repositoryMock = mock(Repository.class);
    collectionConverterMock = mock(CollectionConverter.class);
    relationSearchResultCreatorMock = mock(RelationSearchResultCreator.class);

    sourceSearchResult = new SearchResult();
    sourceSearchResult.setSearchType(SOURCE_TYPE);
    sourceSearchResult.setIds(sourceIds);

    targetSearchResult = new SearchResult();
    targetSearchResult.setSearchType(TARGET_TYPE);
    targetSearchResult.setIds(targetIds);

    when(vreMock.getVreId()).thenReturn(VRE_ID);
    when(repositoryMock.getRelationsByType(RELATION_VARIATION, relationTypeIds)).thenReturn(foundRelations);
    when(repositoryMock.getEntity(SEARCH_RESULT_TYPE, sourceSearchId)).thenReturn(sourceSearchResult);
    when(repositoryMock.getEntity(SEARCH_RESULT_TYPE, targetSearchId)).thenReturn(targetSearchResult);
    when(collectionConverterMock.toFilterableSet(foundRelations)).thenReturn(filterableRelationsMock);
    when(filterableRelationsMock.filter(Mockito.<RelationSourceTargetPredicate<Relation>> any())).thenReturn(filteredRelations);
    when(relationSearchResultCreatorMock.create(VRE_ID, TYPE_STRING, filteredRelations, SOURCE_TYPE, sourceIds, TARGET_TYPE, targetIds, relationTypeIds)).thenReturn(relationSearchResult);

    instance = new MongoRelationSearcher(repositoryMock, collectionConverterMock, relationSearchResultCreatorMock);
  }

  @Test
  public void testSearchWithRelationTypes() throws Exception {
    // setup
    RelationSearchParameters params = new RelationSearchParameters();
    params.setTypeString(TYPE_STRING);
    params.setRelationTypeIds(relationTypeIds);
    params.setSourceSearchId(sourceSearchId);
    params.setTargetSearchId(targetSearchId);

    // action 
    SearchResult actualResult = instance.search(vreMock, Relation.class, params);

    // verify
    verify(repositoryMock).getRelationsByType(RELATION_VARIATION, relationTypeIds);
    verify(repositoryMock).getEntity(SEARCH_RESULT_TYPE, sourceSearchId);
    verify(repositoryMock).getEntity(SEARCH_RESULT_TYPE, targetSearchId);
    verify(filterableRelationsMock).filter(Mockito.<RelationSourceTargetPredicate<Relation>> any());
    verify(relationSearchResultCreatorMock).create(VRE_ID, TYPE_STRING, filteredRelations, SOURCE_TYPE, sourceIds, TARGET_TYPE, targetIds, relationTypeIds);
    assertThat(actualResult, equalTo(relationSearchResult));
  }

  @Test
  public void testSearchWithOutSpecifiedRelations() throws Exception {
    // setup
    RelationSearchParameters params = new RelationSearchParameters();
    params.setSourceSearchId(sourceSearchId);
    params.setTargetSearchId(targetSearchId);
    params.setTypeString(TYPE_STRING);

    List<String> relationTypeNames = Lists.newArrayList();

    when(vreMock.getReceptionNames()).thenReturn(relationTypeNames);
    when(repositoryMock.getRelationTypeIdsByName(relationTypeNames)).thenReturn(relationTypeIds);

    // action 
    SearchResult actualResult = instance.search(vreMock, Relation.class, params);

    // verify
    verify(repositoryMock).getRelationTypeIdsByName(relationTypeNames);
    verify(repositoryMock).getRelationsByType(RELATION_VARIATION, relationTypeIds);
    verify(repositoryMock).getEntity(SEARCH_RESULT_TYPE, sourceSearchId);
    verify(repositoryMock).getEntity(SEARCH_RESULT_TYPE, targetSearchId);
    verify(filterableRelationsMock).filter(Mockito.<RelationSourceTargetPredicate<Relation>> any());
    verify(relationSearchResultCreatorMock).create(VRE_ID, TYPE_STRING, filteredRelations, SOURCE_TYPE, sourceIds, TARGET_TYPE, targetIds, relationTypeIds);
    assertThat(actualResult, equalTo(relationSearchResult));
  }

  @Test(expected = SearchException.class)
  public void searchShouldThrowAnSearchExceptionWhenTheRepositoryThrowsAnStorageException() throws Exception {
    RelationSearchParameters params = new RelationSearchParameters();
    params.setRelationTypeIds(relationTypeIds);
    params.setSourceSearchId(sourceSearchId);
    params.setTargetSearchId(targetSearchId);

    doThrow(StorageException.class).when(repositoryMock).getRelationsByType(Mockito.<Class<? extends Relation>> any(), Mockito.<List<String>> any());

    // action 
    instance.search(vreMock, Relation.class, params);
  }

}
