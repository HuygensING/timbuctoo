package nl.knaw.huygens.timbuctoo.crud;


import nl.knaw.huygens.timbuctoo.logging.Logmarkers;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class Neo4jLuceneEntityFetcher extends GremlinEntityFetcher {
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Neo4jLuceneEntityFetcher.class);
  public static final int MAX_VERSION_OF_DEPTH = 100;

  private final GraphDatabaseService graphDatabase;
  private final IndexManager indexManager;
  private TinkerpopGraphManager graphManager;

  public Neo4jLuceneEntityFetcher(TinkerpopGraphManager graphManager) {
    this.graphManager = graphManager;
    this.graphDatabase = graphManager.getGraphDatabase();
    this.indexManager = graphDatabase.index();

  }

  public GraphTraversal<Vertex, Vertex> getEntity(GraphTraversalSource source, UUID id, Integer rev,
                                                  String collectionName) {
    if (rev == null) {

      Optional<Vertex> foundVertex = getVertexByIndex(source, id, collectionName);

      if (foundVertex.isPresent()) {
        return source.V(foundVertex.get().id());
      } else {
        return super.getEntity(source, id, rev, collectionName);
      }
    }
    return super.getEntity(source, id, rev, collectionName);
  }

  private Optional<Vertex> getVertexByIndex(GraphTraversalSource source, UUID id, String collectionName) {
    final Graph graph = graphManager.getGraph();

    // Next comes some defensive programming (often introduces new problems in and of itself of course)...

    // Open transaction to be able to access lucene indices
    Transaction transaction = graph.tx();

    if (!transaction.isOpen()) {
      // Will throw: org.neo4j.graphdb.NotInTransactionException: null if transaction is not explicitly opened
      transaction.open();
    }

    // Return when index does not exist
    if (!indexManager.existsForNodes(collectionName)) {
      transaction.close();
      return Optional.empty();
    }

    // Look up this uuid in the lucene index
    Long vertexId = null;
    IndexHits<Node> indexHits = indexManager.forNodes(collectionName).get("tim_id", id.toString());
    if (indexHits.size() > 0) {
      vertexId = indexHits.next().getId();
    }

    // Close the transaction before returning
    transaction.close();

    // Only continue when a neo4j Node was found
    if (vertexId != null) {

      // Look up the vertex for this neo4j Node
      GraphTraversal<Vertex, Vertex> vertexT = source.V(vertexId);

      // Return if the neo4j Node ID matches no vertex (extreme edge case)
      if (!vertexT.hasNext()) {
        LOG.error(Logmarkers.databaseInvariant,
          "Vertex with tim_id {} is found in index with id {}L but not in graph database", id, vertexId);
        return Optional.empty();
      }

      // Get the latest version of the found Vertex
      Vertex foundVertex = vertexT.next();
      int infinityGuard = 0;
      while (foundVertex.vertices(Direction.OUT, "VERSION_OF").hasNext()) {
        // The neo4j index Node is one version_of behind the actual node
        foundVertex = foundVertex.vertices(Direction.OUT, "VERSION_OF").next();
        if (++infinityGuard >= MAX_VERSION_OF_DEPTH) {
          LOG.error(Logmarkers.databaseInvariant, "Vertices with tim_id {} might have circular VERSION_OF", id);
          return Optional.empty();
        }
      }

      // Only if this latest version is truly registered as latest return this as a successful hit
      if (foundVertex.value("isLatest")) {
        return Optional.of(foundVertex);
      } else {
        LOG.error(Logmarkers.databaseInvariant,
          "Last version of vertex with tim_id {} is not marked as isLatest=true", id);
      }
    }

    // Failed to find vertex in lucene index, so return
    return Optional.empty();
  }
}
