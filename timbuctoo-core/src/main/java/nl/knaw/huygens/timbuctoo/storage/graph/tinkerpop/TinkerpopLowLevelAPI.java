package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType.VERSION_OF;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getIdProperty;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getRevisionProperty;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;

class TinkerpopLowLevelAPI {

  private static final IsLatestVersionOfVertex IS_LATEST_VERSION_OF_VERTEX = new IsLatestVersionOfVertex();
  private final Graph db;
  private final VertexDuplicator vertexDuplicator;
  private final EdgeDuplicator edgeDuplicator;

  public TinkerpopLowLevelAPI(Graph db) {
    this(db, new VertexDuplicator(db), new EdgeDuplicator());
  }

  public TinkerpopLowLevelAPI(Graph db, VertexDuplicator vertexDuplicator, EdgeDuplicator edgeDuplicator) {
    this.db = db;
    this.vertexDuplicator = vertexDuplicator;
    this.edgeDuplicator = edgeDuplicator;
  }

  public <T extends Entity> Vertex getLatestVertexById(Class<T> type, String id) {
    // this is needed to check if the type array contains the value requeste type
    Iterable<Vertex> foundVertices = queryByType(type).has(ID_PROPERTY_NAME, id) //
        .vertices();

    for (Vertex vertex : foundVertices) {
      if (IS_LATEST_VERSION_OF_VERTEX.apply(vertex)) {
        return vertex;
      }
    }

    return null;
  }

  private <T extends Entity> GraphQuery queryByType(Class<T> type) {
    return db.query() //
        .has(ELEMENT_TYPES, isOfType(), TypeNames.getInternalName(type)) //
    ;
  }

  private com.tinkerpop.blueprints.Predicate isOfType() {
    return new com.tinkerpop.blueprints.Predicate() {
      @Override
      public boolean evaluate(Object first, Object second) {
        if (first != null && first.getClass().isArray()) {
          Object[] array = (Object[]) first;
          return Lists.newArrayList(array).contains(second);
        }
        return false;
      }
    };
  }

  private static final class IsLatestVersionOfVertex implements Predicate<Vertex> {
    @Override
    public boolean apply(Vertex vertex) {
      Iterable<Edge> incomingVersionOfEdges = vertex.getEdges(Direction.IN, VERSION_OF.name());
      return incomingVersionOfEdges == null || !incomingVersionOfEdges.iterator().hasNext();
    }
  }

  public Vertex getVertexWithRevision(Class<? extends DomainEntity> type, String id, int revision) {
    Iterable<Vertex> vertices = queryByType(type)//
        .has(ID_PROPERTY_NAME, id)//
        .has(REVISION_PROPERTY_NAME, revision)//
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
    Edge latestEdge = null;
    Iterable<Edge> edges = db.query().has(ID_PROPERTY_NAME, id).edges();
    Predicate<Edge> isLaterEdge = new Predicate<Edge>() {
      private int latestRev = 0;

      @Override
      public boolean apply(Edge edge) {
        int rev = ElementHelper.getRevisionProperty(edge);
        if (rev > latestRev) {
          latestRev = rev;
          return true;
        }
        return false;
      }
    };

    for (Iterator<Edge> iterator = edges.iterator(); iterator.hasNext();) {
      Edge edge = iterator.next();
      if (isLaterEdge.apply(edge)) {
        latestEdge = edge;
      }
    }

    return latestEdge;
  }

  public Iterator<Vertex> getLatestVerticesOf(Class<? extends Entity> type) {
    Iterable<Vertex> allVertices = queryByType(type).vertices();

    List<Vertex> latestVertices = getLatestVertices(allVertices);

    return latestVertices.iterator();

  }

  private List<Vertex> getLatestVertices(Iterable<Vertex> allVertices) {
    List<Vertex> latestVertices = Lists.newArrayList();
    for (Iterator<Vertex> iterator = allVertices.iterator(); iterator.hasNext();) {
      Vertex vertex = iterator.next();
      if (IS_LATEST_VERSION_OF_VERTEX.apply(vertex)) {
        latestVertices.add(vertex);
      }
    }
    return latestVertices;
  }

  public void duplicate(Vertex vertex) {
    vertexDuplicator.duplicate(vertex);
  }

  public void duplicate(Edge edge) {
    edgeDuplicator.duplicate(edge);
  }

  public Edge getEdgeWithRevision(Class<? extends Relation> relationType, String id, int revision) {
    Iterable<Edge> edges = db.query().has(ID_PROPERTY_NAME, id).has(REVISION_PROPERTY_NAME, revision).edges();
    return getFirstFromIterable(edges);
  }

  public Iterator<Edge> getLatestEdgesOf(Class<? extends Relation> type) {
    Map<String, Edge> idEdgeMap = Maps.newHashMap();

    Iterable<Edge> edges = db.query().edges();

    for (Iterator<Edge> iterator = edges.iterator(); iterator.hasNext();) {
      Edge edge = iterator.next();

      String id = getIdProperty(edge);

      Edge mappedEdge = idEdgeMap.get(id);

      if (mappedEdge == null || isLaterEdge(edge, mappedEdge)) {
        idEdgeMap.put(id, edge);
      }

    }

    return idEdgeMap.values().iterator();
  }

  private boolean isLaterEdge(Edge edge, Edge mappedEdge) {
    return getRevisionProperty(edge) > getRevisionProperty(mappedEdge);
  }

  public Iterator<Vertex> getVerticesWithId(Class<? extends Entity> type, String id) {
    return queryByType(type).has(ID_PROPERTY_NAME, id).vertices().iterator();
  }

  public Iterator<Vertex> findVerticesByProperty(Class<? extends Entity> type, String propertyName, String propertyValue) {
    Iterable<Vertex> vertices = queryByType(type).has(propertyName, propertyValue).vertices();

    return getLatestVertices(vertices).iterator();
  }

  public Iterator<Edge> findEdgesByProperty(Class<? extends Relation> type, String propertyName, String propertyValue) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }
}
