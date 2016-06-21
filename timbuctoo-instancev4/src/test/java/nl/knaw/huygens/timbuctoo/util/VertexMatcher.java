package nl.knaw.huygens.timbuctoo.util;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;

public class VertexMatcher extends CompositeMatcher<Vertex> {
  private VertexMatcher() {

  }

  public static VertexMatcher likeVertex() {
    return new VertexMatcher();
  }

  public VertexMatcher withType(String type) {
    this.addMatcher(new PropertyMatcher<Vertex, String>("types", containsString(type)) {
      @Override
      protected String getItemValue(Vertex item) {
        return item.value("types");
      }
    });
    return this;
  }

  public VertexMatcher withTimId(String timId) {
    this.addMatcher(new PropertyEqualityMatcher<Vertex, String>("timId", timId) {
      @Override
      protected String getItemValue(Vertex item) {
        return item.value("tim_id");
      }
    });

    return this;
  }

  public VertexMatcher withoutProperty(String propName) {
    this.addMatcher(new WithoutPropertyMatcher(propName));

    return this;
  }

  private static class WithoutPropertyMatcher extends TypeSafeMatcher<Vertex> {
    private final String propertyName;

    public WithoutPropertyMatcher(String propertyName) {

      this.propertyName = propertyName;
    }

    @Override
    protected boolean matchesSafely(Vertex vertex) {

      return !vertex.keys().contains(propertyName);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("vertex does not contain property ")
                 .appendValue(propertyName);
    }

    @Override
    protected void describeMismatchSafely(Vertex item, Description mismatchDescription) {
      mismatchDescription.appendText("vertex with properties ")
                         .appendValue(item.keys())
                         .appendText(" contains property ")
                         .appendValue(propertyName);
    }
  }
}
