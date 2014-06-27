package nl.knaw.huygens.timbuctoo.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class CollectionConverterTest {
  @Test
  public void testToFilterableSet() {
    // setup
    List<Integer> randomValues = Lists.newArrayList(1, 2, 4, 5);

    CollectionConverter collectionConverter = new CollectionConverter();

    // action
    FilterableSet<Integer> actualFilterableSet = collectionConverter.toFilterableSet(randomValues);

    // verify
    assertThat(actualFilterableSet, containsInAnyOrder(1, 2, 4, 5));
  }
}
