package nl.knaw.huygens.timbuctoo.search.converters;

/*
 * #%L
 * Timbuctoo VRE
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.google.common.collect.Lists;
import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetParameter;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParametersV2_1;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import test.timbuctoo.index.model.projecta.ProjectARelation;
import test.timbuctoo.index.model.projecta.ProjectAType1;

import java.util.ArrayList;

import static nl.knaw.huygens.timbuctoo.model.Relation.TYPE_ID_FACET_NAME;
import static nl.knaw.huygens.timbuctoo.search.converters.DefaultFacetParameterMatcher.likeFacetParameter;
import static nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter.RELATION_FACET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static test.timbuctoo.index.model.SearchParametersV1Matcher.likeSearchParametersV1;

public class RelationSearchParametersConverterTest {

  public static final String RELATION_2 = "relation2";
  public static final String RELATION_1 = "relation1";
  public static final ArrayList<String> RELATION_TYPE_NAMES = Lists.newArrayList(RELATION_1, RELATION_2);
  public static final String ID_1 = "id1";
  public static final String ID_2 = "id2";
  public static final ArrayList<String> RELATION_TYPE_IDS = Lists.newArrayList(ID_1, ID_2);
  public static final String SOURCE_SEARCH_RESULT_ID = "sourceSearchResultId";
  public static final String TARGET_SEARCH_RESULT_ID = "targetSearchResultId";
  public static final Class<ProjectAType1> TARGET_TYPE = ProjectAType1.class;
  public static final Class<ProjectARelation> RELATION_TYPE = ProjectARelation.class;
  private RelationSearchParametersConverter instance;
  private Repository repository;
  public static final SearchResult SEARCH_RESULT = new SearchResult();
  private VRE vre;
  private RelationSearchParametersV2_1 relationSearchParametersV2_1;
  public static final String RELATION_TYPE_STRING = TypeNames.getInternalName(RELATION_TYPE);

  @Before
  public void setUp() throws Exception {
    repository = mock(Repository.class);
    vre = mock(VRE.class);
    instance = new RelationSearchParametersConverter(repository);
    setupRelationSearchParametersV2_1();
  }

  private void setupRelationSearchParametersV2_1() {
    relationSearchParametersV2_1 = new RelationSearchParametersV2_1();
    relationSearchParametersV2_1.addFacetParameter(mock(FacetParameter.class));
    relationSearchParametersV2_1.addFacetParameter(new DefaultFacetParameter(RELATION_FACET, RELATION_TYPE_NAMES));
    relationSearchParametersV2_1.setOtherSearchId(SOURCE_SEARCH_RESULT_ID);
  }

  @Test
  public void toSearchParametersV1() {
    // setup
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setRelationTypeIds(RELATION_TYPE_IDS);

    // action
    SearchParametersV1 searchParameters = instance.toSearchParametersV1(relationSearchParameters);

    // verify
    assertNotNull(searchParameters);
    assertThat(searchParameters.getFacetValues(), contains(likeFacetParameter(TYPE_ID_FACET_NAME, RELATION_TYPE_IDS)));
    assertThat(searchParameters.getFacetFields(), contains(TYPE_ID_FACET_NAME));
  }

  @Test
  public void fromRelationParametersV2_1ConvertsTheV2_1RelationsSearchParametersBySearchingExecutingAnExtraSearch() throws Exception {
    // setup
    when(vre.search(argThat(equalTo(TARGET_TYPE)), any(SearchParametersV1.class))).thenReturn(SEARCH_RESULT);

    when(repository.getRelationTypeIdsByName(RELATION_TYPE_NAMES)).thenReturn(RELATION_TYPE_IDS);
    when(repository.addSystemEntity(SearchResult.class, SEARCH_RESULT)).thenReturn(TARGET_SEARCH_RESULT_ID);

    // action
    RelationSearchParameters relationSearchParameters = instance.fromRelationParametersV2_1(RELATION_TYPE, relationSearchParametersV2_1, vre, TARGET_TYPE);

    // verify
    assertThat(relationSearchParameters.getRelationTypeIds(), containsInAnyOrder(ID_1, ID_2));
    assertThat(relationSearchParameters.getSourceSearchId(), is(SOURCE_SEARCH_RESULT_ID));
    assertThat(relationSearchParameters.getTargetSearchId(), is(TARGET_SEARCH_RESULT_ID));
    assertThat(relationSearchParameters.getTypeString(), is(RELATION_TYPE_STRING));

    verify(vre).search(argThat(equalTo(TARGET_TYPE)), argThat(likeSearchParametersV1().withoutFacetParameter(likeFacetParameter(RELATION_FACET, RELATION_TYPE_NAMES))));
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void fromRelationParametersV2_1ThrowsAConversionsExceptionWhenVREsSearchThrowsASearchException() throws Exception {
    // setup
    when(vre.search(argThat(equalTo(TARGET_TYPE)), any(SearchParametersV1.class))).thenThrow(new SearchException(new Exception()));

    // setup expectation
    expectedException.expect(SearchConversionException.class);
    expectedException.expectCause(is(instanceOf(SearchException.class)));

    // action
    instance.fromRelationParametersV2_1(RELATION_TYPE, relationSearchParametersV2_1, vre, TARGET_TYPE);

  }

  @Test
  public void fromRelationParametersV2_1ThrowsAConversionsExceptionWhenVREsSearchThrowsASearchValidationException() throws Exception {
    // setup
    when(vre.search(argThat(equalTo(TARGET_TYPE)), any(SearchParametersV1.class))).thenThrow(new SearchValidationException(new Exception()));

    // setup expectation
    expectedException.expect(SearchConversionException.class);
    expectedException.expectCause(is(instanceOf(SearchValidationException.class)));

    // action
    instance.fromRelationParametersV2_1(RELATION_TYPE, relationSearchParametersV2_1, vre, TARGET_TYPE);
  }

  @Test
  public void fromRelationParametersV2_1ThrowsAConversionsExceptionWhenRepositorysAddSystemEntityThrowsAStorageException() throws Exception {
    // setup
    when(vre.search(argThat(equalTo(TARGET_TYPE)), any(SearchParametersV1.class))).thenReturn(SEARCH_RESULT);

    when(repository.addSystemEntity(SearchResult.class, SEARCH_RESULT)).thenThrow(new StorageException());

    // setup expectation
    expectedException.expect(SearchConversionException.class);
    expectedException.expectCause(is(instanceOf(StorageException.class)));

    // action
    instance.fromRelationParametersV2_1(RELATION_TYPE, relationSearchParametersV2_1, vre, TARGET_TYPE);
  }

  @Test
  public void fromRelationParametersV2_1ThrowsAConversionsExceptionWhenRepositorysAddSystemEntityThrowsAValidationException() throws Exception {
    // setup
    when(vre.search(argThat(equalTo(TARGET_TYPE)), any(SearchParametersV1.class))).thenReturn(SEARCH_RESULT);

    when(repository.addSystemEntity(SearchResult.class, SEARCH_RESULT)).thenThrow(new ValidationException());

    // setup expectation
    expectedException.expect(SearchConversionException.class);
    expectedException.expectCause(is(instanceOf(ValidationException.class)));

    // action
    instance.fromRelationParametersV2_1(RELATION_TYPE, relationSearchParametersV2_1, vre, TARGET_TYPE);
  }
}
