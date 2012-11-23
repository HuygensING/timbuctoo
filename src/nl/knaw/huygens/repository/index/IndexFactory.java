package nl.knaw.huygens.repository.index;

import java.util.Map;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.util.MarginalScholarshipException;

import com.google.common.collect.Maps;

public class IndexFactory {
  private final LocalSolrServer server;
  private final ModelIterator modelIterator;
  private Map<Class<? extends Document>, DocumentIndexer<? extends Document>> indexerCache;

  public IndexFactory(ModelIterator modelIterator, LocalSolrServer server) {
    this.server = server;
    this.modelIterator = modelIterator;
    this.indexerCache = Maps.newHashMap();
  }

  @SuppressWarnings("unchecked")
  public <T extends Document> DocumentIndexer<T> getIndexForType(Class<T> cls) {
    if (indexerCache.containsKey(cls)) {
      return (DocumentIndexer<T>) indexerCache.get(cls);
    }
    DocumentIndexer<T> rv = new DocumentIndexer<T>(cls, modelIterator, server);
    indexerCache.put(cls, rv);
    return rv;
  }


  public void flushIndices() throws MarginalScholarshipException {
    try {
      server.commitAllChanged();
    } catch (Exception e) {
      e.printStackTrace();
      throw new MarginalScholarshipException(e);
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
