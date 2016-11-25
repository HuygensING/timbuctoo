package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface IndexHandler {
  boolean hasIndexFor(Collection collection);

  GraphTraversal<Vertex, Vertex> findByQuickSearch(Collection collection, String query);

  GraphTraversal<Vertex, Vertex> findKeywordsByQuickSearch(Collection collection, String query, String keywordType);

  void addToQuickSearchIndex(Collection collection, String displayName, Vertex vertex);
}
