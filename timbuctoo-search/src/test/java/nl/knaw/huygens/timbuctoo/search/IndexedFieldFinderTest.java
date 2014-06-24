package nl.knaw.huygens.timbuctoo.search;

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
    testFindFields(ClassWithNamedSortableFields.class, "desc", "id", "test", "blah");
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
