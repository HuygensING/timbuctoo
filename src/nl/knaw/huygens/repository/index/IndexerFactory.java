package nl.knaw.huygens.repository.index;

import java.util.Map;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.util.RepositoryException;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class IndexerFactory {

  private final LocalSolrServer server;
  private final ModelIterator modelIterator;
  private Map<Class<? extends Document>, DocumentIndexer<? extends Document>> indexers;
  private final Hub hub;

  @Inject
  public IndexerFactory(ModelIterator modelIterator, LocalSolrServer server, Hub hub) {
    this.server = server;
    this.modelIterator = modelIterator;
    this.indexers = Maps.newHashMap();
    this.hub = hub;
  }

  @SuppressWarnings("unchecked")
  public <T extends Document> DocumentIndexer<T> getIndexForType(Class<T> cls) {
    if (indexers.containsKey(cls)) {
      return (DocumentIndexer<T>) indexers.get(cls);
    }
    DocumentIndexer<T> rv = new DocumentIndexer<T>(cls, modelIterator, server, hub);
    indexers.put(cls, rv);
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
