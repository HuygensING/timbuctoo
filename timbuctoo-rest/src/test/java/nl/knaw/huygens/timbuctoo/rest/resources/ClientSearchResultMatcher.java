package nl.knaw.huygens.timbuctoo.rest.resources;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.ClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

public abstract class ClientSearchResultMatcher<T extends ClientSearchResult> extends TypeSafeMatcher<T> {

  protected final Set<String> sortableFields;
  protected final int numFound;
  protected final List<? extends DomainEntity> results;
  protected final List<String> ids;
  protected final int start;
  protected final int rows;
  protected final String nextLink;
  protected final String prevLink;

  public ClientSearchResultMatcher(int numFound, //
      List<String> ids, //
      List<? extends DomainEntity> results, //
      int start, //
      int rows, //
      Set<String> sortableFields, //
      String nextLink, //
      String prevLink) {
    this.numFound = numFound;
    this.ids = ids;
    this.results = results;
    this.start = start;
    this.rows = rows;
    this.sortableFields = sortableFields;
    this.nextLink = nextLink;
    this.prevLink = prevLink;
  }

  @Override
  protected void describeMismatchSafely(T item, Description mismatchDescription) {
    super.describeMismatchSafely(item, mismatchDescription);
  }

  @Override
  protected boolean matchesSafely(T item) {
    boolean isEqual = Objects.equal(sortableFields, item.getSortableFields());
    isEqual &= Objects.equal(numFound, item.getNumFound());
    isEqual &= Objects.equal(ids, item.getIds());
    isEqual &= Objects.equal(results, item.getResults());
    isEqual &= Objects.equal(start, item.getStart());
    isEqual &= Objects.equal(rows, item.getRows());
    isEqual &= Objects.equal(sortableFields, item.getSortableFields());
    isEqual &= Objects.equal(nextLink, item.getNextLink());
    isEqual &= Objects.equal(prevLink, item.getPrevLink());

    return isEqual;
  }

  protected void addToDescription(Description description, String fieldName, Object value) {
    description.appendText(fieldName) //
        .appendText(" ")//
        .appendValue(value) //
        .appendText("\n");
  }

}
