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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class FilterableSetTest {
  @Mock
  private Set<Integer> innerSet;
  private FilterableSet<Integer> instance;

  @Before
  public void testsetUp() {
    MockitoAnnotations.initMocks(this);

    instance = new FilterableSet<Integer>(innerSet);
  }

  @Test
  public void testAdd() {
    instance.add(3);

    verify(innerSet).add(3);
  }

  @Test
  public void testAddAll() {
    List<Integer> collectionToAdd = Lists.newArrayList(1, 2, 3);

    instance.addAll(collectionToAdd);

    verify(innerSet).addAll(collectionToAdd);

  }

  @Test
  public void testClear() {
    instance.clear();

    verify(innerSet).clear();
  }

  @Test
  public void testContains() {
    instance.contains(2);

    verify(innerSet).contains(2);
  }

  @Test
  public void testContainsAll() {
    List<Integer> collection = Lists.newArrayList(1, 2, 3);

    instance.containsAll(collection);

    verify(innerSet).containsAll(collection);
  }

  @Test
  public void testIsEmpty() {
    instance.isEmpty();

    verify(innerSet).isEmpty();
  }

  @Test
  public void testIterator() {
    instance.iterator();

    verify(innerSet).iterator();
  }

  @Test
  public void testRemove() {
    instance.remove(1);

    verify(innerSet).remove(1);
  }

  @Test
  public void testRemoveAll() {
    List<Integer> collectionToRemove = Lists.newArrayList(1, 2, 3);

    instance.removeAll(collectionToRemove);

    verify(innerSet).removeAll(collectionToRemove);

  }

  @Test
  public void testRetainAll() {
    List<Integer> collectionToRetain = Lists.newArrayList(1, 2, 3);

    instance.retainAll(collectionToRetain);

    verify(innerSet).retainAll(collectionToRetain);

  }

  @Test
  public void testSize() {
    instance.size();

    verify(innerSet).size();

  }

  @Test
  public void testToArray() {
    instance.toArray();

    verify(innerSet).toArray();
  }

  @Test
  public void testToArrayWithArgument() {
    Integer[] integerArray = new Integer[0];

    instance.toArray(integerArray);

    verify(innerSet).toArray(integerArray);
  }

  @Test
  public void testFilter() {
    // setup
    Set<Integer> innerSet = Sets.newHashSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    Predicate<Integer> greaterThan5 = new Predicate<Integer>() {
      @Override
      public boolean apply(Integer input) {
        return input > 5;
      }
    };

    FilterableSet<Integer> instance = new FilterableSet<Integer>(innerSet);

    // action
    Set<Integer> actualFilteredSet = instance.filter(greaterThan5);

    assertThat(actualFilteredSet, containsInAnyOrder(6, 7, 8, 9, 10));
  }
}
