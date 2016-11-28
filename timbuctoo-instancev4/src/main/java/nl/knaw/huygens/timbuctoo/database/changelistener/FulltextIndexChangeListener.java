package nl.knaw.huygens.timbuctoo.database.changelistener;


import nl.knaw.huygens.timbuctoo.database.IndexHandler;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

public class FulltextIndexChangeListener implements ChangeListener {

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
    handleChange(collection, newVertex);
  }

  @Override
  public void onRemoveFromCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    handleRemove(collection, newVertex);
  }

  @Override
  public void onAddToCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    handleChange(collection, newVertex);
  }

  private void handleChange(Collection collection, Vertex vertex) {
    GraphTraversal<Vertex, Vertex> traversal = graphWrapper.getGraph().traversal().V(vertex.id());
    String docCaption = traversal.asAdmin().clone()
      .union(collection.getDisplayName().traversalJson())
      .next()
      .getOrElse(jsn(""))
      .asText();
    if (collection.getEntityTypeName().equals("wwdocument")) {
      Collection wwpersons = collection.getVre().getCollectionForCollectionName("wwpersons").get();
      List<String> authors = traversal.out("isCreatedBy")
        .union(wwpersons.getDisplayName().traversalJson()).map(x -> x.get().getOrElse(jsn("")).asText())
        .toList();
      Collections.sort(authors);
      String authorCaption = String.join(
        "; ",
        authors
      );
      docCaption = authorCaption + " " + docCaption;
    }
    indexHandler.setDisplayNameIndex(collection, docCaption, vertex);
  }

  private void handleRemove(Collection collection, Vertex vertex) {
    indexHandler.removeFromDisplayNameIndex(collection, vertex);
  }
}
