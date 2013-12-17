package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo search
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.search.model.ClassWithNamedSortableFields;
import nl.knaw.huygens.timbuctoo.search.model.ClassWithUnNamedSortableFields;
import nl.knaw.huygens.timbuctoo.search.model.ComplexAnnotatedClass;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class SortableFieldFinderTest {

  private SortableFieldFinder instance;

  @Before
  public void setUp() {
    instance = new SortableFieldFinder();
  }

  @Test
  public void testFindSortableFieldsWithName() {
    Set<String> expected = Sets.newHashSet("blah", "test");
    testFindSortableFields(expected, ClassWithNamedSortableFields.class);
  }

  @Test
  public void testFindSortableFieldsNoneSortableFields() {
    Set<String> expected = Sets.newHashSet();
    testFindSortableFields(expected, ComplexAnnotatedClass.class);
  }

  @Test
  public void testFindSortableFieldsWithoutName() {
    Set<String> expected = Sets.newHashSet();
    testFindSortableFields(expected, ClassWithUnNamedSortableFields.class);
  }

  private void testFindSortableFields(Set<String> expected, Class<? extends Entity> type) {
    Set<String> actual = instance.findFields(type);
    assertThat(actual, equalTo(expected));
  }

}
