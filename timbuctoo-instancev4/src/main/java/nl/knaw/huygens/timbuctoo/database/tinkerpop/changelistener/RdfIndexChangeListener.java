package nl.knaw.huygens.timbuctoo.database.tinkerpop.changelistener;

import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.IndexHandler;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;
import static nl.knaw.huygens.timbuctoo.rdf.RdfProperties.RDF_SYNONYM_PROP;
import static nl.knaw.huygens.timbuctoo.rdf.RdfProperties.RDF_URI_PROP;

public class RdfIndexChangeListener implements ChangeListener {

  private final IndexHandler indexHandler;

  public RdfIndexChangeListener(IndexHandler indexHandler) {
    this.indexHandler = indexHandler;
  }

  @Override
  public void onCreate(Collection collection, Vertex vertex) {
    getProp(vertex, RDF_URI_PROP, String.class).ifPresent(uri -> {
      indexHandler.upsertIntoRdfIndex(collection.getVre(), uri, vertex);
    });
    getProp(vertex, RDF_SYNONYM_PROP, String[].class).ifPresent(uris -> {
      for (String uri : uris) {
        indexHandler.upsertIntoRdfIndex(collection.getVre(), uri, vertex);
      }
    });
  }

  @Override
  public void onPropertyUpdate(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    getProp(newVertex, RDF_URI_PROP, String.class).ifPresent(uri -> {
      indexHandler.upsertIntoRdfIndex(collection.getVre(), uri, newVertex);
    });
    getProp(newVertex, RDF_SYNONYM_PROP, String[].class).ifPresent(uris -> {
      for (String uri : uris) {
        indexHandler.upsertIntoRdfIndex(collection.getVre(), uri, newVertex);
      }
    });
  }

  @Override
  public void onRemoveFromCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    getProp(newVertex, RDF_URI_PROP, String.class).ifPresent(uri -> {
      indexHandler.upsertIntoRdfIndex(collection.getVre(), uri, newVertex);
    });
    getProp(newVertex, RDF_SYNONYM_PROP, String[].class).ifPresent(uris -> {
      for (String uri : uris) {
        indexHandler.upsertIntoRdfIndex(collection.getVre(), uri, newVertex);
      }
    });
  }

  @Override
  public void onAddToCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    //is always called when onCreate or onPropertyUpdate has already been called
  }

  @Override
  public void onCreateEdge(Collection collection, Edge edge) {
  }

  @Override
  public void onEdgeUpdate(Collection collection, Edge oldEdge, Edge newEdge) {
  }
}
