package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.Clock;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.model.vre.Collection.COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;

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
                                                      .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
                                                      .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
                                                      .out(Vre.HAS_COLLECTION_RELATION_NAME)
                                                      .has(RDF_URI_PROP, node.getURI());
    Vertex collectionVertex;
    if (collectionT.hasNext()) {
      collectionVertex = collectionT.next();
    } else {
      collectionVertex = graph.addVertex(DATABASE_LABEL);
      collectionVertex.property(RDF_URI_PROP, node.getURI());
      collectionVertex.property(ENTITY_TYPE_NAME_PROPERTY_NAME, node.getLocalName());
      collectionVertex.property(COLLECTION_NAME_PROPERTY_NAME, node.getLocalName() + "s");
      Vertex vreVertex = graph.traversal().V()
                              .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
                              .has(Vre.VRE_NAME_PROPERTY_NAME, vreName).next();
      vreVertex.addEdge(Vre.HAS_COLLECTION_RELATION_NAME, collectionVertex);
    }

    return new Collection(vreName, collectionVertex, collectionMapper);
  }
}
