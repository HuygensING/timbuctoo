package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.core.CollectionNameHelper;
import org.apache.tinkerpop.gremlin.structure.Vertex;

class PropertyHelper {

  public void removeProperties(Vertex entityVertex, CollectionDescription collectionToRemove) {
    entityVertex.properties().forEachRemaining(prop -> {
      if (hasCollectionPrefix(prop.key(), collectionToRemove)) {
        prop.remove();
      }
    });
  }

  public void movePropertiesToNewCollection(Entity entity, Collection oldCollection, Collection newCollection) {
    entity.vertex.properties().forEachRemaining(prop -> {
      oldCollection
        .getUnprefixedProperty(prop.key())
        .ifPresent(unprefixedPropertyName -> {
          newCollection.addProperty(entity.vertex, unprefixedPropertyName, (String) prop.value(),
            entity.getPropertyType(unprefixedPropertyName));
        });
    });
  }

  private boolean hasCollectionPrefix(String propertyName, CollectionDescription collectionDescription) {
    return propertyName.startsWith(collectionDescription.getPrefix() + "_");
  }

  public void addCurrentProperties(Vertex entityVertex, CollectionDescription collectionDescription) {
    String defaultPrefix = CollectionNameHelper.defaultEntityTypeName(collectionDescription.getVreName());
    String collectionPrefix = collectionDescription.getPrefix();
    entityVertex.properties().forEachRemaining(prop -> {
      if (prop.label().startsWith(defaultPrefix)) {
        String newLabel = collectionPrefix + prop.label().substring(defaultPrefix.length());
        entityVertex.property(newLabel, prop.value());
      }
    });
  }
}
