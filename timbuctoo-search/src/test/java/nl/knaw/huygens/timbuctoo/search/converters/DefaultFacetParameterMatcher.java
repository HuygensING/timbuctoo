package nl.knaw.huygens.timbuctoo.search.converters;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetParameter;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

public class DefaultFacetParameterMatcher extends TypeSafeMatcher<DefaultFacetParameter> {

  private String name;
  private List<String> values;

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
  protected boolean matchesSafely(DefaultFacetParameter item) {
    boolean isEqual = Objects.equal(name, item.getName());
    isEqual &= Objects.equal(values, item.getValues());

    return isEqual;
  }

  public static DefaultFacetParameterMatcher likeFacetParameter(String name, List<String> values) {
    return new DefaultFacetParameterMatcher(name, values);
  }

}
