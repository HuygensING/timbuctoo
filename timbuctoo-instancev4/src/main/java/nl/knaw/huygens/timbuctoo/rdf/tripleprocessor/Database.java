package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.Clock;
import java.util.List;

public class Database {
  public static final String RDF_URI_PROP = "rdfUri";
  private final GraphWrapper graphWrapper;
  private final SystemPropertyModifier systemPropertyModifier;
  private final CollectionMapper collectionMapper;

  public Database(GraphWrapper graphWrapper) {
    this(graphWrapper, new SystemPropertyModifier(Clock.systemDefaultZone()), new CollectionMapper(graphWrapper));
  }

  Database(GraphWrapper graphWrapper, SystemPropertyModifier systemPropertyModifier,
           CollectionMapper collectionMapper) {
    this.graphWrapper = graphWrapper;
    this.systemPropertyModifier = systemPropertyModifier;
    this.collectionMapper = collectionMapper;
  }

  public Vertex findOrCreateEntityVertex(Node node, CollectionDescription collectionDescription) {
    Graph graph = graphWrapper.getGraph();
    final GraphTraversal<Vertex, Vertex> existingT = graph.traversal().V()
                                                          .has(RDF_URI_PROP, node.getURI());
    if (existingT.hasNext()) {
      final Vertex foundVertex = existingT.next();
      collectionMapper.addToCollection(foundVertex, collectionDescription);
      return foundVertex;
    } else {
      Vertex vertex = graph.addVertex();
      vertex.property(RDF_URI_PROP, node.getURI());

      systemPropertyModifier.setCreated(vertex, "rdf-importer");
      systemPropertyModifier.setModified(vertex, "rdf-importer");
      systemPropertyModifier.setTimId(vertex);
      systemPropertyModifier.setRev(vertex, 1);
      systemPropertyModifier.setIsLatest(vertex, true);
      systemPropertyModifier.setIsDeleted(vertex, false);

      collectionMapper.addToCollection(vertex, collectionDescription);
      return vertex;
    }
  }

  public Entity findOrCreateEntity(String vreName, Node node) {
    final Vertex subjectVertex = findOrCreateEntityVertex(node, CollectionDescription.getDefault(vreName));
    // TODO *HERE SHOULD BE A COMMIT* (autocommit?)
    final List<CollectionDescription>
      collections = collectionMapper.getCollectionDescriptions(subjectVertex, vreName);
    return new Entity(subjectVertex, collections);
  }

  public Collection findOrCreateCollection(String vreName, Node node) {
    Graph graph = graphWrapper.getGraph();

    GraphTraversal<Vertex, Vertex> collectionT = graph.traversal().V()
                                                      .has(T.label, LabelP.of("VRE")).has("name", vreName)
                                                      .out("hasCollection").has(RDF_URI_PROP, node.getURI());
    Vertex collectionVertex;
    if (collectionT.hasNext()) {
      collectionVertex = collectionT.next();
    } else {
      collectionVertex = graph.addVertex("collection");
      collectionVertex.property(RDF_URI_PROP, node.getURI());
      collectionVertex.property("entityTypeName", node.getLocalName());
      collectionVertex.property("collectionName", node.getLocalName() + "s");
      Vertex vreVertex = graph.traversal().V()
                              .has(T.label, LabelP.of("VRE")).has("name", vreName).next();
      vreVertex.addEdge("hasCollection", collectionVertex);
    }

    return new Collection(vreName, collectionVertex, collectionMapper);
  }
}
