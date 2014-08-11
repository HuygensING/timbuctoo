package nl.knaw.huygens.timbuctoo.search;

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

import nl.knaw.huygens.facetedsearch.model.FacetDefinition;
import nl.knaw.huygens.facetedsearch.model.FacetType;
import nl.knaw.huygens.facetedsearch.model.RangeFacetDefinition;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

public class FacetDefinitionMatcher extends TypeSafeMatcher<FacetDefinition> {

  private final String expectedName;
  private final String expectedTitle;
  private final FacetType expectedType;

  public FacetDefinitionMatcher(String name, String title, FacetType type) {
    expectedName = name;
    expectedTitle = title;
    expectedType = type;
  }

  @Override
  public void describeTo(Description description) {

    description.appendText("FacetDefinition with name ").appendValue(expectedName) //
        .appendText(" and title ").appendValue(expectedTitle) //
        .appendText(" and type ").appendValue(expectedType);
  }

  @Override
  protected void describeMismatchSafely(FacetDefinition item, Description mismatchDescription) {
    mismatchDescription.appendText("FacetDefinition with name ").appendValue(item.getName()) //
        .appendText(" and title ").appendValue(item.getTitle()) //
        .appendText(" and type ").appendValue(item.getType());
  }

  @Override
  protected boolean matchesSafely(FacetDefinition item) {

    boolean isEqual = Objects.equal(expectedName, item.getName());
    isEqual &= Objects.equal(expectedTitle, item.getTitle());
    isEqual &= Objects.equal(expectedType, item.getType());

    return isEqual;
  }

  public static FacetDefinitionMatcher matchesFacetDefinition(String name, String title, FacetType type) {
    return new FacetDefinitionMatcher(name, title, type);
  }

  public static RangeFacetDefinitionMatcher matchesRangeFacetDefinition(String name, String title, FacetType type, String lowerFieldName, String upperFieldName) {
    return new RangeFacetDefinitionMatcher(name, title, type, lowerFieldName, upperFieldName);
  }

  private static class RangeFacetDefinitionMatcher extends FacetDefinitionMatcher {

    private final String lowerFieldName;
    private final String upperFieldName;

    public RangeFacetDefinitionMatcher(String name, String title, FacetType type, String lowerFieldName, String upperFieldName) {
      super(name, title, type);
      this.lowerFieldName = lowerFieldName;
      this.upperFieldName = upperFieldName;
    }

    @Override
    protected boolean matchesSafely(FacetDefinition item) {
      if (!(item instanceof RangeFacetDefinition)) {
        return false;
      }
      boolean isEqual = super.matchesSafely(item);
      RangeFacetDefinition rangeFacetDefinition = (RangeFacetDefinition) item;

      isEqual &= Objects.equal(lowerFieldName, rangeFacetDefinition.getLowerLimitField());
      isEqual &= Objects.equal(upperFieldName, rangeFacetDefinition.getUpperLimitField());

      return isEqual;
    }

  }

}
