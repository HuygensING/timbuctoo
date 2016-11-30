package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.dto.QuickSearch;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.EmptyGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

public class Neo4jIndexHandler implements IndexHandler {
  private static final String QUICK_SEARCH = "quickSearch";
  private final TinkerpopGraphManager tinkerpopGraphManager;

  public Neo4jIndexHandler(TinkerpopGraphManager tinkerpopGraphManager) {
    this.tinkerpopGraphManager = tinkerpopGraphManager;
  }

  @Override
  public boolean hasIndexFor(Collection collection) {
    return indexManager().existsForNodes(getIndexName(collection));
  }

  @Override
  public GraphTraversal<Vertex, Vertex> findByQuickSearch(Collection collection, QuickSearch quickSearch) {
    return traversalFromIndex(collection, quickSearch);
  }

  @Override
  public GraphTraversal<Vertex, Vertex> findKeywordsByQuickSearch(Collection collection, QuickSearch quickSearch,
                                                                  String keywordType) {
    return traversalFromIndex(collection, quickSearch).has("keyword_type", keywordType);
  }

  @Override
  public void addToQuickSearchIndex(Collection collection, String quickSearchValue, Vertex vertex) {
    Index<Node> index = getFulltextIndex(collection.getCollectionName());

    index.add(graphDatabase().getNodeById((long) vertex.id()), QUICK_SEARCH, quickSearchValue);
  }

  private GraphDatabaseService graphDatabase() {
    return tinkerpopGraphManager.getGraphDatabase();
  }

  @Override
  public void addToOrUpdateQuickSearchIndex(Collection collection, String quickSearchValue, Vertex vertex) {
    this.removeFromQuickSearchIndex(collection, vertex);

    this.addToQuickSearchIndex(collection, quickSearchValue, vertex);
  }

  @Override
  public void removeFromQuickSearchIndex(Collection collection, Vertex vertex) {
    Index<Node> index = getFulltextIndex(collection.getCollectionName());

    index.remove(graphDatabase().getNodeById((long) vertex.id()), QUICK_SEARCH);
  }

  private GraphTraversal<Vertex, Vertex> traversalFromIndex(Collection collection, QuickSearch quickSearch) {
    Index<Node> index = getFulltextIndex(getIndexName(collection));
    IndexHits<Node> hits = index.query(QUICK_SEARCH, createQuery(quickSearch));
    List<Long> ids = StreamSupport.stream(hits.spliterator(), false).map(h -> h.getId()).collect(toList());

    return ids.isEmpty() ? EmptyGraphTraversal.instance() : traversal().V(ids);
  }

  private GraphTraversalSource traversal() {
    return tinkerpopGraphManager.getGraph().traversal();
  }

  private Index<Node> getFulltextIndex(String collectionName) {
    // Add the config below, to make sure the index is case insensitive.
    Map<String, String> indexConfig = MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "type", "fulltext");
    return indexManager().forNodes(collectionName, indexConfig);
  }

  private Object createQuery(QuickSearch quickSearch) {
    String fullMatches = String.join(" ", quickSearch.fullMatches());
    String partialMatches = String.join("* ", quickSearch.partialMatches());
    return fullMatches + " " + partialMatches + "*";
  }

  private IndexManager indexManager() {
    graphDatabase().beginTx();
    return graphDatabase().index();
  }

  private String getIndexName(Collection collection) {
    return collection.getCollectionName();
  }

}
