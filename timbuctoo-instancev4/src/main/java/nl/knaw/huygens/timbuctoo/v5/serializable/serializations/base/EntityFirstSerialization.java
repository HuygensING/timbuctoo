package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on 2017-06-08 09:46.
 */
public class EntityFirstSerialization extends DistinctSerialization {

  private Set<Edge> unpublishedEdges = new HashSet<>();

  @Override
  public void onDistinctEdge(Edge edge) throws IOException {
    if (isDeclared(edge)) {
      onDeclaredEntityEdge(edge);
    } else {
      unpublishedEdges.add(edge);
    }
  }

  private boolean isDeclared(Edge edge) {
    return isEntityDeclared(edge.getSourceEntity()) && (edge.isValueEdge() || isEntityDeclared(edge.getTargetEntity()));
  }

  /**
   * Called for each distinct edge in the graph. Subclasses may override but should call super.
   *
   * @param entity a distinct entity from the graph
   * @throws IOException if the entity could not be written to the output stream
   */
  @Override
  public void onDistinctEntity(Entity entity) throws IOException {
    Set<Edge> edgesToPublish = new HashSet<>();
    for (Edge edge : unpublishedEdges) {
      if (isDeclared(edge)) {
        edgesToPublish.add(edge);
      }
    }
    for (Edge edge : edgesToPublish) {
      onDeclaredEntityEdge(edge);
      unpublishedEdges.remove(edge);
    }
  }

  /**
   * Called for each distinct edge in the graph; entities on both ends of the edge have been passed previously
   * through {@link DistinctSerialization#onDistinctEntity(Entity)}. This implementation does nothing, subclasses
   * may override.
   *
   * @param edge a distinct edge from the graph
   * @throws IOException if the edge could not be written to the output stream
   */
  public void onDeclaredEntityEdge(Edge edge) throws IOException {

  }
}
