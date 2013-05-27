package nl.knaw.huygens.repository.managers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.events.Events.DocumentAddEvent;
import nl.knaw.huygens.repository.events.Events.DocumentDeleteEvent;
import nl.knaw.huygens.repository.events.Events.DocumentEditEvent;
import nl.knaw.huygens.repository.index.SolrDocumentIndexer;
import nl.knaw.huygens.repository.index.IndexerFactory;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.pubsub.Subscribe;
import nl.knaw.huygens.repository.variation.VariationUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This manager is responsible for dealing with listening for document changes and
 * effecting the necessary changes on the index.
 *
 * When written, the datamodel for marginal scholarship uses only direct references
 * from one object to the other, which are one-directional and always away from the
 * object which is the main index. This may have led to assumptions in designing it
 * which no longer hold when different datamodels are used. Caveat emptor.
 * @author gijs
 * 
 * Since we now use the commitWithin feature of Solr, there's no need for flushing
 * indexes, except when closing down.
 * Fixed: The old code registered an incomplete IndexManager instance with the Hub.
 */
@Singleton
public class IndexManager {

  private final DocTypeRegistry docTypeRegistry;
  private final StorageManager storageManager;
  private final IndexerFactory indexFactory;
  private final Set<Class<? extends Document>> indexedTypes;
  private final Map<Class<? extends Document>, List<Class<? extends Document>>> indexRelations;

  @Inject
  public IndexManager(Configuration config, DocTypeRegistry docTypeRegistry, StorageManager storageManager, IndexerFactory indexFactory) {
    this.docTypeRegistry = docTypeRegistry;
    this.storageManager = storageManager;
    this.indexFactory = indexFactory;
    indexedTypes = setupIndexedTypes(config);
    indexRelations = setupIndexRelations(config);
  }

  private Set<Class<? extends Document>> setupIndexedTypes(Configuration conf) {
    Set<Class<? extends Document>> indexedTypes = Sets.newHashSet();
    for (String typeString : conf.getSettings("indexeddoctypes")) {
      Class<? extends Document> type = docTypeRegistry.getClassFromWebServiceTypeString(typeString);
      // Better safe than sorry, this is also checked by the configuration validator...
      if (type == null) {
        throw new RuntimeException("Invalid index type: " + typeString);
      }
      indexedTypes.add(type);
    }
    return indexedTypes;
  }

  @Subscribe
  public <T extends Document> void onDocumentAdd(DocumentAddEvent<T> event) {
    Class<T> type = event.getCls();
    if (indexedTypes.contains(type)) {
      List<T> doc = event.getDocuments();
      indexFactory.getIndexForType(type).add(doc);
    }
  }

  @Subscribe
  public <T extends Document> void onDocumentEdit(DocumentEditEvent<T> event) {
    Class<T> type = event.getCls();
    if (indexedTypes.contains(type)) {
      List<T> docs = event.getDocuments();
      indexFactory.getIndexForType(type).modify(docs);
    }
  }

  @Subscribe
  public <T extends Document> void onDocumentDelete(DocumentDeleteEvent<T> event) {
    Class<T> type = event.getCls();
    if (indexedTypes.contains(type)) {
      List<T> doc = event.getDocuments();
      indexFactory.getIndexForType(type).remove(doc);
    }
  }

  public void clearIndexes() {
    indexFactory.clearIndexes();
  }

  public void close() {
    try {
      indexFactory.close();
    } catch (Exception e) {
      System.err.println("Failed to shut down IndexerFactory");
      e.printStackTrace();
    }
  }

  // -------------------------------------------------------------------

  private Map<Class<? extends Document>, List<Class<? extends Document>>> setupIndexRelations(Configuration conf) {
    Map<Class<? extends Document>, List<Class<? extends Document>>> indexRelations = Maps.newHashMap();
    List<String> keys = conf.getSettingKeys("indexrelations.");
    for (String k : keys) {
      String values = conf.getSetting("indexrelations." + k, "");
      if (!values.isEmpty()) {
        String[] items = values.split(",");
        if (items.length > 0) {
          List<Class<? extends Document>> clsList = Lists.newArrayList();
          for (String item : items) {
            clsList.add(docTypeRegistry.getClassFromWebServiceTypeString(item));
          }
          indexRelations.put(docTypeRegistry.getClassFromWebServiceTypeString(k), clsList);
        }
      }
    }
    return indexRelations;
  }

  /**
   * This (and the code below) was used for handling edits of related documents. It's currenty disabled and will need to be reviewed.
   * @param referredCls
   * @param referredDoc
   * @param flush
   */
  @SuppressWarnings("unused")
  private <T extends Document> void handleRelatedEdit(Class<T> referredCls, T referredDoc, boolean flush) {
    Map<Class<? extends Document>, Set<String>> docsToIndex = getAllReferringDocIds(referredCls, referredDoc.getId());
    modifyAllDocs(docsToIndex);
    if (flush) {
      indexFactory.flushIndices();
    }
  }

  /**
   * This method and its little sisters (getReferringDocIdsByMethod and getReferringDocIdsOfTypeByMethod) collect all the IDs of documents
   * that somehow refer to the first document passed in, in order to re-index them.
   * @param docsToIndex Accumulator map in which all the referring documents are stored.
   * @param referredCls the type of document to which is being referred.
   * @param referredId the document ID to which is being referred
   */
  private <T extends Document> Map<Class<? extends Document>, Set<String>> getAllReferringDocIds(Class<T> referredCls, String... referredId) {
    Map<Class<? extends Document>, Map<List<String>, List<String>>> docMap = Maps.newHashMap();
    getAllReferringDocIdsByMethod(docMap, referredCls, referredId);

    // Get referring, indexed documents for all non-indexed types:
    recurseNonIndexedTypes(docMap);

    // Collapse extra nesting level and return:
    return flattenReferringDocumentMap(docMap);
  }

  private Map<Class<? extends Document>, Set<String>> flattenReferringDocumentMap(Map<Class<? extends Document>, Map<List<String>, List<String>>> docMap) {
    Map<Class<? extends Document>, Set<String>> docsToIndex = Maps.newHashMap();
    for (Map.Entry<Class<? extends Document>, Map<List<String>, List<String>>> entry : docMap.entrySet()) {
      Class<? extends Document> key = entry.getKey();
      Map<List<String>, List<String>> subMap = entry.getValue();
      Set<String> docIds = Sets.newHashSet();
      for (List<String> v : subMap.values()) {
        docIds.addAll(v);
      }
      docsToIndex.put(key, docIds);
    }
    return docsToIndex;
  }

  /**
   * This method goes through the map it's passed, and for each referring document type which is not indexed,
   * checks for documents that refer to *those* documents, until only indexed documents are left.
   * @param docMap the map of documents to modify
   */
  private void recurseNonIndexedTypes(Map<Class<? extends Document>, Map<List<String>, List<String>>> docMap) {
    boolean nonIndexedTypes = false;
    do {
      nonIndexedTypes = false;
      Set<Class<? extends Document>> keySet = Sets.newHashSet(docMap.keySet());
      for (Class<? extends Document> referringType : keySet) {
        Map<List<String>, List<String>> referringDocsMap = docMap.get(referringType);
        if (!indexedTypes.contains(referringType)) {
          nonIndexedTypes = true;
          List<String> referringDocIds = Lists.newArrayList();
          for (List<String> subList : referringDocsMap.values()) {
            referringDocIds.addAll(subList);
          }
          docMap.remove(referringType);
          // recurse: for this list of documents, we look for documents that *are* indexed which refer to *these* documents.
          getAllReferringDocIdsByMethod(docMap, referringType, referringDocIds.toArray(new String[referringDocIds.size()]));
        }
      }
    } while (nonIndexedTypes);
  }

  private <T extends Document> void getAllReferringDocIdsByMethod(Map<Class<? extends Document>, Map<List<String>, List<String>>> docAccumulator, Class<T> referredCls, String... referredId) {
    if (!indexRelations.containsKey(referredCls)) {
      return;
    }
    List<Class<? extends Document>> referringTypes = indexRelations.get(referredCls);
    for (Class<? extends Document> relDocType : referringTypes) {
      getReferringDocIdsOfTypeByMethod(docAccumulator, relDocType, referredCls, referredId);
    }
  }

  private <T extends Document, X extends Document> void getReferringDocIdsOfTypeByMethod(Map<Class<? extends Document>, Map<List<String>, List<String>>> docAccumulator, Class<X> referringType,
      Class<T> referredType, String... referredId) {
    Map<List<String>, List<String>> referringDocIds = storageManager.getReferringDocs(referringType, referredType, referredId);
    if (referringDocIds.isEmpty()) {
      return;
    }
    if (!docAccumulator.containsKey(referringType)) {
      docAccumulator.put(referringType, referringDocIds);
    } else {
      docAccumulator.get(referringType).putAll(referringDocIds);
    }
  }

  /**
   * Modify all the passed documents in their respective indexes
   * @param docsToIndex Map from index class to documents to modify
   */
  private void modifyAllDocs(Map<Class<? extends Document>, Set<String>> docsToIndex) {
    for (Map.Entry<Class<? extends Document>, Set<String>> entry : docsToIndex.entrySet()) {
      modifyAllDocsByType(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Modify (re-index) all these documents
   * @param cls The type of the documents
   * @param docIds The document ids.
   */
  private <T extends Document> void modifyAllDocsByType(Class<T> cls, Set<String> docIds) {
    Class<? extends Document> baseCls = VariationUtils.getBaseClass(cls);
    doReIndex(docIds, baseCls);
  }

  private <T extends Document> void doReIndex(Set<String> docIds, Class<T> baseCls) {
    SolrDocumentIndexer<T> indexer = indexFactory.getIndexForType(baseCls);
    for (String id : docIds) {
      List<T> docs = storageManager.getAllVariations(baseCls, id);
      indexer.modify(docs);
    }
  }

}
