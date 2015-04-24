package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType.VERSION_OF;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;

import java.util.Iterator;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;

class TinkerpopLowLevelAPI {

  private final Graph db;

  public TinkerpopLowLevelAPI(Graph db) {
    this.db = db;
  }

  public <T extends Entity> Vertex getLatestVertexById(Class<T> type, String id) {
    // this is needed to check if the type array contains the value requeste type
    Iterable<Vertex> foundVertices = queryByType(type).has(ID_PROPERTY_NAME, id) //
        .vertices();

    IsLatestVersionOfVertex isLatestVersionOfVertex = new IsLatestVersionOfVertex();

    for (Vertex vertex : foundVertices) {
      if (isLatestVersionOfVertex.apply(vertex)) {
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
    Vertex vertex = null;
    Iterable<Vertex> vertices = queryByType(type)//
        .has(ID_PROPERTY_NAME, id)//
        .has(REVISION_PROPERTY_NAME, revision)//
        .vertices();

    Iterator<Vertex> iterator = vertices.iterator();

    if (iterator.hasNext()) {
      vertex = iterator.next();
    }

    return vertex;
  }

  public Edge getLatestEdgeById(Class<? extends Relation> relationType, String id) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
