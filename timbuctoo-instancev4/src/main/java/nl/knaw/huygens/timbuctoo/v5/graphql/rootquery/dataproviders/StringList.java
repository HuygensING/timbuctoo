package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders;

import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface StringList extends CursorList {
  List<String> getItems();

  static StringList create(Optional<String> prevCursor, Optional<String> nextCursor, List<String> items) {
    return ImmutableStringList.builder()
                              .prevCursor(prevCursor.map(CursorList::encode))
                              .nextCursor(nextCursor.map(CursorList::encode))
                              .items(items)
                              .build();
  }
}
