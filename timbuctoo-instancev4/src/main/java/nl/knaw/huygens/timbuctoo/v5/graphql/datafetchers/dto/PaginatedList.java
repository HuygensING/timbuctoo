package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import com.google.common.base.Charsets;
import org.immutables.value.Value;

import java.util.Base64;
import java.util.List;

@Value.Immutable
public interface PaginatedList {

  Base64.Encoder ENCODER = Base64.getEncoder();

  String getPrevCursor();

  String getNextCursor();

  List<TypedValue> getItems();

  static PaginatedList create(String prevCursor, String nextCursor, List<TypedValue> items) {
    return ImmutablePaginatedList.builder()
      .prevCursor(encode(prevCursor))
      .nextCursor(encode(nextCursor))
      .items(items)
      .build();
  }

  static String encode(String prevCursor) {
    return ENCODER.encodeToString(prevCursor.getBytes(Charsets.UTF_8));
  }
}
