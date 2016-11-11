package nl.knaw.huygens.timbuctoo.rdf;

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

}
