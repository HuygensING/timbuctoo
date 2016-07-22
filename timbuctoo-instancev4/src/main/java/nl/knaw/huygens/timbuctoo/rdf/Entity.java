package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;
import java.util.Set;

public class Entity {
  private final Vertex vertex;
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

  public void addProperty(String propertyName, String value) {
    collections.forEach(collection -> collection.addProperty(vertex, propertyName, value));
  }

  public void addToCollection(Collection newCollection) {
    collections.add(newCollection);
    newCollection.add(vertex);

    // TODO move to the caller of this method
    Optional<Collection> archetypeOptional = newCollection.getArchetype();
    if (archetypeOptional.isPresent()) {
      Collection archetype = archetypeOptional.get();
      collections.add(archetype);
      archetype.add(vertex);
    }

    typesHelper.updateTypeInformation(vertex, collections);
    propertyHelper.setPropertiesForNewCollection(vertex, newCollection, collections);
  }

  public Relation addRelation(RelationType relationType, Entity other) {
    Edge edge = vertex.addEdge(relationType.getRegularName(), other.vertex);

    return new Relation(edge, relationType);
  }

  public void removeFromCollection(Collection collection) {
    collection.remove(vertex);
    collections.remove(collection);
    typesHelper.updateTypeInformation(vertex, collections);
  }
}
