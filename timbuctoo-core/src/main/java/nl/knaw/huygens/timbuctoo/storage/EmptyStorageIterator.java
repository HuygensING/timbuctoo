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

import java.util.Collections;
import java.util.List;

/**
 * An iterator over an empty collection entities.
 */
public class EmptyStorageIterator<T> implements StorageIterator<T> {

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public T next() {
    throw new IllegalStateException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public StorageIterator<T> skip(int count) {
    return this;
  }

  @Override
  public List<T> getSome(int limit) {
    return Collections.emptyList();
  }

  @Override
  public List<T> getAll() {
    return Collections.emptyList();
  }

  @Override
  public void close() {}

}
