package nl.knaw.huygens.timbuctoo.search;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

public class CollectionConverter {

  public <E> FilterableSet<E> toFilterableSet(Collection<E> input) {
    Set<E> innerSet = Sets.newHashSet(input);

    return new FilterableSet<E>(innerSet);
  }

}
