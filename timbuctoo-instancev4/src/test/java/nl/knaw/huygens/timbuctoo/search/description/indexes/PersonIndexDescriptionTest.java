package nl.knaw.huygens.timbuctoo.search.description.indexes;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class PersonIndexDescriptionTest {

  @Test
  public void getSortIndexPropertyNamesReturnsPropertyNamesForAllTypesAndFields() {
    List<String> types = Lists.newArrayList("wwperson", "person", "custom");
    PersonIndexDescription instance = new PersonIndexDescription();

    List<String> results = instance.getSortIndexPropertyNames(types);
    assertThat(results, containsInAnyOrder(
      "wwperson_names_sort",
      "wwperson_deathDate_sort",
      "wwperson_birthDate_sort",
      "wwperson_modified_sort",
      "person_names_sort",
      "person_deathDate_sort",
      "person_birthDate_sort",
      "person_modified_sort",
      "custom_names_sort",
      "custom_deathDate_sort",
      "custom_birthDate_sort",
      "custom_modified_sort"
    ));

  }
}
