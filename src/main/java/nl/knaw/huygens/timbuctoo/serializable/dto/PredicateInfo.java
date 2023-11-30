package nl.knaw.huygens.timbuctoo.serializable.dto;

import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.serializable.dto.ImmutablePredicateInfo;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface PredicateInfo {
  Optional<String> getUri();

  String getSafeName();

  Direction getDirection();

  static PredicateInfo predicateInfo(String safeName, String uri) {
    return predicateInfo(safeName, uri, Direction.OUT);
  }

  static PredicateInfo predicateInfo(String safeName, String uri, Direction direction) {
    return ImmutablePredicateInfo.builder()
      .safeName(safeName)
      .uri(Optional.ofNullable(uri))
      .direction(direction)
      .build();
  }
}
