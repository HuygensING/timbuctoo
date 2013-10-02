package nl.knaw.huygens.repository.search;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import nl.knaw.huygens.repository.model.Entity;
import nl.knaw.huygens.repository.search.model.ClassWithNamedSortableFields;
import nl.knaw.huygens.repository.search.model.ClassWithUnNamedSortableFields;
import nl.knaw.huygens.repository.search.model.ComplexAnnotatedClass;

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
