package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.core.dto.QuickSearch;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import nl.knaw.huygens.timbuctoo.util.StreamIterator;
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
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class Neo4jIndexHandler implements IndexHandler {
  private static final String QUICK_SEARCH = "quickSearch";
  private static final String ID_INDEX = "idIndex";
  private static final String TIM_ID = "tim_id";
  private final TinkerPopGraphManager tinkerPopGraphManager;

  public Neo4jIndexHandler(TinkerPopGraphManager tinkerPopGraphManager) {
    this.tinkerPopGraphManager = tinkerPopGraphManager;
  }

  //=====================quick search index=====================
  @Override
  public boolean hasQuickSearchIndexFor(Collection collection) {
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
  public void insertIntoQuickSearchIndex(Collection collection, String quickSearchValue, Vertex vertex) {
    Index<Node> index = getQuickSearchIndex(collection);

    index.add(vertexToNode(vertex), QUICK_SEARCH, quickSearchValue);
  }

  private GraphTraversal<Vertex, Vertex> traversalFromIndex(Collection collection, QuickSearch quickSearch) {
    Index<Node> index = getQuickSearchIndex(collection);
    IndexHits<Node> hits = index.query(QUICK_SEARCH, createQuery(quickSearch));
    List<Long> ids = StreamIterator.stream(hits.iterator()).map(h -> h.getId()).collect(toList());

    return ids.isEmpty() ? EmptyGraphTraversal.instance() : traversal().V(ids);
  }

  private Index<Node> getQuickSearchIndex(Collection collection) {
    // Add the config below, to make sure the index is case insensitive.
    Map<String, String> indexConfig = MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "type", "fulltext");
    return indexManager().forNodes(getIndexName(collection), indexConfig);
  }

  private Object createQuery(QuickSearch quickSearch) {
    String fullMatches = String.join(" ", quickSearch.fullMatches());
    String partialMatches = String.join("* ", quickSearch.partialMatches());
    return fullMatches + " " + partialMatches + "*";
  }

  @Override
  public void removeFromQuickSearchIndex(Collection collection, Vertex vertex) {
    Index<Node> index = getQuickSearchIndex(collection);
    // make sure only this field is removed from the index.
    index.remove(vertexToNode(vertex), QUICK_SEARCH);
  }

  @Override
  public void upsertIntoQuickSearchIndex(Collection collection, String quickSearchValue, Vertex vertex) {
    this.removeFromQuickSearchIndex(collection, vertex);

    this.insertIntoQuickSearchIndex(collection, quickSearchValue, vertex);
  }

  //=====================tim_id index=====================
  @Override
  public Optional<Vertex> findById(UUID timId) {
    IndexHits<Node> hits = getIdIndex(ID_INDEX).query(TIM_ID, timId.toString());
    List<Long> ids = StreamIterator.stream(hits.iterator()).map(h -> h.getId()).collect(toList());
    GraphTraversal<Vertex, Vertex> vertexT = traversal().V(ids);

    return !ids.isEmpty() && vertexT.hasNext() ? Optional.of(vertexT.next()) : Optional.empty();
  }

  @Override
  public void insertIntoIdIndex(UUID timId, Vertex vertex) {
    Index<Node> index = getIdIndex(ID_INDEX);
    index.add(vertexToNode(vertex), TIM_ID, timId.toString());
  }


  @Override
  public void upsertIntoIdIndex(UUID timId, Vertex vertex) {
    removeFromIdIndex(vertex);
    insertIntoIdIndex(timId, vertex);
  }

  @Override
  public void removeFromIdIndex(Vertex vertex) {
    // make sure only this field is removed from the index.
    getIdIndex(ID_INDEX).remove(vertexToNode(vertex), TIM_ID);
  }

  private Index<Node> getIdIndex(String collectionName) {
    return indexManager().forNodes(collectionName);
  }
  //=====================general helper methods=====================

  private GraphTraversalSource traversal() {
    return tinkerPopGraphManager.getGraph().traversal();
  }

  private GraphDatabaseService graphDatabase() {
    return tinkerPopGraphManager.getGraphDatabase();
  }

  private IndexManager indexManager() {
    return graphDatabase().index();
  }

  private String getIndexName(Collection collection) {
    return collection.getCollectionName();
  }

  private Node vertexToNode(Vertex vertex) {
    return graphDatabase().getNodeById((long) vertex.id());
  }

}
