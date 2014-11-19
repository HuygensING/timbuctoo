package nl.knaw.huygens.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

// Property matchers
public abstract class PropertyMatcher<T> extends TypeSafeMatcher<T> {
  private final String propertyName;
  private final String propertyValue;

  public PropertyMatcher(String propertyName, String propertyValue) {
    this.propertyName = propertyName;
    this.propertyValue = propertyValue;
  }

  @Override
  public void describeTo(Description description) {
    describeField(description, propertyName, propertyValue);
  }

  @Override
  protected void describeMismatchSafely(T item, Description mismatchDescription) {
    describeField(mismatchDescription, propertyName, getItemValue(item));
    mismatchDescription.appendText(" instead of: ").appendValue(propertyValue);
  }

  protected void describeField(Description description, String fieldName, Object value) {
    description.appendText(fieldName).appendText(" has value: ").appendValue(value);
  }

  @Override
  protected boolean matchesSafely(T item) {
    return Objects.equal(propertyValue, getItemValue(item));
  }

  protected abstract String getItemValue(T item);
}