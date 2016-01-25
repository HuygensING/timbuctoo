package nl.knaw.huygens.timbuctoo.search;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import nl.knaw.huygens.timbuctoo.util.Timeout;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.Optional;
import java.util.UUID;

public class Searcher {

  private final Cache<UUID, SearchResult> cache;
  private final Graph graph;

  public Searcher(Graph graph, Timeout searchResultAvailabilityTime) {
    this.graph = graph;
    cache = createCache(searchResultAvailabilityTime);
  }

  private static Cache<UUID, SearchResult> createCache(Timeout timeout) {
    return CacheBuilder.newBuilder().expireAfterAccess(timeout.duration, timeout.timeUnit).build();
  }

  public Optional<SearchResult> getSearchResult(UUID id) {
    return Optional.ofNullable(cache.getIfPresent(id));
  }

  public UUID search(TimbuctooQuery query) {

    UUID id = UUID.randomUUID();
    cache.put(id, query.execute(graph));
    return id;
  }

}
