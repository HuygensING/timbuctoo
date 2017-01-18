package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.core.dto.QuickSearch;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import nl.knaw.huygens.timbuctoo.util.StreamIterator;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.EmptyGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.helpers.collection.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.rdf.Database.RDFINDEX_NAME;

public class Neo4jIndexHandler implements IndexHandler {
  private static final String QUICK_SEARCH = "quickSearch";
  private static final String ID_INDEX = "idIndex";
  private static final String TIM_ID = "tim_id";
  private static final Logger LOG = LoggerFactory.getLogger(Neo4jIndexHandler.class);
  private final TinkerPopGraphManager tinkerPopGraphManager;
  private GraphTraversalSource cachedTraversal;

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
  public void upsertIntoQuickSearchIndex(Collection collection, String quickSearchValue, Vertex vertex,
                                         Vertex oldVertex) {
    if (oldVertex != null) {
      this.removeFromQuickSearchIndex(collection, oldVertex);
    }

    Index<Node> index = getQuickSearchIndex(collection);

    index.add(vertexToNode(vertex), QUICK_SEARCH, quickSearchValue);
  }

  //=====================tim_id index=====================
  @Override
  public Optional<Vertex> findById(UUID timId) {
    IndexHits<Node> hits = getIdIndex().query(TIM_ID, timId.toString());

    return hits.hasNext() ? traversal().V(hits.next().getId()).tryNext() : Optional.empty();
  }

  @Override
  @Deprecated
  public void insertIntoIdIndex(UUID timId, Vertex vertex) {
    Index<Node> index = getIdIndex();
    index.add(vertexToNode(vertex), TIM_ID, timId.toString());
  }

  @Override
  public void upsertIntoIdIndex(UUID timId, Vertex vertex) {
    findById(timId).ifPresent(this::removeFromIdIndex);
    insertIntoIdIndex(timId, vertex);
  }

  @Override
  public void removeFromIdIndex(Vertex vertex) {
    // make sure only this field is removed from the index.
    getIdIndex().remove(vertexToNode(vertex), TIM_ID);
  }

  private Index<Node> getIdIndex() {
    return indexManager().forNodes(ID_INDEX);
  }

  //=====================RDF Uri index=====================
  @Override
  public Optional<Vertex> findVertexInRdfIndex(Vre vre, String nodeUri) {
    String vreName = vre.getVreName();
    IndexHits<Node> rdfurls = indexManager().forNodes(RDFINDEX_NAME).get(vreName, nodeUri);
    if (rdfurls.hasNext()) {
      long vertexId = rdfurls.next().getId();
      if (rdfurls.hasNext()) {
        StringBuilder errorMessage = new StringBuilder().append("There is more then one node in ")
                                                        .append(RDFINDEX_NAME)
                                                        .append(" for the vre ")
                                                        .append(vreName)
                                                        .append(" and the rdfUri ")
                                                        .append(nodeUri)
                                                        .append(" namely ")
                                                        .append(vertexId);
        rdfurls.forEachRemaining(x -> errorMessage.append(", ").append(x.getId()));
        LOG.error(errorMessage.toString());
      }
      GraphTraversal<Vertex, Vertex> vertexLookup = traversal().V(vertexId);
      if (vertexLookup.hasNext()) {
        Vertex vertex = vertexLookup.next();
        return Optional.of(vertex);
      } else {
        LOG.error("Index returned a Node for " + vreName + " - " + nodeUri + " but the node id " + vertexId +
          "could not be found using Tinkerpop.");
      }
    }
    return Optional.empty();
  }

  @Override
  public void addVertexToRdfIndex(Vre vre, String nodeUri, Vertex vertex) {
    Index<Node> rdfIndex = indexManager().forNodes(RDFINDEX_NAME);
    org.neo4j.graphdb.Node neo4jNode = graphDatabase().getNodeById((Long) vertex.id());
    rdfIndex.add(neo4jNode, vre.getVreName(), nodeUri);
    rdfIndex.add(neo4jNode, "Admin", nodeUri);
  }

  @Override
  public void removeFromRdfIndex(Vre vre, Vertex vertex) {
    Index<Node> rdfIndex = indexManager().forNodes(RDFINDEX_NAME);
    org.neo4j.graphdb.Node neo4jNode = graphDatabase().getNodeById((Long) vertex.id());
    rdfIndex.remove(neo4jNode, vre.getVreName());
    rdfIndex.remove(neo4jNode, "Admin");
  }

  //=====================Edge tim_id index=====================
  @Override
  public Optional<Edge> findEdgeById(UUID edgeId) {
    RelationshipIndex edgeIdIndex = getEdgeIdIndex();
    IndexHits<Relationship> hits = edgeIdIndex.query(TIM_ID, edgeId.toString());

    return hits.hasNext() ? traversal().E(hits.next().getId()).tryNext() : Optional.empty();
  }

  @Override
  public void upsertIntoEdgeIdIndex(UUID edgeId, Edge edge) {
    Optional<Edge> prevEdge = findEdgeById(edgeId);
    prevEdge.ifPresent(this::removeEdgeFromIdIndex);
    getEdgeIdIndex().add(edgeToRelationship(edge), TIM_ID, edgeId.toString());
  }

  @Override
  public void removeEdgeFromIdIndex(Edge edge) {
    getEdgeIdIndex().remove(edgeToRelationship(edge), TIM_ID);
  }

  private RelationshipIndex getEdgeIdIndex() {
    return indexManager().forRelationships("edgeIdIndex");
  }

  private Relationship edgeToRelationship(Edge edge) {
    return graphDatabase().getRelationshipById((long) edge.id());
  }
  //=====================general helper methods=====================

  private GraphTraversalSource traversal() {
    if (cachedTraversal == null) {
      cachedTraversal = tinkerPopGraphManager.getGraph().traversal();
    }
    return cachedTraversal;
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
