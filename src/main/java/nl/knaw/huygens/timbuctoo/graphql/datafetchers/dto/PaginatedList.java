package nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.graphql.collectionfilter.Facet;
import nl.knaw.huygens.timbuctoo.graphql.rootquery.dataproviders.CursorList;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface PaginatedList<T> extends CursorList {
  List<T> getItems();

  Optional<Long> getTotal();

  List<Facet> getFacets();

  static <U> PaginatedList<U> create(String prevCursor, String nextCursor, List<U> items, Optional<Long> total) {
    return ImmutablePaginatedList.<U>builder()
      .prevCursor(Optional.ofNullable(CursorList.encode(prevCursor)))
      .nextCursor(Optional.ofNullable(CursorList.encode(nextCursor)))
      .items(items)
      .total(total)
      .build();
  }

  static <U> PaginatedList<U> create(String prevCursor, String nextCursor, List<U> items,
                                     Optional<Long> total, List<Facet> facets) {
    return ImmutablePaginatedList.<U>builder()
      .prevCursor(Optional.ofNullable(CursorList.encode(prevCursor)))
      .nextCursor(Optional.ofNullable(CursorList.encode(nextCursor)))
      .items(items)
      .total(total)
      .facets(facets)
      .build();
  }
}
