package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import org.apache.tinkerpop.gremlin.structure.Vertex;

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
}
