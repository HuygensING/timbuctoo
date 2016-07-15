package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.List;

class PropertyHelper {
  // Adds the properties of the current Vre to the new Collection
  public void setCollectionProperties(Vertex vertex, CollectionDescription newCollectionDescription,
                                      List<CollectionDescription> existingCollectionDescriptions) {
    final List<CollectionDescription> allPossibleCollectionDescriptions = new ArrayList<>();
    final CollectionDescription defaultDesc = CollectionDescription.getDefault(newCollectionDescription.getVreName());

    allPossibleCollectionDescriptions.addAll(existingCollectionDescriptions);
    allPossibleCollectionDescriptions.add(defaultDesc);

    vertex.properties().forEachRemaining(prop -> {
      final String unprefixedPropertyName = getUnprefixedPropertyName(prop.key(), allPossibleCollectionDescriptions);
      if (unprefixedPropertyName != null) {
        vertex.property(newCollectionDescription.createPropertyName(unprefixedPropertyName), prop.value());
      }

    });
  }

  private boolean hasCollectionPrefix(String propertyName, CollectionDescription defaultDesc) {
    return propertyName.startsWith(defaultDesc.getPrefix() + "_");
  }

  private String getUnprefixedPropertyName(String propertyName,
                                           List<CollectionDescription> allPossibleCollectionDescriptions) {
    for (CollectionDescription collectionDescription : allPossibleCollectionDescriptions) {
      final String prefix = collectionDescription.getPrefix() + "_";
      if (propertyName.startsWith(prefix)) {
        return propertyName.replaceFirst("^" + prefix, "");
      }
    }
    return null;
  }

  public void removeProperties(Vertex entityVertex, CollectionDescription collectionToRemove) {
    entityVertex.properties().forEachRemaining(prop -> {
      if (hasCollectionPrefix(prop.key(), collectionToRemove)) {
        prop.remove();
      }
    });
  }
}
