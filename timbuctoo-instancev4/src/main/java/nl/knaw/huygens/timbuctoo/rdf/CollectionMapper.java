package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Objects;

class CollectionMapper {

  private GraphWrapper graphWrapper;

  public CollectionMapper(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  public void addToCollection(Vertex vertex, String entityTypeName) {
    final Graph graph = graphWrapper.getGraph();

    if ((Objects.equals(entityTypeName, "unknown") && isInACollection(vertex)) ||
      isInCollection(vertex, entityTypeName)) {
      return;
    }

    if (!Objects.equals(entityTypeName, "unknown") && isInCollection(vertex, "unknown")) {
      removeFromCollection(vertex, "unknown");
    }

    final GraphTraversal<Vertex, Vertex> colTraversal =
      graph.traversal().V().has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName);
    Vertex collectionVertex;
    if (colTraversal.hasNext()) {
      collectionVertex = colTraversal.next();
    } else {
      collectionVertex = graph.addVertex(Collection.DATABASE_LABEL);
    }
    Vertex containerVertex = graph.addVertex(Collection.COLLECTION_ENTITIES_LABEL);
    collectionVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, entityTypeName + "s");
    collectionVertex.property(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName);

    collectionVertex.addEdge(Collection.HAS_ENTITY_NODE_RELATION_NAME, containerVertex);
    containerVertex.addEdge(Collection.HAS_ENTITY_RELATION_NAME, vertex);
  }

  private void removeFromCollection(Vertex vertex, String entityTypeName) {
    graphWrapper.getGraph().traversal().V(vertex.id()).inE(Collection.HAS_ENTITY_RELATION_NAME)
                .where(__.outV().in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                         .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName)).next()
                .remove();
  }

  private boolean isInCollection(Vertex vertex, String entityTypeName) {
    return graphWrapper.getGraph().traversal().V(vertex.id())
                       .in(Collection.HAS_ENTITY_RELATION_NAME)
                       .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                       .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName).hasNext();
  }

  private boolean isInACollection(Vertex vertex) {
    return graphWrapper.getGraph().traversal().V(vertex.id())
                       .in(Collection.HAS_ENTITY_RELATION_NAME)
                       .in(Collection.HAS_ENTITY_NODE_RELATION_NAME).hasNext();
  }
}
