package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
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

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.ClientRelationRepresentation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationClientSearchResult;

import org.hamcrest.Description;

import com.google.common.base.Objects;

public class RelationClientSearchResultMatcher extends ClientSearchResultMatcher<RelationClientSearchResult> {

  private final List<ClientRelationRepresentation> refs;

  private RelationClientSearchResultMatcher(int numFound, List<String> ids, List<? extends DomainEntity> results, int start, int rows, Set<String> sortableFields, String nextLink, String prevLink,
      List<ClientRelationRepresentation> refs) {
    super(numFound, ids, results, start, rows, sortableFields, nextLink, prevLink);
    this.refs = refs;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("RelationClientSearchResult with \n");

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

  @Override
  protected void describeMismatchSafely(RelationClientSearchResult item, Description mismatchDescription) {
    mismatchDescription.appendText("RelationClientSearchResult with \n");

    addToDescription(mismatchDescription, "numFound", item.getNumFound());
    addToDescription(mismatchDescription, "ids", item.getIds());
    addToDescription(mismatchDescription, "refs", item.getRefs());
    addToDescription(mismatchDescription, "results", item.getResults());
    addToDescription(mismatchDescription, "start", item.getStart());
    addToDescription(mismatchDescription, "rows", item.getRows());
    addToDescription(mismatchDescription, "sortableFields", item.getSortableFields());
    addToDescription(mismatchDescription, "nextLink", item.getNextLink());
    addToDescription(mismatchDescription, "prevLink", item.getPrevLink());
  }

  @Override
  protected boolean matchesSafely(RelationClientSearchResult item) {
    boolean isEqual = super.matchesSafely(item);
    isEqual &= Objects.equal(refs, item.getRefs());

    return isEqual;
  }

  public static RelationClientSearchResultMatcherBuilder newClientSearchResultMatcher() {
    return new RelationClientSearchResultMatcherBuilder();
  }

  public static class RelationClientSearchResultMatcherBuilder {

    private int numFound;
    private List<String> ids;
    private List<ClientRelationRepresentation> refs;
    private List<? extends DomainEntity> results;
    private int start;
    private int rows;
    private Set<String> sortableFields;
    private String nextLink;
    private String prevLink;

    public RelationClientSearchResultMatcherBuilder withNumFound(int numFound) {
      this.numFound = numFound;
      return this;
    }

    public RelationClientSearchResultMatcherBuilder withIds(List<String> ids) {
      this.ids = ids;
      return this;
    }

    public RelationClientSearchResultMatcherBuilder withRefs(List<ClientRelationRepresentation> refs) {
      this.refs = refs;
      return this;
    }

    public RelationClientSearchResultMatcherBuilder withResults(List<? extends DomainEntity> results) {
      this.results = results;
      return this;
    }

    public RelationClientSearchResultMatcherBuilder withStart(int start) {
      this.start = start;
      return this;
    }

    public RelationClientSearchResultMatcherBuilder withRows(int rows) {
      this.rows = rows;
      return this;
    }

    public RelationClientSearchResultMatcherBuilder withSortableFields(Set<String> sortableFields) {
      this.sortableFields = sortableFields;
      return this;
    }

    public RelationClientSearchResultMatcherBuilder withNextLink(String nextLink) {
      this.nextLink = nextLink;
      return this;
    }

    public RelationClientSearchResultMatcherBuilder withPrevLink(String prevLink) {
      this.prevLink = prevLink;
      return this;
    }

    public RelationClientSearchResultMatcher build() {
      return new RelationClientSearchResultMatcher(numFound, ids, results, start, rows, sortableFields, nextLink, prevLink, refs);
    }
  }

}
