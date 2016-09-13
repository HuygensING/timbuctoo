package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class PropertyHelper {

  public void removeProperties(Vertex entityVertex, CollectionDescription collectionToRemove) {
    entityVertex.properties().forEachRemaining(prop -> {
      if (hasCollectionPrefix(prop.key(), collectionToRemove)) {
        prop.remove();
      }
    });
  }

  public void setPropertiesForNewCollection(Vertex entityVertex, Collection newCollection,
                                            Set<Collection> collections) {
    CollectionDescription newCollectionDescription = newCollection.getDescription();
    final List<CollectionDescription> allPossibleCollectionDescriptions = new ArrayList<>();
    final CollectionDescription defaultDesc = CollectionDescription.getDefault(newCollectionDescription.getVreName());

    allPossibleCollectionDescriptions.addAll(
      collections.stream().map(Collection::getDescription).collect(Collectors.toList()));

    allPossibleCollectionDescriptions.add(defaultDesc);

    entityVertex.properties().forEachRemaining(prop -> {
      final String unprefixedPropertyName = getUnprefixedPropertyName(prop.key(), allPossibleCollectionDescriptions);
      if (unprefixedPropertyName != null && !newCollection.getVreName().equals("Admin")) {
        newCollection.addProperty(entityVertex, unprefixedPropertyName, (String) prop.value());
      }
    });
  }

  private boolean hasCollectionPrefix(String propertyName, CollectionDescription collectionDescription) {
    return propertyName.startsWith(collectionDescription.getPrefix() + "_");
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
}
