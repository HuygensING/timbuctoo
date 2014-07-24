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

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetParameter;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

public class DefaultFacetParameterMatcher extends TypeSafeMatcher<FacetParameter> {

  private final String name;
  private final List<String> values;

  public DefaultFacetParameterMatcher(String name, List<String> values) {
    this.name = name;
    this.values = values;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("DefaultFacetParameter with name") //
        .appendValue(name) //
        .appendText("values") //
        .appendValue(values);

  }

  @Override
  protected boolean matchesSafely(FacetParameter item) {
    if (!(item instanceof DefaultFacetParameter)) {
      return false;
    }

    DefaultFacetParameter other = (DefaultFacetParameter) item;

    boolean isEqual = Objects.equal(name, other.getName());
    isEqual &= Objects.equal(values, other.getValues());

    return isEqual;
  }

  public static DefaultFacetParameterMatcher likeFacetParameter(String name, List<String> values) {
    return new DefaultFacetParameterMatcher(name, values);
  }

}
