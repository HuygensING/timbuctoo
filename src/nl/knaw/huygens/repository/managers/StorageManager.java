package nl.knaw.huygens.repository.managers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import nl.knaw.huygens.repository.events.Events;
import nl.knaw.huygens.repository.events.Events.DocumentEditEvent;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.persistence.PersistenceException;
import nl.knaw.huygens.repository.persistence.PersistenceManager;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.storage.RelatedDocument;
import nl.knaw.huygens.repository.storage.RelatedDocuments;
import nl.knaw.huygens.repository.storage.RevisionChanges;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.generic.StorageUtils;
import nl.knaw.huygens.repository.variation.VariationUtils;

@Singleton
public class StorageManager {

  private Storage storage;
  private Map<Class<? extends Document>, Map<Class<? extends Document>, List<List<String>>>> annotationCache;
  private Set<String> documentTypes;

  private final Hub hub;
  private DocumentTypeRegister docTypeRegistry;
  private PersistenceManager persistenceManager;

  @Inject
  public StorageManager(StorageConfiguration storageConf, Storage storage, Hub hub, DocumentTypeRegister docTypeRegistry,
      PersistenceManager persistenceMananger) {
    this.hub = hub;
    this.docTypeRegistry = docTypeRegistry;
    documentTypes = storageConf.getDocumentTypes();
    this.storage = storage;
    this.persistenceManager = persistenceMananger;
    fillAnnotationCache();
    ensureIndices();
  }

  // Test-only!
  protected StorageManager(Storage storage, Set<String> documentTypes, Hub hub, DocumentTypeRegister docTypeRegistry) {
    this.storage = storage;
    this.docTypeRegistry = docTypeRegistry;
    this.documentTypes = documentTypes;
    this.hub = hub;
    fillAnnotationCache();
    ensureIndices();
  }

  public <T extends Document> T getCompleteDocument(String pid, Class<T> entityCls) {
    T rv;
    try {
      rv = storage.getItem(pid, entityCls);
    } catch (IOException e) {
      e.printStackTrace();
      rv = null;
    }
    if (rv != null) {
      rv.fetchAll(storage);
    }
    return rv;
  }

  public <T extends Document> T getDocument(String pid, Class<T> entityCls) {
    try {
      return storage.getItem(pid, entityCls);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public <T extends Document> List<T> getAllVariations(String id, Class<T> baseCls) {
    List<T> rv;
    try {
      rv = storage.getAllVariations(id, baseCls);
    } catch (IOException e) {
      e.printStackTrace();
      rv = null;
    }
    return rv;
  }

  public <T extends Document> StorageIterator<T> getAll(Class<T> entityCls) {
    return storage.getAllByType(entityCls);
  }

  public <T extends Document> RevisionChanges<T> getVersions(String id, Class<T> entityCls) {
    return storage.getAllRevisions(id, entityCls);
  }

  public <T extends Document> void addDocument(T doc, Class<T> entityCls) throws IOException {
    storage.addItem(doc, entityCls);
    persistDocumentVersion(doc, entityCls);
    doThrowEvent(doc.getId(), VariationUtils.getBaseClass(entityCls), Events.DocumentAddEvent.class);
  }

  private <T extends Document> void persistDocumentVersion(T doc, Class<T> entityClass) {
    String pid = null;
    try {
      // TODO make persistent id dependent on version.
      pid = persistenceManager.persistObject(doc.getId(), docTypeRegistry.getCollectionId(entityClass));
      storage.setPID(entityClass, pid, doc.getId());
    } catch (PersistenceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public <T extends Document> void modifyDocument(T doc, Class<T> entityCls) throws IOException {
    String id = doc.getId();
    storage.updateItem(id, doc, entityCls);
    persistDocumentVersion(doc, entityCls);
    doThrowEvent(doc.getId(), VariationUtils.getBaseClass(entityCls), DocumentEditEvent.class);
  }

  public <T extends Document> void removeDocument(T doc, Class<T> entityCls) throws IOException {
    storage.deleteItem(doc.getId(), entityCls, doc.getLastChange());
    doThrowEvent(doc.getId(), VariationUtils.getBaseClass(entityCls), Events.DocumentDeleteEvent.class);
  }

  private <X extends Document> void doThrowEvent(String id, Class<X> baseCls, @SuppressWarnings("rawtypes") Class<? extends Events.DocumentChangeEvent> t)
      throws IOException {
    List<X> docs = storage.getAllVariations(id, baseCls);
    for (X doc : docs) {
      doc.fetchAll(storage);
    }
    try {
      hub.publish(t.getConstructor(Class.class, List.class).newInstance(baseCls, docs));
    } catch (Exception ex) {
      throw new IOException(ex);
    }
  }

  public <T extends Document> StorageIterator<T> getByMultipleIds(List<String> ids, Class<T> entityCls) {
    return storage.getByMultipleIds(ids, entityCls);
  }

  public List<Document> getLastChanged(int limit) {
    try {
      return storage.getLastChanged(limit);
    } catch (IOException e) {
      e.printStackTrace();
      return Collections.<Document> emptyList();
    }
  }

  public <T extends Document> List<T> getAllLimited(Class<T> cls, int offset, int limit) {
    if (limit == 0) {
      return Collections.<T> emptyList();
    }
    return StorageUtils.resolveIterator(storage.getAllByType(cls), offset, limit);
  }

  public <X extends Document, T extends Document> Map<List<String>, List<String>> getReferringDocs(Class<X> referringType, Class<T> referredType, String... referredIds) {
    Map<Class<? extends Document>, List<List<String>>> mappings = annotationCache.get(referringType);
    if (mappings == null || mappings.isEmpty()) {
      return Collections.emptyMap();
    }
    List<List<String>> docAccessDescList = mappings.get(referredType);
    if (docAccessDescList == null || docAccessDescList.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<List<String>, List<String>> rv = Maps.newHashMap();
    for (List<String> accessDesc : docAccessDescList) {
      List<String> referringDocIds = storage.getIdsForQuery(referringType, accessDesc, referredIds);
      if (referringDocIds != null && !referringDocIds.isEmpty()) {
        rv.put(accessDesc, referringDocIds);
      }
    }
    return rv;
  }

  private void fillAnnotationCache() {
    annotationCache = Maps.newHashMap();
    for (String docType : documentTypes) {
      Class<? extends Document> cls = docTypeRegistry.getClassFromTypeString(docType);
      annotationCache.put(cls, getAllAnnotations(cls));
    }
  }

  public void ensureIndices() {
    for (Class<? extends Document> cls : annotationCache.keySet()) {
      Collection<List<List<String>>> accessors = annotationCache.get(cls).values();
      // Make into one list:
      List<List<String>> accessorList = Lists.newArrayList();
      for (List<List<String>> accessorListItem : accessors) {
        accessorList.addAll(accessorListItem);
      }
      storage.ensureIndex(cls, accessorList);
    }
  }

  private Map<Class<? extends Document>, List<List<String>>> getAllAnnotations(Class<? extends Document> refDocType) {
    Annotation[] annotations = refDocType.getAnnotations();

    Map<Class<? extends Document>, List<List<String>>> rv = Maps.newHashMap();

    for (Annotation ann : annotations) {
      // Single annotation:
      if (ann.annotationType().equals(RelatedDocument.class)) {
        parseSingleAnnotation(rv, (RelatedDocument) ann);

        // Multiple annotations:
      } else if (ann.annotationType().equals(RelatedDocuments.class)) {
        RelatedDocument[] relDocColl = ((RelatedDocuments) ann).value();
        for (RelatedDocument relDocAnnot : relDocColl) {
          parseSingleAnnotation(rv, relDocAnnot);
        }
      }
    }
    return rv;
  }

  private void parseSingleAnnotation(Map<Class<? extends Document>, List<List<String>>> rv, RelatedDocument relDocAnnot) {
    Class<? extends Document> relatedType = relDocAnnot.type();
    List<String> accessorList = Lists.newArrayList(relDocAnnot.accessors());
    if (!rv.containsKey(relatedType)) {
      List<List<String>> listOfLists = Lists.newArrayList();
      rv.put(relatedType, listOfLists);
    }
    rv.get(relatedType).add(accessorList);
  }

  public Storage getStorage() {
    return storage;
  }

  public void close() {
    try {
      storage.destroy();
    } catch (Exception ex) {
      System.err.println("Failed to close storage!");
      ex.printStackTrace();
    }
  }
}
