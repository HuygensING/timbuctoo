package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.util.Change;

public interface VariationStorage extends BasicStorage {

  <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws IOException;

  /**
   * Get the given variation of an entity.
   */
  <T extends DomainEntity> T getVariation(Class<T> type, String id, String variation) throws IOException;

  <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws IOException;

  <T extends DomainEntity> RevisionChanges<T> getAllRevisions(Class<T> type, String id) throws IOException;

  /**
   * Is the specified relation present in the storage?
   */
  boolean relationExists(Relation relation) throws IOException;

  /**
   * Returns an iterator for all relations involving the specified domain entity,
   * either as 'source' or as 'target' (or both).
   */
  StorageIterator<Relation> getRelationsOf(Class<? extends DomainEntity> type, String id) throws IOException;

  void addRelationsTo(Class<? extends DomainEntity> type, String id, DomainEntity entity);

  <T extends DomainEntity> void setPID(Class<T> type, String id, String pid);

  /**
  * Returns the id's of the domain entities of the specified type, that are not persisted.
  * 
  * Note that by design the method does not return variations of a type
  * that already has been persisted.
  * For example, if {@code Person} is a primitive type and a variation
  * {@code XyzPerson} of an existing entity has been added, this method
  * will not retrieve the id of that entity.
  */
  <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws IOException;

  /**
   * Returns the id's of all relations involving the entities with the specified id's,
   * either as 'source' or as 'target' (or both).
   * 
   * @param ids a list of id's to find the relations for
   * @return a list of id's of the corresponding relations
   * @throws IOException wrapped exception around the database exceptions
   */
  List<String> getRelationIds(List<String> ids) throws IOException;

  /**
   * Removes non-persistent domain entities with the specified type and id's..
   */
  <T extends DomainEntity> void removeNonPersistent(Class<T> type, List<String> ids) throws IOException;

  <T extends DomainEntity> void deleteItem(Class<T> type, String id, Change change) throws IOException;

}
