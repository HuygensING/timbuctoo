package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo search
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class FilterableSet<E> implements Set<E> {

  private final Set<E> innerSet;

  public FilterableSet(Set<E> innerSet) {
    this.innerSet = innerSet;
  }

  @Override
  public boolean add(E e) {
    return innerSet.add(e);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    return innerSet.addAll(c);
  }

  @Override
  public void clear() {
    innerSet.clear();

  }

  @Override
  public boolean contains(Object o) {
    return innerSet.contains(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return innerSet.containsAll(c);
  }

  @Override
  public boolean isEmpty() {
    return innerSet.isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    return innerSet.iterator();
  }

  @Override
  public boolean remove(Object o) {
    return innerSet.remove(o);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return innerSet.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return innerSet.retainAll(c);
  }

  @Override
  public int size() {
    return innerSet.size();
  }

  @Override
  public Object[] toArray() {
    return innerSet.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return innerSet.toArray(a);
  }

  public Set<E> filter(Predicate<E> predicate) {
    return FluentIterable.from(this).filter(predicate).toSet();
  }

  @Override
  public String toString() {
    return innerSet.toString();
  }
}
