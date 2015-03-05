package nl.knaw.huygens.timbuctoo.storage.neo4j;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.neo4j.graphdb.PropertyContainer;

public class PropertyContainerTypeMatcher<T extends PropertyContainer> extends TypeSafeMatcher<Class<T>> {

  private Class<T> expectedType;

  public PropertyContainerTypeMatcher(Class<T> expectedType) {
    this.expectedType = expectedType;
  }

  public static <U extends PropertyContainer> PropertyContainerTypeMatcher<U> isPropertyContainerType(Class<U> expectedType) {
    return new PropertyContainerTypeMatcher<U>(expectedType);
  }

  @Override
  public void describeTo(Description description) {
    // TODO Auto-generated method stub

  }

  @Override
  protected boolean matchesSafely(Class<T> item) {
    return expectedType.isAssignableFrom(item);
  }

}
