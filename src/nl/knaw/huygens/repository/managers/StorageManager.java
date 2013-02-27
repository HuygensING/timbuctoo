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

import nl.knaw.huygens.repository.events.Events.DocumentAddEvent;
import nl.knaw.huygens.repository.events.Events.DocumentChangeEvent;
import nl.knaw.huygens.repository.events.Events.DocumentDeleteEvent;
import nl.knaw.huygens.repository.events.Events.DocumentEditEvent;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.storage.RelatedDocument;
import nl.knaw.huygens.repository.storage.RelatedDocuments;
import nl.knaw.huygens.repository.storage.RevisionChanges;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.generic.StorageFactory;
import nl.knaw.huygens.repository.storage.generic.StorageUtils;
import nl.knaw.huygens.repository.util.Configuration;

public class StorageManager {

  private Storage storage;
  private Map<Class<? extends Document>, Map<Class<? extends Document>, List<List<String>>>> annotationCache;
  private Set<String> documentTypes;
  private final Hub hub;

  public StorageManager(Configuration conf, Hub hub) {
    this.hub = hub;
    StorageConfiguration storageConf = new StorageConfiguration(conf);
    documentTypes = storageConf.getDocumentTypes();
    storage = StorageFactory.getInstance(storageConf);
    fillAnnotationCache();
    ensureIndices();
  }

  // Test-only!
  protected StorageManager(Storage storage, Set<String> documentTypes, Hub hub) {
    this.storage = storage;
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

  public <T extends Document> StorageIterator<T> getAll(Class<T> entityCls) {
    return storage.getAllByType(entityCls);
  }

  public <T extends Document> RevisionChanges getVersions(String id, Class<T> entityCls) {
    return storage.getAllRevisions(id, entityCls);
  }

  public <T extends Document> void addDocument(T doc, Class<T> entityCls) throws IOException {
    storage.addItem(doc, entityCls);
    try {
      doc.fetchAll(storage);
      hub.publish(added(doc, entityCls));
    } catch (Exception ex) {
      throw new IOException(ex);
    }
  }


  public <T extends Document> void modifyDocument(T doc, Class<T> entityCls) throws IOException {
    String id = doc.getId();
    storage.updateItem(id, doc, entityCls);

    try {
      doc.fetchAll(storage);
      hub.publish(modified(doc, entityCls));
    } catch (Exception ex) {
      throw new IOException(ex);
    }
  }

  public <T extends Document> void removeDocument(T doc, Class<T> entityCls) throws IOException {
    storage.deleteItem(doc.getId(), entityCls, doc.getLastChange());
    try {
      hub.publish(removed(doc, entityCls));
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
      return Collections.<Document>emptyList();
    }
  }

  public <T extends Document> List<T> getAllLimited(Class<T> cls, int offset, int limit) {
    if (limit == 0) {
      return Collections.<T>emptyList();
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
      Class<? extends Document> cls = Document.getSubclassByString(docType);
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

  private <T extends Document> DocumentChangeEvent<T> added(T doc, Class<T> cls) {
    return new DocumentAddEvent<T>(doc, cls);
  }

  private <T extends Document> DocumentChangeEvent<T> modified(T doc, Class<T> cls) {
    return new DocumentEditEvent<T>(doc, cls);
  }

  private <T extends Document> DocumentChangeEvent<T> removed(T doc, Class<T> cls) {
    return new DocumentDeleteEvent<T>(doc, cls);
  }

  public void close() {
    try {
      storage.destroy();
    } catch (Exception ex) {
      System.err.println("Failed to close storage!");
      ex.printStackTrace();
    }
  }

  public void removeFromReferringDocs(Class<? extends Document> cls, Map<List<String>, List<String>> referringDocsByAccessors, String referredId, Change change) {
    for (Map.Entry<List<String>, List<String>> entry : referringDocsByAccessors.entrySet()) {
      List<String> accessorList = entry.getKey();
      List<String> docs = entry.getValue();
      storage.removeReference(cls, accessorList, docs, referredId, change);
    }
  }
}
