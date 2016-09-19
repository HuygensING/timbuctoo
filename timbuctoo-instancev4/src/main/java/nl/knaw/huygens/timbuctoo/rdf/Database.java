package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.Clock;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.COLLECTION_ENTITIES_LABEL;
import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.COLLECTION_IS_UNKNOWN_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.IS_RELATION_COLLECTION_PROPERTY_NAME;

public class Database {
  static final String RDF_URI_PROP = "rdfUri";
  private static final int ENTITY_CACHE_SIZE = 1024 * 1024;
  private final TinkerpopGraphManager graphWrapper;
  private final SystemPropertyModifier systemPropertyModifier;
  private Cache<String, Long> entityCache = CacheBuilder.newBuilder().maximumSize(ENTITY_CACHE_SIZE).build();

  public Database(TinkerpopGraphManager graphWrapper) {
    this(graphWrapper, new SystemPropertyModifier(Clock.systemDefaultZone()));
  }

  Database(TinkerpopGraphManager graphWrapper, SystemPropertyModifier systemPropertyModifier) {
    this.graphWrapper = graphWrapper;
    this.systemPropertyModifier = systemPropertyModifier;
  }

  private String getNodeUri(Node node, String vreName) {
    if (!node.isBlank()) {
      return node.getURI();
    }
    return String.format("%s:%s", vreName, node.getBlankNodeId());
  }

  public Entity findOrCreateEntity(String vreName, Node node) {
    String nodeUri = getNodeUri(node, vreName);

    Entity entity = findEntity(vreName, nodeUri);
    if (entity == null) {
      entity = createEntity(vreName, nodeUri);
    }
    return entity;
  }

  private Entity findEntity(String vreName, String nodeUri) {
    Graph graph = graphWrapper.getGraph();
    Long vertexId = entityCache.getIfPresent(nodeUri);
    if (vertexId != null) {
      final Vertex vertex = graph.traversal().V(vertexId).next();
      return new Entity(vertex, getCollections(vertex, vreName));
    } else {
      final GraphTraversal<Vertex, Vertex> existingT = graph.traversal().V()
                                                            .hasLabel(Vre.DATABASE_LABEL)
                                                            .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
                                                            .out(Vre.HAS_COLLECTION_RELATION_NAME)
                                                            .out(HAS_ENTITY_NODE_RELATION_NAME)
                                                            .out(HAS_ENTITY_RELATION_NAME)
                                                            .has(RDF_URI_PROP, nodeUri);

      if (existingT.hasNext()) {
        Vertex vertex = existingT.next();
        vertexId = (Long) vertex.id();
        entityCache.put(nodeUri, vertexId);

        return new Entity(vertex, getCollections(vertex, vreName));
      }
    }
    return null;
  }

  private Entity createEntity(String vreName, String nodeUri) {
    Vertex vertex = graphWrapper.getGraph().addVertex();
    vertex.property(RDF_URI_PROP, nodeUri);

    systemPropertyModifier.setCreated(vertex, "rdf-importer");
    systemPropertyModifier.setModified(vertex, "rdf-importer");
    systemPropertyModifier.setTimId(vertex);
    systemPropertyModifier.setRev(vertex, 1);
    systemPropertyModifier.setIsLatest(vertex, true);
    systemPropertyModifier.setIsDeleted(vertex, false);

    entityCache.put(nodeUri, (Long) vertex.id());

    Collection collection = findOrCreateCollection(CollectionDescription.getDefault(vreName));
    Entity entity = new Entity(vertex, getCollections(vertex, vreName));
    entity.addToCollection(collection);
    Optional<Collection> archetype = collection.getArchetype();
    if (archetype.isPresent()) {
      entity.addToCollection(archetype.get());
    }

    return entity;
  }

  private Set<Collection> getCollections(Vertex foundVertex, String vreName) {
    return graphWrapper
      .getGraph().traversal()
      .V(foundVertex.id())
      .in(HAS_ENTITY_RELATION_NAME)
      .in(HAS_ENTITY_NODE_RELATION_NAME)
      .where(
        __.in(Vre.HAS_COLLECTION_RELATION_NAME)
          .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
      ).map(collectionT -> {
        return new Collection(vreName, collectionT.get(), graphWrapper);
      }).toSet();
  }

  public Collection getDefaultCollection(String vreName) {
    return findOrCreateCollection(CollectionDescription.getDefault(vreName));
  }

  public Collection findOrCreateCollection(String vreName, Node node) {
    CollectionDescription collectionDescription =
      CollectionDescription.createCollectionDescription(node.getLocalName(), vreName, node.getURI());
    return findOrCreateCollection(collectionDescription);
  }


  public Collection findOrCreateCollection(CollectionDescription collectionDescription) {
    Graph graph = graphWrapper.getGraph();
    final GraphTraversal<Vertex, Vertex> colTraversal =
      graph.traversal().V()
           .hasLabel(Vre.DATABASE_LABEL)
           .has(Vre.VRE_NAME_PROPERTY_NAME, collectionDescription.getVreName())
           .out(Vre.HAS_COLLECTION_RELATION_NAME)
           .has(RDF_URI_PROP, collectionDescription.getRdfUri());

    Vertex collectionVertex;
    if (colTraversal.hasNext()) {
      collectionVertex = colTraversal.next();
    } else {
      collectionVertex = graph.addVertex(DATABASE_LABEL);
      collectionVertex.property(COLLECTION_NAME_PROPERTY_NAME, collectionDescription.getCollectionName());
      collectionVertex.property(ENTITY_TYPE_NAME_PROPERTY_NAME, collectionDescription.getEntityTypeName());
      collectionVertex.property(RDF_URI_PROP, collectionDescription.getRdfUri());
      collectionVertex.property(IS_RELATION_COLLECTION_PROPERTY_NAME, false);
      collectionVertex.property(COLLECTION_IS_UNKNOWN_PROPERTY_NAME, collectionDescription.isUnknown());

      Vertex containerVertex = graphWrapper.getGraph().addVertex(COLLECTION_ENTITIES_LABEL);
      collectionVertex.addEdge(HAS_ENTITY_NODE_RELATION_NAME, containerVertex);

      if (!collectionVertex.vertices(Direction.IN, Vre.HAS_COLLECTION_RELATION_NAME).hasNext()) {
        addCollectionToVre(collectionDescription, collectionVertex);
      }
      addCollectionToArchetype(collectionVertex);
    }

    return new Collection(collectionDescription.getVreName(), collectionVertex, graphWrapper);
  }

  public Collection getConcepts() {
    return findArchetypeCollection("concepts").get();
  }

  public Optional<Collection> findArchetypeCollection(String name) {
    CollectionDescription collectionDescription = CollectionDescription.createForAdmin(name);
    Graph graph = graphWrapper.getGraph();
    final GraphTraversal<Vertex, Vertex> colTraversal =
      graph.traversal().V()
           .hasLabel(Vre.DATABASE_LABEL)
           .has(Vre.VRE_NAME_PROPERTY_NAME, collectionDescription.getVreName())
           .out(Vre.HAS_COLLECTION_RELATION_NAME)
           .has(ENTITY_TYPE_NAME_PROPERTY_NAME, collectionDescription.getEntityTypeName());

    Vertex collectionVertex;
    if (colTraversal.hasNext()) {
      collectionVertex = colTraversal.next();

      return Optional.of(new Collection(collectionDescription.getVreName(), collectionVertex, graphWrapper));
    } else {
      return Optional.empty();
    }
  }

  public RelationType findOrCreateRelationType(Node predicate) {
    final GraphTraversal<Vertex, Vertex> relationTypeT =
      graphWrapper.getGraph().traversal().V().hasLabel("relationtype").has(RDF_URI_PROP, predicate.getURI());

    if (relationTypeT.hasNext()) {
      return new RelationType(relationTypeT.next());
    }

    final String relationTypePrefix = "relationtype_";
    final Vertex relationTypeVertex = graphWrapper.getGraph().addVertex("relationtype");

    relationTypeVertex.property(RDF_URI_PROP, predicate.getURI());
    relationTypeVertex.property("types", "[\"relationtype\"]");
    relationTypeVertex.property(relationTypePrefix + "targetTypeName", "concept");
    relationTypeVertex.property(relationTypePrefix + "sourceTypeName", "concept");
    relationTypeVertex.property(relationTypePrefix + "symmetric", false);
    relationTypeVertex.property(relationTypePrefix + "reflexive", false);
    relationTypeVertex.property(relationTypePrefix + "derived", false);
    relationTypeVertex.property(relationTypePrefix + "regularName", predicate.getLocalName());
    relationTypeVertex.property(relationTypePrefix + "inverseName", "inverse:" + predicate.getLocalName());

    systemPropertyModifier.setTimId(relationTypeVertex);
    systemPropertyModifier.setCreated(relationTypeVertex, "rdf-importer");
    systemPropertyModifier.setModified(relationTypeVertex, "rdf-importer");
    systemPropertyModifier.setIsLatest(relationTypeVertex, true);
    systemPropertyModifier.setRev(relationTypeVertex, 1);

    return new RelationType(relationTypeVertex);
  }

  private Vertex addCollectionToArchetype(Vertex collectionVertex) {

    final Vertex archetypeVertex = graphWrapper
      .getGraph().traversal().V().hasLabel(Vre.DATABASE_LABEL)
      .has(Vre.VRE_NAME_PROPERTY_NAME, "Admin")
      .out(Vre.HAS_COLLECTION_RELATION_NAME)
      .has(ENTITY_TYPE_NAME_PROPERTY_NAME, "concept")
      .next();

    if (!collectionVertex.vertices(Direction.OUT, HAS_ARCHETYPE_RELATION_NAME).hasNext()) {
      collectionVertex.addEdge(HAS_ARCHETYPE_RELATION_NAME, archetypeVertex);
    }
    return archetypeVertex;
  }

  private void addCollectionToVre(CollectionDescription collectionDescription, Vertex collectionVertex) {
    Vertex vreVertex = graphWrapper.getGraph().traversal().V()
                                   .hasLabel(Vre.DATABASE_LABEL)
                                   .has(Vre.VRE_NAME_PROPERTY_NAME, collectionDescription.getVreName())
                                   .next();
    vreVertex.addEdge(Vre.HAS_COLLECTION_RELATION_NAME, collectionVertex);
  }


  public boolean isKnownArchetype(String archetype) {
    return graphWrapper.getGraph().traversal().V()
                       // Admin VRE contains the archetypes.
                       .hasLabel(Vre.DATABASE_LABEL)
                       .has(Vre.VRE_NAME_PROPERTY_NAME, "Admin")
                       .out(Vre.HAS_COLLECTION_RELATION_NAME)
                       .has(ENTITY_TYPE_NAME_PROPERTY_NAME, archetype)
                       .hasNext();
  }

  public Set<Entity> findEntitiesByCollection(Collection collection) {
    CollectionDescription description = collection.getDescription();
    String typeName = description.getEntityTypeName();
    String vreName = description.getVreName();
    return graphWrapper.getGraph().traversal().V().has(T.label, LabelP.of(DATABASE_LABEL))
                       .has(ENTITY_TYPE_NAME_PROPERTY_NAME, typeName)
                       .out(HAS_ENTITY_NODE_RELATION_NAME).out(HAS_ENTITY_RELATION_NAME)
                       .toSet().stream().map(v -> new Entity(v, getCollections(v, vreName)))
                       .collect(Collectors.toSet());

  }


}
