package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface PaginatedList {
  String getPrevCursor();

  String getNextCursor();

  List<TypedValue> getItems();

  static PaginatedList create(String prevCursor, String nextCursor, List<TypedValue> items) {
    return ImmutablePaginatedList.builder()
      .prevCursor(prevCursor)
      .nextCursor(nextCursor)
      .items(items)
      .build();
  }
}
