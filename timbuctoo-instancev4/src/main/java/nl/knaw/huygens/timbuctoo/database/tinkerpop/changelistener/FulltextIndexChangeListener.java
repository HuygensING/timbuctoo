package nl.knaw.huygens.timbuctoo.database.tinkerpop.changelistener;


import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.IndexHandler;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

public class FulltextIndexChangeListener implements ChangeListener {
  private static final Logger LOG = LoggerFactory.getLogger(FulltextIndexChangeListener.class);

  private final IndexHandler indexHandler;
  private final GraphWrapper graphWrapper;

  public FulltextIndexChangeListener(IndexHandler indexHandler, GraphWrapper graphWrapper) {
    this.indexHandler = indexHandler;
    this.graphWrapper = graphWrapper;
  }

  @Override
  public void onCreate(Collection collection, Vertex vertex) {
    handleChange(collection, vertex);
  }

  @Override
  public void onPropertyUpdate(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    oldVertex.ifPresent(vertex -> handleRemove(collection, vertex));
    handleChange(collection, newVertex);
  }

  @Override
  public void onRemoveFromCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    oldVertex.ifPresent(vertex -> handleRemove(collection, vertex));
    handleRemove(collection, newVertex);
  }

  @Override
  public void onAddToCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    handleChange(collection, newVertex);
  }

  @Override
  public void onCreateEdge(Collection collection, Edge edge) {

  }

  @Override
  public void onEdgeUpdate(Collection collection, Edge oldEdge, Edge newEdge) {

  }

  private void handleChange(Collection collection, Vertex vertex) {
    GraphTraversal<Vertex, Vertex> traversal = graphWrapper.getGraph().traversal().V(vertex.id());
    final GraphTraversal<Vertex, Try<JsonNode>> displayNameT = traversal.asAdmin().clone()
                                                                 .union(collection.getDisplayName().traversalJson());
    String docCaption = displayNameT.hasNext() ?
      displayNameT.next().getOrElse(jsn("")).asText() :
      null;

    if (collection.getEntityTypeName().equals("wwdocument")) {
      Collection wwpersons = collection.getVre().getCollectionForCollectionName("wwpersons").get();
      List<String> authors = traversal.out("isCreatedBy")
                                      .union(wwpersons.getDisplayName().traversalJson())
                                      .map(x -> x.get().getOrElse(jsn("")).asText())
                                      .toList();
      Collections.sort(authors);
      String authorCaption = String.join(
        "; ",
        authors
      );
      docCaption = authorCaption + " " + docCaption;
    }
    if (docCaption == null) {
      LOG.warn(databaseInvariant,
        "Displayname traversal resulted in no results vertexId={} collection={} propertyType={}",
        vertex.id(), collection.getCollectionName(), collection.getDisplayName().getUniqueTypeId());
    } else {
      indexHandler.insertIntoQuickSearchIndex(collection, docCaption, vertex);
    }
  }

  private void handleRemove(Collection collection, Vertex vertex) {
    indexHandler.removeFromQuickSearchIndex(collection, vertex);
  }
}
