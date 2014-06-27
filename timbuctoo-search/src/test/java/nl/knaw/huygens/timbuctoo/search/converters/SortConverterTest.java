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

import static nl.knaw.huygens.timbuctoo.search.converters.SortParameterMatcher.likeSortParameter;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.parameters.SortDirection;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SortConverterTest {

  @Mock
  private List<SortParameter> sortParametersMock;
  private final String sortField = "id";
  private SortConverter instance;
  private SearchParametersV1 searchParametersV1Mock;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    instance = new SortConverter() {
      @Override
      protected List<SortParameter> createSortParamterList() {
        return sortParametersMock;
      }
    };

    searchParametersV1Mock = mock(SearchParametersV1.class);
  }

  @Test
  public void testAddToV1WithOutSortDirection() {
    // setup
    SearchParameters searchParameters = new SearchParameters().setSort(sortField);

    // action
    instance.addToV1(searchParameters, searchParametersV1Mock);

    // verify
    verifyThatAListOfSortParametersIsAddedToSearchParametersV1(//
        searchParametersV1Mock, //
        sortField, //
        SortDirection.ASCENDING);
  }

  protected void verifyThatAListOfSortParametersIsAddedToSearchParametersV1(//
      SearchParametersV1 searchParametersV1Mock, //
      String sortField, //
      SortDirection sortDirection) {

    verify(sortParametersMock).add(argThat(likeSortParameter(sortField, sortDirection)));
    verify(searchParametersV1Mock).setSortParameters(sortParametersMock);
  }

  @Test
  public void testAddToV1WithSortDirectionDesc() {
    // setup
    SearchParameters searchParameters = new SearchParameters().setSort(sortField);
    searchParameters.setSortDir("desc");

    // action
    instance.addToV1(searchParameters, searchParametersV1Mock);

    // verify
    verifyThatAListOfSortParametersIsAddedToSearchParametersV1( //
        searchParametersV1Mock, //
        sortField, // 
        SortDirection.DESCENDING);
  }

  @Test
  public void testAddToV1WithSortDirectionAsc() {
    // setup
    SearchParameters searchParameters = new SearchParameters().setSort(sortField);
    searchParameters.setSortDir("asc");

    // action
    instance.addToV1(searchParameters, searchParametersV1Mock);

    // verify
    verifyThatAListOfSortParametersIsAddedToSearchParametersV1( //
        searchParametersV1Mock, //
        sortField, // 
        SortDirection.ASCENDING);
  }

  @Test
  public void testAddToV1WithoutSortField() {
    // setup
    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setSortDir("asc");

    // action
    instance.addToV1(searchParameters, searchParametersV1Mock);

    // verify
    verify(searchParametersV1Mock).setSortParameters(sortParametersMock);
    verifyZeroInteractions(sortParametersMock);
  }
}
