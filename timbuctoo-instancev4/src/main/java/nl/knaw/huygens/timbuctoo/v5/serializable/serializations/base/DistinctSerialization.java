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
    if (!distinctUris.contains(entity.getUri())) {
      distinctUris.add(entity.getUri());
      onDistinctEntity(entity);
    }
  }

  public void onDistinctEdge(Edge edge) throws IOException {
    //System.out.println(edge);
  }

  public void onDistinctEntity(Entity entity) throws IOException {
    //System.out.println(entity);
  }

  public boolean isEntityDeclared(Entity entity) {
    return distinctUris.contains(entity.getUri());
  }
}
