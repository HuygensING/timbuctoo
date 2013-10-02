package nl.knaw.huygens.repository.storage;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.repository.annotations.RelatedDocument;
import nl.knaw.huygens.repository.annotations.RelatedDocuments;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.messages.ActionType;
import nl.knaw.huygens.repository.messages.Broker;
import nl.knaw.huygens.repository.messages.Producer;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.model.Relation;
import nl.knaw.huygens.repository.model.SearchResult;
import nl.knaw.huygens.repository.persistence.PersistenceWrapper;

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

  private final DocTypeRegistry docTypeRegistry;
  private final Producer producer;
  private PersistenceWrapper persistenceWrapper;

  @Inject
  public StorageManager(StorageConfiguration storageConf, VariationStorage storage, Broker broker, DocTypeRegistry registry, PersistenceWrapper persistenceWrapper) {
    docTypeRegistry = registry;
    producer = setupProducer(broker);
    documentTypes = storageConf.getDocumentTypes();
    this.storage = storage;
    this.persistenceWrapper = persistenceWrapper;
    fillAnnotationCache();
    ensureIndices();
  }

  // Test-only!
  protected StorageManager(VariationStorage storage, Set<String> documentTypes, Broker broker, DocTypeRegistry registry, PersistenceWrapper persistenceWrapper) {
    docTypeRegistry = registry;
    producer = setupProducer(broker);
    this.documentTypes = documentTypes;
    this.storage = storage;
    this.persistenceWrapper = persistenceWrapper;
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

  private void sendIndexMessage(ActionType action, String type, String id) {
    if (producer != null) {
      try {
        producer.send(action, type, id);
      } catch (JMSException e) {
        LOG.error("Error while sending message {} - {} - {}\n{}", action, type, id, e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

  // -------------------------------------------------------------------

  public <T extends Document> T getDocument(Class<T> type, String id) {
    try {
      return storage.getItem(type, id);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  /**
   * Returns a single document matching the non-null fields of
   * the specified document, or null if no such document exists.
   */
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

  /* A bit of code duplication, but I think it is more readable than calling this method from addDocument and then persisting it.
   * This code is needed, because of issue #1774 in Redmine. It contains the question if the persistent identifier should be added autmaticallly. 
   */
  public <T extends Document> String addDocumentWithoutPersisting(Class<T> type, T doc, boolean isComplete) throws IOException {
    String id = storage.addItem(type, doc);
    if (DomainDocument.class.isAssignableFrom(type) && isComplete) {
      sendIndexMessage(ActionType.INDEX_ADD, docTypeRegistry.getINameForType(type), id);
    }
    return id;
  }

  /**
   * A convenience method for ${@code addDocument(type, doc, true)}
   */
  public <T extends Document> String addDocument(Class<T> type, T doc) throws IOException {
    return addDocument(type, doc, true);
  }

  /**
   * Stores an item into the database. When no exception is thrown and the document is of the type DomainDocument, the document is persisted. 
   * If the boolean isComplete is true the document will be indexed as well.
   * 
   * @param type should be a DomainDocument
   * @param doc should be of a the type used in type.
   * @param isComplete marks if the document contains all it's references and relations, 
   * when this boolean is true the document will be indexed
   * @throws IOException when thrown by storage
   */
  public <T extends Document> String addDocument(Class<T> type, T doc, boolean isComplete) throws IOException {
    String id = storage.addItem(type, doc);
    if (DomainDocument.class.isAssignableFrom(type)) {
      persistDocumentVersion(type, doc);
      if (isComplete) {
        sendIndexMessage(ActionType.INDEX_ADD, docTypeRegistry.getINameForType(type), id);
      }
    }
    return id;
  }

  private <T extends Document> void persistDocumentVersion(Class<T> type, T doc) {
    try {
      // TODO make persistent id dependent on version.
      Class<? extends Document> baseType = docTypeRegistry.getBaseClass(type);
      String collectionId = docTypeRegistry.getINameForType(baseType);
      String pid = persistenceWrapper.persistObject(collectionId, doc.getId());
      storage.setPID(type, pid, doc.getId());
    } catch (PersistenceException e) {
      LOG.error("Error while handling {} {}", type.getName(), doc.getId());
    }
  }

  public <T extends Document> void modifyDocumentWithoutPersisting(Class<T> type, T doc) throws IOException {
    storage.updateItem(type, doc.getId(), doc);
    if (DomainDocument.class.isAssignableFrom(type)) {
      sendIndexMessage(ActionType.INDEX_MOD, docTypeRegistry.getINameForType(type), doc.getId());
    }
  }

  public <T extends Document> void modifyDocument(Class<T> type, T doc) throws IOException {
    storage.updateItem(type, doc.getId(), doc);
    if (DomainDocument.class.isAssignableFrom(type)) {
      persistDocumentVersion(type, doc);
      sendIndexMessage(ActionType.INDEX_MOD, docTypeRegistry.getINameForType(type), doc.getId());
    }
  }

  public <T extends Document> void removeDocument(Class<T> type, T doc) throws IOException {
    storage.deleteItem(type, doc.getId(), doc.getLastChange());
    //TODO do something with the PID.
    if (DomainDocument.class.isAssignableFrom(type)) {
      sendIndexMessage(ActionType.INDEX_DEL, docTypeRegistry.getINameForType(type), doc.getId());
    }
  }

  public int removeAllSearchResults() {
    return storage.removeAll(SearchResult.class);
  }

  public int removeSearchResultsBefore(Date date) {
    return storage.removeByDate(SearchResult.class, SearchResult.DATE_FIELD, date);
  }

  /**
   * Removes all the objects of type <T>, that have no persistent identifier.
   * The idea behind this method is that {@code DomainDocument}s without persistent identifier are not validated yet.
   * After a bulk import non of the imported documents will have a persistent identifier, until a user has agreed with the imported collection.  
   * 
   * @param <T> extends {@code DomainDocument}, because {@code SystemDocument}s have no persistent identifiers.
   * @param type the type all of the objects should removed permanently from.
   */
  public <T extends DomainDocument> void removePermanently(Class<T> type) {
    Collection<String> ids = storage.getAllIdsWithoutPIDOfType(type);

    String typeString = docTypeRegistry.getINameForType(type);

    for (String id : ids) {
      sendIndexMessage(ActionType.INDEX_DEL, typeString, id);
    }

    try {
      storage.removePermanently(type, ids);
    } finally {
      //roll back
      for (String id : ids) {
        sendIndexMessage(ActionType.INDEX_ADD, typeString, id);
      }
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

  public int countRelations(Relation relation) {
    return storage.countRelations(relation);
  }

}
