package nl.knaw.huygens.timbuctoo.search.converters;

/*
 * #%L
 * Timbuctoo search
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

import static nl.knaw.huygens.timbuctoo.search.converters.DefaultFacetParameterMatcher.likeFacetParameter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class RelationSearchParametersConverterTest {
  @Mock
  List<FacetParameter> facetParameterList;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void toSearchParamtersV1() {
    // setup
    final SearchParametersV1 searchParametersV1Mock = mock(SearchParametersV1.class);

    RelationSearchParametersConverter instance = new RelationSearchParametersConverter() {
      @Override
      protected SearchParametersV1 createSearchParametersV1() {
        return searchParametersV1Mock;
      }

      @Override
      protected List<FacetParameter> createFacetParameterList() {
        return facetParameterList;
      }
    };

    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    List<String> relationTypeIds = Lists.newArrayList("id1", "id2");
    relationSearchParameters.setRelationTypeIds(relationTypeIds);

    // action
    SearchParametersV1 actualSearchParametersV1 = instance.toSearchParamtersV1(relationSearchParameters);

    // verify
    assertThat(actualSearchParametersV1, is(notNullValue()));
    verify(facetParameterList).add(argThat(likeFacetParameter(Relation.TYPE_ID_FACET_NAME, relationTypeIds)));
    verify(searchParametersV1Mock).setFacetParameters(facetParameterList);
  }

}
