package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.crud.changelistener.AddLabelChangeListener;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.HAS_NEXT_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_INITIAL_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_PROPERTY_RELATION_NAME;
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

  // Use for testing only
  Collection(String vreName, Vertex vertex, GraphWrapper graphWrapper, CollectionDescription collectionDescription) {
    this(vreName, vertex, graphWrapper, collectionDescription, new PropertyHelper());
  }

  // Use for testing only
  Collection(String vreName, Vertex vertex, GraphWrapper graphWrapper,
             CollectionDescription collectionDescription, PropertyHelper propertyHelper) {
    this.vreName = vreName;
    this.vertex = vertex;
    this.graphWrapper = graphWrapper;
    this.collectionDescription = collectionDescription;
    this.propertyHelper = propertyHelper;

  }

  public void add(Vertex entityVertex, List<Collection> collections) {
    addToCollection(
      entityVertex,
      collectionDescription,
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

    // BEGIN CREATE COLLECTION
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
    String collectionPropertyName = getDescription().createPropertyName(propName);
    entityVertex.property(collectionPropertyName, value);

    Iterator<Vertex> vertices = vertex.vertices(Direction.OUT, HAS_PROPERTY_RELATION_NAME);

    addNewPropertyConfig(propName, collectionPropertyName, vertices);

  }

  private void addNewPropertyConfig(String propName, String collectionPropertyName, Iterator<Vertex> vertices) {

    if (!isKnownProperty(collectionPropertyName, vertices)) {
      Vertex newPropertyConfig = graphWrapper.getGraph().addVertex("property");
      newPropertyConfig.property("clientName", propName);
      newPropertyConfig.property("dbName", collectionPropertyName);
      newPropertyConfig.property("type", "text");

      vertex.addEdge(HAS_PROPERTY_RELATION_NAME, newPropertyConfig);
      if (!vertex.edges(Direction.OUT, HAS_INITIAL_PROPERTY_RELATION_NAME).hasNext()) {
        vertex.addEdge(HAS_INITIAL_PROPERTY_RELATION_NAME, newPropertyConfig);
      } else {
        Vertex lastProperty = getLastProperty();
        lastProperty.addEdge(HAS_NEXT_PROPERTY_RELATION_NAME, newPropertyConfig);
      }
    }
  }

  private boolean isKnownProperty(String collectionPropertyName, Iterator<Vertex> vertices) {
    for (; vertices.hasNext(); ) {
      Vertex relatedProperty = vertices.next();
      if (Objects.equals(relatedProperty.value("dbName"), collectionPropertyName)) {
        return true;
      }
    }
    return false;
  }

  private Vertex getLastProperty() {
    return graphWrapper.getGraph().traversal().V(vertex.id())
                       .out(HAS_INITIAL_PROPERTY_RELATION_NAME)
                       .until(__.not(__.outE(HAS_NEXT_PROPERTY_RELATION_NAME)))
                       .repeat(__.out(HAS_NEXT_PROPERTY_RELATION_NAME))
                       .next();
  }

  public void remove(Vertex entityVertex) {
    GraphTraversal<Vertex, Edge> edgeToRemove =
      graphWrapper.getGraph().traversal().V(vertex.id()).out(HAS_ENTITY_NODE_RELATION_NAME)
                  .outE(HAS_ENTITY_RELATION_NAME).where(__.inV().hasId(entityVertex.id()));
    if (edgeToRemove.hasNext()) {
      edgeToRemove.next().remove();
      propertyHelper.removeProperties(entityVertex, collectionDescription);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Collection) {
      Collection other = (Collection) obj;
      return Objects.equals(collectionDescription, other.collectionDescription);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return collectionDescription.hashCode();
  }
}
