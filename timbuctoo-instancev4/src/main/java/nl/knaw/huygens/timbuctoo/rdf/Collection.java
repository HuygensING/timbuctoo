package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.properties.RdfImportedDefaultDisplayname;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.core.CollectionNameHelper.defaultEntityTypeName;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.HAS_DISPLAY_NAME_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.HAS_INITIAL_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.HAS_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.HAS_NEXT_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.rdf.RdfProperties.RDF_URI_PROP;


public class Collection {
  private final String vreName;
  private final Vertex vertex;
  private final GraphWrapper graphWrapper;
  private final PropertyHelper propertyHelper;
  private final CollectionDescription collectionDescription;
  protected GraphTraversalSource traversal;
  protected Vertex entityNode;

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
    traversal = graphWrapper.getGraph().traversal();

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
    if (!isInCollection(entityVertex, this.collectionDescription)) {
      Vertex containerVertex = vertex.vertices(Direction.OUT, HAS_ENTITY_NODE_RELATION_NAME).next();
      containerVertex.addEdge(HAS_ENTITY_RELATION_NAME, entityVertex);

      propertyHelper.addCurrentProperties(entityVertex, collectionDescription);
    }
  }

  public void remove(Vertex entityVertex) {
    if (entityNode == null) {
      entityNode = vertex.vertices(Direction.OUT, HAS_ENTITY_NODE_RELATION_NAME).next();
    }
    Iterator<Edge> edges = entityVertex.edges(Direction.IN);
    while (edges.hasNext()) {
      Edge edge = edges.next();
      if (edge.outVertex().equals(entityNode)) {
        edge.remove();
        propertyHelper.removeProperties(entityVertex, collectionDescription);
        break;
      }
    }
  }

  public void copyFromPropertiesFrom(Vertex entityVertex, Collection fromCollection) {
    fromCollection.getPropertiesFor(entityVertex).forEach( property -> {
      addProperty(
        entityVertex,
        property.get(LocalProperty.CLIENT_PROPERTY_NAME),
        property.get("value"),
        property.get(LocalProperty.PROPERTY_TYPE_NAME)
      );
    });
  }

  public void addProperty(Vertex entityVertex, String propName, String value, String type) {
    if (!collectionDescription.getVreName().equals("Admin")) {
      String collectionPropertyName = getDescription().createPropertyName(propName);
      Iterator<Vertex> vertices = vertex.vertices(Direction.OUT, HAS_PROPERTY_RELATION_NAME);
      addNewPropertyConfig(propName, collectionPropertyName, vertices, type);
      entityVertex.property(collectionPropertyName, value);
    }
  }

  public List<Map<String, String>> getPropertiesFor(Vertex entityVertex) {
    final Iterator<Vertex> propertyConfigs = vertex.vertices(Direction.OUT, HAS_PROPERTY_RELATION_NAME);
    final List<Map<String, String>> properties = Lists.newArrayList();
    propertyConfigs.forEachRemaining(configVertex -> {
      final String collectionPropertyName = configVertex.<String>property(LocalProperty.DATABASE_PROPERTY_NAME).value();

      if (entityVertex.property(collectionPropertyName).isPresent()) {
        final String propertyType = configVertex.<String>property(LocalProperty.PROPERTY_TYPE_NAME).value();
        final String propName = configVertex.<String>property(LocalProperty.CLIENT_PROPERTY_NAME).value();
        final String propertyValue = entityVertex.<String>property(collectionPropertyName).value();
        final Map<String, String> propertyMap = Maps.newHashMap();

        propertyMap.put(LocalProperty.PROPERTY_TYPE_NAME, propertyType);
        propertyMap.put(LocalProperty.CLIENT_PROPERTY_NAME, propName);
        propertyMap.put(LocalProperty.DATABASE_PROPERTY_NAME, collectionPropertyName);
        propertyMap.put("value", propertyValue);
        properties.add(propertyMap);
      }
    });
    return properties;
  }


  public Optional<String> getPropertyType(String collectionPropertyName) {
    Iterator<Vertex> vertices = vertex.vertices(Direction.OUT, HAS_PROPERTY_RELATION_NAME);
    final Optional<Vertex> knownProperty = getKnownProperty(collectionPropertyName, vertices);
    if (knownProperty.isPresent() && knownProperty.get().property(LocalProperty.PROPERTY_TYPE_NAME).isPresent()) {
      return Optional.of(knownProperty.get().<String>property(LocalProperty.PROPERTY_TYPE_NAME).value());
    }
    return Optional.empty();
  }


  public Optional<Property> getProperty(Vertex entityVertex, String propName) {
    String collectionPropertyName = getDescription().createPropertyName(propName);
    if (entityVertex.property(collectionPropertyName).isPresent()) {
      return Optional.of(entityVertex.property(collectionPropertyName));
    }
    return Optional.empty();
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

  private void addNewPropertyConfig(String propName, String collectionPropertyName, Iterator<Vertex> vertices,
                                    String type) {

    final Optional<Vertex> knownProperty = getKnownProperty(collectionPropertyName, vertices);
    if (!knownProperty.isPresent()) {
      Vertex newPropertyConfig = graphWrapper.getGraph().addVertex("property");
      newPropertyConfig.property(LocalProperty.CLIENT_PROPERTY_NAME, propName);
      newPropertyConfig.property(LocalProperty.DATABASE_PROPERTY_NAME, collectionPropertyName);
      newPropertyConfig.property(LocalProperty.PROPERTY_TYPE_NAME, type);

      vertex.addEdge(HAS_PROPERTY_RELATION_NAME, newPropertyConfig);
      if (!vertex.edges(Direction.OUT, HAS_INITIAL_PROPERTY_RELATION_NAME).hasNext()) {
        vertex.addEdge(HAS_INITIAL_PROPERTY_RELATION_NAME, newPropertyConfig);
      } else {
        Vertex lastProperty = getLastProperty();
        lastProperty.addEdge(HAS_NEXT_PROPERTY_RELATION_NAME, newPropertyConfig);
      }
    } else {
      knownProperty.get().property(LocalProperty.PROPERTY_TYPE_NAME, type);
    }
  }

  private Optional<Vertex> getKnownProperty(String collectionPropertyName, Iterator<Vertex> vertices) {
    for (; vertices.hasNext(); ) {
      Vertex relatedProperty = vertices.next();
      if (Objects.equals(relatedProperty.value("dbName"), collectionPropertyName)) {
        return Optional.of(relatedProperty);
      }
    }
    return Optional.empty();
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

  @Override
  public String toString() {
    return "{Collection " + this.collectionDescription.getCollectionName() + "}";
  }


  public void removeProperty(Vertex vertex, String propertyName) {
    String collectionPropertyName = getDescription().createPropertyName(propertyName);
    vertex.property(collectionPropertyName).remove();
  }

  public Optional<String> getUnprefixedProperty(String propertyName) {
    if (propertyName.startsWith(collectionDescription.getPrefix() + "_")) {
      return Optional.of(propertyName.replaceFirst("^" + collectionDescription.getPrefix() + "_", ""));
    } else {
      return Optional.empty();
    }
  }


}
