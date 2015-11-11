package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.tinkerpop.blueprints.Vertex;

public class VertexMatcher extends CompositeMatcher<Vertex> {
  private VertexMatcher() {}

  public static VertexMatcher likeVertex() {
    return new VertexMatcher();
  }

  public VertexMatcher withId(String id) {
    addMatcher(new PropertyEqualityMatcher<Vertex, String>(Entity.DB_ID_PROP_NAME, id) {

      @Override
      protected String getItemValue(Vertex item) {
        return item.getProperty(Entity.DB_ID_PROP_NAME);
      }
    });

    return this;
  }
}
