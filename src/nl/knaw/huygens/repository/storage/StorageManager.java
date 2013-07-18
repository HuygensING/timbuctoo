package nl.knaw.huygens.repository.storage;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;

import nl.knaw.huygens.repository.annotations.RelatedDocument;
import nl.knaw.huygens.repository.annotations.RelatedDocuments;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.messages.Broker;
import nl.knaw.huygens.repository.messages.Producer;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.persistence.PersistenceException;
import nl.knaw.huygens.repository.persistence.PersistenceManager;
import nl.knaw.huygens.repository.variation.VariationUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StorageManager {

  private static final Logger LOG = LoggerFactory.getLogger(StorageManager.class);

  private VariationStorage storage;
  private Map<Class<? extends Document>, Map<Class<? extends Document>, List<List<String>>>> annotationCache;
  private Set<String> documentTypes;

  private final Producer producer;
  private DocTypeRegistry docTypeRegistry;
  private PersistenceManager persistenceManager;

  @Inject
  public StorageManager(StorageConfiguration storageConf, VariationStorage storage, Broker broker, DocTypeRegistry docTypeRegistry, PersistenceManager persistenceMananger) {
    producer = setupProducer(broker);
    this.docTypeRegistry = docTypeRegistry;
    documentTypes = storageConf.getDocumentTypes();
    this.storage = storage;
    this.persistenceManager = persistenceMananger;
    fillAnnotationCache();
    ensureIndices();
  }

  // Test-only!
  protected StorageManager(VariationStorage storage, Set<String> documentTypes, Broker broker, DocTypeRegistry docTypeRegistry, PersistenceManager persistenceManager) {
    producer = null;
    this.storage = storage;
    this.docTypeRegistry = docTypeRegistry;
    this.documentTypes = documentTypes;
    this.persistenceManager = persistenceManager;
    fillAnnotationCache();
    ensureIndices();
  }

  /**
   * Clears the data store.
   */
  public void clear() {
    storage.empty();
  }

  /**
   * Closes the data store.
   */
  public void close() {
    storage.close();
    if (producer != null) {
      producer.closeQuietly();
    }
  }

  // -------------------------------------------------------------------

  private Producer setupProducer(Broker broker) {
    try {
      return broker.newProducer(Broker.INDEX_QUEUE, "StorageManagerProducer");
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }
  }

  private void sendIndexMessage(String action, String type, String id) {
    if (producer != null) {
      try {
        producer.send(action, type, id);
      } catch (JMSException e) {
        throw new RuntimeException(e);
      }
    }
  }

  // -------------------------------------------------------------------

  public <T extends Document> T getCompleteDocument(Class<T> type, String id) {
    try {
      return storage.getItem(type, id);
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  public <T extends Document> T searchDocument(Class<T> type, T example) {
    try {
      return storage.searchItem(type, example);
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), example.getId());
      return null;
    }

  }

  public <T extends DomainDocument> T getCompleteVariation(Class<T> type, String id, String variation) {
    try {
      return storage.getVariation(type, id, variation);
    } catch (Exception e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  public <T extends Document> T getDocument(Class<T> type, String id) {
    try {
      return storage.getItem(type, id);
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  public <T extends Document> List<T> getAllVariations(Class<T> type, String id) {
    try {
      return storage.getAllVariations(type, id);
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  public <T extends Document> StorageIterator<T> getAll(Class<T> type) {
    return storage.getAllByType(type);
  }

  public <T extends Document> RevisionChanges<T> getVersions(Class<T> type, String id) {
    try {
      return storage.getAllRevisions(type, id);
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  public <T extends Document> void addDocument(Class<T> type, T doc, boolean isComplete) throws IOException {
    storage.addItem(type, doc);
    persistDocumentVersion(type, doc);
    if (DomainDocument.class.isAssignableFrom(type) && isComplete) {
      sendIndexMessage(Broker.INDEX_ADD, VariationUtils.getBaseClass(type).getSimpleName(), doc.getId());
    }
  }

  public <T extends Document> void addDocument(Class<T> type, T doc) throws IOException {
    addDocument(type, doc, true);
  }

  private <T extends Document> void persistDocumentVersion(Class<T> type, T doc) {
    try {
      // TODO make persistent id dependent on version.
      Class<? extends Document> baseType = docTypeRegistry.getBaseClass(type);
      String collectionId = docTypeRegistry.getINameForType(baseType);
      String pid = persistenceManager.persistObject(collectionId, doc.getId());
      storage.setPID(type, pid, doc.getId());
    } catch (PersistenceException e) {
      LOG.error("Error while handling {} {}", type.getName(), doc.getId());
    }
  }

  public <T extends Document> void modifyDocument(Class<T> type, T doc) throws IOException {
    storage.updateItem(type, doc.getId(), doc);
    persistDocumentVersion(type, doc);
    if (DomainDocument.class.isAssignableFrom(type)) {
      sendIndexMessage(Broker.INDEX_MOD, VariationUtils.getBaseClass(type).getSimpleName(), doc.getId());
    }
  }

  public <T extends Document> void removeDocument(Class<T> type, T doc) throws IOException {
    storage.deleteItem(type, doc.getId(), doc.getLastChange());
    if (DomainDocument.class.isAssignableFrom(type)) {
      sendIndexMessage(Broker.INDEX_DEL, VariationUtils.getBaseClass(type).getSimpleName(), doc.getId());
    }
  }

  public <T extends Document> StorageIterator<T> getByMultipleIds(Class<T> type, List<String> ids) {
    return storage.getByMultipleIds(type, ids);
  }

  public List<Document> getLastChanged(int limit) {
    try {
      return storage.getLastChanged(limit);
    } catch (IOException e) {
      LOG.error("Error while handling {}", limit);
      return Collections.<Document> emptyList();
    }
  }

  public <T extends Document> List<T> getAllLimited(Class<T> type, int offset, int limit) {
    if (limit == 0) {
      return Collections.<T> emptyList();
    }
    return StorageUtils.resolveIterator(storage.getAllByType(type), offset, limit);
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
      Class<? extends Document> cls = docTypeRegistry.getTypeForIName(docType);
      annotationCache.put(cls, getAllRelatedDocumentAnnotations(cls));
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

  private Map<Class<? extends Document>, List<List<String>>> getAllRelatedDocumentAnnotations(Class<? extends Document> refDocType) {
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

}
