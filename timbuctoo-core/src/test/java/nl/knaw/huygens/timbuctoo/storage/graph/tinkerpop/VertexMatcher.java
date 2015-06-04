package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.tinkerpop.blueprints.Vertex;

public class VertexMatcher extends CompositeMatcher<Vertex> {
  private VertexMatcher() {}

  public static VertexMatcher likeVertex() {
    return new VertexMatcher();
  }

  public VertexMatcher withId(String id) {
    addMatcher(new PropertyEqualtityMatcher<Vertex, String>(Entity.ID_DB_PROPERTY_NAME, id) {

      @Override
      protected String getItemValue(Vertex item) {
        return item.getProperty(Entity.ID_DB_PROPERTY_NAME);
      }
    });

    return this;
  }
}
