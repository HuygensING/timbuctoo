package nl.knaw.huygens.timbuctoo.v5.datastores.dto;

import org.immutables.value.Value;

@Value.Immutable
public interface MarkedSubject {
  String getSubject();

  String getMarker();

  static MarkedSubject create(String subject, String marker) {
    return ImmutableMarkedSubject.builder()
      .subject(subject)
      .marker(marker)
      .build();
  }
}
