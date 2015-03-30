package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;

import org.neo4j.graphdb.PropertyContainer;

public class PropertyContainerHelper {
  /**
   * Get the revision property of the property container.
   * @param propertyContainer the property container to get the revision from
   * @return 0 if the property container is null or does not have the property, 
   * else it returns the property.
   */
  public static int getRevisionProperty(PropertyContainer propertyContainer) {
    return propertyContainer != null && propertyContainer.hasProperty(REVISION_PROPERTY_NAME) ? //
    (int) propertyContainer.getProperty(REVISION_PROPERTY_NAME)
        : 0;
  }
}
