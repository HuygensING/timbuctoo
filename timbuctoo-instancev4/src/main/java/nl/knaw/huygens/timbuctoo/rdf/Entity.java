package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Set;

public class Entity {
  private final Vertex vertex;
  private final Set<Collection> collections;
  private final TypesHelper typesHelper;

  public Entity(Vertex vertex, Set<Collection> collections) {
    this(vertex, collections, new TypesHelper());
  }

  Entity(Vertex vertex, Set<Collection> collections, TypesHelper typesHelper) {
    this.vertex = vertex;
    this.collections = collections;
    this.typesHelper = typesHelper;
  }

  public void addProperty(String propertyName, String value) {
    collections.forEach(collection -> collection.addProperty(vertex, propertyName, value));
  }

  public void addToCollection(Collection collection) {
    collections.add(collection);
    collection.add(vertex, collections);

    Collection archetype = collection.getArchetype();
    collections.add(archetype);
    archetype.add(vertex, collections);

    typesHelper.updateTypeInformation(vertex, collections);
  }

  public Relation addRelation(RelationType relationType, Entity other) {
    Edge edge = vertex.addEdge(relationType.getRegularName(), other.vertex);

    return new Relation(edge, relationType);
  }

  public void removeFromCollection(Collection collection) {
    collection.remove(vertex);
    collections.remove(collection);
  }
}
