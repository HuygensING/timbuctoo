package nl.knaw.huygens.timbuctoo.v5.graphql.archetypes.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePredicateDefinition.class)
@JsonDeserialize(as = ImmutablePredicateDefinition.class)
public interface PredicateDefinition {

  String getName();

  String getPredicateUri();

  String getDescription();

  Optional<Reference> getReference();

  @Value.Default
  default boolean isList() {
    return false;
  }
}
