package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto;

import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import org.immutables.value.Value;

@Value.Immutable
public interface CursorSubject extends CursorContainer {
  String getCursor();

  SubjectReference getSubject();

  static CursorSubject create(String cursor, String subject) {
    return ImmutableCursorSubject.builder()
      .cursor(cursor)
      .subject(SubjectReference.create(subject))
      .build();
  }
}
