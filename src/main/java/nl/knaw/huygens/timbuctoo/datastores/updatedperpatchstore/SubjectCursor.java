package nl.knaw.huygens.timbuctoo.datastores.updatedperpatchstore;

import nl.knaw.huygens.timbuctoo.datastores.CursorValue;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public interface SubjectCursor extends CursorValue {
  String getSubject();

  Set<Integer> getVersions();

  static SubjectCursor create(String subject, Set<Integer> versions) {
    return ImmutableSubjectCursor.builder()
                                 .subject(subject)
                                 .versions(versions)
                                 .cursor(subject)
                                 .build();
  }

  static SubjectCursor create(String subject, Set<Integer> versions, String cursor) {
    return ImmutableSubjectCursor.builder()
                                 .subject(subject)
                                 .versions(versions)
                                 .cursor(cursor)
                                 .build();
  }
}
