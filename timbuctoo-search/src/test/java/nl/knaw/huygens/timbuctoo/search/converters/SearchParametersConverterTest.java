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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import org.junit.Test;

public class SearchParametersConverterTest {
  @Test
  public void testToV1() {
    // setUp
    final SearchParametersV1 searchParametersV1Mock = mock(SearchParametersV1.class);
    FacetFieldConverter facetFieldConveterMock = mock(FacetFieldConverter.class);
    SortConverter sortConverterMock = mock(SortConverter.class);
    FacetParameterConverter facetParameterConverter = mock(FacetParameterConverter.class);

    String term = "test";
    String typeString = "testType";
    boolean fuzzy = false;
    SearchParameters searchParameters = new SearchParameters().setTerm(term).setTypeString(typeString).setFuzzy(fuzzy);

    SearchParametersConverter instance = new SearchParametersConverter(facetFieldConveterMock, sortConverterMock, facetParameterConverter) {
      @Override
      protected SearchParametersV1 createParametersV1() {
        return searchParametersV1Mock;
      }
    };

    // action
    SearchParametersV1 actualV1 = instance.toV1(searchParameters);

    // verify
    verify(searchParametersV1Mock).setTerm(term);
    verify(searchParametersV1Mock).setTypeString(typeString);
    verify(searchParametersV1Mock).setFuzzy(fuzzy);
    verify(sortConverterMock).addToV1(searchParameters, searchParametersV1Mock);
    verify(facetFieldConveterMock).addToV1(searchParameters, searchParametersV1Mock);
    verify(facetParameterConverter).addToV1(searchParameters, searchParametersV1Mock);

    assertThat(actualV1, instanceOf(SearchParametersV1.class));

  }
}
