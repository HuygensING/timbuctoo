package nl.knaw.huygens.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;

public abstract class LabelEqualityMatcher<T, V> extends org.hamcrest.TypeSafeMatcher<T> {

  private final String expectedLabel;
  private final Matcher<String> matcher;

  public LabelEqualityMatcher(String expectedLabel) {
    this.expectedLabel = expectedLabel;
    this.matcher = equalTo(expectedLabel);

  }

  @Override
  protected boolean matchesSafely(T item) {
    boolean matches = matcher.matches(getItemValue(item));
    return matches;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("label") //
               .appendText(" has value ");
    matcher.describeTo(description);
  }

  @Override
  protected void describeMismatchSafely(T item, Description mismatchDescription) {
    mismatchDescription.appendText("label").appendText(" ");
    matcher.describeMismatch(getItemValue(item), mismatchDescription);
  }

  protected abstract V getItemValue(T item);

}
