package nl.knaw.huygens.hamcrest;

import com.google.common.base.Joiner;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ListContainsItemsInAnyOrderMatcher<T> extends TypeSafeMatcher<List<T>> {
  private final List<T> variations;
  private List<T> nonMatchingItems;
  private List<T> extraFound;

  private ListContainsItemsInAnyOrderMatcher(List<T> variations) {
    this.variations = variations;
  }

  public static <E> ListContainsItemsInAnyOrderMatcher<E> containsInAnyOrder(List<E> variations) {
    return new ListContainsItemsInAnyOrderMatcher<E>(variations);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("Contains in any order ").appendValue(variations);
  }

  @Override
  protected void describeMismatchSafely(List<T> item, Description mismatchDescription) {
    if (nonMatchingItems != null) {
      mismatchDescription.appendText("No matchers found for ");
      mismatchDescription.appendText(Joiner.on(", ").join(nonMatchingItems));
    } else {
      mismatchDescription.appendText("Extra items found ");
      mismatchDescription.appendText(Joiner.on(", ").join(extraFound));
    }
  }

  @Override
  protected boolean matchesSafely(List<T> item) {
    if (item.containsAll(variations)) {
      if (variations.containsAll(item)) {
        return true;
      }

      extraFound = item.stream().filter(variation -> !variations.contains(variation)).collect(toList());

      return false;
    }

    nonMatchingItems = variations.stream().filter(variation -> !item.contains(variation)).collect(toList());

    return false;
  }


}
