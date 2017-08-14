package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import com.google.common.base.Charsets;
import org.immutables.value.Value;

import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface PaginatedList<T extends DatabaseResult> extends Iterable<T> {

  Base64.Encoder ENCODER = Base64.getEncoder();

  Optional<String> getPrevCursor();

  Optional<String> getNextCursor();

  List<T> getItems();

  default Iterator<T> iterator() {
    return getItems().iterator();
  }

  static <U extends DatabaseResult> PaginatedList<U> create(String prevCursor, String nextCursor, List<U> items) {
    return ImmutablePaginatedList.<U>builder()
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
