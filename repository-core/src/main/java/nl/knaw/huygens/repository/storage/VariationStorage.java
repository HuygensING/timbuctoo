package nl.knaw.huygens.repository.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.repository.model.DomainEntity;
import nl.knaw.huygens.repository.model.Entity;
import nl.knaw.huygens.repository.model.Relation;

public interface VariationStorage extends BasicStorage {

  <T extends Entity> List<T> getAllVariations(Class<T> type, String id) throws IOException;

  /**
   * Get the given variation of an entity.
   */
  <T extends DomainEntity> T getVariation(Class<T> type, String id, String variation) throws IOException;

  <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws IOException;

  /**
   * Counts the number of stored relations with non-null fields
   * as in the specified {@Relation} instance.
   */
  int countRelations(Relation relation);

  /**
   * Returns all the ids of objects of type <T>, that are not persisted.
   * @param type
   * @return
   */
  <T extends DomainEntity> Collection<String> getAllIdsWithoutPIDOfType(Class<T> type);

  /**
   * Permanently removes the objects from the database.
   * 
   * @param type 
   * @param ids
   */
  <T extends DomainEntity> void removePermanently(Class<T> type, Collection<String> ids);
}
