package nl.knaw.huygens.timbuctoo.storage;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.util.Iterator;
import java.util.List;

/**
 * WARNING The current implementation does not allow you to safely 
 * update items while iterating.
 */
public interface StorageIterator<T> extends Iterator<T> {

  /**
   * Returns the number of items that can be iterated over.
   */
  int size();

  /**
   * Skips the specified number of items;
   * returns a reference to the iterators.
   */
  StorageIterator<T> skip(int count);

  /**
   * Returns at most {@code limit} items and closes iterator.
   */
  List<T> getSome(int limit);

  /**
   * Returns all items and closes iterator.
   */
  List<T> getAll();

  /**
   * Closes this iterator, after which it cannot be used anymore.
   */
  void close();

}
