package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

public class Entity {
  private final Vertex vertex;
  private final List<CollectionDescription> collections;

  public Entity(Vertex vertex, List<CollectionDescription> collections) {
    this.vertex = vertex;
    this.collections = collections;
  }

  public void addProperty(String propertyName, String value) {
    collections.forEach(collectionDescription -> vertex.property(
      collectionDescription.createPropertyName(propertyName), value));
  }

  public void addToCollection(Collection collection) {
    collections.add(collection.getDescription());
    collection.add(vertex);
  }

  public Relation addRelation(Node node, Entity other) {
    Edge edge = vertex.addEdge(node.getLocalName(), other.vertex);

    return new Relation(edge, node);
  }
}
