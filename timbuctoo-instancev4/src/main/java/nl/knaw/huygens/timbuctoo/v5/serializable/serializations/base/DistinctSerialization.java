package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on 2017-06-08 09:35.
 */
public class DistinctSerialization extends BaseSerialization {

  private Set<String> distinctUris = new HashSet<>();
  private Set<Integer> distinctEdgeHashes = new HashSet<>();

  @Override
  public void onEdge(Edge edge) throws IOException {
    int hash = edge.hashCode();
    if (!distinctEdgeHashes.contains(hash)) {
      distinctEdgeHashes.add(hash);
      onDistinctEdge(edge);
    }
  }

  @Override
  public void onEntity(Entity entity) throws IOException {
    if (entity == null || entity.getUri() == null) {
      return;
    }
    if (!distinctUris.contains(entity.getUri())) {
      distinctUris.add(entity.getUri());
      onDistinctEntity(entity);
    }
  }

  /**
   * Called for each distinct edge in the graph. Both source entity and target entity are on the given edge, though
   * no guarantee is given for completeness of these entities. This implementation does nothing,
   * subclasses may override.
   *
   * @param edge a distinct edge from the graph
   * @throws IOException if the edge could not be written to the output stream
   */
  public void onDistinctEdge(Edge edge) throws IOException {
    //System.out.println(edge);
  }

  /**
   * Called for each distinct entity in the graph. Outgoing edges on the entity are populated, no guarantee is
   * given for completeness of incomming edges. This implementation does nothing, subclasses may override.
   *
   * @param entity a distinct entity from the graph
   * @throws IOException if the entity could not be written to the output stream
   */
  public void onDistinctEntity(Entity entity) throws IOException {
    //System.out.println(entity);
  }

  public boolean isEntityDeclared(Entity entity) {
    if (entity == null || entity.getUri() == null) {
      return false;
    }
    return distinctUris.contains(entity.getUri());
  }
}
