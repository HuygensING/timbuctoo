package nl.knaw.huygens.hamcrest;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.internal.ReflectiveTypeFinder;

import com.google.common.collect.Lists;

public class CompositeMatcher<T> extends TypeSafeMatcher<T> {

  private List<TypeSafeMatcher<T>> matchers;
  private List<TypeSafeMatcher<T>> failed;

  public CompositeMatcher() {
    matchers = Lists.newArrayList();
    failed = Lists.newArrayList();
  }

  public CompositeMatcher(ReflectiveTypeFinder typeFinder) {
    super(typeFinder);
  }

  @Override
  public void describeTo(Description description) {
    description.appendList("(", " " + "and" + " ", ")", getMatchers());
  }

  @Override
  protected void describeMismatchSafely(T item, Description mismatchDescription) {
    for (final TypeSafeMatcher<T> matcher : getFailed()) {
      matcher.describeMismatch(item, mismatchDescription);
    }
  }

  @Override
  protected boolean matchesSafely(T item) {
    for (TypeSafeMatcher<T> matcher : getMatchers()) {
      if (!matcher.matches(item)) {
        addFailedMatcher(matcher);
      }
    }
    return hasFailed();
  }

  protected List<TypeSafeMatcher<T>> getMatchers() {
    return matchers;
  }

  protected List<TypeSafeMatcher<T>> getFailed() {
    return failed;
  }

  protected boolean hasFailed() {
    return getFailed().isEmpty();
  }

  protected boolean addFailedMatcher(TypeSafeMatcher<T> matcher) {
    return getFailed().add(matcher);
  }

  protected void addMatcher(TypeSafeMatcher<T> propertyMatcher) {
    getMatchers().add(propertyMatcher);
  }

}