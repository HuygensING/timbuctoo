package nl.knaw.huygens.timbuctoo.server.rest;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;

import java.util.List;

public class SearchResponseV2_1Matcher extends CompositeMatcher<SearchResponseV2_1> {
  private SearchResponseV2_1Matcher() {

  }

  public static SearchResponseV2_1Matcher likeSearchResponse() {
    return new SearchResponseV2_1Matcher();
  }

  public SearchResponseV2_1Matcher withFullTextSearchFields(List<String> fullTextSearchFields) {
    this.addMatcher(
      new PropertyEqualityMatcher<SearchResponseV2_1, List<String>>("fullTextSearchFields", fullTextSearchFields) {
        @Override
        protected List<String> getItemValue(SearchResponseV2_1 item) {
          return item.getFullTextSearchFields();
        }
      });

    return this;
  }

  public SearchResponseV2_1Matcher withSortableFields(List<String> sortableFields) {
    this.addMatcher(new PropertyEqualityMatcher<SearchResponseV2_1, List<String>>("sortableFields", sortableFields) {
      @Override
      protected List<String> getItemValue(SearchResponseV2_1 item) {
        return item.getSortableFields();
      }
    });
    return this;
  }

  public SearchResponseV2_1Matcher withRefs(List<EntityRef> refs) {
    this.addMatcher(new PropertyEqualityMatcher<SearchResponseV2_1, List<EntityRef>>("refs", refs) {
      @Override
      protected List<EntityRef> getItemValue(SearchResponseV2_1 item) {
        return item.getRefs();
      }
    });
    return this;
  }
}
