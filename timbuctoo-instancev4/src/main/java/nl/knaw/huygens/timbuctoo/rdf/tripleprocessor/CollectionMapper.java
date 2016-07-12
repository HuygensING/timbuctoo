package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Objects;

class CollectionMapper {

  private GraphWrapper graphWrapper;

  public CollectionMapper(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  public void addToCollection(Vertex vertex, CollectionDescription collectionDescription) {
    final Graph graph = graphWrapper.getGraph();

    if ((Objects.equals(collectionDescription.getEntityTypeName(), "unknown") && isInACollection(vertex)) ||
      isInCollection(vertex, collectionDescription)) {
      return;
    }

    final CollectionDescription defaultCollectionDescription =
      CollectionDescription.getDefault(collectionDescription.getVreName());

    if (!Objects.equals(collectionDescription.getEntityTypeName(), "unknown") &&
      isInCollection(vertex, defaultCollectionDescription)) {
      removeFromCollection(vertex, defaultCollectionDescription);
    }

    final GraphTraversal<Vertex, Vertex> colTraversal =
      graph.traversal().V().has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, collectionDescription.getEntityTypeName());
    Vertex collectionVertex;
    if (colTraversal.hasNext()) {
      collectionVertex = colTraversal.next();
    } else {
      collectionVertex = graph.addVertex(Collection.DATABASE_LABEL);
    }

    Vertex vreVertex = graph.traversal().V()
                            .hasLabel(Vre.DATABASE_LABEL)
                            .has(Vre.VRE_NAME_PROPERTY_NAME, collectionDescription.getVreName())
                            .next();
    collectionVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, collectionDescription.getCollectionName());
    collectionVertex.property(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, collectionDescription.getEntityTypeName());
    vreVertex.addEdge(Vre.HAS_COLLECTION_RELATION_NAME, collectionVertex);

    Vertex containerVertex = graph.addVertex(Collection.COLLECTION_ENTITIES_LABEL);
    collectionVertex.addEdge(Collection.HAS_ENTITY_NODE_RELATION_NAME, containerVertex);
    containerVertex.addEdge(Collection.HAS_ENTITY_RELATION_NAME, vertex);
  }

  public List<CollectionDescription> getCollectionDescriptions(Vertex vertex, String vreName) {
    return graphWrapper
      .getGraph().traversal()
      .V(vertex.id())
      .in(Collection.HAS_ENTITY_RELATION_NAME)
      .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
      .where(
        __.in(Vre.HAS_COLLECTION_RELATION_NAME)
          .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
      ).map(collectionT -> {
        return new CollectionDescription(collectionT.get().value(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME), vreName);
      }).toList();
  }


  private void removeFromCollection(Vertex vertex, CollectionDescription collectionDescription) {
    graphWrapper.getGraph().traversal().V(vertex.id()).inE(Collection.HAS_ENTITY_RELATION_NAME)
                .where(
                  __.outV().in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, collectionDescription.getEntityTypeName())
                    .in(Vre.HAS_COLLECTION_RELATION_NAME)
                    .has(Vre.VRE_NAME_PROPERTY_NAME, collectionDescription.getVreName())
                )
                .next().remove();
  }

  private boolean isInCollection(Vertex vertex, CollectionDescription collectionDescription) {
    return graphWrapper.getGraph().traversal().V(vertex.id())
                       .in(Collection.HAS_ENTITY_RELATION_NAME)
                       .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                       .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, collectionDescription.getEntityTypeName())
                       .hasNext();
  }

  private boolean isInACollection(Vertex vertex) {
    return graphWrapper.getGraph().traversal().V(vertex.id())
                       .in(Collection.HAS_ENTITY_RELATION_NAME)
                       .in(Collection.HAS_ENTITY_NODE_RELATION_NAME).hasNext();
  }
}
