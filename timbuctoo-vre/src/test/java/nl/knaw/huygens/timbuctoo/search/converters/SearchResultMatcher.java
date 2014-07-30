package nl.knaw.huygens.timbuctoo.search.converters;

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

  private SearchResultMatcher(String searchType, List<String> ids, String term, List<SortParameter> sort, List<Facet> facets) {
    this.searchType = searchType;
    this.ids = ids;
    this.term = term;
    this.sort = sort;
    this.facets = facets;
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
        .appendValue(searchType);

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
        .appendValue(item.getSearchType());
  }

  @Override
  protected boolean matchesSafely(SearchResult item) {
    boolean isEqual = Objects.equal(searchType, item.getSearchType());
    isEqual &= Objects.equal(term, item.getTerm());
    isEqual &= Objects.equal(sort, item.getSort());
    isEqual &= Objects.equal(facets, item.getFacets());
    isEqual &= Objects.equal(ids, item.getIds());

    return isEqual;
  }

  public static SearchResultMatcher likeSearchResult(String searchType, List<String> ids, String term, List<SortParameter> sort, List<Facet> facets) {
    return new SearchResultMatcher(searchType, ids, term, sort, facets);
  }

  public static SearchResultMatcher likeRelationSearchResult(String searchType, List<String> ids, String term, List<SortParameter> sort, List<Facet> facets, List<String> sourceIds,
      List<String> targetIds) {
    return new RelationSearchResultMatcher(searchType, ids, term, sort, facets, sourceIds, targetIds);
  }

  private static class RelationSearchResultMatcher extends SearchResultMatcher {
    private List<String> sourceIds;
    private List<String> targetIds;

    private RelationSearchResultMatcher(String searchType, List<String> ids, String term, List<SortParameter> sort, List<Facet> facets, List<String> sourceIds, List<String> targetIds) {
      super(searchType, ids, term, sort, facets);
      this.sourceIds = sourceIds;
      this.targetIds = targetIds;
    }

    @Override
    public void describeTo(Description description) {
      super.describeTo(description);
      description.appendText(" sourceIds ") //
          .appendValue(sourceIds) //
          .appendText(" targetIds ")//
          .appendValue(targetIds);

    }

    @Override
    protected void describeMismatchSafely(SearchResult item, Description mismatchDescription) {
      // TODO Auto-generated method stub
      super.describeMismatchSafely(item, mismatchDescription);
      mismatchDescription.appendText(" sourceIds ") //
          .appendValue(item.getSourceIds()) //
          .appendText(" targetIds ")//
          .appendValue(item.getTargetIds());
    }

    @Override
    protected boolean matchesSafely(SearchResult item) {
      boolean isEqual = super.matchesSafely(item);
      isEqual &= Objects.equal(sourceIds, item.getSourceIds());
      isEqual &= Objects.equal(targetIds, item.getTargetIds());
      return isEqual;
    }
  }

}
