package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.dto.QuickSearch;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface IndexHandler {
  boolean hasIndexFor(Collection collection);

  GraphTraversal<Vertex, Vertex> findByQuickSearch(Collection collection, QuickSearch quickSearch);

  GraphTraversal<Vertex, Vertex> findKeywordsByQuickSearch(Collection collection, QuickSearch quickSearch,
                                                           String keywordType);

  void addToQuickSearchIndex(Collection collection, String displayName, Vertex vertex);
}
