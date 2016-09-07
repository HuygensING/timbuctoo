package nl.knaw.huygens.timbuctoo.database.dto;

import nl.knaw.huygens.timbuctoo.database.EntityMapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;

public class EntityIterator implements Iterator<Entity> {

  private final EntityMapper entityMapper;
  private final GraphTraversal<Vertex, Vertex> entityVertices;

  public EntityIterator(EntityMapper entityMapper, GraphTraversal<Vertex, Vertex> entityVertices) {
    this.entityMapper = entityMapper;
    this.entityVertices = entityVertices;
  }

  @Override
  public boolean hasNext() {
    return entityVertices.hasNext();
  }

  @Override
  public Entity next() {
    Vertex next = entityVertices.next();

    return entityMapper.mapEntity(next);
  }
}
