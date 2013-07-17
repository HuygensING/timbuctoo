package nl.knaw.huygens.repository.index;

import java.util.Map;

import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.util.RepositoryException;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Deprecated
public class IndexerFactory {

  private final LocalSolrServer server;
  private final Map<Class<? extends Document>, DocumentIndexer<? extends Document>> indexers;

  @Inject
  public IndexerFactory(Configuration config, DocTypeRegistry registry, LocalSolrServer server, Hub hub) {
    this.server = server;

    indexers = Maps.newHashMap();
    for (String doctype : config.getSettings("indexeddoctypes")) {
      Class<? extends Document> type = registry.getTypeForIName(doctype);
      indexers.put(type, SolrDocumentIndexer.newInstance(type, server, hub));
    }
  }

  public <T extends Document> DocumentIndexer<T> indexerForType(Class<T> type) {
    @SuppressWarnings("unchecked")
    DocumentIndexer<T> indexer = (DocumentIndexer<T>) indexers.get(type);
    return (indexer != null) ? indexer : new NoDocumentIndexer<T>();
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
      System.err.println("Failed to shut down Solr server");
      e.printStackTrace();
    }
  }

}
