package nl.knaw.huygens.timbuctoo.storage.file;

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

import java.util.Collections;
import java.util.LinkedList;

import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.IdCreator;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

public abstract class FileCollection<T extends SystemEntity> {

  /**
   * Add an entity to the collection and return the id of the entity.
   * 
   * @param entity
   *          the entity to add.
   * @return the id of the entity.
   */
  public abstract String add(T entity);

  /**
   * Find an entity by example.
   * 
   * @param example
   *          the entity has to match
   * @return an entity or null if no matches are found.
   */
  public abstract T findItem(T example);

  /**
   * Get item by id.
   * 
   * @param id
   *          id to get the item for
   * @return the item if found, else null
   */
  public abstract T get(String id);

  /**
   * Get all the items.
   * 
   * @return an iterator iterate through the results.
   */
  public abstract StorageIterator<T> getAll();

  /**
   * Get all as array.
   * @return the collection as an array.
   */
  public abstract T[] asArray();

  /**
   * Updates an existing item, if the item is found
   * 
   * @param item
   *          the item to update.
   */
  public abstract void updateItem(T item);

  /**
   * Delete an item if it exists.
   * 
   * @param item
   *          the item to delete.
   */
  public abstract void deleteItem(T item);

  protected abstract LinkedList<String> getIds();

  protected String createId(String idPrefix) {
    return IdCreator.create(idPrefix, getNewNumber(idPrefix, 1));
  }

  private long getNewNumber(String idPrefix, int i) {
    LinkedList<String> ids = getIds();

    Collections.sort(ids);

    int highestNumber = 0;

    if (!ids.isEmpty()) {
      String lastId = ids.getLast();
      highestNumber = Integer.parseInt(lastId.replace(idPrefix, ""));
    }

    return highestNumber + 1;
  }

}
