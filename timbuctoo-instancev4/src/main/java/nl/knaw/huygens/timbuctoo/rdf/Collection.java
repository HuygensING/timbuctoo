package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.properties.RdfImportedDefaultDisplayname;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.model.properties.converters.StringToStringConverter;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.HAS_NEXT_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_DISPLAY_NAME_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_INITIAL_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.rdf.Database.RDF_URI_PROP;


public class Collection {
  private final String vreName;
  private final Vertex vertex;
  private final GraphWrapper graphWrapper;
  private final PropertyHelper propertyHelper;
  private final CollectionDescription collectionDescription;

  public Collection(String vreName, Vertex vertex, GraphWrapper graphWrapper) {
    this(vreName, vertex, graphWrapper, CollectionDescription.fromVertex(vreName, vertex));
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

    findOrCreateDisplayName();
  }

  private void findOrCreateDisplayName() {
    if (!vertex.vertices(Direction.OUT, HAS_DISPLAY_NAME_RELATION_NAME).hasNext()) {
      final Vertex displayName = graphWrapper.getGraph().addVertex(ReadableProperty.DATABASE_LABEL);
      displayName.property(ReadableProperty.CLIENT_PROPERTY_NAME, "@displayName");
      displayName.property(LocalProperty.DATABASE_PROPERTY_NAME, "rdfUri");
      displayName.property(ReadableProperty.PROPERTY_TYPE_NAME, RdfImportedDefaultDisplayname.TYPE);
      vertex.addEdge(HAS_DISPLAY_NAME_RELATION_NAME, displayName);
    }
  }

  public void add(Vertex entityVertex) {
    // If the requested collection is the default collection and the entity is already in a collection: return
    // If the entity is already in the requested collection: return
    if ((collectionDescription.equals(CollectionDescription.getDefault(vreName)) && isInACollection(entityVertex)) ||
      isInCollection(entityVertex, collectionDescription)) {

      return;
    }

    Vertex containerVertex = vertex.vertices(Direction.OUT, HAS_ENTITY_NODE_RELATION_NAME).next();
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

  public Optional<Collection> getArchetype() {
    Iterator<Vertex> archetypeVertex = vertex.vertices(Direction.OUT, HAS_ARCHETYPE_RELATION_NAME);
    if (archetypeVertex.hasNext()) {
      Vertex next = archetypeVertex.next();
      return Optional.of(new Collection("Admin", next, graphWrapper, CollectionDescription.getAdmin(next)));
    }
    return Optional.empty();
  }

  public void setArchetype(Collection archetypeCollection, String originalArchetypeUri) {
    vertex.edges(Direction.OUT, HAS_ARCHETYPE_RELATION_NAME).forEachRemaining(Element::remove);
    Edge edge = vertex.addEdge(HAS_ARCHETYPE_RELATION_NAME, archetypeCollection.vertex);
    edge.property(RDF_URI_PROP, originalArchetypeUri);
    copyDisplayNameFromArchetype(archetypeCollection);
  }

  private void copyDisplayNameFromArchetype(Collection archetypeCollection) {
    final Iterator<Vertex> archetypeDisplayNameT = archetypeCollection
      .vertex.vertices(Direction.OUT, HAS_DISPLAY_NAME_RELATION_NAME);

    if (archetypeDisplayNameT.hasNext()) {
      final Vertex archetypeDisplayName = archetypeDisplayNameT.next();
      vertex.edges(Direction.OUT, HAS_DISPLAY_NAME_RELATION_NAME).forEachRemaining(Element::remove);
      final Vertex displayName = graphWrapper.getGraph().addVertex(ReadableProperty.DATABASE_LABEL);
      archetypeDisplayName.properties().forEachRemaining(archetypeDisplayNameProp -> {
        final String value = (String) archetypeDisplayNameProp.value();
        final String key = archetypeDisplayNameProp.key();
        if (key.equals(LocalProperty.DATABASE_PROPERTY_NAME)) {
          // FIXME: string manipulation to get unprefixed property name
          final String newValue = value.replaceAll("^.+_", "");
          displayName.property(key, collectionDescription.getEntityTypeName() + "_" + newValue);
        } else {
          displayName.property(key, value);
        }
      });
      vertex.addEdge(HAS_DISPLAY_NAME_RELATION_NAME, displayName);
    }
  }

  public String getVreName() {
    return vreName;
  }

  public CollectionDescription getDescription() {
    return collectionDescription;
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
      newPropertyConfig
        .property(LocalProperty.PROPERTY_TYPE_NAME, new StringToStringConverter().getUniqueTypeIdentifier());

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
