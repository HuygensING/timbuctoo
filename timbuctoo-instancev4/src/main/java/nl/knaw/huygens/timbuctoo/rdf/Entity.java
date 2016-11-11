package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.properties.converters.StringToStringConverter;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Entity {
  public final Vertex vertex;
  private final Set<Collection> collections;
  private final TypesHelper typesHelper;
  private final PropertyHelper propertyHelper;

  public Entity(Vertex vertex, Set<Collection> collections) {
    this(vertex, collections, new TypesHelper(), new PropertyHelper());
  }

  Entity(Vertex vertex, Set<Collection> collections, TypesHelper typesHelper, PropertyHelper propertyHelper) {
    this.vertex = vertex;
    this.collections = collections;
    this.typesHelper = typesHelper;
    this.propertyHelper = propertyHelper;
  }

  public void addProperty(String propertyName, String value, String type) {
    collections.forEach(collection -> collection.addProperty(vertex, propertyName, value, type));
  }

  public Optional<Property> getProperty(String unprefixedPropertyName) {
    for (Collection collection : collections) {
      Optional<Property> property = collection.getProperty(vertex, unprefixedPropertyName);
      if (property.isPresent()) {
        return property;
      }
    }
    return Optional.empty();
  }


  public List<Map<String, String>> getProperties() {
    List<Map<String, String>> properties = Lists.newArrayList();

    for (Collection collection : collections) {
      final List<Map<String, String>> collectionProperties = collection.getPropertiesFor(vertex);
      properties.addAll(collectionProperties);
    }

    return properties;
  }

  public Optional<String> getPropertyValue(String propertyName) {
    final Optional<Property> property = getProperty(propertyName);
    if (property.isPresent()) {
      return Optional.of((String) property.get().value());
    } else {
      return Optional.empty();
    }
  }

  public String getPropertyType(String unprefixedPropertyName) {
    final Optional<Property> property = getProperty(unprefixedPropertyName);
    if (property.isPresent()) {
      for (Collection collection : collections) {
        Optional<String> propertyType = collection.getPropertyType(property.get().key());
        if (propertyType.isPresent()) {
          return propertyType.get();
        }
      }
      // Cannot derive type from collection configuration, so default to string type
      return new StringToStringConverter().getUniqueTypeIdentifier();
    } else {
      // Cannot find property, so default to string type
      return new StringToStringConverter().getUniqueTypeIdentifier();
    }
  }



  public void removeProperty(String propertyName) {
    collections.forEach(collection -> collection.removeProperty(vertex, propertyName));
  }

  public void addToCollection(Collection newCollection) {
    handleCollectionAdd(newCollection);
    newCollection.getArchetype().ifPresent(this::handleCollectionAdd);
  }

  public void moveToCollection(Collection oldCollection, Collection newCollection) {
    addToCollection(newCollection);
    propertyHelper.movePropertiesToNewCollection(this, oldCollection, newCollection);
    newCollection.getArchetype().ifPresent(newArchetype -> {
      oldCollection.getArchetype().ifPresent(oldArchetype -> {
        propertyHelper.movePropertiesToNewCollection(this, oldArchetype, newArchetype);
      });
    });
    removeFromCollection(oldCollection);
  }

  private void handleCollectionAdd(Collection newCollection) {
    collections.add(newCollection);
    newCollection.add(vertex);

    typesHelper.updateTypeInformation(vertex, collections);
  }

  public Relation addRelation(RelationType relationType, Entity other) {
    Edge edge = vertex.addEdge(relationType.getRegularName(), other.vertex);

    return new Relation(edge, relationType);
  }

  public void removeFromCollection(Collection collection) {
    handleCollectionRemove(collection);
    collection.getArchetype().ifPresent(archetype -> {
      boolean lastCollectionOfArchetype = !collections.stream()
                                                      .filter(x -> archetype.equals(x.getArchetype().orElse(null)))
                                                      .findAny()
                                                      .isPresent();
      if (lastCollectionOfArchetype) {
        handleCollectionRemove(archetype);
      }
    });
  }

  private void handleCollectionRemove(Collection collection) {
    collection.remove(vertex);
    collections.remove(collection);
    typesHelper.updateTypeInformation(vertex, collections);
  }

  public void removeRelation(RelationType relationType, Entity other) {
    vertex.edges(Direction.BOTH, relationType.getRegularName()).forEachRemaining(edge -> {
      if (edge.inVertex().id().equals(vertex.id()) && edge.outVertex().id().equals(other.vertex.id())) {
        edge.remove();
      } else if (edge.inVertex().id().equals(other.vertex.id()) && edge.outVertex().id().equals(vertex.id())) {
        edge.remove();
      }
    });
  }
}
