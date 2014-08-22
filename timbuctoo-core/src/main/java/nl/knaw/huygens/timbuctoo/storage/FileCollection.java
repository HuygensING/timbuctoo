package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.model.SystemEntity;

public abstract class FileCollection<T extends SystemEntity> {
  /**
   * Add an entity to the collection and return the id of the entity.
   * @param entity the entity to add.
   * @return the id of the entity.
   */
  public abstract String add(T entity);

  /**
   * Find an entity by example.
   * @param example the entity has to match
   * @return an entity or null if no matches are found.
   */
  public abstract T findItem(T example);

  /**
   * Get item by id.
   * @param id id to get the item for
   * @return the item if found, else null
   */
  public abstract T get(String id);

  /**
   * Get all the items.
   * @return an iterator iterate through the results.
   */
  public abstract StorageIterator<T> getAll();

  /**
   * Updates an existing item, if the item is found
   * @param item the item to update.
   */
  public abstract void updateItem(T item);

  /**
   * Delete an item if it exists.
   * @param item the item to delete.
   */
  public abstract void deleteItem(T item);
}
