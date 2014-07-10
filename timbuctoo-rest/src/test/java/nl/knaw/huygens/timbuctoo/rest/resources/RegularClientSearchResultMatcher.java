package nl.knaw.huygens.timbuctoo.rest.resources;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.ClientEntityRepresentation;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

public class RegularClientSearchResultMatcher extends TypeSafeMatcher<RegularClientSearchResult> {

  private String term;
  private List<Facet> facets;
  private int numFound;
  private List<String> ids;
  private List<ClientEntityRepresentation> refs;
  private List<? extends DomainEntity> results;
  private int start;
  private int rows;
  private Set<String> sortableFields;
  private String nextLink;
  private String prevLink;

  private RegularClientSearchResultMatcher( //
      String term, //
      List<Facet> facets, //
      int numFound, //
      List<String> ids, //
      List<ClientEntityRepresentation> refs, //
      List<? extends DomainEntity> results, //
      int start, //
      int rows, //
      Set<String> sortableFields, //
      String nextLink, //
      String prevLink) {
    this.term = term;
    this.facets = facets;
    this.numFound = numFound;
    this.ids = ids;
    this.refs = refs;
    this.results = results;
    this.start = start;
    this.rows = rows;
    this.sortableFields = sortableFields;
    this.nextLink = nextLink;
    this.prevLink = prevLink;

  }

  @Override
  public void describeTo(Description description) {
    description.appendText("ClientSearchResult with \n");

    addToDescription(description, "term", term);
    addToDescription(description, "facets", facets);
    addToDescription(description, "numFound", numFound);
    addToDescription(description, "ids", ids);
    addToDescription(description, "refs", refs);
    addToDescription(description, "results", results);
    addToDescription(description, "start", start);
    addToDescription(description, "rows", rows);
    addToDescription(description, "sortableFields", sortableFields);
    addToDescription(description, "nextLink", nextLink);
    addToDescription(description, "prevLink", prevLink);
  }

  private void addToDescription(Description description, String fieldName, Object value) {
    description.appendText(fieldName) //
        .appendText(" ")//
        .appendValue(value) //
        .appendText("\n");
  }

  @Override
  protected void describeMismatchSafely(RegularClientSearchResult item, Description mismatchdescription) {
    mismatchdescription.appendText("ClientSearchResult with \n");

    addToDescription(mismatchdescription, "term", item.getTerm());
    addToDescription(mismatchdescription, "facets", item.getFacets());
    addToDescription(mismatchdescription, "numFound", item.getNumFound());
    addToDescription(mismatchdescription, "ids", item.getIds());
    addToDescription(mismatchdescription, "refs", item.getRefs());
    addToDescription(mismatchdescription, "results", item.getResults());
    addToDescription(mismatchdescription, "start", item.getStart());
    addToDescription(mismatchdescription, "rows", item.getRows());
    addToDescription(mismatchdescription, "sortableFields", item.getSortableFields());
    addToDescription(mismatchdescription, "nextLink", item.getNextLink());
    addToDescription(mismatchdescription, "prevLink", item.getPrevLink());
  }

  @Override
  protected boolean matchesSafely(RegularClientSearchResult item) {
    boolean isEqual = Objects.equal(term, item.getTerm());
    isEqual &= Objects.equal(facets, item.getFacets());
    isEqual &= Objects.equal(numFound, item.getNumFound());
    isEqual &= Objects.equal(ids, item.getIds());
    isEqual &= Objects.equal(refs, item.getRefs());
    isEqual &= Objects.equal(results, item.getResults());
    isEqual &= Objects.equal(start, item.getStart());
    isEqual &= Objects.equal(rows, item.getRows());
    isEqual &= Objects.equal(sortableFields, item.getSortableFields());
    isEqual &= Objects.equal(nextLink, item.getNextLink());
    isEqual &= Objects.equal(prevLink, item.getPrevLink());

    return isEqual;
  }

  public static ClientSearchResultMatcherBuilder newClientSearchResultMatcher() {
    return new ClientSearchResultMatcherBuilder();
  }

  public static class ClientSearchResultMatcherBuilder {
    private String term;
    private List<Facet> facets;
    private int numFound;
    private List<String> ids;
    private List<ClientEntityRepresentation> refs;
    private List<? extends DomainEntity> results;
    private int start;
    private int rows;
    private Set<String> sortableFields;
    private String nextLink;
    private String prevLink;

    public ClientSearchResultMatcherBuilder withTerm(String term) {
      this.term = term;
      return this;
    }

    public ClientSearchResultMatcherBuilder withFacets(List<Facet> facets) {
      this.facets = facets;
      return this;
    }

    public ClientSearchResultMatcherBuilder withNumFound(int numFound) {
      this.numFound = numFound;
      return this;
    }

    public ClientSearchResultMatcherBuilder withIds(List<String> ids) {
      this.ids = ids;
      return this;
    }

    public ClientSearchResultMatcherBuilder withRefs(List<ClientEntityRepresentation> refs) {
      this.refs = refs;
      return this;
    }

    public ClientSearchResultMatcherBuilder withResults(List<? extends DomainEntity> results) {
      this.results = results;
      return this;
    }

    public ClientSearchResultMatcherBuilder withStart(int start) {
      this.start = start;
      return this;
    }

    public ClientSearchResultMatcherBuilder withRows(int rows) {
      this.rows = rows;
      return this;
    }

    public ClientSearchResultMatcherBuilder withSortableFields(Set<String> sortableFields) {
      this.sortableFields = sortableFields;
      return this;
    }

    public ClientSearchResultMatcherBuilder withNextLink(String nextLink) {
      this.nextLink = nextLink;
      return this;
    }

    public ClientSearchResultMatcherBuilder withPrevLink(String prevLink) {
      this.prevLink = prevLink;
      return this;
    }

    public RegularClientSearchResultMatcher build() {
      return new RegularClientSearchResultMatcher(term, facets, numFound, ids, refs, results, start, rows, sortableFields, nextLink, prevLink);
    }
  }
}
