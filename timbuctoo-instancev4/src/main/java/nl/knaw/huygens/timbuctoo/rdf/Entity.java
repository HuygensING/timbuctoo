package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

public class Entity {
  private final Vertex vertex;
  private final List<Collection> collections;

  public Entity(Vertex vertex, List<Collection> collections) {
    this.vertex = vertex;
    this.collections = collections;
  }

  public void addProperty(String propertyName, String value) {
    collections.forEach(collection -> collection.addProperty(vertex, propertyName, value));
  }

  public void addToCollection(Collection collection) {
    collections.add(collection);
    collection.add(vertex, collections);
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
