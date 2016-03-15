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

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

public class SearchResultMatcher extends TypeSafeMatcher<SearchResult> {

  private String searchType;
  private List<String> ids;
  private String term;
  private List<SortParameter> sort;
  private List<Facet> facets;
  private String vreId;

  private SearchResultMatcher(String searchType, List<String> ids, String term, List<SortParameter> sort, List<Facet> facets, String vreId) {
    this.searchType = searchType;
    this.ids = ids;
    this.term = term;
    this.sort = sort;
    this.facets = facets;
    this.vreId = vreId;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("SearchResult with ids ") //
        .appendValue(ids) //
        .appendText(" term ") //
        .appendValue(term) //
        .appendText(" sort ") //
        .appendValue(sort) //
        .appendText(" facets ") //
        .appendValue(facets) //
        .appendText(" searchType ") //
        .appendValue(searchType) //
        .appendText(" vreId ") //
        .appendValue(vreId);

  }

  @Override
  protected void describeMismatchSafely(SearchResult item, Description mismatchDescription) {
    mismatchDescription.appendText("SearchResult with ids ") //
        .appendValue(item.getIds()) //
        .appendText(" term ") //
        .appendValue(item.getTerm()) //
        .appendText(" sort ") //
        .appendValue(item.getSort()) //
        .appendText(" facets ") //
        .appendValue(item.getFacets()) //
        .appendText(" searchType ") //
        .appendValue(item.getSearchType()) //
        .appendText(" vreId ") //
        .appendValue(item.getVreId());
  }

  @Override
  protected boolean matchesSafely(SearchResult item) {
    boolean isEqual = Objects.equal(searchType, item.getSearchType());
    isEqual &= Objects.equal(term, item.getTerm());
    isEqual &= Objects.equal(sort, item.getSort());
    isEqual &= Objects.equal(facets, item.getFacets());
    isEqual &= Objects.equal(ids, item.getIds());
    isEqual &= Objects.equal(vreId, item.getVreId());

    return isEqual;
  }

  public static SearchResultMatcher likeSearchResult(String searchType, List<String> ids, String term, List<SortParameter> sort, List<Facet> facets, String vreId) {
    return new SearchResultMatcher(searchType, ids, term, sort, facets, vreId);
  }

}
