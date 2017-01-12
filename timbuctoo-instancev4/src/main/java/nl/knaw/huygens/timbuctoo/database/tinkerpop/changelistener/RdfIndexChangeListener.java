package nl.knaw.huygens.timbuctoo.database.tinkerpop.changelistener;

import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.IndexHandler;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;

public class RdfIndexChangeListener implements ChangeListener {

  private final IndexHandler indexHandler;

  public RdfIndexChangeListener(IndexHandler indexHandler) {
    this.indexHandler = indexHandler;
  }

  @Override
  public void onCreate(Collection collection, Vertex vertex) {
    getProp(vertex, "rdfUri", String.class).ifPresent(uri -> {
      indexHandler.addVertexToRdfIndex(collection.getVre(), uri, vertex);
    });
  }

  @Override
  public void onPropertyUpdate(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    oldVertex.ifPresent(vertex -> indexHandler.removeFromRdfIndex(collection.getVre(), vertex));
    getProp(newVertex, "rdfUri", String.class).ifPresent(uri -> {
      indexHandler.addVertexToRdfIndex(collection.getVre(), uri, newVertex);
    });
  }

  @Override
  public void onRemoveFromCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
  }

  @Override
  public void onAddToCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
  }

  @Override
  public void onCreateEdge(Collection collection, Edge edge) {

  }
}
