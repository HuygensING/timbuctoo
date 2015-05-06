package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_DB_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

public class ElementHelper {
  /**
   * Get the revision property of the graph element.
   * @param element the graph element to get the revision from
   * @return 0 if the element is null or does not have the property, 
   * else it returns the property.
   */
  public static int getRevisionProperty(Element element) {
    return containsProperty(element, REVISION_PROPERTY_NAME) ? //
    (int) element.getProperty(REVISION_PROPERTY_NAME)
        : 0;
  }

  private static boolean containsProperty(Element element, String propertyName) {
    return element != null && element.getProperty(propertyName) != null;
  }

  public static String getIdProperty(Element element) {
    return containsProperty(element, ID_DB_PROPERTY_NAME) ? //
    (String) element.getProperty(ID_DB_PROPERTY_NAME) //
        : null;
  }

  public static Vertex sourceOfEdge(Edge edge) {
    return edge.getVertex(Direction.OUT);
  }

  public static Vertex targetOfEdge(Edge edge) {
    return edge.getVertex(Direction.IN);
  }
}
