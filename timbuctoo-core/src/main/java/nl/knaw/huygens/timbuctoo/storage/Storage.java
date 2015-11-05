package nl.knaw.huygens.timbuctoo.storage;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;

/**
 * Defines the API for storage and retrieval of entities.
 */
public interface Storage {

  void createIndex(boolean unique, Class<? extends Entity> type, String... fields) throws StorageException;

  /**
   * Returns storage statistics for the specified entity type.
   */
  <T extends Entity> String getStatistics(Class<T> type);

  /**
   * Closes the underlying storage.
   */
  void close();

  /**
   * Checks if the storage is available.
   * @return true if the storage is available.
   */
  boolean isAvailable();

  // --- add entities --------------------------------------------------

  /**
   * Adds the specified system entity to the storage; returns its assigned id.
   */
  <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws StorageException;

  /**
   * Adds the specified domain entity to the storage; returns its assigned id.
   */
  <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws StorageException;

  // --- update entities -----------------------------------------------

  /**
   * Updates the specified system entity in the storage.
   * The id and the revision of the entity must match with the stored entity.
   */
  <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws StorageException;

  /**
   * Updates the specified doamin entity in the storage.
   * The id and the revision of the entity must match with the stored entity.
   */
  <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws StorageException;

  <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws StorageException;

  // --- delete entities -----------------------------------------------

  /**
   * Deletes the spcified system entity.
   */
  <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws StorageException;

  /**
   * Deletes all system entities with the specified type.
   * @return The number of entities removed.
   */
  <T extends SystemEntity> int deleteSystemEntities(Class<T> type) throws StorageException;

  /**
   * Deletes system entities that have a value of the specified date field
   * that is older than the specified date.
   * @return The number of entities removed.
   */
  <T extends SystemEntity> int deleteByModifiedDate(Class<T> type, Date dateValue) throws StorageException;

  <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id, Change change) throws StorageException;

  /**
   * Deletes non-persistent domain entities with the specified type and id's..
   */
  <T extends DomainEntity> void deleteNonPersistent(Class<T> type, List<String> ids) throws StorageException;

  /**
   * Removes the variation of type from a stored DomainEntity
   * @param type the type of the variation to remove.
   * @param id the id of the entity to remove the variation from.
   * @param change the change to update the modified property.
   * @throws IllegalArgumentException is thrown when the entity is a primitive.
   * @throws NoSuchEntityException is thrown when the (variation) type does not exist for this entity.
   * @throws StorageException is thrown when the delete of the variation fails.
   */
  <T extends DomainEntity> void deleteVariation(Class<T> type, String id, Change change) throws IllegalArgumentException, NoSuchEntityException, StorageException;

  /**
   * Deletes all the relations linked to the DomainEntity with {@code id}.
   * @param type the type of relation to delete.
   * @param id the id of the DomainEntity.
   * @throws StorageException is thrown when the communication with the database fails.
   */
  void deleteRelationsOfEntity(Class<Relation> type, String id) throws StorageException;

  /**
   * Sets the accepted property of a relation to false.
   * @param type the project type of the relation to decline
   * @param id the id of the entity for which the relations should be declined.
   * @param change
   * @throws IllegalArgumentException when the variation type is a primitive.
   * @throws StorageException is thrown when the update fails.
   */
  <T extends Relation> void declineRelationsOfEntity(Class<T> type, String id, Change change) throws IllegalArgumentException, StorageException;

  // -------------------------------------------------------------------

  /**
   * Returns {@code true} if the specified entity exists, {@code false} otherwise.
   */
  <T extends Entity> boolean entityExists(Class<T> type, String id) throws StorageException;

  /**
   * Retrieves an {@code Entity} with it's project variation, 
   * if that does not exist the default variation will be returned.
   * The default variation will only be true for {@code DomainEntities}.
   * @param type the type of the {@code Entity} requested
   * @param id the id of the requested {@code Entity}
   * @return the {@code Entity} or the default variation if available or null when neither exist. 
   * @throws StorageException when something goes wrong accessing the database.
   */
  <T extends Entity> T getEntityOrDefaultVariation(Class<T> type, String id) throws StorageException;

  /**
   * Retrieves the specified entity, or {@code null} if no such entity exists.
   */
  <T extends Entity> T getEntity(Class<T> type, String id) throws StorageException;

  /**
   * Retrieves all system entities of the specified type.
   */
  <T extends SystemEntity> StorageIterator<T> getSystemEntities(Class<T> type) throws StorageException;

  /**
   * Retrieves all domain entities of the specified type.
   */
  <T extends DomainEntity> StorageIterator<T> getDomainEntities(Class<T> type) throws StorageException;

  /**
   * Retrieves entities by type and property.
   */
  <T extends Entity> StorageIterator<T> getEntitiesByProperty(Class<T> type, String field, String value) throws StorageException;

  /**
   * Returns the number of items in the collection corresponding with the specified type.
   */
  <T extends Entity> long count(Class<T> type);

  /**
   * Find an entity which has the specified property.
   */
  <T extends Entity> T findItemByProperty(Class<T> type, String field, String value) throws StorageException;

  /**
   * Returns a list of all variations of the specified <em>primitive</em> entity.
   * Returns an empty list when the combination of the {@code type} and {@code id} is not found.
   */
  <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws StorageException;

  <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws StorageException;

  <T extends DomainEntity> List<T> getAllRevisions(Class<T> type, String id) throws StorageException;

  <T extends Relation> T findRelation(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException;

  /**
   * Returns an iterator for all relations that match the specified id's, each of which may be null.
   */
  <T extends Relation> StorageIterator<T> findRelations(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException;

  /**
   * Returns an iterator for all relations of the specified entity id.
   */
  <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException;

  /**
  * Returns the id's of the domain entities of the specified type, that are not persisted.
  * 
  * Note that by design the method does not return variations of a type
  * that already has been persisted.
  * For example, if {@code Person} is a primitive type and a variation
  * {@code XyzPerson} of an existing entity has been added, this method
  * will not retrieve the id of that entity.
  */
  <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws StorageException;

  /**
   * Returns the id's of all relations involving the entities with the specified id's,
   * either as 'source' or as 'target' (or both).
   * 
   * @param ids a list of id's to find the relations for
   * @return a list of id's of the corresponding relations
   * @throws StorageException wrapped exception around the database exceptions
   */
  List<String> getRelationIds(List<String> ids) throws StorageException;

  /**
   * Get all the relations that have the type in {@code relationTypIds}.
   * @param type the type of the relations to get.
   * @param relationTypeIds the relation type should be in this collection.
   * @return a collection with the found relations.
   * @throws StorageException wrapped exception around the database exceptions
   */
  <T extends Relation> List<T> getRelationsByType(Class<T> type, List<String> relationTypeIds) throws StorageException;

  /**
   * Checks of a variation of the entity.
   * @param type the type of the variation
   * @param id the id of the entity, that should contain the variation
   * @return true if the variation exist false if not.
   * @throws StorageException wrapped exception around the database exceptions 
   */
  boolean doesVariationExist(Class<? extends DomainEntity> type, String id) throws StorageException;

}
