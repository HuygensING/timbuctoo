package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import com.google.common.base.Charsets;
import org.immutables.value.Value;

import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface PaginatedList extends Iterable<TypedValue> {

  Base64.Encoder ENCODER = Base64.getEncoder();

  Optional<String> getPrevCursor();

  Optional<String> getNextCursor();

  List<TypedValue> getItems();

  default Iterator<TypedValue> iterator() {
    return getItems().iterator();
  }

  static PaginatedList create(String prevCursor, String nextCursor, List<TypedValue> items) {
    return ImmutablePaginatedList.builder()
      .prevCursor(Optional.ofNullable(encode(prevCursor)))
      .nextCursor(Optional.ofNullable(encode(nextCursor)))
      .items(items)
      .build();
  }

  static String encode(String prevCursor) {
    if (prevCursor == null) {
      return null;
    } else {
      return ENCODER.encodeToString(prevCursor.getBytes(Charsets.UTF_8));
    }
  }
}
