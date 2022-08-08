package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.datastores.CursorValue;
import org.immutables.value.Value;

@Value.Immutable
public interface CursorUri extends CursorValue {
  String getUri();

  @Value.Auxiliary
  String getCursor();

  static CursorUri create(String uri) {
    return create(uri, uri);
  }

  static CursorUri create(String uri, String cursor) {
    return ImmutableCursorUri.builder()
                             .uri(uri)
                             .cursor(cursor)
                             .build();
  }
}
