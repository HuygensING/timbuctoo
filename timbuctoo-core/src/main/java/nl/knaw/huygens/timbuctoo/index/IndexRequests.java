package nl.knaw.huygens.timbuctoo.index;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.UUID;

// FIXME: Should be removed when ActiveMQ is made persistent. See TIM-403 and TIM
@Singleton
public class IndexRequests {

  private final Cache<String, IndexRequest> cache;

  @Inject
  public IndexRequests() {
    this(CacheBuilder.newBuilder().build());
  }

  IndexRequests(Cache<String, IndexRequest> cache) {
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
