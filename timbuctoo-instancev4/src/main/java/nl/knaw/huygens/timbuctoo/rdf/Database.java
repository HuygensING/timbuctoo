package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.relationtypes.RelationTypeService;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.slf4j.Logger;

import java.time.Clock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.COLLECTION_ENTITIES_LABEL;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.COLLECTION_IS_UNKNOWN_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.IS_RELATION_COLLECTION_PROPERTY_NAME;
import static org.slf4j.LoggerFactory.getLogger;

public class Database {
  private static final Logger LOG = getLogger(Database.class);

  public static final String RDF_URI_PROP = "rdfUri";
  public static final String RDFINDEX_NAME = "rdfUrls";
  public static final String RDF_SYNONYM_PROP = "rdfAlternatives";
  private final TinkerPopGraphManager graphWrapper;
  private final SystemPropertyModifier systemPropertyModifier;
  private GraphTraversalSource cachedTraversal;
  private Index<org.neo4j.graphdb.Node> rdfIndex;
  private GraphDatabaseService graphDatabase;
  private Map<String, Collection> collectionCache = new HashMap<>();

  public Database(TinkerPopGraphManager graphWrapper) {
    this(graphWrapper, new SystemPropertyModifier(Clock.systemDefaultZone()));
  }

  Database(TinkerPopGraphManager graphWrapper, SystemPropertyModifier systemPropertyModifier) {
    this.graphWrapper = graphWrapper;
    this.systemPropertyModifier = systemPropertyModifier;
    graphDatabase = graphWrapper.getGraphDatabase();
    final Transaction transaction = graphWrapper.getGraph().tx();
    boolean hasOwnTransaction = false;
    if (!transaction.isOpen()) {
      transaction.open();
      hasOwnTransaction = true;
    }
    rdfIndex = graphDatabase.index().forNodes(RDFINDEX_NAME);
    if (hasOwnTransaction) {
      transaction.close();
    }
  }

  private String getNodeUri(Node node, String vreName) {
    if (!node.isBlank()) {
      return node.getURI();
    }
    return String.format("%s:%s", vreName, node.getBlankNodeId());
  }

  public Entity findOrCreateEntity(String vreName, Node node) {
    String nodeUri = getNodeUri(node, vreName);

    return findEntity(vreName, nodeUri)
      .orElseGet(() -> createEntity(vreName, nodeUri));
  }

  public Entity findOrCreateEntity(String vreName, String entityReference) {
    return findEntity(vreName, entityReference)
      .orElseGet(() -> createEntity(vreName, entityReference));
  }

  public Optional<Entity> findEntity(String vreName, Node node) {
    return findEntity(vreName, getNodeUri(node, vreName));
  }

  public Optional<Entity> findEntity(String vreName, String nodeUri) {
    final Optional<Vertex> entityV = findVertexInRdfIndex(vreName, nodeUri);
    return entityV.isPresent() ?
      Optional.of(new Entity(entityV.get(), getCollections(entityV.get(), vreName)))
      : Optional.empty();
  }

  private Optional<Vertex> findVertexInRdfIndex(String indexName, String nodeUri) {
    IndexHits<org.neo4j.graphdb.Node> rdfurls = rdfIndex.get(indexName, nodeUri);
    if (rdfurls.hasNext()) {
      long vertexId = rdfurls.next().getId();
      if (rdfurls.hasNext()) {
        StringBuilder errorMessage = new StringBuilder().append("There is more then one node in ")
                                                        .append(indexName)
                                                        .append(" for the rdfUrl ")
                                                        .append(nodeUri)
                                                        .append(" ")
                                                        .append("namely ")
                                                        .append(vertexId);
        rdfurls.forEachRemaining(x -> errorMessage.append(", ").append(x.getId()));
        LOG.error(errorMessage.toString());
      }
      GraphTraversal<Vertex, Vertex> vertexLookup = traversal().V(vertexId);
      if (vertexLookup.hasNext()) {
        Vertex vertex = vertexLookup.next();
        return Optional.of(vertex);
      } else {
        LOG.error("Index returned a Node for " + indexName + " - " + nodeUri + " but the node id " + vertexId +
          "could not be found using Tinkerpop.");
      }
    }
    return Optional.empty();
  }

  private Entity createEntity(String vreName, String nodeUri) {
    Vertex vertex = graphWrapper.getGraph().addVertex();
    vertex.property(RDF_URI_PROP, nodeUri);
    vertex.property(RDF_SYNONYM_PROP, new String[0]);

    systemPropertyModifier.setCreated(vertex, "rdf-importer");
    systemPropertyModifier.setModified(vertex, "rdf-importer");
    if (nodeUri.startsWith("http://timbuctoo.huygens.knaw.nl/mapping/" + vreName)) {
      String timId = nodeUri.substring(nodeUri.lastIndexOf("/") + 1);
      systemPropertyModifier.setTimId(vertex, timId);
    } else {
      systemPropertyModifier.setTimId(vertex);
    }
    systemPropertyModifier.setRev(vertex, 1);
    systemPropertyModifier.setIsLatest(vertex, true);
    systemPropertyModifier.setIsDeleted(vertex, false);

    Collection collection = getDefaultCollection(vreName);
    Entity entity = new Entity(vertex, getCollections(vertex, vreName));
    entity.addToCollection(collection);

    org.neo4j.graphdb.Node neo4jNode = graphDatabase.getNodeById((Long) vertex.id());
    rdfIndex.add(neo4jNode, vreName, nodeUri);
    rdfIndex.add(neo4jNode, "Admin", nodeUri);


    return entity;
  }

  private GraphTraversalSource traversal() {
    if (cachedTraversal == null) {
      cachedTraversal = graphWrapper
        .getGraph().traversal();
    }
    return cachedTraversal;
  }

  private Set<Collection> getCollections(Vertex foundVertex, String vreName) {
    return traversal()
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
    return collectionCache.computeIfAbsent(
      vreName + "<<default>>",
      name -> findOrCreateCollection(
        CollectionDescription.getDefault(vreName)
      )
    );
  }

  public Collection findOrCreateCollection(String vreName, Node subject) {
    return findOrCreateCollection(vreName, subject.getURI(), subject.getLocalName());
  }

  public Collection findOrCreateCollection(String vreName, String collectionUri, String entityTypeName) {
    return collectionCache.computeIfAbsent(
      vreName + collectionUri,
      name -> findOrCreateCollection(
        CollectionDescription.createCollectionDescription(entityTypeName, vreName, collectionUri)
      )
    );
  }


  private Collection findOrCreateCollection(CollectionDescription collectionDescription) {
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

  public RelationType findOrCreateRelationType(String predicateUri, String simpleName) {
    final Optional<Vertex> relationTypeV =
      findVertexInRdfIndex(RelationTypeService.RELATIONTYPE_INDEX_NAME, predicateUri);

    if (relationTypeV.isPresent()) {
      final Vertex relationTypeVertex = relationTypeV.get();
      boolean isInverse = relationTypeVertex.<String>property("relationtype_inverseName")
        .value().equals(simpleName);
      return new RelationType(relationTypeVertex, isInverse);
    }

    final String relationTypePrefix = "relationtype_";
    final Vertex relationTypeVertex = graphWrapper.getGraph().addVertex("relationtype");

    relationTypeVertex.property(RDF_URI_PROP, predicateUri);
    relationTypeVertex.property("types", "[\"relationtype\"]");
    relationTypeVertex.property(relationTypePrefix + "targetTypeName", "concept");
    relationTypeVertex.property(relationTypePrefix + "sourceTypeName", "concept");
    relationTypeVertex.property(relationTypePrefix + "symmetric", false);
    relationTypeVertex.property(relationTypePrefix + "reflexive", false);
    relationTypeVertex.property(relationTypePrefix + "derived", false);
    relationTypeVertex.property(relationTypePrefix + "regularName", simpleName);
    relationTypeVertex.property(relationTypePrefix + "inverseName", "inverse:" + simpleName);

    systemPropertyModifier.setTimId(relationTypeVertex);
    systemPropertyModifier.setCreated(relationTypeVertex, "rdf-importer");
    systemPropertyModifier.setModified(relationTypeVertex, "rdf-importer");
    systemPropertyModifier.setIsLatest(relationTypeVertex, true);
    systemPropertyModifier.setRev(relationTypeVertex, 1);

    org.neo4j.graphdb.Node neo4jNode = graphDatabase.getNodeById((Long) relationTypeVertex.id());
    rdfIndex.add(neo4jNode, RelationTypeService.RELATIONTYPE_INDEX_NAME, predicateUri);

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
    Iterator<Vertex> vreTraversal = graphWrapper.getGraph().traversal().V()
                                     .hasLabel(Vre.DATABASE_LABEL)
                                     .has(Vre.VRE_NAME_PROPERTY_NAME, collectionDescription.getVreName());
    if (vreTraversal.hasNext()) {
      vreTraversal.next().addEdge(Vre.HAS_COLLECTION_RELATION_NAME, collectionVertex);
    } else {
      throw new IllegalStateException("Vre " + collectionDescription.getVreName() + " not found");
    }
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

  public void addRdfSynonym(String vreName, Entity entity, String synonymUri) {
    String[] oldRdfUri = entity.vertex.value(RDF_SYNONYM_PROP);
    String[] newRdfUri = Arrays.copyOf(oldRdfUri, oldRdfUri.length + 1);

    newRdfUri[newRdfUri.length - 1] = synonymUri;

    entity.vertex.property(RDF_SYNONYM_PROP, newRdfUri);

    org.neo4j.graphdb.Node neo4jNode = graphDatabase.getNodeById((Long) entity.vertex.id());
    rdfIndex.add(neo4jNode, vreName, synonymUri);
  }

  public void copyEdgesFromObjectIntoSubject(Entity subjectEntity, Entity objectEntity) {

    objectEntity.vertex.edges(Direction.OUT).forEachRemaining(edge -> {
      // skip duplicates
      final Iterator<Vertex> existingV = subjectEntity.vertex.vertices(Direction.OUT, edge.label());
      while (existingV.hasNext()) {
        if (existingV.next().equals(edge.inVertex())) {
          return;
        }
      }
      final Edge newEdge = subjectEntity.vertex.addEdge(edge.label(), edge.inVertex());
      edge.properties().forEachRemaining(prop -> newEdge.property(prop.key(), prop.value()));
    });

    objectEntity.vertex.edges(Direction.IN).forEachRemaining(edge -> {
      // skip duplicates
      final Iterator<Vertex> existingV = subjectEntity.vertex.vertices(Direction.IN, edge.label());
      while (existingV.hasNext()) {
        if (existingV.next().equals(edge.outVertex())) {
          return;
        }
      }
      final Edge newEdge = edge.outVertex().addEdge(edge.label(), subjectEntity.vertex);
      edge.properties().forEachRemaining(prop -> newEdge.property(prop.key(), prop.value()));
    });
  }

  public void purgeEntity(String vreName, Entity objectEntity) {
    org.neo4j.graphdb.Node neo4jNode = graphDatabase.getNodeById((Long) objectEntity.vertex.id());
    rdfIndex.remove(neo4jNode, vreName);
    objectEntity.vertex.remove();
  }


}
