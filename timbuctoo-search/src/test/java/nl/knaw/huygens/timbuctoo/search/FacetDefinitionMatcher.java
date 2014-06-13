package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.facetedsearch.model.FacetDefinition;
import nl.knaw.huygens.facetedsearch.model.FacetType;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

public class FacetDefinitionMatcher extends TypeSafeMatcher<FacetDefinition> {

  private String expectedName;
  private String expectedTitle;
  private FacetType expectedType;

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

}
