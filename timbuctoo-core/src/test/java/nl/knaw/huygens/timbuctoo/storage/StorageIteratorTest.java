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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;

import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class StorageIteratorTest {

  private List<String> list;

  @Before
  public void setupList() {
    list = Lists.newArrayList("t", "i", "m", "b", "u", "c", "t", "o", "o");
  }

  @Test
  public void testEmptyIteratorHasNext() {
    StorageIterator<String> iterator = StorageIteratorStub.newInstance();
    assertFalse(iterator.hasNext());
  }

  @Test(expected = NoSuchElementException.class)
  public void testEmptyIteratorNext() {
    StorageIterator<String> iterator = StorageIteratorStub.newInstance();
    iterator.next();
  }

  @Test
  public void testEmptyIteratorSize() {
    StorageIterator<String>iterator = StorageIteratorStub.newInstance();
    assertEquals(0, iterator.size());
  }

  @Test
  public void testEmptyIteratorGetSome() {
    StorageIterator<String>iterator = StorageIteratorStub.newInstance();
    assertEquals(0, iterator.getSome(1).size());
  }

  @Test
  public void testEmptyIteratorGetAll() {
    StorageIterator<String> iterator = StorageIteratorStub.newInstance();
    assertEquals(0, iterator.getAll().size());
  }

  @Test
  public void testIteratorHasNext() {
    StorageIterator<String>iterator = StorageIteratorStub.newInstance("x");
    assertTrue(iterator.hasNext());
  }

  @Test(expected = NoSuchElementException.class)
  public void testIteratorNext() {
    StorageIterator<String>iterator = StorageIteratorStub.newInstance(list);
    for (int i = 0; i < list.size(); i++) {
      assertEquals(list.get(i), iterator.next());
    }
    iterator.next();
  }

  @Test
  public void testIteratorSize() {
    StorageIterator<String>iterator = StorageIteratorStub.newInstance(list);
    assertEquals(9, iterator.size());
  }

  @Test
  public void testIteratorGetSome() {
    StorageIterator<String> iterator = StorageIteratorStub.newInstance(list);
    assertEquals(5, iterator.getSome(5).size());
  }

  @Test
  public void testIteratorGetAll() {
    StorageIterator<String> iterator = StorageIteratorStub.newInstance(list);
    assertEquals(9, iterator.getAll().size());
  }

  @Test
  public void testIteratorSkip() {
    StorageIterator<String> iterator = StorageIteratorStub.newInstance(list);
    iterator.skip(3);
    assertEquals(6, iterator.getAll().size());
  }

}
