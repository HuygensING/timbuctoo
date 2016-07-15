package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.Objects;

import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.HAS_NEXT_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.COLLECTION_ENTITIES_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_INITIAL_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_PROPERTY_RELATION_NAME;


public class Collection {
  private final String vreName;
  private final Vertex vertex;
  private final GraphWrapper graphWrapper;
  private final PropertyHelper propertyHelper;
  private final CollectionDescription collectionDescription;
  private Collection archetype;

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

  public void add(Vertex entityVertex) {
    // If the requested collection is the default collection and the entity is already in a collection: return
    // If the entity is already in the requested collection: return
    if ((collectionDescription.equals(CollectionDescription.getDefault(vreName)) && isInACollection(entityVertex)) ||
      isInCollection(entityVertex, collectionDescription)) {

      return;
    }

    Vertex containerVertex = graphWrapper.getGraph().addVertex(COLLECTION_ENTITIES_LABEL);
    vertex.addEdge(HAS_ENTITY_NODE_RELATION_NAME, containerVertex);
    containerVertex.addEdge(HAS_ENTITY_RELATION_NAME, entityVertex);
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


  public void addProperty(Vertex entityVertex, String propName, String value) {
    String collectionPropertyName = getDescription().createPropertyName(propName);
    entityVertex.property(collectionPropertyName, value);

    Iterator<Vertex> vertices = vertex.vertices(Direction.OUT, HAS_PROPERTY_RELATION_NAME);

    addNewPropertyConfig(propName, collectionPropertyName, vertices);

  }

  public Collection getArchetype() {
    // TODO make field
    Vertex archetypeVertex = vertex.vertices(Direction.OUT, HAS_ARCHETYPE_RELATION_NAME).next();
    archetype = new Collection("Admin", archetypeVertex, graphWrapper);
    return archetype;
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

  private void addNewPropertyConfig(String propName, String collectionPropertyName, Iterator<Vertex> vertices) {

    if (!isKnownProperty(collectionPropertyName, vertices)) {
      Vertex newPropertyConfig = graphWrapper.getGraph().addVertex("property");
      newPropertyConfig.property(LocalProperty.CLIENT_PROPERTY_NAME, propName);
      newPropertyConfig.property(LocalProperty.DATABASE_PROPERTY_NAME, collectionPropertyName);
      newPropertyConfig.property(LocalProperty.PROPERTY_TYPE_NAME, "text");

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
