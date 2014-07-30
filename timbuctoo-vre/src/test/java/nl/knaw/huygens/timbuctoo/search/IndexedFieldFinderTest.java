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

import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.model.ClassWithMupltipleFullTestSearchFields;
import nl.knaw.huygens.timbuctoo.search.model.ClassWithNamedSortableFields;
import nl.knaw.huygens.timbuctoo.search.model.ComplexAnnotatedClass;
import nl.knaw.huygens.timbuctoo.search.model.SimpleAnnotatedClass;
import nl.knaw.huygens.timbuctoo.search.model.SimpleAnnotatedSubClass;

import org.junit.Test;

public class IndexedFieldFinderTest {
  @Test
  public void testFindFieldsDomainEntity() {
    testFindFields(DomainEntity.class, "desc", "id");
  }

  @Test
  public void testFindFieldsSimpleAnnotatedClass() {
    testFindFields(SimpleAnnotatedClass.class, "desc", "id", "dynamic_s_simple");
  }

  @Test
  public void testFindFieldsSimpleAnnotatedSubClass() {
    testFindFields(SimpleAnnotatedSubClass.class, "desc", "id", "dynamic_s_simple", "dynamic_s_prop");
  }

  @Test
  public void testFindFieldsComplexAnnotatedClass() {
    testFindFields(ComplexAnnotatedClass.class, "desc", "id", "dynamic_t_complex1", "dynamic_t_complex2");
  }

  @Test
  public void testFindFieldsSortableFields() {
    testFindFields(ClassWithNamedSortableFields.class, "desc", "id", "dynamic_sort_test", "dynamic_sort_blah");
  }

  @Test
  public void testFindFieldsFullTextSearchFields() {
    testFindFields(ClassWithMupltipleFullTestSearchFields.class, "desc", "id", "dynamic_t_simple", "dynamic_t_simple1");
  }

  private void testFindFields(Class<? extends DomainEntity> type, String... expectedFields) {
    // setup
    IndexedFieldFinder instance = new IndexedFieldFinder();

    // action
    Set<String> fields = instance.findFields(type);

    // verify
    assertThat(fields, containsInAnyOrder(expectedFields));
  }
}
