package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.model.vre.Collection.COLLECTION_ENTITIES_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.IS_RELATION_COLLECTION_PROPERTY_NAME;

public class Database {
  public static final String RDF_URI_PROP = "rdfUri";
  public static final Logger LOG = LoggerFactory.getLogger(Database.class);

  private final GraphWrapper graphWrapper;
  private final SystemPropertyModifier systemPropertyModifier;

  private static final int ENTITY_CACHE_SIZE = 1024 * 1024;

  // TODO add cache loader
  private Cache<String, Long> entityCache = CacheBuilder.newBuilder().maximumSize(ENTITY_CACHE_SIZE).build();

  public Database(GraphWrapper graphWrapper) {
    this(graphWrapper, new SystemPropertyModifier(Clock.systemDefaultZone()));
  }

  Database(GraphWrapper graphWrapper, SystemPropertyModifier systemPropertyModifier) {
    this.graphWrapper = graphWrapper;
    this.systemPropertyModifier = systemPropertyModifier;
  }

  public Vertex findOrCreateEntityVertex(Node node, String vreName) {
    Graph graph = graphWrapper.getGraph();
    Long vertexId = entityCache.getIfPresent(node.getURI());

    if (vertexId == null) {
      final GraphTraversal<Vertex, Vertex> existingT = graph
        .traversal().V()
        .hasLabel(Vre.DATABASE_LABEL)
        .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
        .out(Vre.HAS_COLLECTION_RELATION_NAME)
        .out(HAS_ENTITY_NODE_RELATION_NAME)
        .out(HAS_ENTITY_RELATION_NAME)
        .has(RDF_URI_PROP, node.getURI());

      Collection collection = findOrCreateCollection(CollectionDescription.getDefault(vreName));
      Vertex vertex;
      if (existingT.hasNext()) {
        // System.out.println("Existing vertex with uri: " +  node.getURI());
        vertex = existingT.next();
        collection.add(vertex);
      } else {
        vertex = graph.addVertex();
        vertex.property(RDF_URI_PROP, node.getURI());

        systemPropertyModifier.setCreated(vertex, "rdf-importer");
        systemPropertyModifier.setModified(vertex, "rdf-importer");
        systemPropertyModifier.setTimId(vertex);
        systemPropertyModifier.setRev(vertex, 1);
        systemPropertyModifier.setIsLatest(vertex, true);
        systemPropertyModifier.setIsDeleted(vertex, false);

        collection.add(vertex);
        graphWrapper.getGraph().tx().commit();
      }
      vertexId = (Long)vertex.id();
      entityCache.put(node.getURI(), vertexId);
    }

    return graph.traversal().V(vertexId).next();
  }

  public Entity findOrCreateEntity(String vreName, Node node) {
    final Vertex vertex = findOrCreateEntityVertex(node, vreName);
    // TODO *HERE SHOULD BE A COMMIT* (autocommit?)
    return new Entity(vertex, getCollections(vertex, vreName));
  }

  private Set<Collection> getCollections(Vertex foundVertex, String vreName) {
    Set<Collection> collections = graphWrapper
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
    return collections;
  }

  public Collection getDefaultCollection(String vreName) {
    return findOrCreateCollection(CollectionDescription.getDefault(vreName));
  }

  public Collection findOrCreateCollection(String vreName, Node node) {
    CollectionDescription collectionDescription =
      CollectionDescription.createCollectionDescription(node.getLocalName(), vreName, node.getURI());
    return findOrCreateCollection(collectionDescription);
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
      Vertex containerVertex = graphWrapper.getGraph().addVertex(COLLECTION_ENTITIES_LABEL);
      collectionVertex.addEdge(HAS_ENTITY_NODE_RELATION_NAME, containerVertex);

      if (!collectionVertex.vertices(Direction.IN, Vre.HAS_COLLECTION_RELATION_NAME).hasNext()) {
        addCollectionToVre(collectionDescription, collectionVertex);
      }
      addCollectionToArchetype(collectionVertex);
    }


    return new Collection(collectionDescription.getVreName(), collectionVertex, graphWrapper);
  }


  public RelationType findOrCreateRelationType(Node predicate) {
    final GraphTraversal<Vertex, Vertex> relationtypeT =
      graphWrapper.getGraph().traversal().V().hasLabel("relationtype").has(RDF_URI_PROP, predicate.getURI());

    if (relationtypeT.hasNext()) {
      return new RelationType(relationtypeT.next());
    }

    final String relationtypePrefix = "relationtype_";
    final Vertex relationTypeVertex = graphWrapper.getGraph().addVertex("relationtype");

    relationTypeVertex.property(RDF_URI_PROP, predicate.getURI());
    relationTypeVertex.property("types", "[\"relationtype\"]");
    relationTypeVertex.property(relationtypePrefix + "targetTypeName", "concept");
    relationTypeVertex.property(relationtypePrefix + "sourceTypeName", "concept");
    relationTypeVertex.property(relationtypePrefix + "symmetric", false);
    relationTypeVertex.property(relationtypePrefix + "reflexive", false);
    relationTypeVertex.property(relationtypePrefix + "derived", false);
    relationTypeVertex.property(relationtypePrefix + "regularName", predicate.getLocalName());
    relationTypeVertex.property(relationtypePrefix + "inverseName", "inverse:" + predicate.getLocalName());

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

  public void addCollectionToVre(CollectionDescription collectionDescription, Vertex collectionVertex) {
    Vertex vreVertex = graphWrapper.getGraph().traversal().V()
                                   .hasLabel(Vre.DATABASE_LABEL)
                                   .has(Vre.VRE_NAME_PROPERTY_NAME, collectionDescription.getVreName())
                                   .next();
    vreVertex.addEdge(Vre.HAS_COLLECTION_RELATION_NAME, collectionVertex);
  }


}
