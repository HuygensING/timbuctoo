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

import nl.knaw.huygens.facetedsearch.model.parameters.SortDirection;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

public class SortParameterMatcher extends TypeSafeMatcher<SortParameter> {

  private final String name;
  private final SortDirection sortDirection;

  public SortParameterMatcher(String name, SortDirection sortDirection) {
    this.name = name;
    this.sortDirection = sortDirection;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("SortParameter with name") //
        .appendValue(name) //
        .appendText("sortDirection") //
        .appendValue(sortDirection);

  }

  @Override
  protected boolean matchesSafely(SortParameter item) {
    return Objects.equal(name, item.getFieldName()) && Objects.equal(sortDirection, item.getDirection());
  }

  public static SortParameterMatcher likeSortParameter(String name, SortDirection sortDirection) {
    return new SortParameterMatcher(name, sortDirection);
  }

}
