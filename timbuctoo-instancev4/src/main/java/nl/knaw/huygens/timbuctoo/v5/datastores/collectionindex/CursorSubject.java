package nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex;

import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorContainer;
import org.immutables.value.Value;

@Value.Immutable
public interface CursorSubject extends CursorContainer {
  String getCursor();

  String getSubjectUri();

  static CursorSubject create(String cursor, String subject) {
    return ImmutableCursorSubject.builder()
      .cursor(cursor)
      .subjectUri(subject)
      .build();
  }
}
