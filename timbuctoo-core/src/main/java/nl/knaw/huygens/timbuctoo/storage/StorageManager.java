package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Archive;
import nl.knaw.huygens.timbuctoo.model.Archiver;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.Keyword;
import nl.knaw.huygens.timbuctoo.model.Legislation;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.storage.StorageStatus.KV;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StorageManager {

  private static final Logger LOG = LoggerFactory.getLogger(StorageManager.class);

  private final TypeRegistry registry;
  private final VariationStorage storage;

  @Inject
  public StorageManager(VariationStorage storage, TypeRegistry registry) {
    this.registry = registry;
    this.storage = storage;
  }

  /**
   * Closes the data store.
   */
  public void close() {
    storage.close();
  }

  /**
   * Clears the data store.
   */
  public void clear() {
    storage.empty();
  }

  // -------------------------------------------------------------------

  // TODO generate entity types dynamically
  public StorageStatus getStatus() {
    StorageStatus status = new StorageStatus();

    status.addDomainEntityCount(getCount(Archive.class));
    status.addDomainEntityCount(getCount(Archiver.class));
    status.addDomainEntityCount(getCount(Keyword.class));
    status.addDomainEntityCount(getCount(Legislation.class));
    status.addDomainEntityCount(getCount(Person.class));
    status.addDomainEntityCount(getCount(Relation.class));

    status.addSystemEntityCount(getCount(RelationType.class));
    status.addSystemEntityCount(getCount(SearchResult.class));
    status.addSystemEntityCount(getCount(User.class));

    return status;
  }

  private KV<Long> getCount(Class<? extends Entity> type) {
    return new KV<Long>(type.getSimpleName(), storage.count(type));
  }

  // -------------------------------------------------------------------

  public <T extends Entity> T getEntity(Class<T> type, String id) {
    try {
      return storage.getItem(type, id);
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      LOG.error("exception", e);
      return null;
    }
  }

  public <T extends DomainEntity> T getEntityWithRelations(Class<T> type, String id) {
    try {
      T entity = storage.getItem(type, id);
      addRelationTo(type, id, entity);
      return entity;
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
      return null;
    }
  }

  // We retrieve all relations involving the specified entity by its id.
  // Next we need to filter the relations that are compatible with the entity type:
  // a relation is only valid if the entity type we are handling is assignable
  // to the type specified in the relation.
  // For example, if a relation is specified for a DCARArchiver, it is visible when
  // dealing with an en entity type DCARArchiver, but not for Archiver.
  public <T extends DomainEntity> void addRelationTo(Class<T> type, String id, T entity) {
    StorageIterator<Relation> iterator = null;
    try {
      iterator = storage.getRelationsOf(type, id); // db access
      while (iterator.hasNext()) {
        Relation relation = iterator.next(); // db access
        RelationType relType = getEntity(RelationType.class, relation.getTypeRef().getId()); // db access
        if (relation.hasSourceId(id)) {
          Class<? extends Entity> cls = registry.getTypeForIName(relation.getSourceType());
          if (cls != null && cls.isAssignableFrom(type)) {
            Reference reference = relation.getTargetRef();
            entity.addRelation(relType.getRegularName(), getEntityRef(reference)); // db access
          }
        } else if (relation.hasTargetId(id)) {
          Class<? extends Entity> cls = registry.getTypeForIName(relation.getTargetType());
          if (cls != null && cls.isAssignableFrom(type)) {
            Reference reference = relation.getSourceRef();
            entity.addRelation(relType.getInverseName(), getEntityRef(reference)); // db access
          }
        } else {
          throw new IllegalStateException("Impossible");
        }
      }
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getName(), id);
    } finally {
      if (iterator != null) {
        iterator.close();
      }
    }
  }

  public EntityRef getEntityRef(Reference reference) {
    String iname = reference.getType();
    String xname = registry.getXNameForIName(iname);
    Class<? extends Entity> type = registry.getTypeForIName(iname);
    Entity entity = getEntity(type, reference.getId());
    return new EntityRef(iname, xname, reference.getId(), entity.getDisplayName());
  }

  public <T extends SystemEntity> T findEntity(Class<T> type, String key, String value) {
    try {
      return storage.findItemByKey(type, key, value);
    } catch (IOException e) {
      LOG.error("Error while handling {}", type.getName());
      return null;
    }
  }

  /**
   * Returns a single system entity matching the non-null fields of
   * the specified entity, or null if no such entity exists.
   */
  public <T extends SystemEntity> T findEntity(Class<T> type, T example) {
    try {
      return storage.findItem(type, example);
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

  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) {
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

  /* A bit of code duplication, but I think it is more readable than calling this method from addEntity and then persisting it.
   * This code is needed, because of issue #1774 in Redmine. It contains the question if the persistent identifier should be added autmaticallly. 
   */
  public <T extends Entity> String addEntityWithoutPersisting(Class<T> type, T doc, boolean isComplete) throws IOException {
    return storage.addItem(type, doc);
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
   * @return the id of the newly created Entity.
   * @throws IOException when thrown by storage
   */
  public <T extends Entity> String addEntity(Class<T> type, T doc, boolean isComplete) throws IOException {
    return storage.addItem(type, doc);
  }

  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) {
    storage.setPID(type, id, pid);
  }

  public <T extends Entity> void modifyEntityWithoutPersisting(Class<T> type, T doc) throws IOException {
    storage.updateItem(type, doc.getId(), doc);
  }

  public <T extends Entity> void modifyEntity(Class<T> type, T doc) throws IOException {
    storage.updateItem(type, doc.getId(), doc);
  }

  public <T extends Entity> void removeEntity(Class<T> type, T doc) throws IOException {
    storage.deleteItem(type, doc.getId(), doc.getLastChange());
  }

  public int removeAllSearchResults() {
    return storage.removeAll(SearchResult.class);
  }

  public int removeSearchResultsBefore(Date date) {
    return storage.removeByDate(SearchResult.class, SearchResult.DATE_FIELD, date);
  }

  /**
   * Retrieves all the id's of type {@code <T>} that does not have a persistent id. 
   * 
   * @param type the type of the id's that should be retrieved
   * @return a list with all the ids.
   * @throws IOException when the storage layer throws an exception it will be forwarded.
   */
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws IOException {
    return storage.getAllIdsWithoutPIDOfType(type);
  }

  /**
   * Returns the id's of the relations, connected to the entities with the input id's.
   * The input id's can be the source id as well as the target id of the Relation. 
   * 
   * @param ids a list of id's to find the relations for
   * @return a list of id's of the corresponding relations
   * @throws IOException re-throws the IOExceptions of the storage
   */
  public List<String> getRelationIds(List<String> ids) throws IOException {
    return storage.getRelationIds(ids);
  }

  /**
   * Removes non-persistent domain entities with the specified type and id's..
   * The idea behind this method is that domain entities without persistent identifier are not validated yet.
   * After a bulk import non of the imported entity will have a persistent identifier, until a user has agreed with the imported collection.  
   * 
   * @param <T> extends {@code DomainEntity}, because system entities have no persistent identifiers.
   * @param type the type all of the objects should removed permanently from
   * @param ids the id's to remove permanently
   * @throws IOException when the storage layer throws an exception it will be forwarded
   */
  public <T extends DomainEntity> void removeNonPersistent(Class<T> type, List<String> ids) throws IOException {
    storage.removeNonPersistent(type, ids);
  }

  public <T extends Entity> List<T> getAllLimited(Class<T> type, int offset, int limit) {
    if (limit == 0) {
      return Collections.<T> emptyList();
    }
    return StorageUtils.resolveIterator(storage.getAllByType(type), offset, limit);
  }

  public boolean relationExists(Relation relation) {
    return storage.relationExists(relation);
  }

}
