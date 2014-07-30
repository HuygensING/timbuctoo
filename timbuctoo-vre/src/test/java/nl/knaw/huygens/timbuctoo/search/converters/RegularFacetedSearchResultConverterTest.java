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

import static nl.knaw.huygens.timbuctoo.search.converters.SearchResultMatcher.likeSearchResult;
import static org.hamcrest.MatcherAssert.assertThat;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import org.junit.Test;

public class RegularFacetedSearchResultConverterTest extends FacetedSearchResultConverterTestBase {

  @Test
  public void testConvert() {
    // setup
    FacetedSearchResultConverter instance = new RegularFacetedSearchResultConverter();

    // action
    SearchResult actualSearchResult = instance.convert(TYPE_STRING, facetedSearchResult);

    // verify
    assertThat(actualSearchResult, likeSearchResult(TYPE_STRING, IDS, SEARCH_TERM, SORT, FACETS));
  }
}
