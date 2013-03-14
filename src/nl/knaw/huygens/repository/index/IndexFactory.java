package nl.knaw.huygens.repository.index;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.util.RepositoryException;

@Singleton
public class IndexFactory {
  private final LocalSolrServer server;
  private final ModelIterator modelIterator;
  private Map<Class<? extends Document>, DocumentIndexer<? extends Document>> indexerCache;
  private final Hub hub;

  @Inject
  public IndexFactory(ModelIterator modelIterator, LocalSolrServer server, Hub hub) {
    this.server = server;
    this.modelIterator = modelIterator;
    this.indexerCache = Maps.newHashMap();
    this.hub = hub;
  }

  @SuppressWarnings("unchecked")
  public <T extends Document> DocumentIndexer<T> getIndexForType(Class<T> cls) {
    if (indexerCache.containsKey(cls)) {
      return (DocumentIndexer<T>) indexerCache.get(cls);
    }
    DocumentIndexer<T> rv = new DocumentIndexer<T>(cls, modelIterator, server, hub);
    indexerCache.put(cls, rv);
    return rv;
  }


  public void flushIndices() throws RepositoryException {
    try {
      server.commitAllChanged();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RepositoryException(e);
    }
  }

  public void close() {
    try {
      server.shutdown();
    } catch (Exception ex) {
      System.err.println("Failed to shut down solr server.");
      ex.printStackTrace();
    }
  }
}
