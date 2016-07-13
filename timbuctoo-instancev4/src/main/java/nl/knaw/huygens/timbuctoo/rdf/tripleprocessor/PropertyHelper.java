package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.List;

class PropertyHelper {
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
      if (hasCollectionPrefix(prop.key(), defaultDesc)) {
        prop.remove();
      }
    });
  }

  private boolean hasCollectionPrefix(String propertyName, CollectionDescription defaultDesc) {
    return propertyName.startsWith(defaultDesc.getPrefix() + "_");
  }

  private String getUnprefixedPropertyName(String propertyName,
                                           List<CollectionDescription> allPossibleCollectionDescriptions) {
    for (CollectionDescription collectionDescription: allPossibleCollectionDescriptions) {
      final String prefix = collectionDescription.getPrefix() + "_";
      if (propertyName.startsWith(prefix)) {
        return propertyName.replaceFirst("^" + prefix, "");
      }
    }
    return null;
  }


}
