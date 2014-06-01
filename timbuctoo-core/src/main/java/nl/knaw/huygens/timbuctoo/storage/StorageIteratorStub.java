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

import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.collect.Lists;

/**
 * A {@code StorageIterator} implementation that uses supplied data.
 * When no data is supplied, it is an iterator over an empty data set.
 */
public class StorageIteratorStub<T> implements StorageIterator<T> {

  public static <T> StorageIteratorStub<T> newInstance(List<T> list) {
    return new StorageIteratorStub<T>(list);
  }

  public static <T> StorageIteratorStub<T> newInstance(T... items) {
    List<T> list = Lists.newArrayList(items);
    return new StorageIteratorStub<T>(list);
  }

  public static <T> StorageIteratorStub<T> newInstance() {
    List<T> list = Lists.newArrayList();
    return new StorageIteratorStub<T>(list);
  }

  private final List<T> list;
  private int pos;

  private StorageIteratorStub(List<T> list) {
    this.list = list;
    pos = 0;
  }

  @Override
  public boolean hasNext() {
    return (pos < list.size());
  }

  @Override
  public T next() {
    if (hasNext()) {
      return list.get(pos++);
    } 
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public StorageIterator<T> skip(int count) {
    if (count > 0) {
      pos += count;
    }
    return this;
  }

  @Override
  public List<T> getSome(int limit) {
    List<T> result = Lists.newArrayList();
    while (limit-- > 0 && hasNext()) {
      result.add(next());
    }
    return result;
  }

  @Override
  public List<T> getAll() {
    return getSome(Integer.MAX_VALUE);
  }

  @Override
  public void close() {}

}
