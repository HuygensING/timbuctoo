package nl.knaw.huygens.timbuctoo.util;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

public class VertexPropertyMatcher extends CompositeMatcher<VertexProperty> {

  private VertexPropertyMatcher() {

  }

  public static VertexPropertyMatcher likeVertexProperty() {
    return new VertexPropertyMatcher();
  }

  public VertexPropertyMatcher withKey(String key) {
    this.addMatcher(new PropertyEqualityMatcher<VertexProperty, String>("key", key) {
      @Override
      protected String getItemValue(VertexProperty item) {
        return item.key();
      }
    });
    return this;
  }

  public VertexPropertyMatcher withValue(Object value) {
    this.addMatcher(new PropertyEqualityMatcher<VertexProperty, Object>("value", value) {
      @Override
      protected Object getItemValue(VertexProperty item) {
        return item.value();
      }
    });
    return this;
  }
}
