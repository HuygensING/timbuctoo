package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.crud.changelistener.AddLabelChangeListener;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;

class CollectionMapper {

  private final GraphWrapper graphWrapper;
  private final PropertyHelper propertyHelper;

  public CollectionMapper(GraphWrapper graphWrapper) {
    this(graphWrapper, new PropertyHelper());
  }

  CollectionMapper(GraphWrapper graphWrapper, PropertyHelper propertyHelper) {
    this.graphWrapper = graphWrapper;
    this.propertyHelper = propertyHelper;
  }

  public void addToCollection(Vertex entityVertex, CollectionDescription collectionDescription) {
    final Graph graph = graphWrapper.getGraph();

    // If the requested collection is the default collection and the entity is already in a collection: return
    // If the entity is already in the requested collection: return
    if ((Objects.equals(collectionDescription.getEntityTypeName(), "unknown") && isInACollection(entityVertex)) ||
      isInCollection(entityVertex, collectionDescription)) {
      return;
    }

    final CollectionDescription defaultCollectionDescription =
      CollectionDescription.getDefault(collectionDescription.getVreName());

    // If the requested collection is NOT the default collection, but the entity is still in the default collection:
    // remove the entity from the default collection
    if (!Objects.equals(collectionDescription.getEntityTypeName(), "unknown") &&
      isInCollection(entityVertex, defaultCollectionDescription)) {
      removeFromCollection(entityVertex, defaultCollectionDescription);
    }

    final Vertex collectionVertex = findOrCreateCollectionVertex(collectionDescription, graph);
    if (!collectionVertex.vertices(Direction.IN, Vre.HAS_COLLECTION_RELATION_NAME).hasNext()) {
      addCollectionToVre(collectionDescription, graph, collectionVertex);
    }

    final Vertex archetypeVertex = addCollectionToArchetype(graph, collectionVertex);

    if (!isInCollection(entityVertex, new CollectionDescription("concept", "Admin"))) {
      addEntityVertexToArchetype(entityVertex, archetypeVertex);
    }

    addEntityVertexToCollection(entityVertex, graph, collectionVertex);

    // TODO *HERE SHOULD BE A COMMIT* (autocommit?)

    addTypesPropertyToEntity(entityVertex, collectionDescription, archetypeVertex);

    // TODO *HERE SHOULD BE A COMMIT* (autocommit?)

    new AddLabelChangeListener().onUpdate(Optional.empty(), entityVertex);

    propertyHelper.setCollectionProperties(entityVertex, collectionDescription,
      getCollectionDescriptions(entityVertex, collectionDescription.getVreName()));
  }

  private void addEntityVertexToArchetype(Vertex entityVertex, Vertex archetypeVertex) {
    archetypeVertex.vertices(Direction.OUT, Collection.HAS_ENTITY_NODE_RELATION_NAME)
                   .next().addEdge(Collection.HAS_ENTITY_RELATION_NAME, entityVertex);

  }

  private Vertex addCollectionToArchetype(Graph graph, Vertex collectionVertex) {

    final Vertex archetypeVertex = graph.traversal().V().hasLabel(Vre.DATABASE_LABEL)
                                        .has(Vre.VRE_NAME_PROPERTY_NAME, "Admin")
                                        .out(Vre.HAS_COLLECTION_RELATION_NAME)
                                        .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "concept")
                                        .next();
    if (!collectionVertex.vertices(Direction.OUT, Collection.HAS_ARCHETYPE_RELATION_NAME).hasNext()) {
      collectionVertex.addEdge(Collection.HAS_ARCHETYPE_RELATION_NAME, archetypeVertex);
    }
    return archetypeVertex;
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

  private void addTypesPropertyToEntity(Vertex vertex, CollectionDescription collectionDescription,
                                        Vertex archetypeVertex) {

    List<String> entityTypeNames =
      getCollectionDescriptions(vertex, collectionDescription.getVreName())
        .stream().map(CollectionDescription::getEntityTypeName).collect(Collectors.toList());

    entityTypeNames.add(archetypeVertex.value(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME));

    vertex.property("types", jsnA(entityTypeNames.stream().map(JsonBuilder::jsn)).toString());
  }


  private void addEntityVertexToCollection(Vertex vertex, Graph graph, Vertex collectionVertex) {
    Vertex containerVertex = graph.addVertex(Collection.COLLECTION_ENTITIES_LABEL);
    collectionVertex.addEdge(Collection.HAS_ENTITY_NODE_RELATION_NAME, containerVertex);
    containerVertex.addEdge(Collection.HAS_ENTITY_RELATION_NAME, vertex);
  }


  private void addCollectionToVre(CollectionDescription collectionDescription, Graph graph, Vertex collectionVertex) {
    Vertex vreVertex = graph.traversal().V()
                            .hasLabel(Vre.DATABASE_LABEL)
                            .has(Vre.VRE_NAME_PROPERTY_NAME, collectionDescription.getVreName())
                            .next();
    vreVertex.addEdge(Vre.HAS_COLLECTION_RELATION_NAME, collectionVertex);
  }


  private Vertex findOrCreateCollectionVertex(CollectionDescription collectionDescription, Graph graph) {
    Vertex collectionVertex;
    final GraphTraversal<Vertex, Vertex> colTraversal =
      graph.traversal().V().has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, collectionDescription.getEntityTypeName());

    if (colTraversal.hasNext()) {
      collectionVertex = colTraversal.next();
    } else {
      collectionVertex = graph.addVertex(Collection.DATABASE_LABEL);
    }

    collectionVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, collectionDescription.getCollectionName());
    collectionVertex.property(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, collectionDescription.getEntityTypeName());
    return collectionVertex;
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
