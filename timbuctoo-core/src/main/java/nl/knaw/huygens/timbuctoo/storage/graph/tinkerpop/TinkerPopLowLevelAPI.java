package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.IsOfTypePredicate;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.TinkerPopGraphQueryBuilderFactory;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.TinkerPopResultFilter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.TinkerPopResultFilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.Entity.DB_ID_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_REV_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.IS_LATEST;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getIdProperty;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getRevisionProperty;

class TinkerPopLowLevelAPI {

  private static final String VERSION_OF_LABEL = SystemRelationType.VERSION_OF.name();
  public static final Logger LOG = LoggerFactory.getLogger(TinkerPopLowLevelAPI.class);
  public static final IsOfTypePredicate IS_TYPE_OF = new IsOfTypePredicate();
  private final Graph db;
  private final VertexDuplicator vertexDuplicator;
  private final EdgeManipulator edgeManipulator;
  private TinkerPopGraphQueryBuilderFactory queryBuilderFactory;
  private TinkerPopResultFilterBuilder resultFilterBuilder;

  public TinkerPopLowLevelAPI(Graph db) {
    this(db, new VertexDuplicator(db), new EdgeManipulator(), new TinkerPopGraphQueryBuilderFactory(db), new TinkerPopResultFilterBuilder());
  }

  public TinkerPopLowLevelAPI(Graph db, VertexDuplicator vertexDuplicator, EdgeManipulator edgeManipulator, TinkerPopGraphQueryBuilderFactory queryBuilderFactory,
                              TinkerPopResultFilterBuilder resultFilterBuilder) {
    this.db = db;
    this.vertexDuplicator = vertexDuplicator;
    this.edgeManipulator = edgeManipulator;
    this.queryBuilderFactory = queryBuilderFactory;
    this.resultFilterBuilder = resultFilterBuilder;
  }

  public <T extends Entity> Vertex getLatestVertexById(Class<T> type, String id) {
    // this is needed to check if the type array contains the value requeste type
    Iterable<Vertex> foundVertices = queryLatestByType(type).has(DB_ID_PROP_NAME, id) //
      .vertices();

    return getFirstFromIterable(foundVertices);
  }

  public Vertex getLatestVertexById(String id) {
    Iterable<Vertex> vertices = queryLatest().has(DB_ID_PROP_NAME, id).vertices();

    return getFirstFromIterable(vertices);
  }

  private GraphQuery queryLatest() {
    return db.query().has(IS_LATEST, true);
  }

  private <T extends Entity> GraphQuery queryLatestByType(Class<T> type) {
    return queryByType(type).has(IS_LATEST, true);
  }

  private <T extends Entity> GraphQuery queryByType(Class<T> type) {
    return db.query() //
      .has(ELEMENT_TYPES, IS_TYPE_OF, TypeNames.getInternalName(type));
  }

  public Vertex getVertexWithRevision(Class<? extends DomainEntity> type, String id, int revision) {
    Iterable<Vertex> vertices = queryByType(type)//
      .has(DB_ID_PROP_NAME, id)//
      .has(DB_REV_PROP_NAME, revision)//
      .vertices();

    return getFirstFromIterable(vertices);
  }

  private <T extends Element> T getFirstFromIterable(Iterable<T> elements) {
    T element = null;
    Iterator<T> iterator = elements.iterator();

    if (iterator.hasNext()) {
      element = iterator.next();
    }

    return element;
  }

  public Edge getLatestEdgeById(Class<? extends Relation> relationType, String id) {
    Iterable<Edge> edges = queryLatestByType(relationType).has(DB_ID_PROP_NAME, id).edges();

    return getFirstFromIterable(edges);
  }

  public Iterator<Vertex> getLatestVerticesOf(Class<? extends Entity> type) {
    Stopwatch retrieveStopwatch = Stopwatch.createStarted();
    LOG.debug("Retrieve vertices of type [{}]", type);
    Stopwatch queryStopwatch = Stopwatch.createStarted();

    LOG.debug("Query vertices of type [{}]", type);
    Iterable<Vertex> vertices = queryByType(type).has(IS_LATEST, true).vertices();
    LOG.debug("Query vertices of type [{}] ended in [{}]", type, queryStopwatch.stop());

    LOG.debug("Retrieve vertices of type [{}] ended in [{}]", type, retrieveStopwatch.stop());
    return vertices.iterator();

  }

  public void duplicate(Vertex vertex) {
    vertexDuplicator.duplicate(vertex);
  }

  public void duplicate(Edge edge) {
    edgeManipulator.duplicate(edge);
  }

  public Edge getEdgeWithRevision(Class<? extends Relation> relationType, String id, int revision) {
    Iterable<Edge> edges = db.query().has(DB_ID_PROP_NAME, id).has(DB_REV_PROP_NAME, revision).edges();
    return getFirstFromIterable(edges);
  }

  public Iterator<Edge> getLatestEdgesOf(Class<? extends Relation> type) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    LOG.debug("Begin get latest edges for [{}]", type);

    Stopwatch queryStopwatch = Stopwatch.createStarted();
    LOG.debug("Begin query latest edges for [{}]", type);
    Iterable<Edge> edges = queryLatestByType(type).edges();
    LOG.debug("End querying latest edges for [{}] in [{}]", type, queryStopwatch.stop());

    LOG.debug("End get latest edges for [{}] in [{}]", type, stopwatch.stop());
    return edges.iterator();
  }

  /**
   * Filter the latest edges from the iterable. It also filters the system edges, like VERSION_OF.
   * @param edges the edges to the latest from
   * @return the latest non-system edges
   */
  public Iterator<Edge> getLatestEdges(Iterable<Edge> edges) {

    Map<String, Edge> latestEdgeMap = Maps.newHashMap();
    for (Iterator<Edge> iterator = edges.iterator(); iterator.hasNext(); ) {
      Edge edge = iterator.next();

      String id = getIdProperty(edge);

      Edge mappedEdge = latestEdgeMap.get(id);

      if (!isVersionOfEdge(edge) && (mappedEdge == null || isLaterEdge(edge, mappedEdge))) {
        latestEdgeMap.put(id, edge);
      }

    }

    return latestEdgeMap.values().iterator();
  }

  private boolean isVersionOfEdge(Edge edge) {
    return VERSION_OF_LABEL.equals(edge.getLabel());
  }

  private boolean isLaterEdge(Edge edge, Edge mappedEdge) {
    return getRevisionProperty(edge) > getRevisionProperty(mappedEdge);
  }

  public Iterator<Vertex> getVerticesWithId(Class<? extends Entity> type, String id) {
    return queryByType(type).has(DB_ID_PROP_NAME, id).vertices().iterator();
  }

  public Iterator<Vertex> findLatestVerticesByProperty(Class<? extends Entity> type, String propertyName, String propertyValue) {
    Iterable<Vertex> vertices = queryByType(type).has(IS_LATEST, true).has(propertyName, propertyValue).vertices();

    return vertices.iterator();
  }


  public Iterator<Vertex> findVerticesWithoutProperty(Class<? extends DomainEntity> type, String propertyName) {
    Iterable<Vertex> vertices = queryByType(type).hasNot(propertyName).vertices();

    return vertices.iterator();
  }

  public Iterator<Edge> findEdgesWithoutProperty(Class<? extends Relation> relationType, String propertyName) {
    Iterable<Edge> edges = db.query().hasNot(propertyName).edges();

    return edges.iterator();
  }

  public Iterator<Edge> findLatestEdgesByProperty(Class<? extends Relation> type, String propertyName, String propertyValue) {
    Iterable<Edge> edges = queryLatest().has(propertyName, propertyValue).edges();

    return edges.iterator();
  }

  /**
   * Returns all the latest outgoing edges of the latest version of
   * the vertex with the id property with the value of sourceId.
   *
   * @param type     the type of the relation to find
   * @param sourceId the id of the vertex to find the edges for
   * @return the found edges or an empty iterator non are found
   */
  public Iterator<Edge> findEdgesBySource(Class<? extends Relation> type, String sourceId) {
    return getEdgesByVertex(sourceId, Direction.OUT);
  }

  /**
   * Returns all the latest incoming edges of the latest version of
   * the vertex with the id property with the value of targetId.
   *
   * @param type     the type of the relation to find
   * @param targetId the id of the vertex to find the edges for
   * @return the found edges or an empty iterator non are found
   */
  public Iterator<Edge> findEdgesByTarget(Class<? extends Relation> type, String targetId) {
    return getEdgesByVertex(targetId, Direction.IN);
  }

  private Iterator<Edge> getEdgesByVertex(String vertexId, Direction direction) {
    Iterable<Vertex> vertices = queryLatest().has(DB_ID_PROP_NAME, vertexId).vertices();

    Iterator<Vertex> iterator = vertices.iterator();
    if (iterator.hasNext()) {
      Iterable<Edge> outgoingEdges = iterator.next().query().direction(direction).has(IS_LATEST, true).edges();

      return outgoingEdges.iterator();
    }

    return Lists.<Edge>newArrayList().iterator();
  }

  public Iterator<Edge> findEdges(Class<? extends Relation> type, TimbuctooQuery query) {
    Iterable<Edge> edges = query.createGraphQuery(queryBuilderFactory.newQueryBuilder(type)).edges();

    TinkerPopResultFilter<Edge> filter = resultFilterBuilder.buildFor(query);

    Iterable<Edge> filteredEdges = filter.filter(edges);

    return filteredEdges.iterator();
  }

  public <T extends Entity> Iterator<Vertex> findVertices(Class<T> type, TimbuctooQuery query) {
    Iterable<Vertex> vertices = query.createGraphQuery(queryBuilderFactory.newQueryBuilder(type)).vertices();

    TinkerPopResultFilter<Vertex> resultFilter = resultFilterBuilder.buildFor(query);

    Iterable<Vertex> filteredVertices = resultFilter.filter(vertices);

    return filteredVertices.iterator();
  }

}
