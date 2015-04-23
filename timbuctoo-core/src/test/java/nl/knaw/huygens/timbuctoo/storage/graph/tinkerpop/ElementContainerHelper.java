package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;

import org.neo4j.graphdb.PropertyContainer;

public class ElementContainerHelper {
  /**
   * Get the revision property of the property container.
   * @param propertyContainer the property container to get the revision from
   * @return 0 if the property container is null or does not have the property, 
   * else it returns the property.
   */
  public static int getRevisionProperty(PropertyContainer propertyContainer) {
    return containsProperty(propertyContainer, REVISION_PROPERTY_NAME) ? //
    (int) propertyContainer.getProperty(REVISION_PROPERTY_NAME)
        : 0;
  }

  private static boolean containsProperty(PropertyContainer propertyContainer, String propertyName) {
    return propertyContainer != null && propertyContainer.hasProperty(propertyName);
  }

  public static String getIdProperty(PropertyContainer propertyContainer) {
    return containsProperty(propertyContainer, ID_PROPERTY_NAME) ? //
    (String) propertyContainer.getProperty(ID_PROPERTY_NAME) //
        : null;
  }
}
