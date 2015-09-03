package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public final class IsOfTypePredicate implements com.tinkerpop.blueprints.Predicate {

  public static final Logger LOG = LoggerFactory.getLogger(IsOfTypePredicate.class);
  private final LoadingCache<Object, String> cache;

  public IsOfTypePredicate() {
    cache = CacheBuilder.newBuilder().maximumSize(100).build(new CacheLoader<Object, String>() {
      @Override
      public String load(Object key) throws Exception {
        return "\"" + key + "\"";
      }
    });
  }

  @Override
  public boolean evaluate(Object object, Object shouldApplyTo) {
    if (object != null && (object instanceof String)) {
      return ((String) object).contains(getSubstring(shouldApplyTo));
    }
    return false;
  }

  protected String getSubstring(Object shouldApplyTo) {
    String value = null;
    try { 
      value = cache.get(shouldApplyTo);
    } catch (ExecutionException e) {
      LOG.error("Error retrieving item from cache", e);
    }
    return value;
  }
}
