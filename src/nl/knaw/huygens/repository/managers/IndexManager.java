package nl.knaw.huygens.repository.managers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.index.DocumentIndexer;
import nl.knaw.huygens.repository.index.IndexFactory;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Events.DocumentAddEvent;
import nl.knaw.huygens.repository.model.Events.DocumentDeleteEvent;
import nl.knaw.huygens.repository.model.Events.DocumentEditEvent;
import nl.knaw.huygens.repository.model.storage.StorageIterator;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.pubsub.Subscribe;
import nl.knaw.huygens.repository.util.Configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
 */
public class IndexManager {

  private Set<Class<? extends Document>> indexedTypes;
  private IndexFactory indexFactory;
  private Map<Class<? extends Document>, List<Class<? extends Document>>> indexRelations;
  private StorageManager storageManager;

  public IndexManager(Configuration conf, StorageManager storageManager, IndexFactory indexFactory) {
    this.storageManager = storageManager;
    indexedTypes = Sets.newHashSet();
    String[] docTypes = conf.getSetting("indexeddoctypes").split(",");
    for (String docType : docTypes) {
      indexedTypes.add(Document.getSubclassByString(docType));
    }
    indexRelations = Maps.newHashMap();
    List<String> keys = conf.getSettingKeys("indexrelations.");
    for (String k : keys) {
      String values = conf.getSetting("indexrelations." + k, "");
      if (!values.isEmpty()) {
        String[] items = values.split(",");
        if (items.length > 0) {
          List<Class<? extends Document>> clsList = Lists.newArrayList();
          for (String item : items) {
            clsList.add(Document.getSubclassByString(item));
          }
          indexRelations.put(Document.getSubclassByString(k), clsList);
        }
      }
    }
    this.indexFactory = indexFactory;
    subscribeUs();
  }

  protected IndexManager(IndexFactory factory, String indexedTypes, Map<Class<? extends Document>, List<Class<? extends Document>>> indexRelations) {
    this.indexFactory = factory;
    this.indexedTypes = Sets.newHashSet();
    String[] docTypes = indexedTypes.split(",");
    for (String docType : docTypes) {
      this.indexedTypes.add(Document.getSubclassByString(docType));
    }
    this.indexRelations = indexRelations == null ? Maps.<Class<? extends Document>, List<Class<? extends Document>>>newHashMap() : indexRelations;
    subscribeUs();
  }

  private void subscribeUs() {
    Hub.getInstance().subscribe(this);
  }

  @Subscribe
  public <T extends Document> void onDocumentAdd(DocumentAddEvent<T> evt) {
    T doc = evt.getDocument();
    Class<T> cls = evt.getCls();
    if (indexedTypes.contains(cls)) {
      DocumentIndexer<T> indexer = indexFactory.getIndexForType(cls);
      indexer.add(doc);
      indexFactory.flushIndices();
    }
  }

  @Subscribe
  public <T extends Document> void onDocumentEdit(DocumentEditEvent<T> evt) {
    T doc = evt.getDocument();
    Class<T> cls = evt.getCls();
    if (indexedTypes.contains(cls)) {
      DocumentIndexer<T> indexer = indexFactory.getIndexForType(cls);
      indexer.modify(doc);
      handleRelatedEdit(cls, doc, false);
      indexFactory.flushIndices();
    } else {
      handleRelatedEdit(cls, doc, true);
    }
  }

  @Subscribe
  public <T extends Document> void onDocumentDelete(DocumentDeleteEvent<T> evt) {
    T doc = evt.getDocument();
    Class<T> cls = evt.getCls();
    if (indexedTypes.contains(cls)) {
      DocumentIndexer<T> indexer = indexFactory.getIndexForType(cls);
      indexer.remove(doc);
      handleRelatedDelete(cls, doc, false);
      indexFactory.flushIndices();
    } else {
      handleRelatedDelete(cls, doc, true);
    }
  }

  private <T extends Document> void handleRelatedEdit(Class<T> referredCls, T referredDoc, boolean flush) {
    Map<Class<? extends Document>, Set<String>> docsToIndex = getAllReferringDocIds(referredCls, referredDoc.getId());
    modifyAllDocs(docsToIndex);
    if (flush) {
      indexFactory.flushIndices();
    }
  }

  private <T extends Document> void handleRelatedDelete(Class<T> referredCls, T referredDoc, boolean flush) {
    Map<Class<? extends Document>, Map<List<String>, List<String>>> docMap = Maps.newHashMap();
    getAllReferringDocIdsByMethod(docMap, referredCls, referredDoc.getId());
    for (Class<? extends Document> cls : docMap.keySet()) {
      storageManager.removeFromReferringDocs(cls, docMap.get(cls), referredDoc.getId(), referredDoc.getLastChange());
    }
    recurseNonIndexedTypes(docMap);
    modifyAllDocs(flattenReferringDocumentMap(docMap));
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

  private <T extends Document, X extends Document> void getReferringDocIdsOfTypeByMethod(Map<Class<? extends Document>, Map<List<String>, List<String>>> docAccumulator, Class<X> referringType, Class<T> referredType, String... referredId) {
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
    StorageIterator<T> docs = storageManager.getByMultipleIds(Lists.newArrayList(docIds), cls);
    DocumentIndexer<T> indexer = indexFactory.getIndexForType(cls);
    try {
      while (docs.hasNext()) {
        T referringDoc = docs.next();
        referringDoc.fetchAll(storageManager.getStorage());
        indexer.modify(referringDoc);
      }
    } finally {
      docs.close();
    }
  }

  public void close() {
    try {
      indexFactory.flushIndices();
      indexFactory.close();
    } catch (Exception ex) {
      System.err.println("Failed to shut down indexing.");
      ex.printStackTrace();
    }
  }
}
