package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;

public interface VariationStorage extends BasicStorage {

  <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws IOException;

  /**
   * Get the given variation of an entity.
   */
  <T extends DomainEntity> T getVariation(Class<T> type, String id, String variation) throws IOException;

  <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws IOException;

  /**
   * Counts the number of stored relations with non-null fields
   * as in the specified {@code Relation} instance.
   */
  int countRelations(Relation relation);

  /**
   * Returns the id's of the relations, connected to the entities with the input id's.
   * The input id's can be the source id as well as the target id of the Relation. 
   * 
   * @param ids a collection of id's to find the relations for
   * @return a collection of id's of the corresponding relations
   * @throws IOException wrapped exception around the database exceptions
   */
  Collection<String> getRelationIds(Collection<String> ids) throws IOException;

  <T extends DomainEntity> void setPID(Class<T> type, String id, String pid);

  /**
   * Returns all the ids of objects of type <T>, that are not persisted.
   */
  <T extends DomainEntity> Collection<String> getAllIdsWithoutPIDOfType(Class<T> type) throws IOException;

  /**
   * Permanently removes the objects from the database.
   */
  <T extends DomainEntity> void removePermanently(Class<T> type, Collection<String> ids) throws IOException;

}
