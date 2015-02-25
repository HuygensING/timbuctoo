package nl.knaw.huygens.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public abstract class PropertyMatcher<T, V> extends TypeSafeMatcher<T> {

  protected String propertyName;
  protected Matcher<V> matcher;

  public PropertyMatcher(String propertyName, Matcher<V> matcher) {
    this.propertyName = propertyName;
    this.matcher = matcher;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(propertyName) //
        .appendText(" matches ");
    matcher.describeTo(description);
  }

  @Override
  protected void describeMismatchSafely(T item, Description mismatchDescription) {
    mismatchDescription.appendText(propertyName);
    matcher.describeMismatch(getItemValue(item), mismatchDescription);
  }

  @Override
  protected boolean matchesSafely(T item) {
    return matcher.matches(getItemValue(item));
  }

  protected abstract V getItemValue(T item);

}