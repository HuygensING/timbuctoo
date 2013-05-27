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
  private final Hub hub;
  private final Map<Class<? extends Document>, SolrDocumentIndexer<? extends Document>> indexers;

  @Inject
  public IndexerFactory(ModelIterator modelIterator, LocalSolrServer server, Hub hub) {
    this.server = server;
    this.modelIterator = modelIterator;
    this.hub = hub;
    indexers = Maps.newHashMap();
  }

  public synchronized <T extends Document> SolrDocumentIndexer<T> getIndexForType(Class<T> type) {
    @SuppressWarnings("unchecked")
    SolrDocumentIndexer<T> indexer = (SolrDocumentIndexer<T>) indexers.get(type);
    if (indexer == null) {
      indexer = new SolrDocumentIndexer<T>(type, modelIterator, server, hub);
      indexers.put(type, indexer);
    }
    return indexer;
  }

  public void clearIndexes() {
    try {
      server.deleteAll();
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
  }

  public void flushIndices() throws RepositoryException {
    try {
      server.commitAll();
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
  }

  public void close() {
    try {
      server.commitAll();
      server.shutdown();
    } catch (Exception e) {
      System.err.println("Failed to shut down solr server.");
      e.printStackTrace();
    }
  }

}
