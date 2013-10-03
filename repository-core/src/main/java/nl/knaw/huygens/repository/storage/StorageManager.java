package nl.knaw.huygens.repository.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jms.JMSException;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.messages.ActionType;
import nl.knaw.huygens.repository.messages.Broker;
import nl.knaw.huygens.repository.messages.Producer;
import nl.knaw.huygens.repository.model.DomainEntity;
import nl.knaw.huygens.repository.model.Entity;
import nl.knaw.huygens.repository.model.Relation;
import nl.knaw.huygens.repository.model.SearchResult;
import nl.knaw.huygens.repository.persistence.PersistenceWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StorageManager {

  private static final Logger LOG = LoggerFactory.getLogger(StorageManager.class);

  private final DocTypeRegistry docTypeRegistry;
  private final VariationStorage storage;
  private final PersistenceWrapper persistenceWrapper;
  private final Producer producer;

  @Inject
  public StorageManager(VariationStorage storage, Broker broker, DocTypeRegistry registry, PersistenceWrapper persistenceWrapper) {
    docTypeRegistry = registry;
    this.storage = storage;
    this.persistenceWrapper = persistenceWrapper;
    producer = setupProducer(broker);
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

  public <T extends Entity> T getEntity(Class<T> type, String id) {
    try {
      return storage.getItem(type, id);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  /**
   * Returns a single entity matching the non-null fields of
   * the specified entity, or null if no such entity exists.
   */
  public <T extends Entity> T searchEntity(Class<T> type, T example) {
    try {
      return storage.searchItem(type, example);
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), example.getId());
      return null;
    }
  }

  public <T extends DomainEntity> T getCompleteVariation(Class<T> type, String id, String variation) {
    try {
      return storage.getVariation(type, id, variation);
    } catch (Exception e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  public <T extends Entity> List<T> getAllVariations(Class<T> type, String id) {
    try {
      return storage.getAllVariations(type, id);
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  public <T extends Entity> StorageIterator<T> getAll(Class<T> type) {
    return storage.getAllByType(type);
  }

  public <T extends Entity> RevisionChanges<T> getVersions(Class<T> type, String id) {
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
  public <T extends Entity> String addEntityWithoutPersisting(Class<T> type, T doc, boolean isComplete) throws IOException {
    String id = storage.addItem(type, doc);
    if (DomainEntity.class.isAssignableFrom(type) && isComplete) {
      sendIndexMessage(ActionType.INDEX_ADD, docTypeRegistry.getINameForType(type), id);
    }
    return id;
  }

  /**
   * A convenience method for ${@code addEntity(type, doc, true)}
   */
  public <T extends Entity> String addEntity(Class<T> type, T doc) throws IOException {
    return addEntity(type, doc, true);
  }

  /**
   * Stores an item into the database. When no exception is thrown and the entity is of the type DomainEntity, the entity is persisted. 
   * If the boolean isComplete is true the entity will be indexed as well.
   * 
   * @param type should be a DomainEntity
   * @param doc should be of a the type used in type.
   * @param isComplete marks if the entity contains all it's references and relations, 
   * when this boolean is true the entity will be indexed
   * @throws IOException when thrown by storage
   */
  public <T extends Entity> String addEntity(Class<T> type, T doc, boolean isComplete) throws IOException {
    String id = storage.addItem(type, doc);
    if (DomainEntity.class.isAssignableFrom(type)) {
      persistEntityVersion(type, doc);
      if (isComplete) {
        sendIndexMessage(ActionType.INDEX_ADD, docTypeRegistry.getINameForType(type), id);
      }
    }
    return id;
  }

  private <T extends Entity> void persistEntityVersion(Class<T> type, T doc) {
    try {
      // TODO make persistent id dependent on version.
      Class<? extends Entity> baseType = docTypeRegistry.getBaseClass(type);
      String collectionId = docTypeRegistry.getINameForType(baseType);
      String pid = persistenceWrapper.persistObject(collectionId, doc.getId());
      storage.setPID(type, pid, doc.getId());
    } catch (PersistenceException e) {
      LOG.error("Error while handling {} {}", type.getName(), doc.getId());
    }
  }

  public <T extends Entity> void modifyEntityWithoutPersisting(Class<T> type, T doc) throws IOException {
    storage.updateItem(type, doc.getId(), doc);
    if (DomainEntity.class.isAssignableFrom(type)) {
      sendIndexMessage(ActionType.INDEX_MOD, docTypeRegistry.getINameForType(type), doc.getId());
    }
  }

  public <T extends Entity> void modifyEntity(Class<T> type, T doc) throws IOException {
    storage.updateItem(type, doc.getId(), doc);
    if (DomainEntity.class.isAssignableFrom(type)) {
      persistEntityVersion(type, doc);
      sendIndexMessage(ActionType.INDEX_MOD, docTypeRegistry.getINameForType(type), doc.getId());
    }
  }

  public <T extends Entity> void removeEntity(Class<T> type, T doc) throws IOException {
    storage.deleteItem(type, doc.getId(), doc.getLastChange());
    //TODO do something with the PID.
    if (DomainEntity.class.isAssignableFrom(type)) {
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
   * The idea behind this method is that domain entities without persistent identifier are not validated yet.
   * After a bulk import non of the imported entity will have a persistent identifier, until a user has agreed with the imported collection.  
   * 
   * @param <T> extends {@code DomainEntity}, because system entities have no persistent identifiers.
   * @param type the type all of the objects should removed permanently from.
   * @throws IOException 
   */
  public <T extends DomainEntity> void removePermanently(Class<T> type) throws IOException {
    Collection<String> ids = storage.getAllIdsWithoutPIDOfType(type);

    String typeString = docTypeRegistry.getINameForType(type);

    for (String id : ids) {
      sendIndexMessage(ActionType.INDEX_DEL, typeString, id);
    }

    try {
      storage.removePermanently(type, ids);
    } catch (IOException ex) {
      //roll back
      for (String id : ids) {
        sendIndexMessage(ActionType.INDEX_ADD, typeString, id);
      }
      throw ex;
    }
  }

  public <T extends Entity> StorageIterator<T> getByMultipleIds(Class<T> type, List<String> ids) {
    return storage.getByMultipleIds(type, ids);
  }

  public List<Entity> getLastChanged(int limit) {
    try {
      return storage.getLastChanged(limit);
    } catch (IOException e) {
      LOG.error("Error while handling {}", limit);
      return Collections.<Entity> emptyList();
    }
  }

  public <T extends Entity> List<T> getAllLimited(Class<T> type, int offset, int limit) {
    if (limit == 0) {
      return Collections.<T> emptyList();
    }
    return StorageUtils.resolveIterator(storage.getAllByType(type), offset, limit);
  }

  public int countRelations(Relation relation) {
    return storage.countRelations(relation);
  }

}
