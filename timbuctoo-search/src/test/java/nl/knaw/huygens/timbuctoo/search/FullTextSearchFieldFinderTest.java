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

import java.util.HashSet;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.search.model.ClassWithInheritedFullTextSearchFields;
import nl.knaw.huygens.timbuctoo.search.model.ClassWithMupltipleFullTestSearchFields;
import nl.knaw.huygens.timbuctoo.search.model.ClassWithMupltipleFullTestSearchFieldsNotAllFTS;
import nl.knaw.huygens.timbuctoo.search.model.ClassWithMupltipleFullTestSearchFieldsWithSameName;
import nl.knaw.huygens.timbuctoo.search.model.ComplexAnnotatedClassNoneFaceted;
import nl.knaw.huygens.timbuctoo.search.model.SimpleClassWithOneFullTextSearchField;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class FullTextSearchFieldFinderTest {

  private FullTextSearchFieldFinder instance;

  @Before
  public void setUp() {
    instance = new FullTextSearchFieldFinder();
  }

  @Test
  public void testFindFullTextSearchFieldSingleField() {
    HashSet<String> expected = Sets.newHashSet("dynamic_t_simple");
    testFindFullTextSearchField(SimpleClassWithOneFullTextSearchField.class, expected);
  }

  @Test
  public void testFindFullTextSearchFieldMultipleFields() {
    HashSet<String> expected = Sets.newHashSet("dynamic_t_simple", "dynamic_t_simple1");
    testFindFullTextSearchField(ClassWithMupltipleFullTestSearchFields.class, expected);
  }

  @Test
  public void testFindFullTextSearchFieldMultipleFieldsSameName() {
    HashSet<String> expected = Sets.newHashSet("dynamic_t_simple");
    testFindFullTextSearchField(ClassWithMupltipleFullTestSearchFieldsWithSameName.class, expected);
  }

  @Test
  public void testFindFullTextSearchFieldMultipleFieldsNotAllFullText() {
    HashSet<String> expected = Sets.newHashSet("dynamic_t_simple");
    testFindFullTextSearchField(ClassWithMupltipleFullTestSearchFieldsNotAllFTS.class, expected);
  }

  @Test
  public void testFindFullTextSearchFieldsInherited() {
    HashSet<String> expected = Sets.newHashSet("dynamic_t_simple");
    testFindFullTextSearchField(ClassWithInheritedFullTextSearchFields.class, expected);
  }

  @Test
  public void testFindFullTextSearchFieldsComplex() {
    HashSet<String> expected = Sets.newHashSet("dynamic_t_complex1", "dynamic_t_complex2");
    testFindFullTextSearchField(ComplexAnnotatedClassNoneFaceted.class, expected);
  }

  private void testFindFullTextSearchField(Class<? extends Entity> type, Set<String> expected) {
    Set<String> actual = instance.findFields(type);
    assertThat(actual, equalTo(expected));
  }

}
