package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

public class Neo4jIndexHandler implements IndexHandler {
  private static final String QUICK_SEARCH = "quickSearch";
  private final GraphDatabaseService graphDatabase;
  private final GraphTraversalSource traversal;

  public Neo4jIndexHandler(TinkerpopGraphManager tinkerpopGraphManager) {
    this.graphDatabase = tinkerpopGraphManager.getGraphDatabase();
    this.traversal = tinkerpopGraphManager.getGraph().traversal();
  }

  @Override
  public boolean hasIndexFor(Collection collection) {
    return indexManager().existsForNodes(getIndexName(collection));
  }

  @Override
  public GraphTraversal<Vertex, Vertex> findByQuickSearch(Collection collection, String query) {
    return traversalFromIndex(collection, query);
  }

  @Override
  public GraphTraversal<Vertex, Vertex> findKeywordsByQuickSearch(Collection collection, String query,
                                                                  String keywordType) {
    return traversalFromIndex(collection, query).has("keyword_type", keywordType);
  }

  @Override
  public void addToQuickSearchIndex(Collection collection, String displayName, Vertex vertex) {
    Index<Node> index = indexManager().forNodes(collection.getCollectionName());

    index.add(graphDatabase.getNodeById((long) vertex.id()), QUICK_SEARCH, displayName);
  }

  private GraphTraversal<Vertex, Vertex> traversalFromIndex(Collection collection, String query) {
    Index<Node> index = indexManager().forNodes(getIndexName(collection));
    IndexHits<Node> hits = index.query(QUICK_SEARCH, query);
    List<Long> ids = StreamSupport.stream(hits.spliterator(), false).map(h -> h.getId()).collect(toList());

    return traversal.V(ids);
  }

  private IndexManager indexManager() {
    graphDatabase.beginTx();
    return graphDatabase.index();
  }

  private String getIndexName(Collection collection) {
    return collection.getCollectionName();
  }

}
