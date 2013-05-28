package nl.knaw.huygens.repository.index;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.pubsub.Hub;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Collection of document indexers.
 */
@Singleton
public class DocumentIndexers {

  private final Map<Class<? extends Document>, DocumentIndexer<? extends Document>> indexers;

  @Inject
  public DocumentIndexers(Configuration config, DocTypeRegistry registry, LocalSolrServer server, Hub hub) {
    indexers = Maps.newHashMap();
    for (String doctype : config.getSettings("indexeddoctypes")) {
      Class<? extends Document> type = registry.getClassFromWebServiceTypeString(doctype);
      // Better safe than sorry, this is also checked by the configuration validator...
      if (type == null) {
        throw new RuntimeException("Invalid document type: " + doctype);
      }
      indexers.put(type, SolrDocumentIndexer.newInstance(type, server, hub));
    }
  }

  public <T extends Document> DocumentIndexer<T> indexerForType(Class<T> type) {
    @SuppressWarnings("unchecked")
    DocumentIndexer<T> indexer = (DocumentIndexer<T>) indexers.get(type);
    return (indexer != null) ? indexer : new NoDocumentIndexer<T>();
  }

  public Set<Class<? extends Document>> getIndexedTypes() {
    return Collections.unmodifiableSet(indexers.keySet());
  }

}
