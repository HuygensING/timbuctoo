package nl.knaw.huygens.timbuctoo.crud;


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

import java.util.Optional;
import java.util.UUID;

public class Neo4jLuceneEntityFetcher extends GremlinEntityFetcher {

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

    Transaction transaction = graph.tx();
    if (!transaction.isOpen()) {
      transaction.open();
    }
    if (!indexManager.existsForNodes(collectionName)) {
      return Optional.empty();
    }

    Long vertexId = null;
    IndexHits<Node> indexHits = indexManager.forNodes(collectionName).get("tim_id", id.toString());
    if (indexHits.size() > 0) {
      vertexId = indexHits.next().getId();
    }
    transaction.close();
    if (vertexId != null) {
      GraphTraversal<Vertex, Vertex> vertexT = source.V(vertexId);
      if (!vertexT.hasNext()) {
        return Optional.empty();
      }

      Vertex foundVertex = vertexT.next();

      while (foundVertex.vertices(Direction.OUT, "VERSION_OF").hasNext()) {
        // The neo4j index Node is one version_of behind the actual node
        foundVertex = foundVertex.vertices(Direction.OUT, "VERSION_OF").next();
      }
      if (foundVertex.value("isLatest")) {
        return Optional.of(foundVertex);
      }
    }
    return Optional.empty();
  }
}
