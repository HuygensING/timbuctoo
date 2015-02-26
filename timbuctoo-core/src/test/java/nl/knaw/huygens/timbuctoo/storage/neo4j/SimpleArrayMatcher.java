package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.Matchers.arrayContaining;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SimpleArrayMatcher<T> extends TypeSafeMatcher<T[]> {

  private final Class<T> componentType;
  private Matcher<T[]> arrayContainingMatcher;
  private T[] values;

  private SimpleArrayMatcher(Class<T> type) {
    this.componentType = type;
  }

  public static <U> SimpleArrayMatcher<U> isSimpleArrayOfType(Class<U> componentType) {
    return new SimpleArrayMatcher<U>(componentType);
  }

  public SimpleArrayMatcher<T> withValues(@SuppressWarnings("unchecked") T... values) {
    this.values = values;
    arrayContainingMatcher = arrayContaining(values);
    return this;
  }

  @Override
  public void describeTo(Description description) {
    describe(description, componentType, values);
  }

  @Override
  protected void describeMismatchSafely(T[] item, Description mismatchDescription) {
    describe(mismatchDescription, item.getClass().getComponentType(), item);
  }

  private void describe(Description description, Class<?> type, T[] values) {
    description.appendText("An array of type ")//
        .appendValue(type)//
        .appendText(" and with values: ")//
        .appendValue(values);
  }

  @Override
  protected boolean matchesSafely(T[] item) {
    if (item.getClass().getComponentType() != componentType) {
      return false;
    }
    return arrayContainingMatcher.matches(item);
  }
}
