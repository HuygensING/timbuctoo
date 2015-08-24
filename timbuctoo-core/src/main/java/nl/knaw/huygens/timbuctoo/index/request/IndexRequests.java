package nl.knaw.huygens.timbuctoo.index.request;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Singleton;

import java.util.Map;
import java.util.UUID;

// FIXME: Should be removed when ActiveMQ is made persistent. See TIM-403 and TIM
@Singleton
public class IndexRequests {

  public static final int TEN_MINUTES = 10 * 3600 * 1000;
  private final Cache<String, IndexRequest> cache;
  private final int timeout;

  public IndexRequests() {
    this(TEN_MINUTES);
  }

  IndexRequests(int timeout) {
    this.timeout = timeout;
    this.cache = CacheBuilder.newBuilder().build();
  }


  public String add(IndexRequest indexRequest) {
    String id = UUID.randomUUID().toString();
    cache.put(id, indexRequest);

    purge();

    return id;
  }

  public IndexRequest get(String id) {
    IndexRequest request = cache.getIfPresent(id);

    purge();

    return request;
  }

  private void purge() {
    for (Map.Entry<String, IndexRequest> entry : cache.asMap().entrySet()) {
      IndexRequest value = entry.getValue();
      if (value.canBeDiscarded(timeout)){
        cache.invalidate(entry.getKey());
      }
    }
  }
}
