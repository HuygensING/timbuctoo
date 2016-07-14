package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.crud.changelistener.AddLabelChangeListener;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;

public class Collection {
  private final String vreName;
  private final Vertex vertex;
  private final GraphWrapper graphWrapper;
  private final PropertyHelper propertyHelper;
  private final CollectionDescription collectionDescription;

  public Collection(String vreName, Vertex vertex, GraphWrapper graphWrapper) {
    this(vreName, vertex, graphWrapper,
      new CollectionDescription(vertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME), vreName));
  }

  Collection(String vreName, Vertex vertex, GraphWrapper graphWrapper, CollectionDescription collectionDescription) {

    this.vreName = vreName;
    this.vertex = vertex;
    this.graphWrapper = graphWrapper;
    this.collectionDescription = collectionDescription;
    this.propertyHelper = new PropertyHelper();
  }

  public void add(Vertex entityVertex, List<Collection> collections) {
    addToCollection(
      entityVertex,
      new CollectionDescription(vertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME), vreName),
      collections);
  }

  public String getVreName() {
    return vreName;
  }

  public CollectionDescription getDescription() {
    return collectionDescription;
  }

  public Vertex getVertex() {
    return vertex;
  }

  private void addToCollection(Vertex entityVertex, CollectionDescription requestCollection,
                               List<Collection> entityCollections) {
    final Graph graph = graphWrapper.getGraph();

    // If the requested collection is the default collection and the entity is already in a collection: return
    // If the entity is already in the requested collection: return
    if ((Objects.equals(requestCollection.getEntityTypeName(), "unknown") && isInACollection(entityVertex)) ||
      isInCollection(entityVertex, requestCollection)) {
      return;
    }

    final CollectionDescription defaultCollectionDescription =
      CollectionDescription.getDefault(requestCollection.getVreName());

    // If the requested collection is NOT the default collection, but the entity is still in the default collection:
    // remove the entity from the default collection
    if (!Objects.equals(requestCollection.getEntityTypeName(), "unknown") &&
      isInCollection(entityVertex, defaultCollectionDescription)) {
      // FIXME remove from default collection should be part of Entity's addToCollection
      removeFromCollection(entityVertex, defaultCollectionDescription);
    }

    // BEGIN CREATE COLLECTION
    // final Vertex archetypeVertex = addCollectionToArchetype(graph, collectionVertex);
    final Vertex archetypeVertex =
      vertex.vertices(Direction.OUT, HAS_ARCHETYPE_RELATION_NAME).next();
    // END CREATE COLLECTION
    // BEGIN ADD ENTITY TO ARCHETYPE
    if (!isInCollection(entityVertex, new CollectionDescription("concept", "Admin"))) {
      addEntityVertexToArchetype(entityVertex, archetypeVertex);
    }
    // END ADD ENTITY TO ARCHETYPE
    // BEGIN ADD ENTITY TO COLLECTION
    // FIXME use Collection
    addEntityVertexToCollection(entityVertex, graph, vertex);
    // END ADD ENTITY TO COLLECTION
    // TODO *HERE SHOULD BE A COMMIT* (autocommit?)

    // BEGIN UPDATE ENTITY VERTEX TYPE INFORMATION
    // FIXME should be part of Entity
    addTypesPropertyToEntity(entityVertex, archetypeVertex, entityCollections
      .stream()
      .map(Collection::getDescription).collect(Collectors.toList()));

    // TODO *HERE SHOULD BE A COMMIT* (autocommit?)

    // FIXME should be part of Entity
    new AddLabelChangeListener().onUpdate(Optional.empty(), entityVertex);
    // END UPDATE ENTITY VERTEX TYPE INFORMATION
    // Add the properties of the VRE to the newly added collection
    propertyHelper.setCollectionProperties(entityVertex, requestCollection, entityCollections
      .stream()
      .map(Collection::getDescription).collect(Collectors.toList()));

    // TODO remove unknown properties?
  }

  private void addTypesPropertyToEntity(Vertex vertex,
                                        Vertex archetypeVertex,
                                        List<CollectionDescription> collectionDescriptions) {

    List<String> entityTypeNames = collectionDescriptions
      .stream().map(CollectionDescription::getEntityTypeName).collect(Collectors.toList());

    entityTypeNames
      .add(archetypeVertex.value(nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME));

    vertex.property("types", jsnA(entityTypeNames.stream().map(JsonBuilder::jsn)).toString());
  }

  private void addEntityVertexToArchetype(Vertex entityVertex, Vertex archetypeVertex) {
    archetypeVertex.vertices(Direction.OUT, HAS_ENTITY_NODE_RELATION_NAME)
                   .next().addEdge(HAS_ENTITY_RELATION_NAME, entityVertex);

  }

  private void addEntityVertexToCollection(Vertex vertex, Graph graph, Vertex collectionVertex) {
    Vertex containerVertex = graph.addVertex(nl.knaw.huygens.timbuctoo.model.vre.Collection.COLLECTION_ENTITIES_LABEL);
    collectionVertex.addEdge(HAS_ENTITY_NODE_RELATION_NAME, containerVertex);
    containerVertex.addEdge(HAS_ENTITY_RELATION_NAME, vertex);
  }


  private void removeFromCollection(Vertex vertex, CollectionDescription collectionDescription) {
    graphWrapper.getGraph().traversal().V(vertex.id()).inE(HAS_ENTITY_RELATION_NAME)
                .where(
                  __.outV().in(HAS_ENTITY_NODE_RELATION_NAME)
                    .has(ENTITY_TYPE_NAME_PROPERTY_NAME, collectionDescription.getEntityTypeName())
                    .in(Vre.HAS_COLLECTION_RELATION_NAME)
                    .has(Vre.VRE_NAME_PROPERTY_NAME, collectionDescription.getVreName())
                )
                .next().remove();
  }

  private boolean isInCollection(Vertex vertex, CollectionDescription collectionDescription) {
    return graphWrapper.getGraph().traversal().V(vertex.id())
                       .in(HAS_ENTITY_RELATION_NAME)
                       .in(HAS_ENTITY_NODE_RELATION_NAME)
                       .has(ENTITY_TYPE_NAME_PROPERTY_NAME, collectionDescription.getEntityTypeName())
                       .hasNext();
  }

  private boolean isInACollection(Vertex vertex) {
    return graphWrapper.getGraph().traversal().V(vertex.id())
                       .in(HAS_ENTITY_RELATION_NAME)
                       .in(HAS_ENTITY_NODE_RELATION_NAME).hasNext();
  }

  public void addProperty(Vertex entityVertex, String propName, String value) {
    entityVertex.property(getDescription().createPropertyName(propName), value);
  }

  public String createPropertyName(String propertyName) {
    return getDescription().createPropertyName(propertyName);
  }
}
