package nl.knaw.huygens.repository.managers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import nl.knaw.huygens.repository.storage.VariationStorage;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.generic.StorageUtils;
import nl.knaw.huygens.repository.variation.VariationUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StorageManager {

  /*
   * TODO: use 3 types of Storage and Remove the use of MongoComplexStorage
   * - Storage
   * - PersistentStorage
   * - VariationStorage
   */

  private VariationStorage variationStorage;
  private Map<Class<? extends Document>, Map<Class<? extends Document>, List<List<String>>>> annotationCache;
  private Set<String> documentTypes;

  private final Hub hub;
  private DocumentTypeRegister docTypeRegistry;
  private PersistenceManager persistenceManager;

  @Inject
  public StorageManager(StorageConfiguration storageConf, VariationStorage variationStorage, Hub hub, DocumentTypeRegister docTypeRegistry, PersistenceManager persistenceMananger) {
    this.hub = hub;
    this.docTypeRegistry = docTypeRegistry;
    documentTypes = storageConf.getDocumentTypes();
    this.variationStorage = variationStorage;
    this.persistenceManager = persistenceMananger;
    fillAnnotationCache();
    ensureIndices();
  }

  // Test-only!
  protected StorageManager(VariationStorage variationStorage, Set<String> documentTypes, Hub hub, DocumentTypeRegister docTypeRegistry, PersistenceManager persistenceManager) {
    this.variationStorage = variationStorage;
    this.docTypeRegistry = docTypeRegistry;
    this.documentTypes = documentTypes;
    this.hub = hub;
    this.persistenceManager = persistenceManager;
    fillAnnotationCache();
    ensureIndices();
  }

  public <T extends Document> T getCompleteDocument(Class<T> type, String id) {
    T rv;
    try {
      rv = variationStorage.getItem(type, id);
    } catch (IOException e) {
      e.printStackTrace();
      rv = null;
    }

    return rv;
  }

  /**
   * Get the latest document for a specific variation.
   * @param type
   * @param id
   * @param variation
   * @return
   */
  public <T extends Document> T getCompleteVariation(Class<T> type, String id, String variation) {
    try {
      return variationStorage.getVariation(type, id, variation);
    } catch (Exception ex) {
      return null;
    }
  }

  public <T extends Document> T getDocument(Class<T> type, String id) {
    try {
      return variationStorage.getItem(type, id);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public <T extends Document> List<T> getAllVariations(Class<T> type, String id) {
    List<T> rv;
    try {
      rv = variationStorage.getAllVariations(type, id);
    } catch (IOException e) {
      e.printStackTrace();
      rv = null;
    }
    return rv;
  }

  public <T extends Document> StorageIterator<T> getAll(Class<T> type) {
    return variationStorage.getAllByType(type);
  }

  public <T extends Document> RevisionChanges<T> getVersions(Class<T> type, String id) {
    return variationStorage.getAllRevisions(type, id);
  }

  public <T extends Document> void addDocument(Class<T> type, T doc) throws IOException {
    variationStorage.addItem(type, doc);
    persistDocumentVersion(type, doc);
    doThrowEvent(VariationUtils.getBaseClass(type), doc.getId(), Events.DocumentAddEvent.class);
  }

  private <T extends Document> void persistDocumentVersion(Class<T> type, T doc) {
    try {
      // TODO make persistent id dependent on version.
      String collectionId = docTypeRegistry.getCollectionId(type);
      String pid = persistenceManager.persistObject(collectionId, doc.getId());
      variationStorage.setPID(type, pid, doc.getId());
    } catch (PersistenceException e) {
      e.printStackTrace();
    }
  }

  public <T extends Document> void modifyDocument(Class<T> type, T doc) throws IOException {
    variationStorage.updateItem(type, doc.getId(), doc);
    persistDocumentVersion(type, doc);
    doThrowEvent(VariationUtils.getBaseClass(type), doc.getId(), DocumentEditEvent.class);
  }

  public <T extends Document> void removeDocument(Class<T> type, T doc) throws IOException {
    variationStorage.deleteItem(type, doc.getId(), doc.getLastChange());
    doThrowEvent(VariationUtils.getBaseClass(type), doc.getId(), Events.DocumentDeleteEvent.class);
  }

  private <T extends Document> void doThrowEvent(Class<T> type, String id, @SuppressWarnings("rawtypes") Class<? extends Events.DocumentChangeEvent> t) throws IOException {
    List<T> docs = variationStorage.getAllVariations(type, id);

    try {
      hub.publish(t.getConstructor(Class.class, List.class).newInstance(type, docs));
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  public <T extends Document> StorageIterator<T> getByMultipleIds(Class<T> type, List<String> ids) {
    return variationStorage.getByMultipleIds(type, ids);
  }

  public List<Document> getLastChanged(int limit) {
    try {
      return variationStorage.getLastChanged(limit);
    } catch (IOException e) {
      e.printStackTrace();
      return Collections.<Document> emptyList();
    }
  }

  public <T extends Document> List<T> getAllLimited(Class<T> type, int offset, int limit) {
    if (limit == 0) {
      return Collections.<T> emptyList();
    }
    return StorageUtils.resolveIterator(variationStorage.getAllByType(type), offset, limit);
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
      List<String> referringDocIds = variationStorage.getIdsForQuery(referringType, accessDesc, referredIds);
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
      variationStorage.ensureIndex(cls, accessorList);
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
    return variationStorage;
  }

  public void close() {
    try {
      variationStorage.destroy();
    } catch (Exception e) {
      System.err.println("Failed to close storage!");
      e.printStackTrace();
    }
  }

}
