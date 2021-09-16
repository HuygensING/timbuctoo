package nl.knaw.huygens.timbuctoo.v5.datastores.updatedperpatchstore;

import nl.knaw.huygens.timbuctoo.v5.datastores.CursorValue;
import org.immutables.value.Value;

@Value.Immutable
public interface SubjectCursor extends CursorValue {
  String getSubject();

  static SubjectCursor create(String subject, String cursor) {
    return ImmutableSubjectCursor.builder()
                                 .subject(subject)
                                 .cursor(cursor)
                                 .build();
  }
}
