package nl.knaw.huygens.timbuctoo.rest.resources;

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
