package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

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

  public Optional<String> getProperty(String propertyName) {
    for (Collection collection : collections) {
      Optional<String> propertyValue = collection.getProperty(vertex, propertyName);
      if (propertyValue.isPresent()) {
        return propertyValue;
      }
    }
    return Optional.empty();
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
    propertyHelper.movePropertiesToNewCollection(vertex, oldCollection, newCollection);
    newCollection.getArchetype().ifPresent(newArchetype -> {
      oldCollection.getArchetype().ifPresent(oldArchetype -> {
        propertyHelper.movePropertiesToNewCollection(vertex, oldArchetype, newArchetype);
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
