package nl.knaw.huygens.timbuctoo.search.converters;

import nl.knaw.huygens.facetedsearch.model.parameters.SortDirection;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

public class SortParameterMatcher extends TypeSafeMatcher<SortParameter> {

  private String name;
  private SortDirection sortDirection;

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
    return Objects.equal(name, item.getFieldname()) && Objects.equal(sortDirection, item.getDirection());
  }

  public static SortParameterMatcher likeSortParameter(String name, SortDirection sortDirection) {
    return new SortParameterMatcher(name, sortDirection);
  }

}
