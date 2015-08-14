package nl.knaw.huygens.timbuctoo.index;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;

import java.util.UUID;

public class IndexRequestStatus {

  private final Cache<String, IndexRequest> cache;

  @Inject
  public IndexRequestStatus() {
    this(CacheBuilder.newBuilder().build());
  }

  IndexRequestStatus(Cache<String, IndexRequest> cache) {
    this.cache = cache;
  }

  public String add(IndexRequest indexRequest) {
    String id = UUID.randomUUID().toString();
    cache.put(id, indexRequest);

    return id;
  }

  public IndexRequest get(String id) {
    return cache.getIfPresent(id);
  }
}
