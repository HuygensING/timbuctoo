package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.tinkerpop.gremlin.structure.Vertex;

class PropertyHelper {

  public static final String ALT_LABEL = "altLabel";

  public void removeProperties(Vertex entityVertex, CollectionDescription collectionToRemove) {
    entityVertex.properties().forEachRemaining(prop -> {
      if (hasCollectionPrefix(prop.key(), collectionToRemove)) {
        prop.remove();
      }
    });
  }

  public void movePropertiesToNewCollection(Vertex entityVertex, Collection oldCollection, Collection newCollection) {
    entityVertex.properties().forEachRemaining(prop -> {
      oldCollection
        .getUnprefixedProperty(prop.key())
        .ifPresent(unprefixedPropertyName -> {
          if (unprefixedPropertyName.equals(ALT_LABEL)) {
            newCollection.addToListProperty(entityVertex, unprefixedPropertyName, (String) prop.value());
          } else {
            newCollection.addProperty(entityVertex, unprefixedPropertyName, (String) prop.value());
          }
        });
    });
  }

  private boolean hasCollectionPrefix(String propertyName, CollectionDescription collectionDescription) {
    return propertyName.startsWith(collectionDescription.getPrefix() + "_");
  }

}
