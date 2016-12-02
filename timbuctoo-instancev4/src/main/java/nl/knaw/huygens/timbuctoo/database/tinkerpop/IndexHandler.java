package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.dto.QuickSearch;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.UUID;

public interface IndexHandler {
  //=====================quick search index=====================
  boolean hasQuickSearchIndexFor(Collection collection);

  GraphTraversal<Vertex, Vertex> findByQuickSearch(Collection collection, QuickSearch quickSearch);

  GraphTraversal<Vertex, Vertex> findKeywordsByQuickSearch(Collection collection, QuickSearch quickSearch,
                                                           String keywordType);

  /**
   * This method does not prevent multiple entries for a vertex.
   * Use {@link #upsertIntoQuickSearchIndex(Collection, String, Vertex)} if you want to update the existing
   * entry or a new entry for unknown vertices.
   */
  void insertIntoQuickSearchIndex(Collection collection, String quickSearchValue, Vertex vertex);

  /**
   * This method adds or update a index entry and makes shure the index have only one entry for each vertex.
   */
  void upsertIntoQuickSearchIndex(Collection collection, String quickSearchValue, Vertex vertex);

  void removeFromQuickSearchIndex(Collection collection, Vertex vertex);

  //=====================tim_id index=====================
  GraphTraversal<Vertex, Vertex> findById(UUID timId);

  /**
   * This method does not prevent multiple entries for a vertex.
   * Use {@link #upsertIntoIdIndex(UUID, Vertex)} if you want to update the existing entry
   * or a new entry for unknown vertices.
   */
  void insertIntoIdIndex(UUID timId, Vertex vertex);

  /**
   * This method adds or update a index entry and makes shure the index have only one entry for each vertex.
   */
  void upsertIntoIdIndex(UUID timId, Vertex vertex);

  void removeFromIdIndex(Vertex vertex);
}
