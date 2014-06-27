package nl.knaw.huygens.timbuctoo.search;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

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
    return Sets.filter(this, predicate);
  }

  @Override
  public String toString() {
    return innerSet.toString();
  }
}
